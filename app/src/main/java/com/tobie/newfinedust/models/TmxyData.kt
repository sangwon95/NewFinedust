package com.tobie.newfinedust.models

data class TmxyData(
    var returnType: String = "json",
    var numOfRows: Int = 100,
    var pageNo: Int = 1,
    var umdName: String = "장동",
)