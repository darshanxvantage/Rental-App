package com.xvantage.rental.ui.splash

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
import com.xvantage.rental.ui.onboarding.BoardingScreenActivity
import com.xvantage.rental.utils.AppPreference
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var layoutBinding: ActivitySplashBinding
    private lateinit var appPreference: AppPreference

    companion object {
        private const val SPLASH_DELAY = 2000L
        private const val INTENT_FLAGS = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        layoutBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        appPreference = AppPreference(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
//        startActivity(Intent(this, DashboardActivity::class.java))

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = when {
                /*!appPreference.getToken().isNullOrEmpty() -> {
                    Intent(this, DashboardActivity::class.java)
                }
                else -> {
                    Intent(this, DashboardActivity::class.java)
                }
*/                !appPreference.getToken().isNullOrEmpty() -> {
                    Intent(this, DashboardActivity::class.java)
                }
                !appPreference.isFirstTimePreview() -> {
                    Intent(this, BoardingScreenActivity::class.java)
                }
                else -> {
                    Intent(this, AuthActivity::class.java)
                }
            }
            intent.flags = INTENT_FLAGS
            startActivity(intent)
        }, SPLASH_DELAY)
    }
}
