package com.xvantage.rental.ui.explore.createListing.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.xvantage.rental.databinding.FragmentReviewSubmitBinding
import com.xvantage.rental.ui.explore.common.ExploreConstants
import com.xvantage.rental.ui.explore.createListing.CreateListingFormState
import com.xvantage.rental.ui.explore.createListing.CreateListingViewModel
import com.xvantage.rental.ui.explore.createListing.WizardStepFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class ReviewSubmitFragment : Fragment(), WizardStepFragment {

    private var _binding: FragmentReviewSubmitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateListingViewModel by activityViewModels()
    private val inrFormat = NumberFormat.getInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewSubmitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderReview(viewModel.formState.value)
    }

    private fun renderReview(form: CreateListingFormState) {
        binding.tvReviewTitle.text = form.title.ifBlank { "(no title)" }

        val categoryName = viewModel.categories.value.firstOrNull { it.id == form.categoryFk }?.name ?: "Category"
        binding.tvReviewCategory.text = categoryName

        binding.tvReviewAddress.text = "${form.address}, ${form.locality}, ${form.city}" +
                (form.landmark.takeIf { it.isNotBlank() }?.let { " (near $it)" } ?: "")

        binding.tvReviewContact.text = "📞 ${form.contactNumber}" +
                (form.whatsappNumber.takeIf { it.isNotBlank() }?.let { " • WhatsApp: $it" } ?: "")

        val genderLabel = ExploreConstants.genderLabels[form.genderPreference] ?: form.genderPreference
        val occupancyLabel = ExploreConstants.occupancyLabels[form.occupancyFor] ?: form.occupancyFor
        binding.tvReviewGenderOccupancy.text = "$genderLabel • Ideal for: $occupancyLabel • ${form.totalBeds} total beds, ${form.availableBeds} available"

        binding.tvReviewSharingPrices.text = if (form.sharingPrices.isEmpty()) {
            "No pricing added"
        } else {
            form.sharingPrices.joinToString("\n") { row ->
                val label = ExploreConstants.sharingTypeLabel(row.sharingType)
                "$label — ₹${inrFormat.format(row.price)}/mo (${row.availableBeds} beds available)"
            }
        }

        val activeAmenities = mutableListOf<String>()
        if (form.hasWifi) activeAmenities.add("Wi-Fi")
        if (form.isAc) activeAmenities.add("AC")
        if (form.isAttachedWashroom) activeAmenities.add("Attached Washroom")
        if (form.hasLaundry) activeAmenities.add("Laundry")
        if (form.hasHousekeeping) activeAmenities.add("Housekeeping")
        if (form.hasCctv) activeAmenities.add("CCTV")
        if (form.hasBiometricEntry) activeAmenities.add("Biometric Entry")
        if (form.hasPowerBackup) activeAmenities.add("Power Backup")
        if (form.hasRoWater) activeAmenities.add("RO Water")
        if (form.hasParking) activeAmenities.add("Parking")
        if (form.hasGym) activeAmenities.add("Gym")
        if (form.hasLift) activeAmenities.add("Lift")
        if (form.hasStudyTable) activeAmenities.add("Study Table")
        if (form.hasWardrobe) activeAmenities.add("Wardrobe")
        if (form.foodIncluded) {
            val foodType = ExploreConstants.foodTypeLabels[form.foodType] ?: form.foodType
            activeAmenities.add("Food ($foodType)")
        }

        binding.tvReviewAmenities.text = if (activeAmenities.isEmpty()) {
            "No amenities selected"
        } else {
            activeAmenities.joinToString(", ")
        }

        val totalPhotos = form.existingImageCount + form.newImageUris.size
        binding.tvReviewPhotoCount.text = "$totalPhotos photo(s) added"
    }

    // nothing to validate on this final screen - CreateListingActivity calls
    // viewModel.submit() directly when Next is tapped on the last step
    override fun validateAndSave(): Boolean = true

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ReviewSubmitFragment()
    }
}