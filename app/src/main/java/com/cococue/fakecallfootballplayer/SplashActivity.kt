package com.cococue.fakecallfootballplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cococue.fakecallfootballplayer.databinding.ActivitySplashBinding
import com.cococue.fakecallfootballplayer.utils.AdManager
import com.cococue.fakecallfootballplayer.utils.LocaleHelper

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDurationMs = 2000L
    private var isNavigated = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.applyLanguage(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize AdManager & Remote Ad Config on Splash Screen
        AdManager.init(this)

        // Apply smooth fade-in animation
        val fadeInAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            duration = 1000
        }
        binding.layoutSplashContent.startAnimation(fadeInAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            showAppOpenAdAndNavigate()
        }, splashDurationMs)
    }

    private fun showAppOpenAdAndNavigate() {
        if (!isNavigated) {
            if (AdManager.isAppOpenAdLoaded()) {
                AdManager.showAppOpenAdIfAvailable(this) {
                    navigateToMain()
                }
            } else {
                // Wait up to 1 extra second for App Open Ad to finish loading
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!isNavigated) {
                        AdManager.showAppOpenAdIfAvailable(this) {
                            navigateToMain()
                        }
                    }
                }, 1000L)
            }
        }
    }

    private fun navigateToMain() {
        if (!isNavigated) {
            isNavigated = true
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
