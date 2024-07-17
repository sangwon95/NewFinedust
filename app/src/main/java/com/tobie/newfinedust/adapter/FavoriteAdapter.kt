package com.tobie.newfinedust.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tobie.newfinedust.databinding.FavoriteListItemBinding
import com.tobie.newfinedust.models.FavoritesListEventListener

class FavoriteAdapter(
    private var addressList: ArrayList<String>,
    private val eventListener: FavoritesListEventListener,

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
        notifyItemRangeChanged(pos, addressList.size)

        addressList.forEachIndexed { index, value ->
            Log.d("FavoriteAdapter", "index: $index, value: $value")
        }
    }


    inner class FavoriteViewHolder(private var binding: FavoriteListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(address: String, pos: Int) {
            binding.addressTextView.text = address
            binding.deleteImageView.setOnClickListener {
                eventListener.deleteListener(address, pos)
                Log.d("FavoriteAdapter", "address: ${address}, pos: $pos")
            }
            binding.favoriteListItemLayout.setOnClickListener {
                eventListener.selectListener(addressList[pos])
            }
        }
    }
}