package com.xvantage.rental.ui.explore.createListing.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.xvantage.rental.R
import com.xvantage.rental.databinding.FragmentSelectCategoryBinding
import com.xvantage.rental.network.response.explore.ExploreCategoryResponse
import com.xvantage.rental.ui.explore.common.ExploreConstants
import com.xvantage.rental.ui.explore.createListing.CreateListingViewModel
import com.xvantage.rental.ui.explore.createListing.WizardStepFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectCategoryFragment : Fragment(), WizardStepFragment {

    private var _binding: FragmentSelectCategoryBinding? = null
    private val binding get() = _binding!!

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
                renderCards()
            }
        }

        if (viewModel.categories.value.isEmpty()) {
            viewModel.loadCategories()
        } else {
            categories = viewModel.categories.value
            renderCards()
        }
    }

    private fun renderCards() {
        val container = binding.categoryCardContainer
        container.removeAllViews()
        val selectedCategoryFk = viewModel.formState.value.categoryFk
        val density = requireContext().resources.displayMetrics.density

        categories.forEach { category ->
            val isSelected = category.id == selectedCategoryFk

            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (14 * density).toInt() }
                radius = 16 * density
                cardElevation = if (isSelected) 6 * density else 2 * density
                strokeWidth = (2 * density).toInt()
                strokeColor = if (isSelected) ContextCompat.getColor(requireContext(), R.color.profile_success)
                else 0x1A000000
                setCardBackgroundColor(
                    if (isSelected) 0xFFF0FFF5.toInt() else 0xFFFFFFFF.toInt()
                )
            }

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
            }

            val iconCircle = LinearLayout(requireContext()).apply {
                val size = (52 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size)
                gravity = Gravity.CENTER
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_tintable)
            }
            val iconView = ImageView(requireContext()).apply {
                val iconSize = (26 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
                setImageResource(ExploreConstants.categoryFallbackIcon(category.slug))
            }
            iconCircle.addView(iconView)

            val textColumn = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).apply { marginStart = (14 * density).toInt() }
            }
            val nameView = TextView(requireContext()).apply {
                text = category.name
                textSize = 16f
                setTextColor(0xFF111111.toInt())
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            val descView = TextView(requireContext()).apply {
                text = category.description ?: "Tap to select"
                textSize = 12f
                setTextColor(0xFF9E9E9E.toInt())
                setPadding(0, (2 * density).toInt(), 0, 0)
            }
            textColumn.addView(nameView)
            textColumn.addView(descView)

            row.addView(iconCircle)
            row.addView(textColumn)

            if (isSelected) {
                val checkIcon = ImageView(requireContext()).apply {
                    val size = (24 * density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size)
                    setImageResource(R.drawable.ic_verified_badge)
                }
                row.addView(checkIcon)
            }

            card.addView(row)
            card.setOnClickListener {
                viewModel.updateForm { copy(categoryFk = category.id) }
                viewModel.loadCategoryFields(category.id)
                renderCards() // refresh selected-state styling immediately
            }
            container.addView(card)
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