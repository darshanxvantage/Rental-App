package com.xvantage.rental.ui.explore.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.ExploreRepository
import com.xvantage.rental.network.response.explore.FavoriteEntryResponse
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    val favorites = MutableStateFlow<List<FavoriteEntryResponse>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    /** position that was successfully removed, so the Fragment can update the adapter */
    val removedPosition = MutableStateFlow<Int?>(null)

    private var currentPage = 1
    private var hasNextPage = true
    private val pageSize = 10

    fun loadFavorites(reset: Boolean = true) {
        if (reset) {
            currentPage = 1
            hasNextPage = true
        }
        if (!hasNextPage && !reset) return

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            when (val result = repository.myFavorites(currentPage, pageSize)) {
                is ResultWrapper.Success -> {
                    val paginated = result.value.data
                    val newRows = paginated?.rows ?: emptyList()
                    favorites.value = if (reset) newRows else favorites.value + newRows
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

    fun removeFavorite(favorite: FavoriteEntryResponse, position: Int) {
        viewModelScope.launch {
            when (repository.toggleFavorite(favorite.listingFk)) {
                is ResultWrapper.Success -> {
                    removedPosition.value = position
                }
                is ResultWrapper.Error -> {
                    errorMessage.value = "Could not remove from favorites. Please try again."
                }
                else -> {}
            }
        }
    }
}