package com.xvantage.rental.ui.dashboard

import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.xvantage.rental.databinding.BottomsheetFeedbackBinding
import kotlinx.coroutines.launch
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import android.content.res.ColorStateList
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityDashboardBinding
import com.xvantage.rental.databinding.ToolbarLayoutBinding
import com.xvantage.rental.ui.addProperty.activity.AddPropertyActivity
import com.xvantage.rental.ui.auth.AuthActivity
import com.xvantage.rental.ui.base.BaseActivity
import com.xvantage.rental.ui.dashboard.fragment.DuesFragment
import com.xvantage.rental.ui.dashboard.fragment.HomeFragment
import com.xvantage.rental.ui.dashboard.fragment.ProfileFragment
import com.xvantage.rental.ui.search.SearchPropertyActivity
//import com.xvantage.rental.ui.settings.SettingsActivity
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.CommonFunction
import com.xvantage.rental.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : BaseActivity() {

    private lateinit var layoutBinding: ActivityDashboardBinding
    private lateinit var toolbarBinding: ToolbarLayoutBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appPreference: AppPreference
    private val feedbackViewModel: FeedbackViewModel by viewModels()

    private val PKG_BOOKMYFARM = "com.app.bookmyfarm"
    private val PKG_SPYGAME    = "com.xv.spygame"
    private val PKG_AGECALC    = "com.xv.agecalc"
    private val PKG_PUZZLE     = "com.XV.Puzzel"
    private val DEVELOPER_URL  = "https://play.google.com/store/apps/developer?id=XV+Infotech+LLP"


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
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
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
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, systemBarsInsets.bottom)
            insets
        }
    }

    private fun setupViews() {
        drawerLayout   = layoutBinding.drawerLayout
        toolbarBinding = layoutBinding.toolbar
        appPreference  = AppPreference(this)
    }

    private fun setupToolbar() {
        with(toolbarBinding) {
            home.visibility    = View.VISIBLE
            search.visibility  = View.VISIBLE
//            setting.visibility = View.VISIBLE
            btnSave.visibility = View.GONE
            back.visibility    = View.GONE
            home.setOnClickListener   { toggleDrawer() }
            search.setOnClickListener { startActivity(Intent(this@DashboardActivity, SearchPropertyActivity::class.java)) }
//            setting.setOnClickListener { startActivity(Intent(this@DashboardActivity, SettingsActivity::class.java)) }
        }
    }

    private fun setupNavigationDrawer() {
        with(layoutBinding.navigationView) {
            // ── More Apps → Premium BottomSheet ──
            findViewById<View>(R.id.more_apps_tv)?.setOnClickListener {
                closeDrawer()
                showMoreAppsBottomSheet()
            }
            // ── Go Premium ──
            findViewById<View>(R.id.premium_tv)?.setOnClickListener {
                showToast("Coming Soon!")
                closeDrawer()
            }
            // ── Share App ──
            findViewById<View>(R.id.shaer_app_tv)?.setOnClickListener {
                val playStoreLink = "https://play.google.com/store/apps/details?id=com.xv.rentalmaster&hl=en_IN"
                val shareText = """
🏠 xVantage Rental Master

Managing rental properties has never been easier!
✅ Track tenants & payments
✅ Due payment reminders
✅ Property management at your fingertips

📲 Download now:
$playStoreLink
                """.trimIndent()

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "xVantage Rental Master App")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                startActivity(Intent.createChooser(shareIntent, "Share xVantage Rental Master"))
                closeDrawer()
            }
// ── Rate Us ──
            findViewById<View>(R.id.rate_us_tv)?.setOnClickListener {

                closeDrawer()

                val playStorePackage = "com.xv.rentalmaster"

                try {

                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$playStorePackage")
                    )

                    intent.setPackage("com.android.vending")
                    startActivity(intent)

                } catch (e: Exception) {

                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$playStorePackage")
                    )

                    startActivity(intent)
                }
            }
            findViewById<View>(R.id.policy_tv)?.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, PrivacyPolicyActivity::class.java))
                closeDrawer()
            }

