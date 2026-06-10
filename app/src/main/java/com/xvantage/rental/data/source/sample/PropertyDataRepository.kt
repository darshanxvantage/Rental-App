package com.xvantage.rental.data.source.sample

import  android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.util.UUID

object PropertyDataRepository {
    private var properties: List<Property>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun getProperties(): List<Property> {
        if (properties == null) {
            properties = generateDummyProperties()
        }
        return properties!!
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllTenants(): List<Tenant> {
        val properties = getProperties()

        return properties.flatMap { property ->
            property.rooms.mapNotNull { room -> room.tenant }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateDummyProperties(): List<Property> {
        val properties = mutableListOf<Property>()
        var tenantId = 1

        for (propertyIndex in 1..7) {
            val rooms = mutableListOf<Room>()

            for (roomIndex in 1..(5..10).random()) {
                // Randomly determine if the room is occupied
                val isOccupied = (0..1).random() == 1
                val tenant: Tenant? = if (isOccupied) {
                    Tenant(
                        id = UUID.randomUUID().toString(),
                        tenantName = "Tenant $tenantId",
                        tenantEmail = "tenant$tenantId@example.com",
                        moveInDate = LocalDateTime.now().minusMonths((1..12).random().toLong()).toString(),
                        moveOutDate = if ((0..1).random() == 1) LocalDateTime.now().plusMonths((1..12).random().toLong()).toString() else null,
                        contactInfo = "123-456-789$tenantId"
                    ).also { tenantId++ }
                } else {
                    null
                }

                rooms.add(
                    Room(
                        id = UUID.randomUUID().toString(),
                        roomName = "Room $roomIndex",
                        size = "${(15..30).random()}m²",
                        tenant = tenant,
                        createdAt = LocalDateTime.now().minusDays((1..365).random().toLong()).toString(),
                        updatedAt = LocalDateTime.now().toString()
                    )
                )
            }

            properties.add(
                Property(
                    id = UUID.randomUUID().toString(),
                    ownerName = "Owner $propertyIndex",
                    propertyName = "Property $propertyIndex",
                    address = "Address $propertyIndex, City ${(1..100).random()}",
                    rooms = rooms,
                    contactInfo = "owner$propertyIndex@example.com",
                    createdAt = LocalDateTime.now().minusYears((1..3).random().toLong()).toString(),
                    updatedAt = LocalDateTime.now().toString()
                )
            )
        }

        return properties
    }

}