package com.tobie.repository

import com.tobie.newfinedust.models.SubAddressRequestData
import com.tobie.newfinedust.service.RetrofitAddrService
import com.tobie.newfinedust.service.RetrofitKakaoAddrService

class KakaoRepository constructor(private val retrofitService: RetrofitKakaoAddrService) {
    //카카오 읍면동 주소 가져오기
    suspend fun getAddress(query: String)
     = retrofitService.getAddress(
        "KakaoAK 28b317e5c87a9b1e3db9355030e61fe7",
        "similar",
        query,
     )
}