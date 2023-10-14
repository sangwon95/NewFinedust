package com.tobie.newfinedust.utils

import android.location.Address
import android.util.Log
import com.tobie.newfinedust.viewmodels.MainViewModel
import kotlin.math.max

class Etc {
    companion object{
        /**
         * 위도, 경도로 추출된 주소리스트를 가공하여 보다
         * 정확한 주소를 추출한다.
         */
        fun translationAddress (addressList: List<Address>) : String{
             var returnAddress = ""

                for(value in addressList){
                    Log.d(MainViewModel.TAG, value.toString())

                    // thoroughfare=null 경우 도로명주소가 나온경우이다.
                    // umdName 규격상 구도로 주소명을 넣어야된다.
                    if(value.thoroughfare != null){
                        val address = value.getAddressLine(0).split(" ")

                        // sub-admin ex) (구)가 포함된 주소는 index:4번째 까지 포함해야된다.
                        // ex) 충청북도 청주시 흥덕구 가경동
                        returnAddress = if(address[4].contains("동")){
                            "${address[1]} ${address[2]} ${address[3]} ${address[4]}"
                        } else {
                            "${address[1]} ${address[2]} ${address[3]}"
                        }
                    }
                }
            return returnAddress
        }


        /**
         * 통합 대기환경지수 계산
         */
        fun calculateAtmosphericEnvironment(pm10: Int, pm25: Int): String {
            val pm10Rating = when (pm10) {
                0 -> 0 //측정 불가
                in 1..40 -> 1 //좋음
                in 41 .. 50 -> 2 //보통
                in 51 .. 75 -> 3 //나쁨
                in 76 .. 150 -> 4 //매우 나쁨
                else  // 151이상
                -> 5 // 최악
            }

            val pm25Rating = when (pm25) {
                0 -> 0 //측정 불가
                in 1..20 -> 1 //좋음
                in 21 .. 25-> 2 //보통
                in 26 .. 37 -> 3 //나쁜
                in 38 .. 75 -> 4 //매우 나쁨
                else  //76 이상
                -> 5
            }

            return when(max(pm10Rating, pm25Rating)) {
                0 -> "측정 불가"
                1 -> "좋음"
                2 -> "보통"
                3 -> "나쁨"
                4 -> "매우 나쁨"
                5 -> "최악"
                else -> "알수 없음"
            }
        }
    }
}