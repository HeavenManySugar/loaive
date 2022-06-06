package com.ntihs.loaive.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.ntihs.loaive.databinding.ActivityLoginBinding

import com.ntihs.loaive.R
import com.ntihs.loaive.security.KeyStoreHelper
import com.ntihs.loaive.security.SharedPreferencesHelper
import com.ntihs.loaive.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    companion object{
        var preferencesHelper: SharedPreferencesHelper? = null
        var keyStoreHelper: KeyStoreHelper? = null
    }

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.email
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val signup = binding.signup
        preferencesHelper = SharedPreferencesHelper(applicationContext)
        keyStoreHelper = KeyStoreHelper(applicationContext, preferencesHelper)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both email / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.emailError != null) {
                email.error = getString(loginState.emailError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
            else if (password.text.isBlank()) {
                login.isEnabled = false
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
                (password as TextView).text = ""
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success, loginResult.token)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            //finish()
        })

        email.afterTextChanged {
            loginViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        loading.visibility = View.VISIBLE
                        Thread {
                            loginViewModel.login(
                                email.text.toString(),
                                password.text.toString()
                            )
                        }.start()
                    }
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE

                Thread{
                    loginViewModel.login(email.text.toString(), password.text.toString())
                }.start()
            }
        }

        signup!!.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            intent.putExtra("username", email.text.toString())
            intent.putExtra("password", password.text.toString())
            startActivity(intent)
        }

        if(preferencesHelper?.getInput("username") != null && preferencesHelper?.getInput("username") != ""){
            (email as TextView).text = keyStoreHelper?.decrypt(preferencesHelper?.getInput("username")!!)!!
            (password as TextView).text = keyStoreHelper?.decrypt(preferencesHelper?.getInput("password")!!)!!
            loading.visibility = View.VISIBLE

            Thread{
                loginViewModel.login(email.text.toString(), password.text.toString())
            }.start()
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView, token: String?) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_SHORT
        ).show()
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.putExtra("userName", displayName)
        intent.putExtra("token", token)
        startActivity(intent)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}


