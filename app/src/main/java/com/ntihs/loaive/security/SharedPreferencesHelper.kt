package com.ntihs.loaive.security

import android.content.Context
import android.content.SharedPreferences


class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences
    private fun getString(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

    private fun putString(key: String, value: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }

    private fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    private fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(key, value)
            .apply()
    }

    fun setIV(value: String?) {
        putString(PREF_KEY_IV, value!!)
    }

    fun getIV(): String? {
        return getString(PREF_KEY_IV)
    }

    fun setAESKey(value: String?) {
        putString(PREF_KEY_AES, value!!)
    }

    fun getAESKey(): String? {
        return getString(PREF_KEY_AES)
    }

    fun setInput(key: String, value: String) {
        //putString(PREF_KEY_INPUT, value)
        putString(key, value)
    }

    fun getInput(key: String): String? {
        //return getString(PREF_KEY_INPUT)
        return getString(key)
    }

    companion object {
        private const val SHARED_PREF_NAME = "KEYSTORE_SETTING"
        private const val PREF_KEY_AES = "PREF_KEY_AES"
        private const val PREF_KEY_IV = "PREF_KEY_IV"
        private const val PREF_KEY_INPUT = "PREF_KEY_INPUT"
    }

    init {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
    }
}