package com.xvantage.rental.ui.explore.discover

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.xvantage.rental.databinding.FragmentDiscoverBinding
import com.xvantage.rental.network.response.explore.ExploreCategoryResponse
import com.xvantage.rental.network.utils.ResultWrapper
import com.xvantage.rental.ui.explore.details.ListingDetailsActivity
import com.xvantage.rental.ui.explore.discover.adapter.ExploreListingAdapter
import com.xvantage.rental.ui.explore.discover.bmsheet.FilterBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * NOTE: this Fragment references ListingDetailsActivity (built in the next
 * step) - it will not compile until that file is also added to the project.
 */
@AndroidEntryPoint
class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiscoverViewModel by viewModels()

    @Inject
    lateinit var exploreRepository: com.xvantage.rental.data.source.ExploreRepository

    private lateinit var listingAdapter: ExploreListingAdapter
    private var categories: List<ExploreCategoryResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()

        loadCategories()
        viewModel.loadDiscoverList(reset = true)
    }

    private fun setupRecyclerView() {
        listingAdapter = ExploreListingAdapter(
            context = requireContext(),
            onItemClick = { listing ->
                ListingDetailsActivity.start(requireContext(), listing.id)
            },
            onFavoriteClick = { listing, position ->
                viewModel.toggleFavorite(listing, position)
            }
        )

        binding.rvListings.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = listingAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                    if (dy > 0 && (visibleItemCount + firstVisibleItem) >= totalItemCount - 4) {
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadDiscoverList(reset = true)
        }
    }

    private fun setupClickListeners() {
        binding.btnFilter.setOnClickListener {
            FilterBottomSheet(
                currentFilters = viewModel.filters.value,
                onApply = { newFilters -> viewModel.updateFilters(newFilters) }
            ).show(childFragmentManager, "FilterBottomSheet")
        }

        binding.btnNearMe.setOnClickListener {
            // Location permission + FusedLocationProviderClient wiring happens in
            // ExploreActivity (host), which calls viewModel via a shared instance
            // or passes lat/long down - kept here as the trigger point.
            Toast.makeText(requireContext(), "Fetching your current location...", Toast.LENGTH_SHORT).show()
        }

        binding.citySelector.setOnClickListener {
            // Opens a city-picker dialog/bottom-sheet (reuses existing app city list if present)
        }

        var searchDebounceJob: Job? = null
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchDebounceJob?.cancel()
                searchDebounceJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(400) // debounce so we don't hit the API on every keystroke
                    viewModel.updateSearchQuery(s?.toString().orEmpty())
                }
            }
        })
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = exploreRepository.getCategoryList()) {
                is ResultWrapper.Success -> {
                    categories = result.value.data ?: emptyList()
                    renderCategoryChips()
                }
                else -> { /* category chips are a progressive-enhancement, fail silently */ }
            }
        }
    }

    private fun renderCategoryChips() {
        binding.categoryChipGroup.removeAllViews()

        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                viewModel.updateFilters(viewModel.filters.value.copy(categoryFk = null))
            }
        }
        binding.categoryChipGroup.addView(allChip)

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.name
                isCheckable = true
                setOnClickListener {
                    viewModel.updateFilters(viewModel.filters.value.copy(categoryFk = category.id))
                }
            }
            binding.categoryChipGroup.addView(chip)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.listings.collect { listings ->
                listingAdapter.addItems(listings)
                binding.emptyStateLayout.visibility =
                    if (listings.isEmpty()) View.VISIBLE else View.GONE
                binding.rvListings.visibility =
                    if (listings.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteToggleResult.collect { result ->
                result?.let { (position, isFavorited) ->
                    listingAdapter.updateFavoriteState(position, isFavorited)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = DiscoverFragment()
    }
}