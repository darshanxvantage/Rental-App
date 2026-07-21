package com.xvantage.rental.ui.explore.discover.bmsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xvantage.rental.databinding.BottomsheetFilterBinding
import com.xvantage.rental.ui.explore.discover.DiscoverFilters


class FilterBottomSheet(
    private val currentFilters: DiscoverFilters,
    private val onApply: (DiscoverFilters) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefillCurrentFilters()
        setupActionButtons()
    }

    private fun prefillCurrentFilters() {
        binding.etMinPrice.setText(currentFilters.minPrice?.toInt()?.toString() ?: "")
        binding.etMaxPrice.setText(currentFilters.maxPrice?.toInt()?.toString() ?: "")

        when (currentFilters.genderPreference) {
            "male" -> binding.chipGenderMale.isChecked = true
            "female" -> binding.chipGenderFemale.isChecked = true
            "unisex" -> binding.chipGenderUnisex.isChecked = true
            else -> binding.chipGenderAny.isChecked = true
        }

        when (currentFilters.sharingType) {
            "single" -> binding.chipSharingSingle.isChecked = true
            "double" -> binding.chipSharingDouble.isChecked = true
            "triple" -> binding.chipSharingTriple.isChecked = true
            else -> binding.chipSharingAny.isChecked = true
        }

        binding.switchFoodIncluded.isChecked = currentFilters.foodIncluded == true

        when (currentFilters.sortBy) {
            "price_low_high" -> binding.chipSortPriceLow.isChecked = true
            "price_high_low" -> binding.chipSortPriceHigh.isChecked = true
            "popular" -> binding.chipSortPopular.isChecked = true
            else -> binding.chipSortNewest.isChecked = true
        }
    }

    private fun setupActionButtons() {
        binding.btnReset.setOnClickListener {
            onApply(DiscoverFilters())
            dismiss()
        }

        binding.btnApply.setOnClickListener {
            val minPrice = binding.etMinPrice.text?.toString()?.toDoubleOrNull()
            val maxPrice = binding.etMaxPrice.text?.toString()?.toDoubleOrNull()

            val genderPreference = when (binding.genderChipGroup.checkedChipId) {
                binding.chipGenderMale.id -> "male"
                binding.chipGenderFemale.id -> "female"
                binding.chipGenderUnisex.id -> "unisex"
                else -> null
            }

            val sharingType = when (binding.sharingChipGroup.checkedChipId) {
                binding.chipSharingSingle.id -> "single"
                binding.chipSharingDouble.id -> "double"
                binding.chipSharingTriple.id -> "triple"
                else -> null
            }

            val sortBy = when (binding.sortChipGroup.checkedChipId) {
                binding.chipSortPriceLow.id -> "price_low_high"
                binding.chipSortPriceHigh.id -> "price_high_low"
                binding.chipSortPopular.id -> "popular"
                else -> "newest"
            }

            val foodIncluded = if (binding.switchFoodIncluded.isChecked) true else null

            onApply(
                currentFilters.copy(
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    genderPreference = genderPreference,
                    sharingType = sharingType,
                    foodIncluded = foodIncluded,
                    sortBy = sortBy
                )
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}