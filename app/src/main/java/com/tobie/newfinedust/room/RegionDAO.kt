package com.tobie.newfinedust.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import retrofit2.http.DELETE

@Dao
interface RegionDAO {
    @Insert(onConflict = REPLACE)
    fun insert(region: RegionEntity)

    @Query("select * From region")
    fun getAll() : List<RegionEntity>

    @DELETE
    fun delete(region: RegionEntity)
}