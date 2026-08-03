package com.xvantage.rental.utils.constants

object Constant {

    // ---------------- API Request Parameters ----------------

    const val PARAM_USER_ID = "userId"
    const val PARAM_ACCESS_TOKEN = "accessToken"
    const val PARAM_PAGE_NUMBER = "pageNumber"
    const val PARAM_LIMIT = "limit"

    // ---------------- Static Text ----------------

    const val const_aestrisk = "*"
    const val const_dot = "•"
    const val const_multiply = "X"
    const val const_percentage = "%"
    const val const_doller = "$"
    const val const_hash = "#"
    const val const_slash_forward = "/"
    const val const_colon = ":"
    const val const_rs = "Rs."
    const val const_after_point_2 = "%.2f"

    // ---------------- General ----------------

    const val BIRTHDAY_EMPTY = "0000-00-00"
    const val PHONE_FORMATTER = "### ### ####"

    const val DELAY_SPLASH = 3000
    const val DELAY_API_CALL = 20000
    const val DELAY_FRAGMENT_PAUSE = 100
    const val DELAY_SCROLL_PAUSE = 300

    // ---------------- Broadcast ----------------

    const val ACTION_PUSH_NOTIFICATION = "PUSH_NOTIFICATION"
    const val ACTION_PUSH_NOTIFICATION_ORDER_STATUS = "PUSH_NOTIFICATION_ORDER_STATUS"

    // ==========================================================
    // DEVELOPMENT SERVER
    // ==========================================================

    // Your PC IPv4 Address
    const val DEV_SERVER_HOST = "192.168.1.19"

    // Backend Port
    const val DEV_SERVER_PORT = "3006"

    // Base API URL
    const val BASE_URL =
        "http://$DEV_SERVER_HOST:$DEV_SERVER_PORT/api/v1/"

    // Root URL (Images / PDF / Public Files)
    const val SERVER_ROOT_URL =
        "http://$DEV_SERVER_HOST:$DEV_SERVER_PORT"

}