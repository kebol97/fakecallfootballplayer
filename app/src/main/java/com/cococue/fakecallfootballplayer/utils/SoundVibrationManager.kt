package com.cococue.fakecallfootballplayer.utils

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager as AndroidRingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundVibrationManager(private val context: Context) {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    fun startRingtoneAndVibration() {
        try {
            val notification = AndroidRingtoneManager.getDefaultUri(AndroidRingtoneManager.TYPE_RINGTONE)
            ringtone = AndroidRingtoneManager.getRingtone(context, notification)
            ringtone?.play()

            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 1000, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            ringtone?.stop()
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
