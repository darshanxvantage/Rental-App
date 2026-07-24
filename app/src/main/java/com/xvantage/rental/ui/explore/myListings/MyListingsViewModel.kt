package com.xvantage.rental.ui.explore.myListings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.ExploreRepository
import com.xvantage.rental.network.response.explore.ExploreListingResponse
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyListingsViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    val listings = MutableStateFlow<List<ExploreListingResponse>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    val deleteSuccess = MutableStateFlow(false)

    private var currentPage = 1
    private var hasNextPage = true
    private val pageSize = 10
    private var currentStatusFilter: String? = null

    fun loadMyListings(statusFilter: String? = currentStatusFilter, reset: Boolean = true) {
        currentStatusFilter = statusFilter
        if (reset) {
            currentPage = 1
            hasNextPage = true
        }
        if (!hasNextPage && !reset) return

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            when (val result = repository.myListings(currentPage, pageSize, statusFilter)) {
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
        }
    }

    fun loadMore() {
        if (isLoading.value || !hasNextPage) return
        loadMyListings(currentStatusFilter, reset = false)
    }

    fun deleteListing(listingId: String) {
        viewModelScope.launch {
            when (repository.deleteListing(listingId)) {
                is ResultWrapper.Success -> {
                    deleteSuccess.value = true
                    loadMyListings(currentStatusFilter, reset = true)
                }
                is ResultWrapper.Error -> {
                    errorMessage.value = "Could not delete this listing. Please try again."
                }
                else -> {}
            }
        }
    }
}