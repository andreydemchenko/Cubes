package ru.turbopro.cubes.ui.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ru.turbopro.cubes.EMAIL_ERROR_TEXT
import ru.turbopro.cubes.R
import ru.turbopro.cubes.data.utils.LogInErrors
import ru.turbopro.cubes.data.utils.extensions.addEmailToNickname
import ru.turbopro.cubes.databinding.ActivityLoginBinding
import ru.turbopro.cubes.ui.LoginViewErrors
import ru.turbopro.cubes.ui.MyOnFocusChangeListener
import ru.turbopro.cubes.ui.launchHome
import ru.turbopro.cubes.viewmodels.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    private val focusChangeListener = MyOnFocusChangeListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        observeView()
        setUpViews()
    }

    private fun observeView() {
        viewModel.errorStatusLoginFragment.observe(this) { err ->
            modifyErrors(err)
        }

        viewModel.loginErrorStatus.observe(this) { err ->
            when (err) {
                LogInErrors.LERR -> setErrorText(getString(R.string.login_error_text))
                else -> binding.loginErrorTextView.visibility = View.GONE
            }
        }
    }

    private fun setUpViews() {
        binding.loginErrorTextView.visibility = View.GONE

        binding.loginEmailEditText.onFocusChangeListener = focusChangeListener
        binding.loginPasswordEditText.onFocusChangeListener = focusChangeListener

        binding.loginLoginBtn.setOnClickListener {
            onLogin()
            if (viewModel.errorStatusLoginFragment.value == LoginViewErrors.NONE) {
                viewModel.loginErrorStatus.observe(this) {
                    if (it == LogInErrors.NONE) {

                        println(viewModel.userData.value)

                        //viewModel.isUserLoggedIn.observe(viewLifecycleOwner) {
                        //if (it == true) {
                        println("in viewModel.isUserLoggedIn.observe before viewModel.userData.value")
                        viewModel.userData.value?.let { it1 ->
                            println(viewModel.userData.value)
                            application?.let { it2 -> viewModel.login(it1, true, it2) }
                            //}
                            println("in viewModel.isUserLoggedIn.observe after viewModel.userData.value")
                            //}
                            application?.let { launchHome(it) }
                        }
                    }
                }
            }
        }
    }

    private fun modifyErrors(err: LoginViewErrors) {
        when (err) {
            LoginViewErrors.NONE -> setEditTextErrors()
            LoginViewErrors.ERR_EMPTY -> setErrorText("Fill all details")
            LoginViewErrors.ERR_EMAIL -> setEditTextErrors(EMAIL_ERROR_TEXT)
        }
    }

    private fun setErrorText(errText: String?) {
        binding.loginErrorTextView.visibility = View.VISIBLE
        if (errText != null) {
            binding.loginErrorTextView.text = errText
        }
    }

    private fun setEditTextErrors(emError: String? = null) {
        binding.loginErrorTextView.visibility = View.GONE
        binding.loginEmailEditText.error = emError
    }

    private fun onLogin() {
        val em = binding.loginEmailEditText.text.toString().addEmailToNickname()
        val pwd = binding.loginPasswordEditText.text.toString()

        viewModel.loginSubmitData(em, pwd)
    }
}
