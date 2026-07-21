package com.xvantage.rental.network.response.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches src/utils/common.util.js -> getPagination() EXACTLY:
 *   { totalItems, totalPages, currentPage, hasPrevious, hasNext, previous, next, count, rows }
 */
data class ExplorePaginatedResponse<T>(

    @SerializedName("totalItems")
    val totalItems: Int = 0,

    @SerializedName("totalPages")
    val totalPages: Int = 0,

    @SerializedName("currentPage")
    val currentPage: Int = 1,

    @SerializedName("hasPrevious")
    val hasPrevious: Boolean = false,

    @SerializedName("hasNext")
    val hasNext: Boolean = false,

    @SerializedName("previous")
    val previous: Int? = null,

    @SerializedName("next")
    val next: Int? = null,

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("rows")
    val rows: List<T> = emptyList()
)