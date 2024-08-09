package com.tobie.newfinedust.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobie.newfinedust.models.*
import com.tobie.repository.KakaoRepository
import kotlinx.coroutines.*

class SearchViewModel constructor(private val repository: KakaoRepository) : ViewModel() {

    companion object {
        const val TAG = "SearchViewModel - 로그"
    }

    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }

    val loading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    private val _documentsValue = MutableLiveData<ArrayList<Documents>>()
    private val _errorValue = MutableLiveData<Boolean>()

    val documentsValue: MutableLiveData<ArrayList<Documents>> get() = _documentsValue

    val errorValue: MutableLiveData<Boolean> get() = _errorValue

    /**
     * 에어코리아 API를 통해서 미세먼지 수치(데이터)를 가져온다.
     */
    fun getSubAddress(inputText: String) {
        job = viewModelScope.launch {
            try {
                //val subAddressRequestData = SubAddressRequestData(attrfilter = "emd_kor_nm:like:${inputText}")
                val responseAddr = async { repository.getAddress(inputText) } // 읍면동 주소 검색
                val isResponse = responseAddr.await().isSuccessful

                withContext(Dispatchers.IO + exceptionHandler) {
                    if (isResponse) {
                        //_dustCombinedData.postValue(dustCombinedData)
                        Log.d(TAG, "!!"+responseAddr.await().body().toString())
                        _documentsValue.postValue(responseAddr.await().body()!!.documents)

                        loading.postValue(false)
                    } else {
                        Log.d(TAG , "검색하신 주소는 찾을 수가 없습니다..")
                        onError("검색하신 주소는 찾을 수가 없습니다..")
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

    private fun onError(message: String) {
        errorMessage.postValue(message)
        loading.postValue(false)
    }
}