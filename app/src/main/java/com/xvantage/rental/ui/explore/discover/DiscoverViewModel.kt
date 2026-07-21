package com.xvantage.rental.ui.explore.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.ExploreRepository
import com.xvantage.rental.network.response.explore.ExploreListingResponse
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Everything the Discover screen's filter row/bottom-sheet can set. */
data class DiscoverFilters(
    val city: String? = null,
    val categoryFk: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val genderPreference: String? = null,
    val foodIncluded: Boolean? = null,
    val sharingType: String? = null,
    val occupancyFor: String? = null,
    val sortBy: String? = null
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    val listings = MutableStateFlow<List<ExploreListingResponse>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val isLoadingMore = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    val filters = MutableStateFlow(DiscoverFilters())

    /** null = favorite request not yet attempted this session; Pair(position, isFavorited) on success */
    val favoriteToggleResult = MutableStateFlow<Pair<Int, Boolean>?>(null)

    private var currentPage = 1
    private var hasNextPage = true
    private val pageSize = 10

    fun updateFilters(newFilters: DiscoverFilters) {
        filters.value = newFilters
        loadDiscoverList(reset = true)
    }

    fun loadDiscoverList(reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            hasNextPage = true
        }
        if (!hasNextPage && !reset) return

        viewModelScope.launch {
            if (reset) isLoading.value = true else isLoadingMore.value = true
            errorMessage.value = null

            val f = filters.value
            when (val result = repository.discoverList(
                currentPage = currentPage,
                pageSize = pageSize,
                city = f.city,
                categoryFk = f.categoryFk,
                minPrice = f.minPrice,
                maxPrice = f.maxPrice,
                genderPreference = f.genderPreference,
                foodIncluded = f.foodIncluded,
                sharingType = f.sharingType,
                occupancyFor = f.occupancyFor,
                sortBy = f.sortBy
            )) {
                is ResultWrapper.Success -> {
                    val paginated = result.value.data
                    val newRows = paginated?.rows ?: emptyList()
                    listings.value = if (reset) newRows else listings.value + newRows
                    hasNextPage = paginated?.hasNext == true
                    if (hasNextPage) currentPage += 1
                }
                is ResultWrapper.Error -> {
                    errorMessage.value = result.message
                }
                else -> {}
            }

            isLoading.value = false
            isLoadingMore.value = false
        }
    }

    fun loadMore() {
        if (isLoading.value || isLoadingMore.value || !hasNextPage) return
        loadDiscoverList(reset = false)
    }

    fun toggleFavorite(listing: ExploreListingResponse, position: Int) {
        viewModelScope.launch {
            when (val result = repository.toggleFavorite(listing.id)) {
                is ResultWrapper.Success -> {
                    val nowFavorited = result.value.data?.isFavorited ?: !listing.isFavorited
                    favoriteToggleResult.value = position to nowFavorited
                }
                is ResultWrapper.Error -> {
                    errorMessage.value = result.message
                }
                else -> {}
            }
        }
    }
}