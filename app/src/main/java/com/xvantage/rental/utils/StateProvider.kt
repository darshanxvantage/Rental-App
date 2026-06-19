package com.xvantage.rental.utils

import android.content.Context
import com.google.gson.Gson
import java.io.IOException

/**
 * Project: Rental App By XV Team
 *
 * Reads the bundled `indian_states.json` (in app/src/main/assets) and returns
 * the list of Indian states/UTs used to populate the State dropdown on the
 * Create Profile screen.
 */

data class StateListResponse(
    val states: List<String>
)

object StateProvider {

    private const val FILE_NAME = "indian_states.json"

    private var cachedStates: List<String>? = null

    fun getStates(context: Context): List<String> {

        cachedStates?.let {

            return it
        }

        return try {

            val json = context.assets
                .open(FILE_NAME)
                .bufferedReader()
                .use { it.readText() }

            val response = Gson().fromJson(
                json,
                StateListResponse::class.java
            )

            cachedStates = response.states

            android.util.Log.d(
                "STATE",
                "Loaded = ${response.states.size}"
            )

            response.states

        } catch (e: Exception) {

            e.printStackTrace()

            android.util.Log.e(
                "STATE",
                e.message.toString()
            )

            emptyList()
        }
    }
}