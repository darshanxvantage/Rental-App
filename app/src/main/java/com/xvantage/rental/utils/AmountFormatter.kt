package com.xvantage.rental.utils

import java.util.Locale

/**
 * Single place that decides how rupee amounts are shown across the app.
 *
 * Whole-rupee amounts show without decimals (₹5000); amounts that carry
 * paisa show the exact figure instead of silently rounding it away
 * (₹5000.74). Every due/payment/rent screen in the app should format its
 * amounts through this object so the numbers are consistent everywhere -
 * Due Payments list, Collect Rent bottom sheet, invoices, dashboard
 * summary, profile stats, etc.
 */
object AmountFormatter {

    /** e.g. 5000.0 -> "₹5000", 5000.74 -> "₹5000.74" */
    fun format(amount: Double): String {
        val rounded = round2(amount)
        return "₹${plain(rounded)}"
    }

    /**
     * Same idea as format(), but abbreviates large numbers with L (lakh) -
     * meant for compact dashboard/stat cards. Paisa is still preserved for
     * amounts under ₹1,000 where it's most likely to matter.
     */
    fun formatCompact(amount: Double): String {
        val rounded = round2(amount)
        return when {
            rounded >= 100_000 -> "₹${String.format(Locale.US, "%.1f", rounded / 100_000)}L"
            rounded >= 1_000 && rounded == Math.floor(rounded) ->
                "₹${String.format(Locale.US, "%,.0f", rounded)}"
            rounded >= 1_000 ->
                "₹${String.format(Locale.US, "%,.2f", rounded)}"
            else -> "₹${plain(rounded)}"
        }
    }

    /** Amount without the ₹ symbol, for prefilling editable fields, e.g. "5000.74" */
    fun formatPlain(amount: Double): String = plain(round2(amount))

    private fun round2(amount: Double): Double = Math.round(amount * 100.0) / 100.0

    private fun plain(rounded: Double): String {
        return if (rounded == Math.floor(rounded)) {
            rounded.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", rounded)
        }
    }
}