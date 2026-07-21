package com.xvantage.rental.ui.explore.common

import com.xvantage.rental.R
import com.xvantage.rental.network.response.explore.ExploreListingResponse
import com.xvantage.rental.network.response.explore.PriceRangeResponse
import java.text.NumberFormat
import java.util.Locale

/**
 * Converts raw API data into UI-ready strings/icons.
 * Kept separate from the response models so the models stay pure data classes.
 */
object ExploreUiMapper {

    private val inrFormat: NumberFormat = NumberFormat.getInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    /** e.g. "₹6,000 - ₹9,500/mo" or "₹6,000/mo" or "Price on request" */
    fun formatPriceRange(priceRange: PriceRangeResponse?): String {
        val min = priceRange?.minPrice
        val max = priceRange?.maxPrice
        return when {
            min == null && max == null -> "Price on request"
            min != null && max != null && min == max -> "₹${inrFormat.format(min)}/mo"
            min != null && max != null -> "₹${inrFormat.format(min)} - ₹${inrFormat.format(max)}/mo"
            min != null -> "From ₹${inrFormat.format(min)}/mo"
            else -> "Price on request"
        }
    }

    fun formatDistance(distanceKm: Double?): String? {
        if (distanceKm == null) return null
        return if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()} m away" else "%.1f km away".format(distanceKm)
    }

    /** Every "hasXxx" flag on a listing, paired with its display icon + label - only the true ones. */
    fun activeAmenityIcons(listing: ExploreListingResponse): List<Pair<Int, String>> {
        val amenities = mutableListOf<Pair<Int, String>>()
        if (listing.hasWifi) amenities.add(R.drawable.ic_wifi to "Wi-Fi")
        if (listing.isAc) amenities.add(R.drawable.ic_ac to "AC")
        if (listing.hasCctv) amenities.add(R.drawable.ic_cctv to "CCTV")
        if (listing.hasBiometricEntry) amenities.add(R.drawable.ic_biometric to "Biometric Entry")
        if (listing.hasPowerBackup) amenities.add(R.drawable.ic_power_backup to "Power Backup")
        if (listing.hasRoWater) amenities.add(R.drawable.ic_ro_water to "RO Water")
        if (listing.hasParking) amenities.add(R.drawable.ic_parking to "Parking")
        if (listing.hasGym) amenities.add(R.drawable.ic_gym to "Gym")
        if (listing.hasLift) amenities.add(R.drawable.ic_lift to "Lift")
        if (listing.hasLaundry) amenities.add(R.drawable.ic_laundry to "Laundry")
        if (listing.hasHousekeeping) amenities.add(R.drawable.ic_housekeeping to "Housekeeping")
        if (listing.foodIncluded) amenities.add(R.drawable.ic_food to "Food Included")
        if (listing.isAttachedWashroom) amenities.add(R.drawable.ic_ac to "Attached Washroom") // reuse until a dedicated icon is added
        if (listing.hasStudyTable) amenities.add(R.drawable.ic_lift to "Study Table") // reuse until a dedicated icon is added
        if (listing.hasWardrobe) amenities.add(R.drawable.ic_lift to "Wardrobe") // reuse until a dedicated icon is added
        return amenities
    }

    fun statusColorRes(status: String): Int = when (status) {
        "approved" -> R.color.profile_success
        "pending_approval" -> R.color.profile_warning
        "rejected" -> R.color.red
        else -> R.color.gray_sub // draft | inactive
    }

    fun statusLabel(status: String): String = ExploreConstants.statusLabels[status] ?: status

    fun coverImageUrl(listing: ExploreListingResponse): String? {
        val cover = listing.images?.firstOrNull { it.type == "cover" } ?: listing.images?.firstOrNull()
        return ExploreConstants.listingImageUrl(cover?.image)
    }

    fun genderLabel(genderPreference: String): String = ExploreConstants.genderLabels[genderPreference] ?: genderPreference

    fun foodLabel(listing: ExploreListingResponse): String {
        if (!listing.foodIncluded) return "No Food"
        val type = ExploreConstants.foodTypeLabels[listing.foodType] ?: listing.foodType
        val meals = ExploreConstants.mealCountLabels[listing.mealCount] ?: listing.mealCount
        return "$type • $meals"
    }
}