package com.tobie.newfinedust.utils

/**
 * ex)
 * enum class Constants(val value: String) {
 *  API_BASE_URL("https://example.com/api/"),
 *  MAX_RETRY_COUNT("3")
 * }
 */
class Constants {

    companion object{
        const val APP_NAME = "My App"
       //const val API_BASE_URl ="http://onwards.iptime.org:50003"
       const val AIR_API_BASE_URl ="http://apis.data.go.kr"
       const val VWORLD_API_BASE_URl ="https://api.vworld.kr"

        const val FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION
        const val COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION
    }
}

