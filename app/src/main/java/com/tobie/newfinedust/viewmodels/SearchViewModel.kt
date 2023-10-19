package com.tobie.newfinedust.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobie.newfinedust.models.DustCombinedData
import com.tobie.newfinedust.models.FineDustRequestData
import com.tobie.newfinedust.models.SubAddressRequestData
import com.tobie.repository.SearchRepository
import kotlinx.coroutines.*

class SearchViewModel constructor(private val repository: SearchRepository) : ViewModel() {

    companion object {
        const val TAG = "SearchViewModel - 로그"
    }
    init {
        Log.d(MainViewModel.TAG, "생성자 호출");
    }

    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }

    val loading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    /**
     * 에어코리아 API를 통해서 미세먼지 수치(데이터)를 가져온다.
     */
    fun getSubAddress(inputText: String) {
        job = viewModelScope.launch {
            try {
                val subAddressRequestData = SubAddressRequestData(attrfilter = "emd_kor_nm:like:${inputText}")
                val responseAddr = async { repository.getSubAddress(subAddressRequestData) } //읍면동 주소 검색
                val isResponse = responseAddr.await().isSuccessful

                withContext(Dispatchers.IO + exceptionHandler) {
                    if (isResponse) {
                        //_dustCombinedData.postValue(dustCombinedData)
                        Log.d(TAG, responseAddr.await().body().toString())
                        loading.postValue(false)
                    } else {
                        Log.d(MainViewModel.TAG + "Error", "미세먼지 정보, 예보를 불러오지 못했습니다.")
                        onError("미세먼지 정보, 예보를 불러오지 못했습니다.")
                    }
                }
            }
            catch (e: Exception) {
                Log.e(MainViewModel.TAG + "Exception Error:", e.toString())
                // Show AlertDialog..
            }
        }
    }

    private fun onError(message: String) {
        errorMessage.postValue(message)
        loading.postValue(false)
    }
}