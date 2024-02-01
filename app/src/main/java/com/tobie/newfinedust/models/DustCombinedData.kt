package com.tobie.newfinedust.models

data class DustCombinedData(
    val dustItem: DustItem,
    val forecastItem: ForecastItem,
    var address: String? = null
)
