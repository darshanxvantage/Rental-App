package com.xvantage.rental.ui.explore.createListing.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.xvantage.rental.databinding.FragmentSelectCategoryBinding
import com.xvantage.rental.network.response.explore.ExploreCategoryResponse
import com.xvantage.rental.ui.explore.createListing.CreateListingViewModel
import com.xvantage.rental.ui.explore.createListing.WizardStepFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectCategoryFragment : Fragment(), WizardStepFragment {

    private var _binding: FragmentSelectCategoryBinding? = null
    private val binding get() = _binding!!

    // shared with CreateListingActivity and every other step
    private val viewModel: CreateListingViewModel by activityViewModels()

    private var categories: List<ExploreCategoryResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { list ->
                categories = list
                binding.progressCategories.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                renderChips()
            }
        }

        if (viewModel.categories.value.isEmpty()) {
            viewModel.loadCategories()
        } else {
            categories = viewModel.categories.value
            renderChips()
        }
    }

    private fun renderChips() {
        binding.categoryChipGroupWizard.removeAllViews()
        val selectedCategoryFk = viewModel.formState.value.categoryFk

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.name
                isCheckable = true
                isChecked = category.id == selectedCategoryFk
                setOnClickListener {
                    viewModel.updateForm { copy(categoryFk = category.id) }
                    viewModel.loadCategoryFields(category.id)
                }
            }
            binding.categoryChipGroupWizard.addView(chip)
        }
    }

    override fun validateAndSave(): Boolean {
        val selected = viewModel.formState.value.categoryFk
        if (selected.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Please select a category to continue", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SelectCategoryFragment()
    }
}