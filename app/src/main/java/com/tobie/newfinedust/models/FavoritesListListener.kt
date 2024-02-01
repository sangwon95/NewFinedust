package com.tobie.newfinedust.models
interface FavoritesListEventListener {
    fun changedFavoritesListListener(updatedList: ArrayList<String>)
}