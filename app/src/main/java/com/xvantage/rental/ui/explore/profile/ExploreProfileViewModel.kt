package com.xvantage.rental.ui.explore.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.ExploreRepository
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreProfileViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    // null = not checked yet, true/false = actual result
    val hasListing = MutableStateFlow<Boolean?>(null)

    fun checkHasListing() {
        viewModelScope.launch {
            when (val result = repository.myListings(currentPage = 1, pageSize = 1)) {
                is ResultWrapper.Success -> {
                    val rows = result.value.data?.rows ?: emptyList()
                    hasListing.value = rows.isNotEmpty()
                }
                is ResultWrapper.Error -> {
                    // network issue - don't show the Switch button rather than guess wrong
                    hasListing.value = false
                }
                else -> {}
            }
        }
    }
}