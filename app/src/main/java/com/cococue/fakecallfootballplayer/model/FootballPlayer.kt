package com.cococue.fakecallfootballplayer.model

import com.cococue.fakecallfootballplayer.R

data class FootballPlayer(
    val id: String,
    val name: String,
    val clubInfo: String,
    val avatarRes: Int = R.drawable.img_ronaldo,
    val videoRes: Int = R.raw.video_ronaldo,
    val phoneNumber: String = "+1 555 0199",
    val customAvatarUri: String? = null,
    val customVideoUri: String? = null
)
