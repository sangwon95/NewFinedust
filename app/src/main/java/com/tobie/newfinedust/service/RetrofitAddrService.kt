package com.tobie.newfinedust.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tobie.newfinedust.BuildConfig
import com.tobie.newfinedust.models.SubAddress
import com.tobie.newfinedust.utils.Constants
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Objects

interface RetrofitAddrService {

    companion object {
        private var retrofitAddrService: RetrofitAddrService? = null

        var gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        fun getInstance() : RetrofitAddrService {
            if (retrofitAddrService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.VWORLD_API_BASE_URl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                retrofitAddrService = retrofit.create(RetrofitAddrService::class.java)
            }
            return retrofitAddrService!!
        }
    }

    /**
     * 읍/면/동 주소 가져오기
     */
    @GET("/req/data?key=${BuildConfig.VWORLD_API_KEY}")
    suspend fun getSubAddress(
        @Query("request") request: String,
        @Query("format") format: String,
        @Query("data") data: String,
        @Query("attrfilter") attrfilter: String,
        @Query("columns") columns: String,
        @Query("geometry") geometry: Boolean,
        @Query("attribute") attribute: Boolean,
        @Query("crs") crs: String,
        @Query("domain") domain: String,
    ): Response<SubAddress>
}