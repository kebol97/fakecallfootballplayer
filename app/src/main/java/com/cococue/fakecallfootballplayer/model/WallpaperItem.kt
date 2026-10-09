package com.cococue.fakecallfootballplayer.model

data class WallpaperItem(
    val id: String,
    val title: String,
    val resId: Int,
    val isVideo: Boolean = false,
    val category: String = "Photo"
)
