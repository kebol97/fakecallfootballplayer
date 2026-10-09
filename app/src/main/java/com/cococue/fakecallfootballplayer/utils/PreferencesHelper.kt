package com.cococue.fakecallfootballplayer.utils

import android.content.Context

class PreferencesHelper(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var isDisclaimerAccepted: Boolean
        get() = prefs.getBoolean("disclaimer_accepted", false)
        set(value) = prefs.edit().putBoolean("disclaimer_accepted", value).apply()

    var selectedLanguage: String?
        get() = prefs.getString("selected_language", null)
        set(value) = prefs.edit().putString("selected_language", value).apply()
}
