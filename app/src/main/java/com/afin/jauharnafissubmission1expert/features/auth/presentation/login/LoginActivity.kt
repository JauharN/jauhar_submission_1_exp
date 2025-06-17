package com.afin.jauharnafissubmission1expert.features.auth.presentation.login

import android.app.Activity.OVERRIDE_TRANSITION_OPEN
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.afin.jauharnafissubmission1expert.R
import com.afin.jauharnafissubmission1expert.core.utils.Result
import com.afin.jauharnafissubmission1expert.core.utils.ViewModelFactory
import com.afin.jauharnafissubmission1expert.core.utils.showToast
import com.afin.jauharnafissubmission1expert.databinding.ActivityLoginBinding
import com.afin.jauharnafissubmission1expert.features.auth.presentation.register.RegisterActivity
import com.afin.jauharnafissubmission1expert.features.story.presentation.list.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupAction()
        checkIfLoggedIn()
    }

    private fun checkIfLoggedIn() {
        // Check if user is already logged in
        lifecycleScope.launch {
            viewModel.getUser().collect { user ->
                if (user.isLogin && user.token.isNotEmpty()) {
                    navigateToMain()
                }
            }
        }
    }

    private fun setupAction() {
        // Hide back button on login (no previous screen)
        binding.btnBack.visibility = View.GONE

        // Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        // Register link click
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

            // Use new transition API
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.edLoginEmail.error = getString(R.string.error_field_empty)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.edLoginPassword.error = getString(R.string.error_field_empty)
            isValid = false
        } else if (password.length < 8) {
            binding.edLoginPassword.error = getString(R.string.error_password_short)
            isValid = false
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        viewModel.login(email, password).observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }

                is Result.Success -> {
                    showLoading(false)
                    showToast(getString(R.string.login_success))
                    navigateToMain()
                }

                is Result.Error -> {
                    showLoading(false)
                    showToast(result.message)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !isLoading
            edLoginEmail.isEnabled = !isLoading
            edLoginPassword.isEnabled = !isLoading
            tvRegister.isEnabled = !isLoading
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}