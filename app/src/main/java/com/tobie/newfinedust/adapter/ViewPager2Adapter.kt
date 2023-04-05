package com.tobie.newfinedust.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tobie.newfinedust.R

class ViewPager2Adapter(idolList: ArrayList<Int>) : RecyclerView.Adapter<ViewPager2Adapter.PagerViewHolder>() {
    var item = idolList

    override fun getItemCount(): Int = item.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder((parent))

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.txt.text = item[position].toString()
    }

    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.view_item, parent, false)){

        val txt = itemView.findViewById<TextView>(R.id.text_view)
    }


     fun update(idolList: ArrayList<Int>) {
        item = idolList
        notifyItemChanged(item.size-1)
    }
}