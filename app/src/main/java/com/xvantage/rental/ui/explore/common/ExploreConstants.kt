package com.xvantage.rental.ui.explore.common

import com.xvantage.rental.R

/**
 * Central place for every "magic string/number" the Explore feature needs -
 * mirroRoleselectionactivity rs explore-module/config/explore.config.js on the backend.
 */
object ExploreConstants {

    private const val PUBLIC_BASE_URL = "https://api.rental.xvantageinfotech.com/public/"
    private const val LISTING_IMAGE_FOLDER = "explore-listing"
    private const val CATEGORY_ICON_FOLDER = "explore-category"

    const val MAX_IMAGES_PER_LISTING = 15
    const val DEFAULT_NEARBY_RADIUS_KM = 5.0

    /** Full URL for a listing image filename returned by the API. */
    fun listingImageUrl(filename: String?): String? {
        if (filename.isNullOrBlank()) return null
        return "$PUBLIC_BASE_URL$LISTING_IMAGE_FOLDER/$filename"
    }

    /** Full URL for a category icon filename returned by the API. */
    fun categoryIconUrl(filename: String?): String? {
        if (filename.isNullOrBlank()) return null
        return "$PUBLIC_BASE_URL$CATEGORY_ICON_FOLDER/$filename"
    }

    /** Fallback local icon per category slug, used until/if the category has no uploaded icon. */
    fun categoryFallbackIcon(slug: String?): Int {
        return when (slug?.lowercase()) {
            "pg" -> R.drawable.ic_category_pg
            "apartment" -> R.drawable.ic_category_apartment
            "flatmate" -> R.drawable.ic_category_flatmate
            "commercial" -> R.drawable.ic_category_commercial
            else -> R.drawable.ic_category_pg
        }
    }

    val sharingTypeLabels = mapOf(
        "single" to "Single Sharing",
        "double" to "Double Sharing",
        "triple" to "Triple Sharing",
        "four_sharing" to "4 Sharing",
        "dormitory" to "Dormitory"
    )

    fun sharingTypeLabel(type: String): String = sharingTypeLabels[type] ?: type.replace("_", " ")

    val occupancyLabels = mapOf(
        "student" to "Students",
        "working_professional" to "Working Professionals",
        "any" to "Anyone"
    )

    val genderLabels = mapOf(
        "male" to "Boys",
        "female" to "Girls",
        "unisex" to "Co-living (Boys & Girls)"
    )

    val foodTypeLabels = mapOf(
        "veg" to "Vegetarian",
        "non_veg" to "Non-Vegetarian",
        "both" to "Veg & Non-Veg",
        "none" to "No Food Service"
    )

    val mealCountLabels = mapOf(
        "breakfast_only" to "Breakfast Only",
        "two_meals" to "2 Meals a Day",
        "three_meals" to "3 Meals a Day",
        "none" to "No Meals"
    )

    val reportReasons = listOf(
        "fake_listing" to "This listing looks fake",
        "wrong_info" to "Information is incorrect",
        "already_rented" to "Already rented out",
        "spam" to "Spam / misleading",
        "other" to "Other"
    )

    val statusLabels = mapOf(
        "draft" to "Draft",
        "pending_approval" to "Pending Approval",
        "approved" to "Live",
        "rejected" to "Rejected",
        "inactive" to "Inactive"
    )
}