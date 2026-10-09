package com.cococue.fakecallfootballplayer

import android.app.WallpaperManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cococue.fakecallfootballplayer.databinding.ActivityWallpaperDetailBinding
import com.cococue.fakecallfootballplayer.utils.AdManager
import com.cococue.fakecallfootballplayer.utils.LocaleHelper
import com.cococue.fakecallfootballplayer.utils.WallpaperHelper
import android.content.Context

class WallpaperDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWallpaperDetailBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.btnBack.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Football Wallpaper"
        val resId = intent.getIntExtra("EXTRA_RES_ID", R.drawable.bg_wallpaper_1)

        binding.tvWallpaperTitle.text = title
        binding.imgFullWallpaper.setImageResource(resId)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnApplyWallpaper.setOnClickListener {
            showWallpaperOptionsDialog(resId)
        }
    }

    private fun showWallpaperOptionsDialog(resId: Int) {
        val options = arrayOf(
            getString(R.string.home_screen),
            getString(R.string.lock_screen),
            getString(R.string.both_screens)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.set_as_wallpaper)
            .setItems(options) { dialog, which ->
                val flag = when (which) {
                    0 -> WallpaperManager.FLAG_SYSTEM
                    1 -> WallpaperManager.FLAG_LOCK
                    else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    } else {
                        WallpaperManager.FLAG_SYSTEM
                    }
                }

                val success = WallpaperHelper.setWallpaper(this, resId, flag)
                if (success) {
                    Toast.makeText(this, R.string.wallpaper_success, Toast.LENGTH_SHORT).show()
                    AdManager.showInterstitialAd(this) {}
                } else {
                    Toast.makeText(this, "Gagal memasang wallpaper", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .show()
    }
}
