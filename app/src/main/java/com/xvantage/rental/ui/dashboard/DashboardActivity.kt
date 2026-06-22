package com.xvantage.rental.ui.dashboard

import android.os.Bundle
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityDashboardBinding
import com.xvantage.rental.databinding.ToolbarLayoutBinding
import com.xvantage.rental.ui.addProperty.activity.AddPropertyActivity
import com.xvantage.rental.ui.dashboard.fragment.DuesFragment
import com.xvantage.rental.ui.dashboard.fragment.HomeFragment
import com.xvantage.rental.utils.CommonFunction
import com.xvantage.rental.ui.dashboard.fragment.ProfileFragment
import android.content.Intent
import com.xvantage.rental.ui.auth.AuthActivity
import com.xvantage.rental.ui.base.BaseActivity
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.ui.search.SearchPropertyActivity
import com.xvantage.rental.ui.settings.SettingsActivity
import com.xvantage.rental.utils.LocaleHelper
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint




@AndroidEntryPoint
class DashboardActivity : BaseActivity() {
    private lateinit var layoutBinding: ActivityDashboardBinding
    private lateinit var toolbarBinding: ToolbarLayoutBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appPreference: AppPreference

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()
        askNotificationPermission()
        setupViews()
        setupToolbar()
        setupNavigationDrawer()
        setupBottomNavigation()
        initializeDefaultFragment(savedInstanceState)
    }


    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }


    private fun setupWindow() {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        setupWindowInsets()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(layoutBinding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBarsInsets.bottom
            )
            insets
        }
    }

    private fun setupViews() {
        drawerLayout = layoutBinding.drawerLayout
        toolbarBinding = layoutBinding.toolbar
        appPreference = AppPreference(this)
    }

    private fun setupToolbar() {

        with(toolbarBinding) {

            home.visibility = View.VISIBLE
            search.visibility = View.VISIBLE
            setting.visibility = View.VISIBLE

            btnSave.visibility = View.GONE
            back.visibility = View.GONE

            home.setOnClickListener {
                toggleDrawer()
            }

            search.setOnClickListener {

                startActivity(
                    Intent(
                        this@DashboardActivity,
                        SearchPropertyActivity::class.java
                    )
                )
            }

            setting.setOnClickListener {

                startActivity(
                    Intent(
                        this@DashboardActivity,
                        SettingsActivity::class.java
                    )
                )
            }
        }
    }

    private fun setupNavigationDrawer() {
        with(layoutBinding.navigationView) {
            findViewById<View>(R.id.more_apps_tv)?.setOnClickListener {
                showToast("More Apps")
                closeDrawer()
            }

            findViewById<View>(R.id.premium_tv)?.setOnClickListener {
                showToast("Premium")
                closeDrawer()
            }

            findViewById<View>(R.id.shaer_app_tv)?.setOnClickListener {

                val appPackageName = packageName

                val shareIntent = Intent(Intent.ACTION_SEND)

                shareIntent.type = "text/plain"

                shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "RentMaster"
                )

                shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Download RentMaster App:\nhttps://play.google.com/store/apps/details?id=$appPackageName"
                )

                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Share RentMaster"
                    )
                )

                closeDrawer()
            }

            findViewById<View>(R.id.rate_us_tv)?.setOnClickListener {
                CommonFunction().showRatingDialog(this@DashboardActivity)
                closeDrawer()
            }
            findViewById<View>(R.id.logout_tv)
                ?.setOnClickListener {

                    // CLEAR TOKEN

                    appPreference.logoutUser()

                    // OPEN LOGIN SCREEN


                    val intent = Intent(
                        this@DashboardActivity,
                        AuthActivity::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)

                    finish()
                }

        }
    }

    private fun setupBottomNavigation() {
        with(layoutBinding.bottomNavigation) {
            selectedItemId = R.id.home
            updateBottomNavigationIcons(R.id.home)
            setOnNavigationItemSelectedListener { menuItem ->
                handleBottomNavigationItemSelected(menuItem.itemId)
            }
        }
    }

    private fun handleBottomNavigationItemSelected(itemId: Int): Boolean {
        updateBottomNavigationIcons(itemId)
        return when (itemId) {
            R.id.home -> {
                loadFragment(HomeFragment())
                true
            }
            R.id.property -> {
                CommonFunction().navigation(this, AddPropertyActivity::class.java)
                true
            }
            R.id.settings -> {
                loadFragment(DuesFragment())
                true
            }
                    R.id.profile -> {
                loadFragment(ProfileFragment())
                true
            }
            else -> false
        }
    }

    private fun initializeDefaultFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            layoutBinding.bottomNavigation.selectedItemId = R.id.home
            loadFragment(HomeFragment())
        }
    }

    private fun updateBottomNavigationIcons(selectedItemId: Int) {
        val menu = layoutBinding.bottomNavigation.menu
        menu.findItem(R.id.home).setIcon(
            if (selectedItemId == R.id.home) R.drawable.home_nav_selected 
            else R.drawable.home_nav_unselected
        )
        menu.findItem(R.id.settings).setIcon(
            if (selectedItemId == R.id.settings) R.drawable.due_nav_selected 
            else R.drawable.due_nav_unselected
        )
        menu.findItem(R.id.profile).setIcon(
            if (selectedItemId == R.id.profile)
                R.drawable.ic_profile_nav
            else
                R.drawable.ic_profile_nav
        )
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }

    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
        } else {
            openDrawer()
        }
    }

    private fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
        } else {
            super.onBackPressed()
        }
    }
}