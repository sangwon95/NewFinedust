package com.tobie.newfinedust.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RegionEntity::class], version = 1)
abstract class RegionDatabase: RoomDatabase() {
    abstract fun regionDAO(): RegionDAO

    companion object {
        private var INSTANCE: RegionDatabase? = null

        fun getInstance(context: Context) : RegionDatabase? {
            if(INSTANCE == null){
                synchronized(RegionDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        RegionDatabase::class.java, "region.db")
                        .build()
                }
            }
            return INSTANCE
        }
    }
}