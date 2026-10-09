package com.cococue.fakecallfootballplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FakeCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val playerName = intent.getStringExtra("EXTRA_PLAYER_NAME") ?: "Cristiano Ronaldo"
        val playerAvatar = intent.getIntExtra("EXTRA_PLAYER_AVATAR", R.drawable.img_ronaldo)
        val playerVideo = intent.getIntExtra("EXTRA_PLAYER_VIDEO", R.raw.video_ronaldo)
        val template = intent.getStringExtra("EXTRA_TEMPLATE") ?: "WHATSAPP"
        val isVideoCall = intent.getBooleanExtra("EXTRA_IS_VIDEO", true)

        val callIntent = Intent(context, FakeCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_PLAYER_NAME", playerName)
            putExtra("EXTRA_PLAYER_AVATAR", playerAvatar)
            putExtra("EXTRA_PLAYER_VIDEO", playerVideo)
            putExtra("EXTRA_TEMPLATE", template)
            putExtra("EXTRA_IS_VIDEO", isVideoCall)
        }
        context.startActivity(callIntent)
    }
}
