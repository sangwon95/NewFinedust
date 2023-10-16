package com.tobie.newfinedust.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(RegionEntity::class), version = 1)

abstract class RegionDatabase: RoomDatabase() {
    abstract fun regionDAO(): RegionDAO

    companion object {
        private var instance: RegionDatabase? = null

        fun getInstance(context: Context) : RegionDatabase? {
            if(instance == null){
                synchronized(RegionDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        RegionDatabase::class.java, "region")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance
        }
    }
}