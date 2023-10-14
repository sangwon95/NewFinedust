package com.tobie.newfinedust.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * 관측정소별 실시간 측정정보 조회 POJO DataClass
 */
data class Dust(val response: DustResponse)

data class DustResponse(
    @SerializedName("body")
    val dustBody: DustBody,
    @SerializedName("header")
    val dustHeader: DustHeader
)

data class DustBody(
    @SerializedName("items")
    @Expose
    val dustItem: List<DustItem>?,

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


data class DustHeader(
    @SerializedName("resultCode")
    @Expose
    val resultCode: String?,

    @SerializedName("resultMsg")
    @Expose
    val resultMsg: String?
)


data class DustItem(
    @SerializedName("coFlag")
    @Expose
    val coFlag: String?, // 일산화탄소 플래그

    @SerializedName("coGrade")
    @Expose
    val coGrade: String?, // 일산화탄소 지수

    @SerializedName("coValue")
    @Expose
    val coValue: String?, // 일산화탄소

    @SerializedName("dataTime")
    @Expose
    val dataTime: String?, // 측정시간

    @SerializedName("khaiGrade")
    @Expose
    val khaiGrade: String?, // 통합대기환경지수

    @SerializedName("khaiValue")
    @Expose
    val khaiValue: String?, // 통합대기환경지수

    @SerializedName("no2Flag")
    @Expose
    val no2Flag: String?, // 이산화질소 플래그

    @SerializedName("no2Grade")
    @Expose
    val no2Grade: String?, // 이산화질소 지수

    @SerializedName("no2Value")
    @Expose
    val no2Value: String?, // 이산화질소

    @SerializedName("o3Flag")
    @Expose
    val o3Flag: String?, // 오존플래그

    @SerializedName("o3Grade:")
    @Expose
    val o3Grade: String?, // 오존 지수

    @SerializedName("o3Value")
    @Expose
    val o3Value: String?, // 오존

    @SerializedName("pm10Flag")
    @Expose
    val pm10Flag: String?, // 미세먼지 플래그

    @SerializedName("pm10Grade")
    @Expose
    val pm10Grade: String?, // 미세먼지 지수

    @SerializedName("pm10Value")
    @Expose
    val pm10Value: String?, // 미세먼지

    @SerializedName("pm25Flag")
    @Expose
    val pm25Flag: String?, // 초 미세먼지 플래그

    @SerializedName("pm25Grade")
    @Expose
    val pm25Grade: String?, // 초미세먼지 등급

    @SerializedName("pm25Value")
    @Expose
    val pm25Value: String?, // 초 미세먼지

    @SerializedName("so2Flag")
    @Expose
    val so2Flag: String?,

    @SerializedName("so2Grade")
    @Expose
    val so2Grade: String?,

    @SerializedName("so2Value")
    @Expose
    val so2Value: String? // 이황산가스
)