package com.xvantage.rental.ui.explore.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.ExploreRepository
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Tiny shared ViewModel used ONLY for the "does this logged-in user already
 * have an Explore listing?" check. Both switch buttons use the exact same
 * check (has-listing based, NOT role based):
 *  - Dashboard ProfileFragment  -> "Switch to Explore" button
 *  - Explore ExploreProfileFragment -> "Switch to Owner" button
 *
 * Backend note: landlordAuth/studentAuth are the same unified-JWT check
 * (see explore-module/middleware) - any logged-in user, student or owner,
 * can call /explore/landlord/listing/my-list; it just returns listings
 * that belong to them (empty list if none).
 */
@HiltViewModel
class ExploreSwitchViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    // null = not checked yet, true/false = confirmed result
    private val _hasListing = MutableStateFlow<Boolean?>(null)
    val hasListing: StateFlow<Boolean?> = _hasListing.asStateFlow()

    fun checkHasListing() {
        viewModelScope.launch {
            when (val result = repository.myListings(currentPage = 1, pageSize = 1, status = null)) {
                is ResultWrapper.Success -> {
                    _hasListing.value = (result.value.data?.rows?.isNotEmpty() == true)
                }
                is ResultWrapper.Error -> {
                    // fail-safe: if we can't confirm, don't show the switch button
                    _hasListing.value = false
                }
                else -> Unit
            }
        }
    }
}