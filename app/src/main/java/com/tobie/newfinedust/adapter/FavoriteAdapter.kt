package com.tobie.newfinedust.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tobie.newfinedust.databinding.FavoriteListItemBinding
import com.tobie.newfinedust.models.FavoritesListEventListener
import com.tobie.newfinedust.models.TmCoordinates

class FavoriteAdapter(
    private var tmCoordinatesList: ArrayList<TmCoordinates>,
    private val eventListener: FavoritesListEventListener,

    ): RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = FavoriteListItemBinding.inflate(inflater, parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(tmCoordinatesList[position], position)
    }

    override fun getItemCount(): Int {
        return tmCoordinatesList.size
    }

    fun removeDataAt(pos: Int) {
        tmCoordinatesList.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, tmCoordinatesList.size)

        tmCoordinatesList.forEachIndexed { index, value ->
            Log.d("FavoriteAdapter", "index: $index, value: $value")
        }
    }


    inner class FavoriteViewHolder(private var binding: FavoriteListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(tmCoordinates: TmCoordinates, pos: Int) {
            binding.addressTextView.text = tmCoordinates.address

            // 삭제
            binding.deleteImageView.setOnClickListener {
                eventListener.deleteListener(tmCoordinates, pos)
                Log.d("FavoriteAdapter", "address: ${tmCoordinates.address}, pos: $pos")
            }

            // 선택
            binding.favoriteListItemLayout.setOnClickListener {
                eventListener.selectListener(tmCoordinatesList[pos])
            }
        }
    }
}