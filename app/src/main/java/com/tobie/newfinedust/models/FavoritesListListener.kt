package com.tobie.newfinedust.models
interface FavoritesListEventListener {
    fun deleteListener(tmCoordinates: TmCoordinates, position: Int)
    fun selectListener(tmCoordinates: TmCoordinates)
}