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
import com.tobie.newfinedust.models.Documents
import com.tobie.newfinedust.models.DustCombinedData
import com.tobie.newfinedust.models.Feature
import com.tobie.newfinedust.models.FeatureCollection
import com.tobie.newfinedust.utils.Etc

class SearchListAdapter(
    documentItemList: ArrayList<Documents> ,
    var context: Context,
    private val selectedItemListener: AddressClickListener
) : RecyclerView.Adapter<SearchListAdapter.ViewHolder>() {
    private var dataList: ArrayList<Documents> = documentItemList

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
        holder.addressText.text = Etc().addSpaceAfterCityName(item.address)

        //Etc().addSpaceAfterCityName(item.address)

        holder.searchItemView.setOnClickListener {
            selectedItemListener.getAddress(item.address, item.x, item.y)
        }
    }

    fun update(documentItemList: ArrayList<Documents>) {
        dataList = documentItemList
        notifyDataSetChanged()
    }

    fun clean() {
        dataList.clear()
        notifyDataSetChanged()
    }
}