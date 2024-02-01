package com.tobie.newfinedust.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tobie.newfinedust.R
import com.tobie.newfinedust.models.Remain

class RemainAdapter(remainList: ArrayList<Remain>) : RecyclerView.Adapter<RemainAdapter.ViewHolder>() {
    private var dataList: ArrayList<Remain> = remainList

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitle: TextView = view.findViewById(R.id.tv_title)
        val imageStatus: ImageView = view.findViewById(R.id.iv_image)
        val txtStatus: TextView = view.findViewById(R.id.tv_status)
        val txtValue: TextView = view.findViewById(R.id.tv_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.remain_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
      return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.txtTitle.text = item.title
        holder.imageStatus.setImageResource(item.image)
        holder.txtStatus.text = item.status
        holder.txtValue.text = item.value
    }
}