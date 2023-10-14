package com.tobie.newfinedust.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tobie.newfinedust.R
import com.tobie.newfinedust.models.Dust
import com.tobie.newfinedust.models.DustItem
import com.tobie.newfinedust.models.Remain
import com.tobie.newfinedust.utils.Etc
import kotlin.math.log

class ViewPager2Adapter(dustItemList: ArrayList<DustItem>, context: Context, address: ArrayList<String>) : RecyclerView.Adapter<ViewPager2Adapter.PagerViewHolder>() {
    var mDustItemList = dustItemList
    var context = context
    var address: ArrayList<String> = address

    override fun getItemCount(): Int = mDustItemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder((parent))

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {

        //메인박스(미세, 초미세먼지) UI 적용
        setTextInMainBox(holder, position)
    }

    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.view_item, parent, false)){
        var recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)

        var txtAddress: TextView = itemView.findViewById(R.id.tv_address)
        var txtState: TextView = itemView.findViewById(R.id.tv_state)
        var txtPm10: TextView = itemView.findViewById(R.id.tv_pm10)
        var txtPm25: TextView = itemView.findViewById(R.id.tv_pm25)
        var txtDateTime: TextView = itemView.findViewById(R.id.iv_dateTime)
        //val mainImage: ImageView = itemView.findViewById(R.id.iv_main)
    }


    /**
     * viewPage 미세먼지 수치 값을 업데이트 한다.
     */
     fun update(dustItemList: ArrayList<DustItem>) {
         mDustItemList = dustItemList
        notifyItemChanged(dustItemList.size-1)
    }

    private fun setTextInMainBox(holder: PagerViewHolder, position: Int) {
         val pm10Value = mDustItemList[position].pm10Value?.toIntOrNull() ?: 0
         val pm25Value = mDustItemList[position].pm25Value?.toIntOrNull() ?: 0
         val dateTime = mDustItemList[position].dataTime?: "-"

        holder.txtAddress.text = address[position]
        Log.d("결과값 확인!","$pm10Value $pm25Value")
        holder.txtState.text = Etc.calculateAtmosphericEnvironment(pm10Value, pm25Value)

        holder.txtPm10.text = context.getString(R.string.pm_unit, "미세먼지", pm10Value.toString())
        holder.txtPm25.text = context.getString(R.string.pm_unit, "초 미세먼지", pm25Value.toString())
        holder.txtDateTime.text = dateTime


        /**
         * 여기서부터 하면된다.
         * 6/1 각 status 기준치에 맞게 만들어야됨!!
         */
        val data: ArrayList<Remain> = arrayListOf<Remain>(
            Remain("이산화 질소","아주좋음", "${mDustItemList[position].no2Value?:"-"} ppm"),
            Remain("오존","아주좋음", "${mDustItemList[position].o3Grade?:"-"} ppm"),
            Remain("일산화탄소","아주좋음", "${mDustItemList[position].coValue?:"-"} ppm"),
            Remain("이황산가스","아주좋음", "${mDustItemList[position].so2Value?:"-"} ppm"),
        )

        val remainAdapter = RemainAdapter(data)
        holder.recyclerView.adapter = remainAdapter
        holder.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) // 가로 정렬

    }
}