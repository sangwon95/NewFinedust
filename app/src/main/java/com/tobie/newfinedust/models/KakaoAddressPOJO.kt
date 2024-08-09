package com.tobie.newfinedust.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class KakaoAddress(
    val documents: ArrayList<Documents>
)

data class Documents(
    @SerializedName("address_name")
    @Expose
    val address: String,

    @SerializedName("x")
    @Expose
    val x: String,

    @SerializedName("y")
    @Expose
    val y: String,
)
