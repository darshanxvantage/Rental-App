package com.xvantage.rental.data.remote


import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.constants.ApiConstant
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val prefs: AppPreference
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val original = chain.request()

        val builder = original.newBuilder()
            .header(ApiConstant.HEADER_CONTENT_TYPE, ApiConstant.CONTENT_TYPE_JSON)
            .header("roletype", "landlord")
            .header(
                "requesttoken",
                "610904831af1a01c5251e5437c53421338a01032a0c01bcc7db9da73368e339b"
            )

        prefs.getToken()
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                builder.header(
                    "Authorization",
                    "Bearer $it"
                )
            }

        return chain.proceed(builder.build())
    }
}
