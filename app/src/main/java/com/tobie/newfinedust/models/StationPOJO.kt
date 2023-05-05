package com.tobie.newfinedust.models

import com.google.gson.annotations.SerializedName

data class Station(
    @SerializedName("response")
    val response: StationResponse
)

data class StationResponse(
    @SerializedName("body")
    val body: StationBody,

    @SerializedName("header")
    val header: Header
)

data class Header(
    @SerializedName("resultCode")
    val resultCode: String,

    @SerializedName("resultMsg")
    val resultMsg: String
)


data class StationBody(
    @SerializedName("items")
    val stationItems: List<StationItem>,

    @SerializedName("numOfRows")
    val numOfRows: Int,

    @SerializedName("pageNo")
    val pageNo: Int,

    @SerializedName("totalCount")
    val totalCount: Int
)


data class StationItem(
    @SerializedName("addr")
    val addr: String,

    @SerializedName("stationName")
    val stationName: String,

    @SerializedName("tm")
    val tm: Double
)