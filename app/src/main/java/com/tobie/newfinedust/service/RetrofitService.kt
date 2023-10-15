package com.tobie.newfinedust.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tobie.newfinedust.BuildConfig
import com.tobie.newfinedust.models.Dust
import com.tobie.newfinedust.models.Station
import com.tobie.newfinedust.models.Tmxy
import com.tobie.newfinedust.utils.Constants
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    companion object {
        private var retrofitService: RetrofitService? = null

        var gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        fun getInstance() : RetrofitService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.API_BASE_URl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }

    }

    /**
     * 관측정소별 실시간 측정정보 조회
     */
    @GET("/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?serviceKey=${BuildConfig.API_KEY}")
    suspend fun getFineDust(
        @Query("stationName") stationName: String,
        @Query("returnType") returnType: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int,
        @Query("dataTerm") dataTerm: String,
        @Query("ver") ver: String,
    ): Response<Dust>


    /**
     * 주소 검색 기반 tmx, tmy 값 구하기
     * umdName: ex) 대전광역시 유성구 장동 -> 전부
     */
    @GET("/B552584/MsrstnInfoInqireSvc/getTMStdrCrdnt?serviceKey=${BuildConfig.API_KEY}")
    suspend fun getTmxy(
        @Query("returnType") returnType: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int,
        @Query("umdName") umdName: String,
    ): Response<Tmxy>


    /**
     * 근접측정소 목록 조회
     */
    @GET("/B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList?serviceKey=${BuildConfig.API_KEY}")
    suspend fun getStation(
        @Query("returnType") returnType: String,
        @Query("tmX") tmX: String,
        @Query("tmY") tmY: String,
    ): Response<Station>

    /**
     * 미세먼지 예보정보 조회
     */
    @GET("/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?serviceKey=${BuildConfig.API_KEY}")
    suspend fun getForecast(
        @Query("returnType") returnType: String,
        @Query("searchDate") searchDate: String,
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int
    ): Response<Station> //여기서부터 수정해야됨


//    @GET("posts/1")
//    fun getStudent(@Query("school_id") schoolId: Int,
//                   @Query("grade") grade: Int,
//                   @Query("classroom") classroom: Int): Call<ExampleResponse>
//
//
//    //POST 예제
//    @FormUrlEncoded
//    @POST("posts")
//    fun getContactsObject(@Field("idx") idx: String): Call<JsonObject>
//
//    @FormUrlEncoded
//    @POST("add_post_2.php")
//    suspend fun postBoard(
//        @Field("title") title: String,
//        @Field("contents") contents: String
//    ): Response<JsonObject>

}