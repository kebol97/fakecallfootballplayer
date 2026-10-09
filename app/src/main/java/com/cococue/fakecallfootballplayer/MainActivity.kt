package com.cococue.fakecallfootballplayer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.cococue.fakecallfootballplayer.databinding.ActivityMainBinding
import com.cococue.fakecallfootballplayer.utils.AdManager
import com.cococue.fakecallfootballplayer.utils.LocaleHelper
import com.cococue.fakecallfootballplayer.utils.PreferencesHelper
import android.content.Context

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferencesHelper

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.applyLanguage(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply Window Insets for Edge-to-Edge compliance
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Initialize AdManager & fetch Remote JSON Ad Config
        AdManager.init(this)

        prefs = PreferencesHelper(this)
        checkAndShowDisclaimer()

        setupBottomNavigation()
        if (savedInstanceState == null) {
            replaceFragment(FakeCallFragment())
        }
    }

    private fun checkAndShowDisclaimer() {
        if (!prefs.isDisclaimerAccepted) {
            AlertDialog.Builder(this)
                .setTitle(R.string.disclaimer_title)
                .setMessage(R.string.disclaimer_content)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_accept) { dialog, _ ->
                    prefs.isDisclaimerAccepted = true
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_fake_call -> {
                    replaceFragment(FakeCallFragment())
                    true
                }
                R.id.nav_wallpaper -> {
                    replaceFragment(WallpaperFragment())
                    true
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
