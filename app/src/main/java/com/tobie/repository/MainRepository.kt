package com.tobie.repository

import com.tobie.newfinedust.models.FineDustRequestData
import com.tobie.newfinedust.models.StationData
import com.tobie.newfinedust.models.TmxyData
import com.tobie.newfinedust.service.RetrofitAirService

class MainRepository constructor(private val retrofitService: RetrofitAirService) {

    suspend fun getFineDust(fineDustRequestData: FineDustRequestData)
            = retrofitService.getFineDust(
                fineDustRequestData.stationName,
                fineDustRequestData.returnType,
                fineDustRequestData.numOfRows,
                fineDustRequestData.pageNo,
                fineDustRequestData.dataTerm,
                fineDustRequestData.ver
            )

    /**
     * 미세먼지 예보정보 가져오기
     */
    suspend fun getForecast(searchDate: String)
    = retrofitService.getForecast(
        "json",
        searchDate,
        1,
        1
    )

    suspend fun getTmxy(tmXYData: TmxyData)
            = retrofitService.getTmxy(tmXYData.returnType, tmXYData.numOfRows, tmXYData.pageNo, tmXYData.umdName)

    suspend fun getStation(stationData: StationData) = retrofitService.getStation(
        stationData.returnType,
        stationData.tmX,
        stationData.tmY,
       )

}