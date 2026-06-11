package com.xvantage.rental.ui.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.LocaleHelper


/**
 * BaseActivity
 *
 * Apni SAARI Activities mein sirf ek kaam karo:
 *   AppCompatActivity  →  BaseActivity
 *
 * Example:
 *   class DashboardActivity : BaseActivity() { ... }
 *   class ProfileActivity   : BaseActivity() { ... }
 *
 * Bas! Theme + Language automatically saari jagah kaam karegi.
 */
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Theme: super.onCreate se PEHLE apply karo
        val pref = AppPreference(this)
        if (pref.getTheme() == "DARK") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
    }
}