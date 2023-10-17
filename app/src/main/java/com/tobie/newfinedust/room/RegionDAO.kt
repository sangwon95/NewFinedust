package com.tobie.newfinedust.room

import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE


@Dao
interface RegionDAO {
    @Insert(onConflict = REPLACE)
    fun insert(region: RegionEntity)

    @Query(value = "SELECT * FROM region")
    fun getAll() : List<RegionEntity>

    @Delete
    fun delete(region: RegionEntity)
}