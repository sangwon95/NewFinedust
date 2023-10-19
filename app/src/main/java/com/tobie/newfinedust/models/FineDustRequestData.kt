package com.tobie.newfinedust.models

import com.tobie.newfinedust.BuildConfig

/**
 * 관측정소별 실시간 측정정보 조회
 * GET Query Data class
 */
data class FineDustRequestData(
    val serviceKey: String = BuildConfig.AIR_API_KEY,
    val returnType: String = "json",
    val numOfRows: Int = 1,
    val pageNo: Int = 1,
    val stationName: String = "관평동",
    val dataTerm: String = "DAILY",
    val ver: String = "1.0",
)
