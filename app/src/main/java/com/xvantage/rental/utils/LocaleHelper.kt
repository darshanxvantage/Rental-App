package com.xvantage.rental.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    private val languageMap = mapOf(
        "English"  to "en",
        "Hindi"    to "hi",
        "Gujarati" to "gu",
        "Marathi"  to "mr"
    )

    /**
     * Har Activity ke attachBaseContext() mein yeh call karo.
     * Saved language automatically apply hogi.
     */
    fun wrap(context: Context): Context {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val savedLang = prefs.getString("language", "English") ?: "English"
        val isoCode = languageMap[savedLang] ?: "en"
        return applyLocale(context, isoCode)
    }

    /**
     * Language change karne ke liye call karo.
     * Baad mein Activity ka recreate() call karo.
     */
    fun applyLocale(context: Context, isoCode: String): Context {
        val locale = Locale(isoCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}