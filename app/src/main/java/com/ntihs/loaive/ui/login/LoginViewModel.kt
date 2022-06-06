package com.ntihs.loaive.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ntihs.loaive.R
import com.ntihs.loaive.data.LoginRepository
import com.ntihs.loaive.data.Result

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        if (result is Result.Success) {
            _loginResult.postValue(LoginResult(success = LoggedInUserView(displayName = result.data.displayName), token = result.data.token))
        } else {
            _loginResult.postValue(LoginResult(error = R.string.login_failed))
        }
    }

    fun register(email:String, username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.register(email, username, password)

        if (result is Result.Success) {
            _loginResult.postValue(LoginResult(success = LoggedInUserView(displayName = result.data.displayName), token = result.data.token))
        } else {
            _loginResult.postValue(LoginResult(error = R.string.login_failed))
        }
    }

    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun usernameDataChanged(username: String){
        if(!isUserNameValid(username)){
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun password2DataChanged(password: String, password2: String){
        if(password != password2){
            _loginForm.value = LoginFormState(passwordError = R.string.password_unmatched)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }



    // A placeholder email validation check
    private fun isEmailValid(email: String): Boolean {
        return if (email.matches("^(([^<>()[\\\\]\\\\\\\\.,;:\\\\s@\\\\\\\"]+(.[^<>()\\[\\]\\\\\\.,;:\\s@\\\"]+)*)|(\\\"\\.+\\\"))@((\\[[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}\\])|(([a-zA-Z-0-9]+.)+[a-zA-Z]{2,}))\$".toRegex())) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            email.isBlank()
        }
    }
    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return (username.length in 3..15 && username.isNotBlank())
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        var flag = false
        for(item in password){
            if(item.uppercase().first() in 'A'..'Z'){
                flag = true
            }
        }
        return ( password.length > 5 || password.isBlank() ) && flag
    }
}