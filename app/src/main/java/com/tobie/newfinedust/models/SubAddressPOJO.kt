package com.tobie.newfinedust.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SubAddress(
    val response: Response
)

data class Response(
    @SerializedName("page")
    @Expose
    val page: Page,

    @SerializedName("record")
    @Expose
    val record: Record,

    @SerializedName("result")
    @Expose
    val result: Result,

    @SerializedName("service")
    @Expose
    val service: Service,

    @SerializedName("status")
    @Expose
    val status: String
)

data class Service(
    @SerializedName("name")
    @Expose
    val name: String,

    @SerializedName("operation")
    @Expose
    val operation: String,

    @SerializedName("time")
    @Expose
    val time: String,

    @SerializedName("version")
    @Expose
    val version: String
)

data class Feature(
    @SerializedName("id")
    @Expose
    val id: String,

    @SerializedName("properties")
    @Expose
    val properties: Properties,

    @SerializedName("type")
    @Expose
    val type: String
)

data class FeatureCollection(
    @SerializedName("bbox")
    @Expose
    val bbox: List<Double>,

    @SerializedName("features")
    @Expose
    val features: List<Feature>,

    @SerializedName("type")
    @Expose
    val type: String
)

data class Page(
    @SerializedName("current")
    @Expose
    val current: String,

    @SerializedName("size")
    @Expose
    val size: String,

    @SerializedName("total")
    @Expose
    val total: String
)

data class Properties(
    @SerializedName("emd_cd")
    @Expose
    val emd_cd: String,

    @SerializedName("emd_eng_nm")
    @Expose
    val emd_eng_nm: String,

    @SerializedName("emd_kor_nm")
    @Expose
    val emd_kor_nm: String,

    @SerializedName("full_nm")
    @Expose
    val full_nm: String
)

data class Record(
    @SerializedName("current")
    @Expose
    val current: String,

    @SerializedName("total")
    @Expose
    val total: String
)

data class Result(
    @SerializedName("featureCollection")
    @Expose
    val featureCollection: FeatureCollection
)