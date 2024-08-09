package com.tobie.newfinedust.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tobie.newfinedust.BuildConfig
import com.tobie.newfinedust.models.KakaoAddress
import com.tobie.newfinedust.models.SubAddress
import com.tobie.newfinedust.utils.Constants
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.Objects

interface RetrofitKakaoAddrService {

    companion object {
        private var retrofitAddrService: RetrofitKakaoAddrService? = null

        var gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        fun getInstance() : RetrofitKakaoAddrService {
            if (retrofitAddrService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.KAKAO_API_BASE_URl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                retrofitAddrService = retrofit.create(RetrofitKakaoAddrService::class.java)
            }
            return retrofitAddrService!!
        }
    }

    /**
     * Kakao 읍/면/동 주소 API
     */
    @GET("/v2/local/search/address.json")
    suspend fun getAddress(
        @Header("Authorization") authorization: String,
        @Query("analyze_type") analyzeType: String,
        @Query("query") query: String,
    ): Response<KakaoAddress>
}