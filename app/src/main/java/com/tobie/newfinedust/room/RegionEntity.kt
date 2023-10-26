package com.tobie.newfinedust.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "region")
data class RegionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    val region: String
)