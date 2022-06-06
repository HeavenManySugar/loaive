package com.ntihs.loaive.data

import android.security.keystore.KeyProperties.KEY_ALGORITHM_RSA
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.ntihs.loaive.data.model.LoggedInUser
import com.ntihs.loaive.data.model.logindata.LoginData
import com.ntihs.loaive.ui.login.LoginActivity
import okhttp3.*
import java.io.IOException
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.concurrent.CompletableFuture
import javax.crypto.Cipher
import okhttp3.Cookie
import java.lang.StringBuilder


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    // 建立OkHttpClient
    private var client: OkHttpClient = OkHttpClient.Builder().build()

    private var gson = Gson()
    val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_RSA)
    val publicKeyString =
            "RSA/ECB/OAEPWithSHA1AndMGF1Padding KEY"
    val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(Base64.decode(publicKeyString, Base64.DEFAULT)))


    fun login(username: String, password: String): Result<LoggedInUser> {
        val f: CompletableFuture<LoginData> = CompletableFuture()
        val f2: CompletableFuture<String> = CompletableFuture()
        // FormBody放要傳的參數和值
        val formBody = FormBody.Builder()
            .add("email", encryptRSA(username.toByteArray()))
            .add("password", encryptRSA(password.toByteArray()))
            .build()

        // 建立Request，設置連線資訊
        val request: Request = Request.Builder()
            .url("http://YOUR_SERVER_IP:443/member/login")
            .post(formBody) // 使用post連線
            .build()

        // 建立Call
        val call: Call = client.newCall(request)
        // 執行Call連線到網址
        call.enqueue(object : Callback {
            //@Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                // 連線成功，自response取得連線結果
                val result: String = response.body()!!.string()
                val mUser = gson.fromJson(result, LoginData::class.java)
                Log.d("Token", "${response.headers("token")}")
                Log.d("OkHttp result", result)
                f.complete(mUser)
                if(response.headers("token").size > 0)
                    f2.complete(response.headers("token")[0])
            }

            override fun onFailure(call: Call?, e: IOException) {
                // 連線失敗
                f.completeExceptionally(e)
                e.printStackTrace()
            }
        })
        try {
            val mUser = f.get()
            // TODO: handle loggedInUser authentication
            //val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            if(mUser.result?.status != "登入失敗。"){
                /*val encryptedText: String =
                    LoginActivity.keyStoreHelper!!.encrypt("${mUser.result?.loginMember}")
                 */
                return Result.Success(LoggedInUser("${mUser.result?.loginMember}", f2.get()))
            }
            else{
                return Result.Error(IOException(mUser.result?.status))
            }
        } catch (e: Throwable) {
            Log.d("error","${e.message}")
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun register(email: String, username: String, password: String): Result<LoggedInUser> {
        val f: CompletableFuture<LoginData> = CompletableFuture()
        val f2: CompletableFuture<String> = CompletableFuture()
        // FormBody放要傳的參數和值
        val formBody = FormBody.Builder()
            .add("email", encryptRSA(email.toByteArray()))
            .add("name", encryptRSA(username.toByteArray()))
            .add("password", encryptRSA(password.toByteArray()))
            .build()

        // 建立Request，設置連線資訊
        val request: Request = Request.Builder()
            .url("http://YOUR_SERVER_IP:443/member")
            .post(formBody) // 使用post連線
            .build()

        // 建立Call
        val call: Call = client.newCall(request)
        // 執行Call連線到網址
        call.enqueue(object : Callback {
            //@Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                // 連線成功，自response取得連線結果
                val result: String = response.body()!!.string()
                val mUser = gson.fromJson(result, LoginData::class.java)
                Log.d("Token", "${response.headers("token")}")
                Log.d("OkHttp result", result)
                f.complete(mUser)
                if(response.headers("token").size > 0)
                    f2.complete(response.headers("token")[0])
            }

            override fun onFailure(call: Call?, e: IOException) {
                // 連線失敗
                f.completeExceptionally(e)
                e.printStackTrace()
            }
        })
        try {
            val mUser = f.get()
            // TODO: handle loggedInUser authentication
            //val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            if(mUser.result?.status != "註冊失敗。"){
                /*val encryptedText: String =
                    LoginActivity.keyStoreHelper!!.encrypt("${mUser.result?.loginMember}")
                 */
                return Result.Success(LoggedInUser("${mUser.result?.registerMember?.name}", f2.get()))
            }
            else{
                return Result.Error(IOException(mUser.result?.status))
            }
        } catch (e: Throwable) {
            Log.d("error","${e.message}")
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
        LoginActivity.preferencesHelper?.setInput("username", "")
        LoginActivity.preferencesHelper?.setInput("password", "")
    }

    @Throws(Exception::class)
    private fun encryptRSA(plainText: ByteArray): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedByte = cipher.doFinal(plainText)
        return Base64.encodeToString(encryptedByte, Base64.DEFAULT)
    }
}