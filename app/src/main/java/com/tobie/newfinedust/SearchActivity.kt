package com.tobie.newfinedust

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.viewpager2.widget.ViewPager2
import com.tobie.newfinedust.adapter.SearchListAdapter
import com.tobie.newfinedust.adapter.ViewPager2Adapter
import com.tobie.newfinedust.databinding.ActivitySearchBinding
import com.tobie.newfinedust.models.AddressData
import com.tobie.newfinedust.models.DustCombinedData
import com.tobie.newfinedust.models.Feature
import com.tobie.newfinedust.models.FeatureCollection
import com.tobie.newfinedust.service.RetrofitAddrService
import com.tobie.newfinedust.viewmodels.SearchViewModel
import com.tobie.newfinedust.viewmodels.SearchViewModelFactory
import com.tobie.repository.SearchRepository

/**
 * 읍면동 검색 화면
 */
class SearchActivity : AppCompatActivity() {
    companion object {
        const val TAG: String = "SearchActivity - 로그"
    }

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: SearchListAdapter

    //검색한 결과 주소
    private lateinit var searchedList: ArrayList<AddressData>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //view binding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureSearchViewModel()
    }

    private fun configureSearchViewModel() {
        val retrofitAddrService = RetrofitAddrService.getInstance()
        val searchRepository = SearchRepository(retrofitAddrService)

        // MainViewModelFactory: MainViewModel을 통해 전달되는 인자가 있을때 사용됩니다.
        // 생성자나 매개변수를 사용하지 않고 MainViewModel 객체를 인스턴스화한다.
        // mainRepository 지정
        viewModel = ViewModelProvider(
            this, SearchViewModelFactory(searchRepository)
        )[SearchViewModel::class.java]

        viewModel.getSubAddress("중동")

        viewModel.featuresValue.observe(this) {
            Log.d(TAG+"테스트", it.toString())
            val featureList: List<Feature> = it

            adapter = SearchListAdapter(featureList, this)
            binding.recyclerSearch.adapter = adapter // 어뎁터 생성
            binding.recyclerSearch.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        }


    }


    private fun setAddressResultList() {

    }
}