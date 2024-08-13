package com.tobie.newfinedust.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.tobie.newfinedust.adapter.SearchListAdapter
import com.tobie.newfinedust.databinding.ActivitySearchBinding
import com.tobie.newfinedust.models.AddressData
import com.tobie.newfinedust.models.Documents
import com.tobie.newfinedust.models.listener.SearchItemClickListener
import com.tobie.newfinedust.service.RetrofitKakaoAddrService
import com.tobie.newfinedust.viewmodels.SearchViewModel
import com.tobie.newfinedust.repository.KakaoRepository

/**
 * 읍면동 검색 화면
 */
class SearchActivity : AppCompatActivity(), SearchItemClickListener {
    companion object {
        const val TAG: String = "SearchActivity - 로그"
    }

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()

    private lateinit var adapter: SearchListAdapter
    private var impossibleBack: Boolean = true
    private var documentsList: ArrayList<Documents> = arrayListOf()

    // 네비게이션 백버튼 콜백 인스턴스 생성
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로 버튼 이벤트 처리
            if (impossibleBack) {
                finish()
            } else {
                Toast.makeText(applicationContext, "검색 후 주소를 선택해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        // 백버튼 콜백 설정
        this.onBackPressedDispatcher.addCallback(this, callback)

        // EditText에 포커스 설정
        binding.searchEdit.requestFocus()

        binding.searchEdit.addTextChangedListener {
            Log.i(TAG, "입려한 값: $it")
            viewModel.getSubAddress("$it")
        }

        // 위치 권한 거절 후 RoomDB에도 저장된 데이터 없을 때 주소 찾기를 통해 저장이 필요
        // impossibleBack를 통해 데이터가 없을 시 go to back 할 수 없다.
        impossibleBack = intent.getBooleanExtra("impossibleBack", true)
        Log.d(TAG, "impossibleBack: $impossibleBack")

        // 검색 주소 어뎁터 생성
        adapter = SearchListAdapter(documentsList, this, this)
        binding.recyclerSearch.adapter = adapter

        // viewModel 옵저버 등록
        registerObservers()
    }

    override fun onResume() {
        super.onResume()
        // Search EditText text 모두 지우기
        binding.cancelButton.setOnClickListener {
            binding.searchEdit.text.clear()
            adapter.clean()
        }

        // 뒤로가기 버튼
        binding.backImageView.setOnClickListener {
            if(impossibleBack){
                finish()
            } else {
                Toast.makeText(this, "검색 후 주소를 선택해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 옵저버 등록
     */
    private fun registerObservers() {
        viewModel.documentsValue.observe(this) {
            Log.d(TAG , it.toString())
            documentsList = it
            adapter.update(documentsList)
        }

        viewModel.errorValue.observe(this) {
            if(!it && documentsList.size != 0){
                documentsList.clear()
                adapter.clean()
            }
        }
    }

    override fun getAddress(address: String, x: String, y: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("address", address)
            putExtra("x", x)
            putExtra("y", y)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        @Suppress("DEPRECATION")
        onBackPressed()
        return true
    }
}

