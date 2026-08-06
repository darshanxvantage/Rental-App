package com.xvantage.rental.network.utils


import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

object NetworkHelper {

    fun <T> handleApiResponse(response: Response<T>): ResultWrapper<T> {
        return if (response.isSuccessful) {
            response.body()?.let {
                ResultWrapper.Success(it)
            } ?: ResultWrapper.Error("Empty response body", response.code())
        } else {
            val errorBody = response.errorBody()?.string()

            ResultWrapper.Error(
                message = extractFriendlyMessage(errorBody, response.code()),
                statusCode = response.code(),
                errorBody = errorBody
            )
        }
    }

    private fun extractFriendlyMessage(errorBody: String?, statusCode: Int): String {

        if (!errorBody.isNullOrBlank()) {

            try {

                val json = JSONObject(errorBody)

                val message =
                    json.optString("message").takeIf { it.isNotBlank() }
                        ?: json.optString("err").takeIf { it.isNotBlank() }

                if (message != null) {
                    return message
                }

            } catch (e: JSONException) {
                // Not JSON (likely an HTML error page) — fall through to
                // the generic status-based message below.
            }
        }

        return when (statusCode) {
            400 -> "Invalid request. Please check the details and try again."
            401 -> "Session expired. Please log in again."
            403 -> "You don't have permission to do that."
            404 -> "Requested item was not found."
            408 -> "Request timed out. Please check your connection and try again."
            413 -> "The file you're uploading is too large."
            in 500..599 -> "Something went wrong on our end. Please try again."
            else -> "Something went wrong. Please try again."
        }
    }
}


sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T) : ResultWrapper<T>()
    data class Error(
        val message: String,
        val statusCode: Int? = null,
        val errorBody: String? = null
    ) : ResultWrapper<Nothing>()
    object Loading : ResultWrapper<Nothing>()
}