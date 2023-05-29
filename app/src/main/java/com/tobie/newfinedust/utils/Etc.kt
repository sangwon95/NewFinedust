package com.tobie.newfinedust.utils

import android.location.Address
import android.util.Log
import com.tobie.newfinedust.viewmodels.MainViewModel

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
    }
}