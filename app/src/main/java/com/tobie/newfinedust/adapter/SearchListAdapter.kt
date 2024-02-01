package com.tobie.newfinedust.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.tobie.newfinedust.AddressClickListener
import com.tobie.newfinedust.R
import com.tobie.newfinedust.SearchActivity
import com.tobie.newfinedust.models.AddressData
import com.tobie.newfinedust.models.DustCombinedData
import com.tobie.newfinedust.models.Feature
import com.tobie.newfinedust.models.FeatureCollection

class SearchListAdapter(
    addressItemList: ArrayList<Feature>,
    var context: Context,
    private val selectedItemListener: AddressClickListener
) : RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {
    private var dataList: ArrayList<Feature> = addressItemList

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val addressText: TextView = view.findViewById(R.id.tv_searchAddress)
        val searchItemView: ConstraintLayout = view.findViewById(R.id.searchItemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.addressText.text = item.properties.full_nm

        holder.searchItemView.setOnClickListener {
            selectedItemListener.getAddress(item.properties.full_nm)
        }
    }

    fun update(addressItemList: ArrayList<Feature>) {
        dataList = addressItemList
        notifyDataSetChanged()
    }

    fun clean() {
        dataList.clear()
        notifyDataSetChanged()
    }
}