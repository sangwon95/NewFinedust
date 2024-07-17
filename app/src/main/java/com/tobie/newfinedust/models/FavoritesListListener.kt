package com.tobie.newfinedust.models
interface FavoritesListEventListener {
    fun deleteListener(address: String, position: Int)
    fun selectListener(address: String)
}