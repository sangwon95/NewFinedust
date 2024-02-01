//package com.tobie.newfinedust
//
//import android.os.Bundle
//import android.util.Log
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.tobie.newfinedust.adapter.RemainAdapter
//import com.tobie.newfinedust.databinding.FragmentHomeBinding
//import com.tobie.newfinedust.models.DustCombinedData
//import com.tobie.newfinedust.models.Remain
//import com.tobie.newfinedust.utils.Etc
//
//class HomeFragment(var dustData: DustCombinedData) : Fragment() {
//    companion object {
//        const val TAG: String = "HomeFragment 로그"
//    }
//
//    private lateinit var binding:FragmentHomeBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d(TAG, "HomeFragment - onCreate() celled")
//
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentHomeBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        val pm10Value = dustData.dustItem.pm10Value?.toIntOrNull() ?: 0
//        val pm25Value = dustData.dustItem.pm25Value?.toIntOrNull() ?: 0
//        val dateTime = dustData.dustItem.dataTime?: "-"
//
////        binding.txtAddress.text = address[position]
////        binding.txtState.text = Etc.calculateAtmosphericEnvironment(pm10Value, pm25Value)
////        binding.tvPm10.text = context?.getString(R.string.pm_unit, "미세먼지", pm10Value.toString())
////        binding.tvPm25.text = context?.getString(R.string.pm_unit, "초 미세먼지", pm25Value.toString())
////        binding.ivDateTime.text = dateTime
//
//
//        /**
//         * 여기서부터 하면된다.
//         * 6/1 각 status 기준치에 맞게 만들어야됨!!
//         */
////        val data: ArrayList<Remain> = arrayListOf<Remain>(
////            Remain("이산화 질소","아주좋음", "${dustData.dustItem.no2Value?:"-"} ppm"),
////            Remain("오존","아주좋음", "${dustData.dustItem.o3Grade?:"-"} ppm"),
////            Remain("일산화탄소","아주좋음", "${dustData.dustItem.coValue?:"-"} ppm"),
////            Remain("이황산가스","아주좋음", "${dustData.dustItem.so2Value?:"-"} ppm"),
////        )
//
//        val remainAdapter = RemainAdapter(data)
////        binding.recyclerView.adapter = remainAdapter
////        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) // 가로 정렬
//    }
//}