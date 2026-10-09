package com.cococue.fakecallfootballplayer.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, languageCode: String) {
        PreferencesHelper(context).selectedLanguage = languageCode
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)

        updateResources(context, languageCode)
    }

    fun getLanguage(context: Context): String {
        val prefsLang = PreferencesHelper(context).selectedLanguage
        if (!prefsLang.isNullOrEmpty()) {
            return prefsLang
        }
        val locales = AppCompatDelegate.getApplicationLocales()
        if (!locales.isEmpty) {
            val lang = locales[0]?.language
            if (!lang.isNullOrEmpty()) {
                return lang
            }
        }
        val defaultLang = Locale.getDefault().language
        return if (defaultLang == "id") "id" else "en"
    }

    fun applyLanguage(context: Context) {
        val lang = getLanguage(context)
        val appLocale = LocaleListCompat.forLanguageTags(lang)
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != appLocale.toLanguageTags()) {
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
        updateResources(context, lang)
    }

    fun wrapContext(context: Context): ContextWrapper {
        val lang = getLanguage(context)
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocales(LocaleList(locale))

        val newContext = context.createConfigurationContext(config)
        return ContextWrapper(newContext)
    }

    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, languageCode: String) {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocales(LocaleList(locale))
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
