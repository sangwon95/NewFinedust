package com.tobie.newfinedust.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobie.newfinedust.models.*
import com.tobie.newfinedust.repository.HomeRepository
import com.tobie.newfinedust.repository.KakaoRepository
import com.tobie.newfinedust.service.RetrofitAirService
import com.tobie.newfinedust.service.RetrofitKakaoAddrService
import kotlinx.coroutines.*

class SearchViewModel : ViewModel() {

    companion object {
        const val TAG = "SearchViewModel - 로그"
    }

    // 홈 리포지토리 인스턴스 생성
    private val repository: KakaoRepository = KakaoRepository(RetrofitKakaoAddrService.getInstance())

    // LiveData 객체 초기화 - 먼지 데이터, TM 좌표, 첫 번째 주소 등
    private val _documentsValue = MutableLiveData<ArrayList<Documents>>()
    private val _errorValue = MutableLiveData<Boolean>()

    // 외부에서 접근 할 수 있는 LiveData Getter
    val documentsValue: MutableLiveData<ArrayList<Documents>> get() = _documentsValue
    val errorValue: MutableLiveData<Boolean> get() = _errorValue

    /**
     * 에어코리아 API를 통해서 미세먼지 수치(데이터)를 가져온다.
     */
    fun getSubAddress(inputText: String) {
        viewModelScope.launch {
            try {
                val responseAddr = async { repository.getAddress(inputText) }
                val isResponse = responseAddr.await().isSuccessful

                withContext(Dispatchers.IO) {
                    if (isResponse) {
                        Log.d(TAG, "!!"+responseAddr.await().body().toString())
                        _documentsValue.postValue(responseAddr.await().body()!!.documents)
                    } else {
                        Log.d(TAG , "검색하신 주소는 찾을 수가 없습니다..")
                    }
                }
            }
            catch (e: Exception) {
                _errorValue.postValue(false)
                Log.e(TAG + "Exception Error:", e.toString())
                // Show AlertDialog..
            }
        }
    }

}