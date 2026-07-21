package com.xvantage.rental.network.response.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches explore_categories table (explore-module/models/exploreCategory.model.js)
 */
data class ExploreCategoryResponse(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("sort_order")
    val sortOrder: Int = 0,

    @SerializedName("status")
    val status: Boolean = true,

    @SerializedName("fields")
    val fields: List<ExploreCategoryFieldResponse>? = null
)

/**
 * Response of GET /explore/category/:id/fields -> { category, fields }
 */
data class ExploreCategoryFieldsWrapperResponse(

    @SerializedName("category")
    val category: ExploreCategoryResponse?,

    @SerializedName("fields")
    val fields: List<ExploreCategoryFieldResponse> = emptyList()
)