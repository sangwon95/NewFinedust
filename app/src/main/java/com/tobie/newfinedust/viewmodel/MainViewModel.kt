package com.tobie.newfinedust.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel

// 1."Room Data" 조회를 통해서 저장된 지역 데이터가 있는 확인한다.
// 2. 리스트 갯수 만큼 에어코리아 API 호출 하여 미세먼지 데이터를 가져온다.
// 3.
class MainViewModel : ViewModel() {

    companion object {
        const val TAG = "MainViewModel - 로그"
    }

    init {
        Log.d(TAG, "생성자 호출");

        // MutableLiveData initialize
        // ArraryList<Finedust> .. (Finedust: DataClass)
    }


    /**
     * 1. SQLite를 통해서 즐겨찾기 추가된 데이터가 있는지 확인한다.
     * 2. 첫번쨰 지역은 현재위치를 가져와 데이터를 보여준다.
     */
    fun getRegionFromRoom(){
    }

    /**
     * 자신의 위치 정보를 가져온다.
     */
    fun getMyLocation(){

    }

    /**
     * 에어코리아 API를 통해서 미세먼지 수치(데이터)를 가져온다.
     */
    fun getFineDustLevel(){

    }

}