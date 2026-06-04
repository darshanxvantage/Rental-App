package com.xvantage.rental.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xvantage.rental.databinding.ActivitySettingsBinding
import androidx.appcompat.app.AlertDialog
import com.xvantage.rental.utils.AppPreference
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private lateinit var appPreference: AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPreference = AppPreference(this)

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.layoutTheme.setOnClickListener {

            showThemeDialog()
        }
    }
    private fun showThemeDialog() {

        val items = arrayOf(
            "Light Mode",
            "Dark Mode"
        )

        AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setItems(items) { _, which ->

                when (which) {

                    0 -> {

                        appPreference.setTheme("LIGHT")

                        AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_NO
                        )

                        recreate()
                    }

                    1 -> {

                        appPreference.setTheme("DARK")

                        AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_YES
                        )

                        recreate()
                    }
                }
            }
            .show()
    }
}