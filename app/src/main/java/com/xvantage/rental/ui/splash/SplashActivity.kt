package com.xvantage.rental.ui.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import com.xvantage.rental.databinding.ActivitySplashBinding
import com.xvantage.rental.ui.auth.AuthActivity
import com.xvantage.rental.ui.dashboard.DashboardActivity
import com.xvantage.rental.ui.explore.ExploreActivity
import com.xvantage.rental.ui.explore.common.ExploreRoleManager
import com.xvantage.rental.ui.explore.roleSelection.RoleSelectionActivity
import com.xvantage.rental.ui.onboarding.BoardingScreenActivity
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var layoutBinding: ActivitySplashBinding
    private lateinit var appPreference: AppPreference

    companion object {
        private const val SPLASH_DELAY = 2000L
        private const val INTENT_FLAGS =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val prefs = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString("theme", "LIGHT") ?: "LIGHT"
        if (savedTheme == "DARK") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        layoutBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        appPreference = AppPreference(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                android.util.Log.d("FCM_TOKEN", "Token: $token")
                appPreference.setFcmToken(token)
            }
        Handler(Looper.getMainLooper()).postDelayed({

            val intent = when {

                // Logged in AND finished the profile setup -> Dashboard
                !appPreference.getToken().isNullOrEmpty() &&
                        appPreference.isProfileComplete() -> {

                    Intent(
                        this,
                        when (ExploreRoleManager.getRole(this)) {
                            ExploreRoleManager.ROLE_STUDENT -> ExploreActivity::class.java
                            ExploreRoleManager.ROLE_OWNER -> DashboardActivity::class.java
                            // role kadi select j nathi thayu (purana account) -> pehli baar puchho
                            else -> RoleSelectionActivity::class.java
                        }
                    )

                }

                // Logged in but never tapped "Continue" on Create Profile ->
                // resume exactly there instead of opening the Dashboard
                !appPreference.getToken().isNullOrEmpty() &&
                        !appPreference.isProfileComplete() -> {

                    Intent(
                        this,
                        AuthActivity::class.java
                    ).apply {
                        putExtra(AuthActivity.EXTRA_RESUME_PROFILE, true)
                    }

                }

                // First time app open
                !appPreference.isFirstTimePreview() -> {

                    Intent(
                        this,
                        BoardingScreenActivity::class.java
                    )

                }

                // User not logged in
                else -> {

                    Intent(
                        this,
                        AuthActivity::class.java
                    )

                }
            }

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            finish()

        }, SPLASH_DELAY)
    }
}