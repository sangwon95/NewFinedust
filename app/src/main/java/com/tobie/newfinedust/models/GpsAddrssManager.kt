package com.tobie.newfinedust.models

object GpsAddrssManager {
    private var address: String? = null

    fun setAddress(newAddress: String) {
        address = newAddress
    }

    fun getAddress(): String? {
        return address
    }
}