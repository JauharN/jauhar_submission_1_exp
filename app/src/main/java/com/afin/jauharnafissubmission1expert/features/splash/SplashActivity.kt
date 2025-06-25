package com.afin.jauharnafissubmission1expert.features.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.OVERRIDE_TRANSITION_OPEN
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.afin.jauharnafissubmission1expert.BuildConfig
import com.afin.jauharnafissubmission1expert.R
import com.afin.jauharnafissubmission1expert.core.di.Injection
import com.afin.jauharnafissubmission1expert.databinding.ActivitySplashBinding
import com.afin.jauharnafissubmission1expert.features.auth.presentation.login.LoginActivity
import com.afin.jauharnafissubmission1expert.features.story.presentation.list.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set version
        binding.tvVersion.text = "v${BuildConfig.VERSION_NAME}"

        // Memulai animasi
        startAnimation()
        checkUserSession()
    }

    private fun startAnimation() {
        // Fade in
        val fadeIn = ObjectAnimator.ofFloat(binding.logoContainer, View.ALPHA, 0f, 1f).apply {
            duration = 1000
        }

        // Scale animation untuk logo
        val scaleX = ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_X, 0.8f, 1f).apply {
            duration = 800
        }
        val scaleY = ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_Y, 0.8f, 1f).apply {
            duration = 800
        }

        // Translation animation untuk nama app
        val translationY = ObjectAnimator.ofFloat(
            binding.tvAppName,
            View.TRANSLATION_Y,
            50f,
            0f
        ).apply {
            duration = 800
            startDelay = 200
        }

        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY, translationY)
            start()
        }
    }

    private fun checkUserSession() {
        lifecycleScope.launch {

            delay(1500)
            binding.progressBar.visibility = View.VISIBLE

            // Cek user session
            val userPreference = Injection.provideUserPreference(this@SplashActivity)
            val user = userPreference.getUser().first()

            delay(1000)

            val intent = if (user.isLogin && user.token.isNotEmpty()) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, LoginActivity::class.java)
            }

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

            // fade out animation
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            finish()
        }
    }
}