package com.tobie.newfinedust.models.listener

import com.tobie.newfinedust.models.TmCoordinates

interface FavoritesListEventListener {
    fun deleteListener(tmCoordinates: TmCoordinates, position: Int)
    fun selectListener(tmCoordinates: TmCoordinates)
}