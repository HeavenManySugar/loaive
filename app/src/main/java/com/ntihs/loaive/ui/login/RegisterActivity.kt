package com.ntihs.loaive.ui.login

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ntihs.loaive.R
import com.ntihs.loaive.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.email
        val username = binding.username
        val password = binding.password
        val password2 = binding.password2
        val login = binding.login
        val loading = binding.loading


        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@RegisterActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both email / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.emailError != null) {
                email.error = getString(loginState.emailError)
            }
            if (loginState.passwordError != null) {
                if(loginState.passwordError == R.string.password_unmatched){
                    password2.error = getString(loginState.passwordError)
                }else {
                    password.error = getString(loginState.passwordError)
                }
            }
            else if (password.text.isBlank()) {
                login.isEnabled = false
            }
        })

        loginViewModel.loginResult.observe(this@RegisterActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })
        username.afterTextChanged {
            loginViewModel.usernameDataChanged(
                username.text.toString(),
            )
        }

        email.afterTextChanged {
            loginViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }
        password.afterTextChanged {
            loginViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password2.apply {
            afterTextChanged {
                loginViewModel.password2DataChanged(
                    password.text.toString(),
                    password2.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        Thread{
                            loginViewModel.login(
                                email.text.toString(),
                                password.text.toString()
                            )
                        }.start()
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                Thread{
                    loginViewModel.register(email.text.toString(), username.text.toString(), password.text.toString())
                }.start()
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}