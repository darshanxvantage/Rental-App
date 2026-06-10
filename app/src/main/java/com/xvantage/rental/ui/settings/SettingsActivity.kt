package com.xvantage.rental.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.xvantage.rental.databinding.ActivitySettingsBinding
import com.xvantage.rental.utils.AppPreference

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var appPreference: AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPreference = AppPreference(this)

        setupInitialStates()
        setupClickListeners()
        setupSwitchListeners()
    }

    // ─── Load saved states into UI ───────────────────────────────────────────
    private fun setupInitialStates() {

        // Theme subtitle
        val currentTheme = appPreference.getTheme()
        binding.tvThemeSubtitle.text =
            if (currentTheme == "DARK") "Dark Mode" else "Light Mode"

        // Font Size subtitle
        binding.tvFontSizeSubtitle.text =
            appPreference.getFontSize().replaceFirstChar { it.uppercase() }

        // Language subtitle
        binding.tvLanguageSubtitle.text = appPreference.getLanguage()

        // Rent Due Date subtitle
        val day = appPreference.getRentDueDay()
        binding.tvRentDueDateSubtitle.text = "${day}${getDaySuffix(day)} of every month"

        // Switches - restore saved states
        binding.switchPushNotif.isChecked    = appPreference.isPushNotifEnabled()
        binding.switchEmailAlerts.isChecked  = appPreference.isEmailAlertsEnabled()
        binding.switchWhatsApp.isChecked     = appPreference.isWhatsAppAlertsEnabled()
        binding.switchDND.isChecked          = appPreference.isDNDEnabled()
        binding.switchAutoInvoice.isChecked  = appPreference.isAutoInvoiceEnabled()
        binding.switchBiometric.isChecked    = appPreference.isBiometricEnabled()
        binding.switchTwoStep.isChecked      = appPreference.isTwoStepEnabled()
        binding.switchCloudBackup.isChecked  = appPreference.isCloudBackupEnabled()
    }

    // ─── All click listeners ──────────────────────────────────────────────────
    private fun setupClickListeners() {

        // Back
        binding.ivBack.setOnClickListener { finish() }

        // Theme
        binding.layoutTheme.setOnClickListener { showThemeDialog() }

        // Font Size
        binding.layoutFontSize.setOnClickListener { showFontSizeDialog() }

        // Language
        binding.layoutLanguage.setOnClickListener { showLanguageDialog() }

        // Payment Methods
        binding.layoutPaymentMethods.setOnClickListener {
            Toast.makeText(this, "Payment Methods — Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // Rent Due Date
        binding.layoutRentDueDate.setOnClickListener { showRentDueDateDialog() }

        // Income Report
        binding.layoutIncomeReport.setOnClickListener {
            Toast.makeText(this, "Income Report — Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // App PIN
        binding.layoutAppPin.setOnClickListener { showPinDialog() }

        // Export Data
        binding.layoutExportData.setOnClickListener { showExportDialog() }

        // Clear Cache
        binding.layoutClearCache.setOnClickListener { showClearCacheDialog() }

        // Help & FAQ
        binding.layoutHelp.setOnClickListener {
            Toast.makeText(this, "Help & FAQ — Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // Rate the App
        binding.layoutRateApp.setOnClickListener { openPlayStore() }

        // Privacy Policy
        binding.layoutPrivacyPolicy.setOnClickListener {
            Toast.makeText(this, "Privacy Policy — Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        // Logout
        binding.layoutLogout.setOnClickListener { showLogoutDialog() }
    }

    // ─── All switch listeners ─────────────────────────────────────────────────
    private fun setupSwitchListeners() {

        binding.switchPushNotif.setOnCheckedChangeListener { _, isChecked ->
            appPreference.setPushNotifEnabled(isChecked)
            val msg = if (isChecked) "Push notifications enabled" else "Push notifications disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.switchEmailAlerts.setOnCheckedChangeListener { _, isChecked ->
            appPreference.setEmailAlertsEnabled(isChecked)
            val msg = if (isChecked) "Email alerts enabled" else "Email alerts disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.switchWhatsApp.setOnCheckedChangeListener { _, isChecked ->
            appPreference.setWhatsAppAlertsEnabled(isChecked)
            val msg = if (isChecked) "WhatsApp alerts enabled" else "WhatsApp alerts disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.switchDND.setOnCheckedChangeListener { _, isChecked ->
            appPreference.setDNDEnabled(isChecked)
            val msg = if (isChecked) "Do Not Disturb ON (10 PM – 8 AM)" else "Do Not Disturb OFF"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.switchAutoInvoice.setOnCheckedChangeListener { _, isChecked ->
            appPreference.setAutoInvoiceEnabled(isChecked)
            val msg = if (isChecked) "Auto-Invoice enabled" else "Auto-Invoice disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            appPreference.setBiometricEnabled(isChecked)
            val msg = if (isChecked) "Biometric Lock enabled" else "Biometric Lock disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.switchTwoStep.setOnCheckedChangeListener { _, isChecked ->
            appPreference.setTwoStepEnabled(isChecked)
            val msg = if (isChecked) "Two-Step Verify enabled" else "Two-Step Verify disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.switchCloudBackup.setOnCheckedChangeListener { _, isChecked ->
            appPreference.setCloudBackupEnabled(isChecked)
            val msg = if (isChecked) "Cloud Backup enabled" else "Cloud Backup disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Dialog: Theme ───────────────────────────────────────────────────────
    private fun showThemeDialog() {
        val items = arrayOf("🌞  Light Mode", "🌙  Dark Mode")
        val currentIndex = if (appPreference.getTheme() == "DARK") 1 else 0

        AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setSingleChoiceItems(items, currentIndex) { dialog, which ->
                when (which) {
                    0 -> {
                        appPreference.setTheme("LIGHT")
                        binding.tvThemeSubtitle.text = "Light Mode"
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        dialog.dismiss()
                        recreate()
                    }
                    1 -> {
                        appPreference.setTheme("DARK")
                        binding.tvThemeSubtitle.text = "Dark Mode"
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        dialog.dismiss()
                        recreate()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Dialog: Font Size ───────────────────────────────────────────────────
    private fun showFontSizeDialog() {
        val items = arrayOf("Small", "Medium", "Large")
        val saved = appPreference.getFontSize()
        val currentIndex = items.indexOfFirst { it.equals(saved, ignoreCase = true) }.coerceAtLeast(1)

        AlertDialog.Builder(this)
            .setTitle("Select Font Size")
            .setSingleChoiceItems(items, currentIndex) { dialog, which ->
                val selected = items[which]
                appPreference.setFontSize(selected)
                binding.tvFontSizeSubtitle.text = selected
                Toast.makeText(this, "Font size set to $selected", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Dialog: Language ────────────────────────────────────────────────────
    private fun showLanguageDialog() {
        val items = arrayOf("English", "Hindi — हिंदी", "Gujarati — ગુજરાતી", "Marathi — मराठी")
        val saved = appPreference.getLanguage()
        val currentIndex = items.indexOfFirst { it.startsWith(saved) }.coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setSingleChoiceItems(items, currentIndex) { dialog, which ->
                val selected = items[which].split(" — ")[0]
                appPreference.setLanguage(selected)
                binding.tvLanguageSubtitle.text = selected
                Toast.makeText(this, "Language set to $selected", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Dialog: Rent Due Date ───────────────────────────────────────────────
    private fun showRentDueDateDialog() {
        val days = (1..28).map { "${it}${getDaySuffix(it)}" }.toTypedArray()
        val currentDay = appPreference.getRentDueDay()
        val currentIndex = (currentDay - 1).coerceIn(0, 27)

        AlertDialog.Builder(this)
            .setTitle("Rent Due Date")
            .setSingleChoiceItems(days, currentIndex) { dialog, which ->
                val day = which + 1
                appPreference.setRentDueDay(day)
                binding.tvRentDueDateSubtitle.text = "${day}${getDaySuffix(day)} of every month"
                Toast.makeText(this, "Rent due date set to ${day}${getDaySuffix(day)}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Dialog: App PIN ─────────────────────────────────────────────────────
    private fun showPinDialog() {
        val options = arrayOf("Set New PIN", "Change PIN", "Remove PIN")
        AlertDialog.Builder(this)
            .setTitle("App PIN")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Set PIN — Coming Soon!", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(this, "Change PIN — Coming Soon!", Toast.LENGTH_SHORT).show()
                    2 -> {
                        appPreference.setBiometricEnabled(false)
                        Toast.makeText(this, "PIN removed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Dialog: Export Data ─────────────────────────────────────────────────
    private fun showExportDialog() {
        val options = arrayOf("📊  Export as Excel (.xlsx)", "📄  Export as PDF")
        AlertDialog.Builder(this)
            .setTitle("Export Data")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Exporting as Excel — Coming Soon!", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(this, "Exporting as PDF — Coming Soon!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Dialog: Clear Cache ─────────────────────────────────────────────────
    private fun showClearCacheDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Cache")
            .setMessage("This will clear temporary files. Your data will not be deleted. Continue?")
            .setPositiveButton("Clear") { _, _ ->
                clearAppCache()
                Toast.makeText(this, "Cache cleared successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAppCache() {
        try {
            cacheDir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ─── Dialog: Logout ──────────────────────────────────────────────────────
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                appPreference.logoutUser()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                // Navigate to AuthActivity and clear back stack
                val intent = Intent(this, com.xvantage.rental.ui.auth.AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Open Play Store ────────────────────────────────────────────────────
    private fun openPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    // ─── Helper: day suffix (1st, 2nd, 3rd...) ───────────────────────────────
    private fun getDaySuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else           -> "th"
        }
    }
}