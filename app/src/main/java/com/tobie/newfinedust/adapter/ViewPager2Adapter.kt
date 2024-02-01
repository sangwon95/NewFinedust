package com.tobie.newfinedust.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tobie.newfinedust.MainActivity
import com.tobie.newfinedust.R
import com.tobie.newfinedust.databinding.ItemViewBinding
import com.tobie.newfinedust.models.*
import com.tobie.newfinedust.room.RoomListener
import com.tobie.newfinedust.utils.Etc.Companion.calculateAtmosphericEnvironment
import com.tobie.newfinedust.utils.Etc.Companion.getCoValueAirQualityLevel
import com.tobie.newfinedust.utils.Etc.Companion.getNo2ValueAirQualityLevel
import com.tobie.newfinedust.utils.Etc.Companion.getO3GradeAirQualityLevel
import com.tobie.newfinedust.utils.Etc.Companion.getSo2ValueAirQualityLevel
import com.tobie.newfinedust.utils.Etc.Companion.getTextForStatus
import com.tobie.newfinedust.utils.Etc.Companion.getTextForStatusIconImage
import java.io.Serializable

class ViewPager2Adapter(
    private var dustCombinedItemList: MutableList<DustCombinedData>,
    var activity: MainActivity,
    private val roomListener: RoomListener,
    private val intentListener: IntentListener,
) : RecyclerView.Adapter<ViewPager2Adapter.PagerViewHolder>() {

    override fun getItemCount(): Int {
        return dustCombinedItemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ItemViewBinding.inflate(inflater, parent, false)
        return PagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.bind(dustCombinedItemList[position])
    }

    inner class PagerViewHolder(private var binding: ItemViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DustCombinedData) {
            val pm10Value = item.dustItem.pm10Value?.toIntOrNull() ?: 0
            val pm25Value = item.dustItem.pm25Value?.toIntOrNull() ?: 0
            val dateTime = item.dustItem.dataTime?: "-"
            val txtState = calculateAtmosphericEnvironment(pm10Value, pm25Value)

            binding.mainFrame.setBackgroundResource(getTextForStatus(txtState))
            binding.mainFrame.setBackgroundResource(getTextForStatus(txtState)) // background color
            binding.mainImageView.setImageDrawable(ContextCompat.getDrawable(activity, getTextForStatusIconImage(txtState)))
            binding.pm10TextView.text = activity.getString(R.string.pm_unit, "미세먼지", pm10Value.toString())
            binding.pm25TextView.text = activity.getString(R.string.pm_unit, "초 미세먼지", pm25Value.toString())
            binding.dateTimeTextView.text = dateTime
            binding.addressTextView.text = item.address
            binding.stateTextView.text = txtState

            val no2Value = item.dustItem.no2Value ?: "-"
            val o3Value = item.dustItem.o3Value ?: "-"
            val coValue = item.dustItem.coValue ?: "-"
            val so2Value = item.dustItem.so2Value ?: "-"

            val data: ArrayList<Remain> = arrayListOf(
                Remain("이산화 질소", getNo2ValueAirQualityLevel(no2Value), "$no2Value ppm", getTextForStatusIconImage(getNo2ValueAirQualityLevel(no2Value))),
                Remain("오존", getO3GradeAirQualityLevel(o3Value), "$o3Value ppm", getTextForStatusIconImage(getO3GradeAirQualityLevel(o3Value))),
                Remain("일산화탄소", getCoValueAirQualityLevel(coValue), "$coValue ppm", getTextForStatusIconImage(getCoValueAirQualityLevel(coValue))),
                Remain("이황산가스", getSo2ValueAirQualityLevel(so2Value), "$so2Value ppm", getTextForStatusIconImage(getSo2ValueAirQualityLevel(so2Value))),
            )
            val remainAdapter = RemainAdapter(data)
            binding.recyclerView.adapter = remainAdapter
            binding.recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false) // 가로 정렬


            // 미세먼지 예보 정보
            val date = item.forecastItem.informData
            val informCause = item.forecastItem.informCause
            val informOverall = item.forecastItem.informOverall
            binding.forecastDateTextView.text = date
            binding.forecastContentTextView.text = activity.getString(R.string.forecast_unit, informCause, informOverall)


            // 지역 추가 버튼 리스너
            binding.addImageView.setOnClickListener {
                intentListener.addIntentListener() //화면전화
            }


            // 지역 리스트 수정 버튼 리스너
            binding.editImageView.setOnClickListener {
                intentListener.editIntentListener() //화면전화
            }
        }
    }

    /**
     * viewPage 미세먼지 수치 값을 업데이트 한다.
     */
     fun update(dustItemList: ArrayList<DustCombinedData>) {
        dustCombinedItemList = dustItemList
        notifyDataSetChanged()
    }

    fun remove(removeDustItemPosition: Int) {
        dustCombinedItemList.removeAt(removeDustItemPosition)
        notifyDataSetChanged()
    }
}