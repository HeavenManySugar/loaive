package com.ntihs.loaive.security
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.Exception
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal


/**
 * Created by Joe on 2017/5/24.
 */
class KeyStoreHelper(context: Context, sharedPreferencesHelper: SharedPreferencesHelper?) {
    private var keyStore: KeyStore? = null
    private var prefsHelper: SharedPreferencesHelper? = null
    @Throws(Exception::class)
    private fun genKeyStoreKey(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            generateRSAKey_AboveApi23()
        } else {
            generateRSAKey_BelowApi23(context)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Throws(Exception::class)
    private fun generateRSAKey_AboveApi23() {
        val keyPairGenerator = KeyPairGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .build()
        keyPairGenerator.initialize(keyGenParameterSpec)
        keyPairGenerator.generateKeyPair()
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun generateRSAKey_BelowApi23(context: Context) {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 30)
        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(KEYSTORE_ALIAS)
            .setSubject(X500Principal("CN=" + KEYSTORE_ALIAS))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()
        val keyPairGenerator = KeyPairGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER)
        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }

    fun encrypt(plainText: String): String {
        return try {
            encryptAES(plainText)
        } catch (e: Exception) {
            Log.d(TAG, Log.getStackTraceString(e))
            ""
        }
    }

    fun decrypt(encryptedText: String): String {
        return try {
            decryptAES(encryptedText)
        } catch (e: Exception) {
            Log.d(TAG, Log.getStackTraceString(e))
            ""
        }
    }

    @Throws(Exception::class)
    private fun encryptRSA(plainText: ByteArray): String {
        val publicKey = keyStore!!.getCertificate(KEYSTORE_ALIAS).publicKey
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedByte = cipher.doFinal(plainText)
        return Base64.encodeToString(encryptedByte, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    private fun decryptRSA(encryptedText: String?): ByteArray {
        val privateKey = keyStore!!.getKey(
            KEYSTORE_ALIAS,
            null
        ) as PrivateKey
        val cipher =
            Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedBytes =
            Base64.decode(encryptedText, Base64.DEFAULT)
        return cipher.doFinal(encryptedBytes)
    }

    @Throws(Exception::class)
    private fun genAESKey() {
        // Generate AES-Key
        val aesKey = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(aesKey)


        // Generate 12 bytes iv then save to SharedPrefs
        val generated = secureRandom.generateSeed(12)
        val iv = Base64.encodeToString(generated, Base64.DEFAULT)
        prefsHelper!!.setIV(iv)


        // Encrypt AES-Key with RSA Public Key then save to SharedPrefs
        val encryptAESKey = encryptRSA(aesKey)
        prefsHelper!!.setAESKey(encryptAESKey)
    }

    /**
     * AES Encryption
     * @param plainText: A string which needs to be encrypted.
     * @return A base64's string after encrypting.
     */
    @Throws(Exception::class)
    private fun encryptAES(plainText: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, aESKey, IvParameterSpec(iV))

        // 加密過後的byte
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())

        // 將byte轉為base64的string編碼
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    private fun decryptAES(encryptedText: String): String {
        // 將加密過後的Base64編碼格式 解碼成 byte
        val decodedBytes = Base64.decode(encryptedText.toByteArray(), Base64.DEFAULT)

        // 將解碼過後的byte 使用AES解密
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, aESKey, IvParameterSpec(iV))
        return String(cipher.doFinal(decodedBytes))
    }

    private val iV: ByteArray
        private get() {
            val prefIV = prefsHelper!!.getIV()
            return Base64.decode(prefIV, Base64.DEFAULT)
        }

    @get:Throws(Exception::class)
    private val aESKey: SecretKeySpec
        private get() {
            val encryptedKey = prefsHelper!!.getAESKey()
            val aesKey = decryptRSA(encryptedKey)
            return SecretKeySpec(aesKey, AES_MODE)
        }

    companion object {
        private const val TAG = "KEYSTORE"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val RSA_MODE = "RSA/ECB/PKCS1Padding"
        private const val KEYSTORE_ALIAS = "KEYSTORE_DEMO"
    }

    init {
        try {
            prefsHelper = sharedPreferencesHelper
            keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore?.load(null)
            if (!keyStore?.containsAlias(KEYSTORE_ALIAS)!!) {
                prefsHelper!!.setIV("")
                genKeyStoreKey(context)
                genAESKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
