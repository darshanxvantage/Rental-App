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

        val day = appPreference.getRentDueDay()
        binding.tvRentDueDateSubtitle.text = "${day}${getDaySuffix(day)} of every month"


        binding.switchPushNotif.setOnCheckedChangeListener(null)
        binding.switchEmailAlerts.setOnCheckedChangeListener(null)
        binding.switchWhatsApp.setOnCheckedChangeListener(null)
        binding.switchDND.setOnCheckedChangeListener(null)
        binding.switchAutoInvoice.setOnCheckedChangeListener(null)
        binding.switchBiometric.setOnCheckedChangeListener(null)
        binding.switchTwoStep.setOnCheckedChangeListener(null)
        binding.switchCloudBackup.setOnCheckedChangeListener(null)


        binding.switchPushNotif.isChecked   = appPreference.isPushNotifEnabled()
        binding.switchEmailAlerts.isChecked = appPreference.isEmailAlertsEnabled()
        binding.switchWhatsApp.isChecked    = appPreference.isWhatsAppAlertsEnabled()
        binding.switchDND.isChecked         = appPreference.isDNDEnabled()
        binding.switchAutoInvoice.isChecked = appPreference.isAutoInvoiceEnabled()
        binding.switchBiometric.isChecked   = appPreference.isBiometricEnabled()
        binding.switchTwoStep.isChecked     = appPreference.isTwoStepEnabled()
        binding.switchCloudBackup.isChecked = appPreference.isCloudBackupEnabled()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }
        binding.layoutTheme.setOnClickListener { showThemeDialog() }
        binding.layoutFontSize.setOnClickListener { showFontSizeDialog() }
        binding.layoutLanguage.setOnClickListener { showLanguageDialog() }
        binding.layoutPaymentMethods.setOnClickListener { toast("Payment Methods — Coming Soon!") }
        binding.layoutRentDueDate.setOnClickListener { showRentDueDateDialog() }
        binding.layoutIncomeReport.setOnClickListener { toast("Income Report — Coming Soon!") }
        binding.layoutAppPin.setOnClickListener { showPinDialog() }
        binding.layoutExportData.setOnClickListener { showExportDialog() }
        binding.layoutClearCache.setOnClickListener { showClearCacheDialog() }
        binding.layoutHelp.setOnClickListener { toast("Help & FAQ — Coming Soon!") }
        binding.layoutRateApp.setOnClickListener { openPlayStore() }
        binding.layoutPrivacyPolicy.setOnClickListener { toast("Privacy Policy — Coming Soon!") }
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
        binding.switchAutoInvoice.setOnCheckedChangeListener { _, on ->
            appPreference.setAutoInvoiceEnabled(on)
            toast(if (on) "Auto-Invoice ON" else "Auto-Invoice OFF")
        }
        binding.switchBiometric.setOnCheckedChangeListener { _, on ->
            appPreference.setBiometricEnabled(on)
            toast(if (on) "Biometric Lock ON" else "Biometric Lock OFF")
        }
        binding.switchTwoStep.setOnCheckedChangeListener { _, on ->
            appPreference.setTwoStepEnabled(on)
            toast(if (on) "Two-Step Verify ON" else "Two-Step Verify OFF")
        }
        binding.switchCloudBackup.setOnCheckedChangeListener { _, on ->
            appPreference.setCloudBackupEnabled(on)
            toast(if (on) "Cloud Backup ON" else "Cloud Backup OFF")
        }
    }

    private fun showThemeDialog() {
        val items = arrayOf("☀️  Light Mode", "🌙  Dark Mode")
        val current = if (appPreference.getTheme() == "DARK") 1 else 0
        AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setSingleChoiceItems(items, current) { dialog, which ->
                when (which) {
                    0 -> { appPreference.setTheme("LIGHT"); AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
                    1 -> { appPreference.setTheme("DARK");  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
                }
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Cancel", null).show()
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


                val scale = when (selected) { "Small" -> 0.85f; "Large" -> 1.15f; else -> 1.0f }
                val config = android.content.res.Configuration(resources.configuration)
                config.fontScale = scale
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)

                toast("Font size: $selected")
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Cancel", null).show()
    }


    private fun showLanguageDialog() {
        val displayNames = arrayOf(
            "🇬🇧  English",
            "🇮🇳  Hindi — हिंदी",
            "🇮🇳  Gujarati — ગુજરાતી",

        )
        val langKeys = arrayOf("English", "Hindi", "Gujarati", )
        val isoCodes = arrayOf("en", "hi", "gu",)
        val current  = langKeys.indexOf(appPreference.getLanguage()).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setSingleChoiceItems(displayNames, current) { dialog, which ->
                val selectedKey = langKeys[which]
                val selectedIso = isoCodes[which]

                // Save language
                appPreference.setLanguage(selectedKey)
                binding.tvLanguageSubtitle.text = selectedKey

                // Locale apply
                LocaleHelper.applyLocale(this, selectedIso)

                toast("Language: $selectedKey")
                dialog.dismiss()


                val intent = Intent(this, com.xvantage.rental.ui.dashboard.DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null).show()
    }


    private fun showRentDueDateDialog() {
        val days = (1..28).map { "${it}${getDaySuffix(it)}" }.toTypedArray()
        val cur  = (appPreference.getRentDueDay() - 1).coerceIn(0, 27)
        AlertDialog.Builder(this)
            .setTitle("📅 Rent Due Date")
            .setSingleChoiceItems(days, cur) { dialog, which ->
                val day = which + 1
                appPreference.setRentDueDay(day)
                binding.tvRentDueDateSubtitle.text = "${day}${getDaySuffix(day)} of every month"
                toast("Due date: ${day}${getDaySuffix(day)}")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null).show()
    }


    private fun showPinDialog() {
        AlertDialog.Builder(this)
            .setTitle("🔒 App PIN")
            .setItems(arrayOf("Set New PIN", "Change PIN", "Remove PIN")) { _, w ->
                when (w) {
                    0 -> toast("Set PIN — Coming Soon!")
                    1 -> toast("Change PIN — Coming Soon!")
                    2 -> { appPreference.setBiometricEnabled(false); toast("PIN removed") }
                }
            }
            .setNegativeButton("Cancel", null).show()
    }


    private fun showExportDialog() {
        AlertDialog.Builder(this)
            .setTitle("📤 Export Data")
            .setItems(arrayOf("📊  Excel (.xlsx)", "📄  PDF")) { _, w ->
                toast(if (w == 0) "Excel export — Coming Soon!" else "PDF export — Coming Soon!")
            }
            .setNegativeButton("Cancel", null).show()
    }


    private fun showClearCacheDialog() {
        AlertDialog.Builder(this)
            .setTitle("🗑️ Clear Cache")
            .setMessage("Temporary files clear honge. Data safe rahega. Continue?")
            .setPositiveButton("Clear") { _, _ ->
                try { cacheDir.deleteRecursively() } catch (e: Exception) { e.printStackTrace() }
                toast("Cache cleared!")
            }
            .setNegativeButton("Cancel", null).show()
    }


    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Kya aap logout karna chahte hain?")
            .setPositiveButton("Logout") { _, _ ->
                appPreference.logoutUser()
                val intent = Intent(this, com.xvantage.rental.ui.auth.AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null).show()
    }


    private fun openPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun getDaySuffix(day: Int) = when {
        day in 11..13 -> "th"; day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"; day % 10 == 3 -> "rd"; else -> "th"
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}