package com.tobie.newfinedust.models

import com.google.gson.annotations.SerializedName

data class Tmxy(
    val response: TmxyResponse
)

data class TmxyResponse(
    @SerializedName("body")
    val body: TmxyBody,

    @SerializedName("header")
    val header: TmxyHeader
)


data class TmxyBody(
    @SerializedName("items")
    val tmxyItems: List<TmxyItem>,

    @SerializedName("numOfRows")
    val numOfRows: Int,

    @SerializedName("pageNo")
    val pageNo: Int,

    @SerializedName("totalCount")
    val totalCount: Int
)

data class TmxyHeader(
    @SerializedName("resultCode")
    val resultCode: String,

    @SerializedName("resultMsg")
    val resultMsg: String
)


data class TmxyItem(
    @SerializedName("sggName")
    val sggName: String,

    @SerializedName("sidoName")
    val sidoName: String,

    @SerializedName("tmX")
    val tmX: String,

    @SerializedName("tmY")
    val tmY: String,

    @SerializedName("umdName")
    val umdName: String
)