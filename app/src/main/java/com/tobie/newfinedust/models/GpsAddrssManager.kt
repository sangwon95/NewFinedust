package com.tobie.newfinedust.models

object GpsAddrssManager {
    private var tmX: Double? = null
    private var tmY: Double? = null
    private var address: String? = null

    fun set(tmX: Double, tmY: Double, address: String) {
        this.tmX = tmX
        this.tmY = tmY
        this.address = address

    }
}