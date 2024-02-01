package com.tobie.newfinedust.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tobie.newfinedust.databinding.FavoriteListItemBinding
import com.tobie.newfinedust.models.FavoritesListEventListener


class FavoriteAdapter(
    private var addressList: ArrayList<String>,
    private val favoritesListEventListener: FavoritesListEventListener,

): RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = FavoriteListItemBinding.inflate(inflater, parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(addressList[position], position)
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    fun removeDataAt(pos: Int) {
        addressList.removeAt(pos)
        notifyItemRemoved(pos)
        favoritesListEventListener.changedFavoritesListListener(addressList)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val movedItem = addressList.removeAt(fromPosition)
        addressList.add(toPosition, movedItem)
        favoritesListEventListener.changedFavoritesListListener(addressList)
    }

    inner class FavoriteViewHolder(private var binding: FavoriteListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(address: String, position: Int){
            if(position == 0){
                binding.addressTextView.text = "현재 위치: $address"
                binding.swapImageView.visibility = View.GONE
            } else {
                binding.addressTextView.text = address
            }
        }
    }
}