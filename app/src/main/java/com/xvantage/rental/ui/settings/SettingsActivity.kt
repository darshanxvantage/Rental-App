package com.xvantage.rental.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.xvantage.rental.databinding.ActivitySettingsBinding
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.LocaleHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var appPreference: AppPreference

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = AppPreference(this)
        if (pref.getTheme() == "DARK") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appPreference = pref

        setupInitialStates()
        setupClickListeners()
        setupSwitchListeners()
    }

    private fun setupInitialStates() {
        binding.tvThemeSubtitle.text =
            if (appPreference.getTheme() == "DARK") "Dark Mode" else "Light Mode"
        binding.tvFontSizeSubtitle.text = appPreference.getFontSize()
        binding.tvLanguageSubtitle.text = appPreference.getLanguage()

        // Clear all listeners before setting values
        binding.switchPushNotif.setOnCheckedChangeListener(null)
        binding.switchEmailAlerts.setOnCheckedChangeListener(null)
        binding.switchWhatsApp.setOnCheckedChangeListener(null)
        binding.switchDND.setOnCheckedChangeListener(null)

        // Restore saved switch states
        binding.switchPushNotif.isChecked   = appPreference.isPushNotifEnabled()
        binding.switchEmailAlerts.isChecked = appPreference.isEmailAlertsEnabled()
        binding.switchWhatsApp.isChecked    = appPreference.isWhatsAppAlertsEnabled()
        binding.switchDND.isChecked         = appPreference.isDNDEnabled()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }
        binding.layoutTheme.setOnClickListener { showThemeDialog() }
        binding.layoutFontSize.setOnClickListener { showFontSizeDialog() }
        binding.layoutLanguage.setOnClickListener { showLanguageDialog() }
        binding.layoutLogout.setOnClickListener { showLogoutDialog() }
    }

    private fun setupSwitchListeners() {
        binding.switchPushNotif.setOnCheckedChangeListener { _, on ->
            appPreference.setPushNotifEnabled(on)
            toast(if (on) "Push notifications ON" else "Push notifications OFF")
        }
        binding.switchEmailAlerts.setOnCheckedChangeListener { _, on ->
            appPreference.setEmailAlertsEnabled(on)
            toast(if (on) "Email alerts ON" else "Email alerts OFF")
        }
        binding.switchWhatsApp.setOnCheckedChangeListener { _, on ->
            appPreference.setWhatsAppAlertsEnabled(on)
            toast(if (on) "WhatsApp alerts ON" else "WhatsApp alerts OFF")
        }
        binding.switchDND.setOnCheckedChangeListener { _, on ->
            appPreference.setDNDEnabled(on)
            toast(if (on) "Do Not Disturb ON" else "Do Not Disturb OFF")
        }
    }

    private fun showThemeDialog() {
        val items = arrayOf("☀️  Light Mode", "🌙  Dark Mode")
        val current = if (appPreference.getTheme() == "DARK") 1 else 0
        AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setSingleChoiceItems(items, current) { dialog, which ->
                when (which) {
                    0 -> {
                        appPreference.setTheme("LIGHT")
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    1 -> {
                        appPreference.setTheme("DARK")
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFontSizeDialog() {
        val labels    = arrayOf("🔡  Small", "🔤  Medium", "🅰️  Large")
        val sizeNames = arrayOf("Small", "Medium", "Large")
        val current   = sizeNames.indexOf(appPreference.getFontSize()).coerceAtLeast(1)

        AlertDialog.Builder(this)
            .setTitle("Select Font Size")
            .setSingleChoiceItems(labels, current) { dialog, which ->
                val selected = sizeNames[which]
                appPreference.setFontSize(selected)
                binding.tvFontSizeSubtitle.text = selected

                val scale = when (selected) {
                    "Small" -> 0.85f
                    "Large" -> 1.15f
                    else    -> 1.0f
                }
                val config = android.content.res.Configuration(resources.configuration)
                config.fontScale = scale
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)

                toast("Font size: $selected")
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLanguageDialog() {
        val displayNames = arrayOf(
            "🇬🇧  English",
            "🇮🇳  Hindi — हिंदी",
            "🇮🇳  Gujarati — ગુજરાતી"
        )
        val langKeys = arrayOf("English", "Hindi", "Gujarati")
        val isoCodes = arrayOf("en", "hi", "gu")
        val current  = langKeys.indexOf(appPreference.getLanguage()).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setSingleChoiceItems(displayNames, current) { dialog, which ->
                val selectedKey = langKeys[which]
                val selectedIso = isoCodes[which]

                appPreference.setLanguage(selectedKey)
                binding.tvLanguageSubtitle.text = selectedKey
                LocaleHelper.applyLocale(this, selectedIso)

                toast("Language: $selectedKey")
                dialog.dismiss()

                // Restart DashboardActivity so language applies to full app
                val intent = Intent(
                    this,
                    com.xvantage.rental.ui.dashboard.DashboardActivity::class.java
                )
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                appPreference.logoutUser()
                val intent = Intent(
                    this,
                    com.xvantage.rental.ui.auth.AuthActivity::class.java
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}