package com.tobie.newfinedust.repository

import com.tobie.newfinedust.BuildConfig
import com.tobie.newfinedust.models.SubAddressRequestData
import com.tobie.newfinedust.service.RetrofitAddrService
import com.tobie.newfinedust.service.RetrofitKakaoAddrService

class KakaoRepository constructor(private val retrofitService: RetrofitKakaoAddrService) {
    //카카오 읍면동 주소 가져오기
    suspend fun getAddress(query: String)
     = retrofitService.getAddress(
        BuildConfig.KAKAO_API_KEY,
        "similar",
        query,
     )
}