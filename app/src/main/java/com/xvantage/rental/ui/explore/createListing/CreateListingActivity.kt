package com.xvantage.rental.ui.explore.createListing

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.xvantage.rental.ui.explore.createListing.fragment.AmenitiesFragment
import com.xvantage.rental.ui.explore.createListing.fragment.BasicDetailsFragment
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

@AndroidEntryPoint
class CreateListingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateListingBinding
    private val viewModel: CreateListingViewModel by viewModels()

    /** True only when opened right after CreateProfile for a brand-new Owner - the
     * listing form is mandatory in that case: no skipping, submit success goes
     * straight to the Dashboard (clearing the onboarding stack) instead of just finish(). */
    private var isMandatoryOnboarding: Boolean = false

    private val stepTitles = listOf(
        "Select Category", "Basic Details", "Amenities", "Pricing", "Photos", "Review & Submit"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isMandatoryOnboarding = intent.getBooleanExtra(EXTRA_MANDATORY_ONBOARDING, false)
        if (isMandatoryOnboarding) {
            // No skipping the very first listing - hide the back/close affordance.
            binding.btnBackWizard.visibility = android.view.View.INVISIBLE
        }

        val editListingId = intent.getStringExtra(EXTRA_EDIT_LISTING_ID)
        if (editListingId != null) {
            viewModel.updateForm { copy(editingListingId = editListingId) }
            viewModel.loadListingForEdit(editListingId)
        }
        viewModel.loadCategories()
//        viewModel.loadProperties()

        setupButtons()
        observeViewModel()

        onBackPressedDispatcher.addCallback(this) {
            handleBackPress()
        }

        if (savedInstanceState == null) {
            showStep(0)
        }
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
        if (viewModel.currentStep.value == 0) {
            if (isMandatoryOnboarding) {
                Toast.makeText(
                    this,
                    "Please complete your property listing to continue",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                finish()
            }
        } else {
            viewModel.previousStep()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentStep.collect { step ->
                        showStep(step)
                        updateStepChrome(step)
                    }
                }
                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.progressBarWizardLoading.visibility =
                            if (loading) android.view.View.VISIBLE else android.view.View.GONE
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
                            Toast.makeText(
                                this@CreateListingActivity,
                                "Submitted! Your listing will be live after admin approval.",
                                Toast.LENGTH_LONG
                            ).show()
                            if (isMandatoryOnboarding) {
                                val intent = Intent(
                                    this@CreateListingActivity,
                                    com.xvantage.rental.ui.dashboard.DashboardActivity::class.java
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                            } else {
                                setResult(RESULT_OK)
                            }
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun updateStepChrome(step: Int) {
        binding.tvStepTitle.text = stepTitles.getOrElse(step) { "" }
        binding.tvStepCount.text = "${step + 1}/${CreateListingViewModel.TOTAL_STEPS}"
        binding.progressWizard.progress = ((step + 1) * 100) / CreateListingViewModel.TOTAL_STEPS
        binding.btnWizardPrevious.visibility =
            if (step == 0) android.view.View.INVISIBLE else android.view.View.VISIBLE
        binding.btnWizardNext.text =
            if (step == CreateListingViewModel.TOTAL_STEPS - 1) "Submit for Approval" else "Next"
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

    companion object {
        private const val EXTRA_EDIT_LISTING_ID = "explore_edit_listing_id"
        private const val EXTRA_MANDATORY_ONBOARDING = "explore_mandatory_onboarding"

        fun startForCreate(context: Context) {
            context.startActivity(Intent(context, CreateListingActivity::class.java))
        }

        /** New Property Owner, right after CreateProfile - listing is mandatory, no skipping. */
        fun startForOnboarding(context: Context) {
            val intent = Intent(context, CreateListingActivity::class.java)
                .putExtra(EXTRA_MANDATORY_ONBOARDING, true)
            context.startActivity(intent)
        }

        fun startForEdit(context: Context, listingId: String) {
            val intent = Intent(context, CreateListingActivity::class.java)
            intent.putExtra(EXTRA_EDIT_LISTING_ID, listingId)
            context.startActivity(intent)
        }
    }
}