package com.xvantage.rental.ui.explore.createListing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityCreateListingBinding
import com.xvantage.rental.ui.dashboard.DashboardActivity
import com.xvantage.rental.ui.explore.createListing.fragment.AmenitiesFragment
import com.xvantage.rental.ui.explore.createListing.fragment.BasicDetailsFragment
import com.xvantage.rental.ui.explore.createListing.fragment.CelebrationFragment
import com.xvantage.rental.ui.explore.createListing.fragment.HeroWelcomeFragment
import com.xvantage.rental.ui.explore.createListing.fragment.ReviewSubmitFragment
import com.xvantage.rental.ui.explore.createListing.fragment.SelectCategoryFragment
import com.xvantage.rental.ui.explore.createListing.fragment.SharingPriceFragment
import com.xvantage.rental.ui.explore.createListing.fragment.UploadImagesFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Every step fragment implements this so the host can validate before advancing. */
interface WizardStepFragment {
    /** Return true if this step's input is valid (and has been saved into the shared ViewModel). */
    fun validateAndSave(): Boolean
}

/**
 * Premium 3-part wizard: Hero welcome -> 6 form steps -> Celebration.
 * Hero and Celebration hide the normal top/bottom chrome and have their
 * own embedded action button.
 */
@AndroidEntryPoint
class CreateListingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateListingBinding
    private val viewModel: CreateListingViewModel by viewModels()

    private var isOnboarding: Boolean = false
    private var showingCelebration: Boolean = false

    private val stepTitles = listOf(
        "Select Category", "Basic Details", "Amenities", "Pricing", "Photos", "Review & Submit"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isOnboarding = intent.getBooleanExtra(EXTRA_IS_ONBOARDING, false)

        val editListingId = intent.getStringExtra(EXTRA_EDIT_LISTING_ID)
        if (editListingId != null) {
            viewModel.updateForm { copy(editingListingId = editListingId) }
            viewModel.loadListingForEdit(editListingId)
            viewModel.dismissHero() // editing an existing listing - skip the welcome screen
        }
        viewModel.loadCategories()

        setupButtons()
        observeViewModel()

        onBackPressedDispatcher.addCallback(this) {
            handleBackPress()
        }

        if (savedInstanceState == null && viewModel.heroDismissed.value) {
            showStep(0)
        }
    }

    fun isOnboardingFlow(): Boolean = isOnboarding

    /** Called by CelebrationFragment's button - the actual "we're done here" logic. */
    fun finishWizard() {
        setResult(RESULT_OK)
        if (isOnboarding) {
            // first-time Owner onboarding - now unlock the Dashboard
            val dashboardIntent = Intent(this, DashboardActivity::class.java)
            dashboardIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(dashboardIntent)
        }
        finish()
    }

    private fun setupButtons() {
        binding.btnBackWizard.setOnClickListener { handleBackPress() }

        binding.btnWizardPrevious.setOnClickListener {
            viewModel.previousStep()
        }

        binding.btnWizardNext.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.wizardContentFrame)
            val isValid = (currentFragment as? WizardStepFragment)?.validateAndSave() ?: true

            if (!isValid) return@setOnClickListener

            if (viewModel.currentStep.value == CreateListingViewModel.TOTAL_STEPS - 1) {
                viewModel.submit()
            } else {
                viewModel.nextStep()
            }
        }
    }

    private fun handleBackPress() {
        when {
            showingCelebration -> finishWizard() // already submitted - nothing to go back to
            !viewModel.heroDismissed.value -> finish() // on the welcome screen - just exit
            viewModel.currentStep.value == 0 -> finish()
            else -> viewModel.previousStep()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.heroDismissed.collect { dismissed ->
                        if (dismissed && !showingCelebration) {
                            showStep(viewModel.currentStep.value)
                            updateChrome()
                        } else if (!dismissed) {
                            showHero()
                        }
                    }
                }
                launch {
                    viewModel.currentStep.collect { step ->
                        if (viewModel.heroDismissed.value && !showingCelebration) {
                            showStep(step)
                            updateChrome()
                        }
                    }
                }
                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.progressBarWizardLoading.visibility =
                            if (loading) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.errorMessage.collect { message ->
                        message?.let { Toast.makeText(this@CreateListingActivity, it, Toast.LENGTH_SHORT).show() }
                    }
                }
                launch {
                    viewModel.submitSuccess.collect { success ->
                        if (success) {
                            showingCelebration = true
                            showCelebration()
                            updateChrome()
                        }
                    }
                }
            }
        }
    }

    /** Shows/hides the top step-bar and bottom Back/Next bar based on which screen is active. */
    private fun updateChrome() {
        val showWizardChrome = viewModel.heroDismissed.value && !showingCelebration
        binding.topBarWizard.visibility = if (showWizardChrome) View.VISIBLE else View.GONE
        binding.wizardStepDots.visibility =
            if (showWizardChrome) View.VISIBLE else View.GONE
        binding.bottomButtonBar.visibility = if (showWizardChrome) View.VISIBLE else View.GONE

        if (showWizardChrome) {
            val step = viewModel.currentStep.value
            binding.tvStepTitle.text = stepTitles.getOrElse(step) { "" }
            binding.tvStepCount.text = "${step + 1}/${CreateListingViewModel.TOTAL_STEPS}"
            updateStepDots(step)
            binding.btnWizardPrevious.visibility = if (step == 0) View.INVISIBLE else View.VISIBLE
            binding.btnWizardNext.text =
                if (step == CreateListingViewModel.TOTAL_STEPS - 1) "Submit for Approval" else "Next"
        }
    }

    private fun showHero() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.wizardContentFrame, HeroWelcomeFragment.newInstance())
            .commit()
        updateChrome()
    }

    private fun showCelebration() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.wizardContentFrame, CelebrationFragment.newInstance())
            .commit()
    }

    private fun showStep(step: Int) {
        val fragment: Fragment = when (step) {
            0 -> SelectCategoryFragment.newInstance()
            1 -> BasicDetailsFragment.newInstance()
            2 -> AmenitiesFragment.newInstance()
            3 -> SharingPriceFragment.newInstance()
            4 -> UploadImagesFragment.newInstance()
            else -> ReviewSubmitFragment.newInstance()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.wizardContentFrame, fragment)
            .commit()
    }

    private fun updateStepDots(currentStep: Int) {

        val dots = listOf(
            binding.stepDot0,
            binding.stepDot1,
            binding.stepDot2,
            binding.stepDot3,
            binding.stepDot4,
            binding.stepDot5
        )

        dots.forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index <= currentStep)
                    R.drawable.bg_wizard_step_dot_active
                else
                    R.drawable.bg_wizard_step_dot_inactive
            )
        }
    }

    companion object {
        private const val EXTRA_EDIT_LISTING_ID = "explore_edit_listing_id"
        private const val EXTRA_IS_ONBOARDING = "explore_is_onboarding"

        fun startForCreate(context: Context) {
            context.startActivity(Intent(context, CreateListingActivity::class.java))
        }

        fun startForEdit(context: Context, listingId: String) {
            val intent = Intent(context, CreateListingActivity::class.java)
            intent.putExtra(EXTRA_EDIT_LISTING_ID, listingId)
            context.startActivity(intent)
        }

        /** Called only from RoleSelectionActivity when the user picks "Property Owner". */
        fun startForOnboarding(context: Context) {
            val intent = Intent(context, CreateListingActivity::class.java)
            intent.putExtra(EXTRA_IS_ONBOARDING, true)
            context.startActivity(intent)
        }
    }
}