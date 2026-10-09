package com.cococue.fakecallfootballplayer.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory

object WallpaperHelper {
    fun setWallpaper(context: Context, resId: Int, flag: Int): Boolean {
        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = BitmapFactory.decodeResource(context.resources, resId)
            wallpaperManager.setBitmap(bitmap, null, true, flag)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
