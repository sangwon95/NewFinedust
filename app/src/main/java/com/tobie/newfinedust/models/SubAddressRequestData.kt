package com.tobie.newfinedust.models

import com.tobie.newfinedust.BuildConfig

data class SubAddressRequestData(
    val key: String = BuildConfig.AIR_API_KEY,
    val request: String = "GetFeature",
    val format: String  = "json",
    val size: Int = 20,
    val page: Int = 30,
    val data: String = "LT_C_ADEMD_INFO",
    val attrfilter: String = "emd_kor_nm:like:",
    val columns: String = "emd_cd,full_nm,emd_kor_nm,emd_eng_nm,ag_geom",
    val geometry: Boolean = false,
    val attribute: Boolean = true,
    val crs: String = "EPSG:900913",
    val domain: String = "",
)