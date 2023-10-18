package com.tobie.newfinedust.room

interface RoomListener {
    fun onInsertListener(region: RegionEntity)

    fun onGetAllListener()
}