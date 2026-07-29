package com.xvantage.rental.ui.explore.createListing.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.xvantage.rental.databinding.FragmentAmenitiesBinding
import com.xvantage.rental.network.response.explore.ExploreCategoryFieldResponse
import com.xvantage.rental.ui.explore.createListing.CreateListingViewModel
import com.xvantage.rental.ui.explore.createListing.WizardStepFragment
import com.xvantage.rental.ui.explore.createListing.adapter.AmenityFieldAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AmenitiesFragment : Fragment(), WizardStepFragment {

    private var _binding: FragmentAmenitiesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateListingViewModel by activityViewModels()
    private var dynamicFieldAdapter: AmenityFieldAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAmenitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefillFromState()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.formState.collect { form ->
                setupDynamicFields(form.categoryFields, form.dynamicFieldValues)
            }
        }
    }

    private fun prefillFromState() {
        val form = viewModel.formState.value

        binding.switchWifi.isChecked = form.hasWifi
        binding.switchAc.isChecked = form.isAc
        binding.switchAttachedWashroom.isChecked = form.isAttachedWashroom
        binding.switchLaundry.isChecked = form.hasLaundry
        binding.switchHousekeeping.isChecked = form.hasHousekeeping
        binding.switchCctv.isChecked = form.hasCctv
        binding.switchBiometric.isChecked = form.hasBiometricEntry
        binding.switchPowerBackup.isChecked = form.hasPowerBackup
        binding.switchRoWater.isChecked = form.hasRoWater
        binding.switchParking.isChecked = form.hasParking
        binding.switchGym.isChecked = form.hasGym
        binding.switchLift.isChecked = form.hasLift
        binding.switchStudyTable.isChecked = form.hasStudyTable
        binding.switchWardrobe.isChecked = form.hasWardrobe

        binding.switchFoodIncluded.isChecked = form.foodIncluded
        when (form.foodType) {
            "veg" -> binding.chipFoodVeg.isChecked = true
            "non_veg" -> binding.chipFoodNonVeg.isChecked = true
            "both" -> binding.chipFoodBoth.isChecked = true
        }
        when (form.mealCount) {
            "breakfast_only" -> binding.chipMealBreakfast.isChecked = true
            "two_meals" -> binding.chipMealTwo.isChecked = true
            "three_meals" -> binding.chipMealThree.isChecked = true
        }

        binding.etCurfewTime.setText(form.curfewTime)
        binding.etGuestPolicy.setText(form.guestPolicy)
        binding.etHouseRules.setText(form.houseRules)

        form.documentsRequired.forEach { doc ->
            when (doc) {
                "Aadhaar Card" -> binding.chipDocAadhaar.isChecked = true
                "PAN Card" -> binding.chipDocPan.isChecked = true
                "College ID" -> binding.chipDocCollegeId.isChecked = true
                "Company ID" -> binding.chipDocCompanyId.isChecked = true
                "Passport Photo" -> binding.chipDocPhoto.isChecked = true
            }
        }
    }

    private fun setupDynamicFields(fields: List<ExploreCategoryFieldResponse>, existingValues: Map<String, String>) {
        binding.tvDynamicFieldsHeader.visibility = if (fields.isEmpty()) View.GONE else View.VISIBLE
        dynamicFieldAdapter = AmenityFieldAdapter(fields, existingValues)
        binding.rvDynamicFields.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dynamicFieldAdapter
        }
    }

    override fun validateAndSave(): Boolean {
        val foodType = when (binding.foodTypeChipGroup.checkedChipId) {
            binding.chipFoodVeg.id -> "veg"
            binding.chipFoodNonVeg.id -> "non_veg"
            binding.chipFoodBoth.id -> "both"
            else -> "none"
        }
        val mealCount = when (binding.mealCountChipGroup.checkedChipId) {
            binding.chipMealBreakfast.id -> "breakfast_only"
            binding.chipMealTwo.id -> "two_meals"
            binding.chipMealThree.id -> "three_meals"
            else -> "none"
        }

        val checkedDocumentIds = binding.documentsChipGroup.checkedChipIds
        val documents = checkedDocumentIds.mapNotNull { chipId ->
            binding.documentsChipGroup.findViewById<Chip>(chipId)?.text?.toString()
        }

        val dynamicFieldValues = dynamicFieldAdapter?.getFieldValues() ?: viewModel.formState.value.dynamicFieldValues

        viewModel.updateForm {
            copy(
                hasWifi = binding.switchWifi.isChecked,
                isAc = binding.switchAc.isChecked,
                isAttachedWashroom = binding.switchAttachedWashroom.isChecked,
                hasLaundry = binding.switchLaundry.isChecked,
                hasHousekeeping = binding.switchHousekeeping.isChecked,
                hasCctv = binding.switchCctv.isChecked,
                hasBiometricEntry = binding.switchBiometric.isChecked,
                hasPowerBackup = binding.switchPowerBackup.isChecked,
                hasRoWater = binding.switchRoWater.isChecked,
                hasParking = binding.switchParking.isChecked,
                hasGym = binding.switchGym.isChecked,
                hasLift = binding.switchLift.isChecked,
                hasStudyTable = binding.switchStudyTable.isChecked,
                hasWardrobe = binding.switchWardrobe.isChecked,
                foodIncluded = binding.switchFoodIncluded.isChecked,
                foodType = foodType,
                mealCount = mealCount,
                curfewTime = binding.etCurfewTime.text?.toString()?.trim().orEmpty(),
                guestPolicy = binding.etGuestPolicy.text?.toString()?.trim().orEmpty(),
                houseRules = binding.etHouseRules.text?.toString()?.trim().orEmpty(),
                documentsRequired = documents,
                dynamicFieldValues = dynamicFieldValues
            )
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AmenitiesFragment()
    }
}