// ── Feedback ──
            findViewById<View>(R.id.feedback_tv)?.setOnClickListener {
                closeDrawer()
                showFeedbackBottomSheet()
            }

            // ── Logout ──
            findViewById<View>(R.id.logout_tv)?.setOnClickListener {
                appPreference.logoutUser()
                startActivity(Intent(this@DashboardActivity, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
    }

    private fun showMoreAppsBottomSheet() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view   = layoutInflater.inflate(R.layout.bottomsheet_more_apps, null)
        dialog.setContentView(view)

        // Load real Play Store icons via Glide
        loadAppIcon(view, R.id.ivBookMyFarm, PKG_BOOKMYFARM)
        loadAppIcon(view, R.id.ivSpyGame,    PKG_SPYGAME)
        loadAppIcon(view, R.id.ivAgeCalc,    PKG_AGECALC)
        loadAppIcon(view, R.id.ivPuzzle,     PKG_PUZZLE)

        // ── BookMyFarm ──
        view.findViewById<LinearLayout>(R.id.cardBookMyFarm)?.setOnClickListener {
            openApp(PKG_BOOKMYFARM); dialog.dismiss()
        }
        view.findViewById<MaterialButton>(R.id.btnOpenBookMyFarm)?.setOnClickListener {
            openApp(PKG_BOOKMYFARM); dialog.dismiss()
        }

        // ── SpyGame ──
        view.findViewById<LinearLayout>(R.id.cardSpyGame)?.setOnClickListener {
            openApp(PKG_SPYGAME); dialog.dismiss()
        }

        // ── AgeCalc ──
        view.findViewById<LinearLayout>(R.id.cardAgeCalc)?.setOnClickListener {
            openApp(PKG_AGECALC); dialog.dismiss()
        }

        // ── Puzzle ──
        view.findViewById<LinearLayout>(R.id.cardPuzzle)?.setOnClickListener {
            openApp(PKG_PUZZLE); dialog.dismiss()
        }

        // ── View All ──
        view.findViewById<LinearLayout>(R.id.tvViewAllApps)?.setOnClickListener {
            openUrl(DEVELOPER_URL); dialog.dismiss()
        }

        dialog.show()
    }

    private fun showFeedbackBottomSheet() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val sheetBinding = BottomsheetFeedbackBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        feedbackViewModel.resetState()

        // ── Chip colors ───────────────────────────────────────────────────────
        val chipBg = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(Color.parseColor("#0B2140"), Color.parseColor("#F0F1F4"))
        )
        val chipText = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(Color.WHITE, Color.parseColor("#0F1B3D"))
        )
        listOf(
            sheetBinding.chipSuggestion,
            sheetBinding.chipBug,
            sheetBinding.chipComplaint,
            sheetBinding.chipCompliment
        ).forEach { chip ->
            chip.chipBackgroundColor = chipBg
            chip.setTextColor(chipText)
        }

      
        fun openPlayStoreReview() {
            val uri = android.net.Uri.parse("market://details?id=com.xv.rentalmaster")
            try {
                startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: android.content.ActivityNotFoundException) {
                startActivity(android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.xv.rentalmaster&hl=en_IN")
                ))
            }
        }
        sheetBinding.ratingBarFeedback.setOnRatingBarChangeListener { _, _, fromUser ->
            if (!fromUser) return@setOnRatingBarChangeListener
            openPlayStoreReview()
        }

        // ── Submit feedback button ────────────────────────────────────────────
        sheetBinding.btnSubmitFeedback.setOnClickListener {
            val message = sheetBinding.etFeedbackMessage.text.toString().trim()
            val rating = sheetBinding.ratingBarFeedback.rating.toInt()

            val selectedChipId = sheetBinding.chipGroupCategory.checkedChipId
            val category = when (selectedChipId) {
                sheetBinding.chipBug.id        -> "BUG"
                sheetBinding.chipComplaint.id  -> "COMPLAINT"
                sheetBinding.chipCompliment.id -> "COMPLIMENT"
                else                            -> "SUGGESTION"
            }

            if (message.isEmpty()) {
                sheetBinding.tilFeedbackMessage.error = "Please tell us what's on your mind"
                return@setOnClickListener
            }
            sheetBinding.tilFeedbackMessage.error = null

            val appVersion = try {
                packageManager.getPackageInfo(packageName, 0).versionName
            } catch (e: Exception) { null }

            feedbackViewModel.submitFeedback(category, rating, message, appVersion)
        }

        // ── Loading state ─────────────────────────────────────────────────────
        lifecycleScope.launch {
            feedbackViewModel.isSubmitting.collect { loading ->
                sheetBinding.progressFeedback.visibility = if (loading) View.VISIBLE else View.GONE
                sheetBinding.btnSubmitFeedback.isEnabled = !loading
            }
        }

        // ── ✅ Success — popup dikhao ─────────────────────────────────────────
        lifecycleScope.launch {
            feedbackViewModel.submitSuccess.collect { successMsg ->
                if (successMsg != null) {
                    dialog.dismiss()
                    showFeedbackSuccessDialog()
                }
            }
        }

        // ── Error ─────────────────────────────────────────────────────────────
        lifecycleScope.launch {
            feedbackViewModel.submitError.collect { errorMsg ->
                if (errorMsg != null) {
                    showToast("❌ $errorMsg")
                }
            }
        }

        dialog.show()
    }

    // ✅ Beautiful success popup after feedback submit
    private fun showFeedbackSuccessDialog() {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        val successDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .create()

        successDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Custom view banao
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(60, 60, 60, 50)
            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
        }

        val emoji = android.widget.TextView(this).apply {
            text = "🎉"
            textSize = 48f
            gravity = android.view.Gravity.CENTER
        }

        val title = android.widget.TextView(this).apply {
            text = "Thank You!"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#0B2140"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 16, 0, 8)
        }

        val message = android.widget.TextView(this).apply {
            text = "Your feedback has been submitted successfully.\nWe\'ll use it to make RentMaster even better! 🏠"
            textSize = 14f
            setTextColor(Color.parseColor("#444444"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        val btnOk = com.google.android.material.button.MaterialButton(this).apply {
            text = "Great! 👍"
            textSize = 15f
            setBackgroundColor(Color.parseColor("#0B2140"))
            setTextColor(Color.WHITE)
            cornerRadius = 40
            setPadding(40, 0, 40, 0)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                120
            ).apply { gravity = android.view.Gravity.CENTER }
            setOnClickListener { successDialog.dismiss() }
        }

        layout.addView(emoji)
        layout.addView(title)
        layout.addView(message)
        layout.addView(btnOk)

        successDialog.setView(layout)
        successDialog.show()
    }

    private fun loadAppIcon(
        view: View,
        imageViewId: Int,
        packageName: String
    ) {

        val iv = view.findViewById<ImageView>(imageViewId) ?: return

        try {

            val icon = packageManager.getApplicationIcon(packageName)
            iv.setImageDrawable(icon)

        } catch (e: Exception) {

            when (packageName) {

                PKG_BOOKMYFARM -> iv.setImageResource(R.drawable.bookmyfarm_logo)

                PKG_SPYGAME -> iv.setImageResource(R.drawable.spygame_logo)

                PKG_AGECALC -> iv.setImageResource(R.drawable.agecalc_logo)

                PKG_PUZZLE -> iv.setImageResource(R.drawable.puzzle_logo)

                else -> iv.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }
    private fun openApp(packageName: String) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                openUrl("https://play.google.com/store/apps/details?id=$packageName")
            }
        } catch (e: Exception) {
            openUrl("https://play.google.com/store/apps/details?id=$packageName")
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            showToast("Unable to open")
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

        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.content_frame)

        when (itemId) {

            R.id.home -> {

                updateBottomNavigationIcons(R.id.home)

                if (currentFragment !is HomeFragment) {
                    loadFragment(HomeFragment())
                }

                return true
            }


            R.id.property -> {

                CommonFunction().navigation(
                    this,
                    AddPropertyActivity::class.java
                )

                return true
            }


            R.id.settings -> {

                updateBottomNavigationIcons(R.id.settings)

                if (currentFragment !is DuesFragment) {
                    loadFragment(DuesFragment())
                }

                return true
            }


            R.id.profile -> {

                updateBottomNavigationIcons(R.id.profile)

                if (currentFragment !is ProfileFragment) {
                    loadFragment(ProfileFragment())
                }

                return true
            }


            else -> return false
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
            if (selectedItemId == R.id.home) R.drawable.home_nav_selected else R.drawable.home_nav_unselected)
        menu.findItem(R.id.settings).setIcon(
            if (selectedItemId == R.id.settings) R.drawable.due_nav_selected else R.drawable.due_nav_unselected)
        menu.findItem(R.id.profile).setIcon(R.drawable.ic_profile_nav)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }

    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) closeDrawer() else openDrawer()
    }

    private fun openDrawer()  = drawerLayout.openDrawer(GravityCompat.START)
    private fun closeDrawer() = drawerLayout.closeDrawer(GravityCompat.START)
    private fun showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) closeDrawer()
        else super.onBackPressed()
    }
}