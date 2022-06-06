package com.ntihs.loaive.data

import android.util.Log
import com.ntihs.loaive.data.model.LoggedInUser
import com.ntihs.loaive.ui.login.LoginActivity.Companion.keyStoreHelper
import com.ntihs.loaive.ui.login.LoginActivity.Companion.preferencesHelper

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }
    fun login(username: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
            preferencesHelper?.setInput("username", keyStoreHelper?.encrypt(username)!!)
            preferencesHelper?.setInput("password", keyStoreHelper?.encrypt(password)!!)
            Log.d("OK", keyStoreHelper?.decrypt(preferencesHelper?.getInput("username")!!)!!)
        }

        return result
    }

    fun register(email: String, username: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.register(email, username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
            preferencesHelper?.setInput("username", keyStoreHelper?.encrypt(email)!!)
            preferencesHelper?.setInput("password", keyStoreHelper?.encrypt(password)!!)
            Log.d("OK", keyStoreHelper?.decrypt(preferencesHelper?.getInput("username")!!)!!)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}