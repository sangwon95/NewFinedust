package com.tobie.newfinedust.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 미세먼지 POJO DataClass
 */
data class Forecast(val response: ForecastResponse)

data class ForecastResponse(
    @SerializedName("body")
    val forecastBody: ForecastBody,
    @SerializedName("header")
    val forecastHeader: ForecastHeader
)

data class ForecastBody(
    @SerializedName("items")
    @Expose
    val forecastItem: List<ForecastItem>?,

    @SerializedName("numOfRows")
    @Expose
    val numOfRows: Int?,

    @SerializedName("pageNo")
    @Expose
    val pageNo: Int?,

    @SerializedName("totalCount")
    @Expose
    val totalCount: Int?
)


data class ForecastHeader(
    @SerializedName("resultCode")
    @Expose
    val resultCode: String?,

    @SerializedName("resultMsg")
    @Expose
    val resultMsg: String?
)


data class ForecastItem(
    @SerializedName("informCause")
    @Expose
    val informCause: String?,

    @SerializedName("informOverall")
    @Expose
    val informOverall: String?,

    @SerializedName("informData")
    @Expose
    val informData: String?
)