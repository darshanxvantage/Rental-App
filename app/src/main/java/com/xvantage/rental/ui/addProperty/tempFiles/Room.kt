package com.xvantage.rental.ui.addProperty.tempFiles

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

/**
 * Project: Rental App By XV Team
 * Author: Mujammil x Vipul x XV Team
 * Date:  24/04/25
 * <p>
 * Licensed under the Apache License, Version 2.0. See LICENSE file for terms.
 */

@Parcelize
data class Room(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String = "",
    val number: String = "",
    val type: String = "",
    val propertyTypeName: String = "",
    val rent: Double = 0.0,
    val meterReading: Double = 0.0,
    val readingDate: Long = 0L,
    val isOccupied: Boolean = false,
    val tenantId: String = "",
    val bedCount: Int = 1,
    val occupiedBeds: Int = 0
) : Parcelable