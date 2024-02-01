package com.tobie.newfinedust

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
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
class SearchActivity : AppCompatActivity(), AddressClickListener {
    companion object {
        const val TAG: String = "SearchActivity - 로그"
    }

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: SearchListAdapter
    private var impossibleBack: Boolean = true

    // 검색한 결과 주소
    private lateinit var searchedList: ArrayList<AddressData>

    // 네비게이션 백버튼 콜백 인스턴스 생성
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로 버튼 이벤트 처리
            if (impossibleBack) {
                SearchActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(applicationContext, "주소를 선택해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private var featureList: ArrayList<Feature> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //view binding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSearchViewModel()

        this.onBackPressedDispatcher.addCallback(this, callback) // 백버튼 콜백 설정

        binding.searchEdit.addTextChangedListener {
            Log.i(TAG, "입려한 값: $it")
            viewModel.getSubAddress("$it")
        }

        // Search EditText text 모두 지우기
        binding.cancelButton.setOnClickListener {
            binding.searchEdit.text.clear()
        }

        // 뒤로가기 버튼
        binding.searchImage.setOnClickListener {
            if(impossibleBack){
                finish()
            } else {
                Toast.makeText(this, "주소를 선택해주세요.", Toast.LENGTH_LONG).show()
            }
        }

        // 위치 권한 거절 후 RoomDB에도 저장된 데이터 없을 때 주소 찾기를 통해 저장이 필요
        // impossibleBack를 통해 데이터가 없을 시 go to back 할 수 없다.
        impossibleBack = intent.getBooleanExtra("impossibleBack", true)
        Log.d(TAG, "impossibleBack: $impossibleBack")
    }

    private fun initSearchViewModel() {
        val retrofitAddrService = RetrofitAddrService.getInstance()
        val searchRepository = SearchRepository(retrofitAddrService)

        adapter = SearchListAdapter(featureList, this, this)
        binding.recyclerSearch.adapter = adapter // 어뎁터 생성

        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.recyclerSearch.addItemDecoration(dividerItemDecoration)

        // MainViewModelFactory: MainViewModel을 통해 전달되는 인자가 있을때 사용됩니다.
        // 생성자나 매개변수를 사용하지 않고 MainViewModel 객체를 인스턴스화한다.
        // mainRepository 지정
        viewModel = ViewModelProvider(
            this, SearchViewModelFactory(searchRepository)
        )[SearchViewModel::class.java]


        viewModel.featuresValue.observe(this) {
            Log.d(TAG+"테스트", it.toString())
            featureList = it

            Log.d(TAG, "가져온 주소 갯수:${featureList?.size}")
            for(value in featureList!!){
                Log.d(TAG, "가져온 주소:${value.properties.full_nm}!!")
            }
            adapter.update(featureList)
            // 리스트 중간선 설정
//            val dividerItemDecoration = DividerItemDecoration(this, LinearLayout.VERTICAL)
//            val divider = ContextCompat.getDrawable(this, R.drawable.divider_item_decoration)
//            divider?.let { it1 -> dividerItemDecoration.setDrawable(it1) }
//            binding.recyclerSearch.addItemDecoration(dividerItemDecoration)
        }

        viewModel.errorValue.observe(this) {
            if(!it && featureList.size != 0){
                featureList.clear()
                adapter.clean()
            }
        }
    }

    override fun getAddress(address: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("selectedAddress", address)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}

interface AddressClickListener {
    fun getAddress(address: String)
}