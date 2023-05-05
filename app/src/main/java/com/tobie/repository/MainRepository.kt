package com.tobie.repository

import com.tobie.newfinedust.models.FineDustRequestData
import com.tobie.newfinedust.models.StationData
import com.tobie.newfinedust.models.TmxyData
import com.tobie.newfinedust.service.RetrofitService

class MainRepository constructor(private val retrofitService: RetrofitService) {

    suspend fun getFineDust(fineDustRequestData: FineDustRequestData)
            = retrofitService.getFineDust(
                fineDustRequestData.stationName,
                fineDustRequestData.returnType,
                fineDustRequestData.numOfRows,
                fineDustRequestData.pageNo,
                fineDustRequestData.dataTerm,
                fineDustRequestData.ver
            )

    suspend fun getTmxy(tmXY: TmxyData)
            = retrofitService.getTmxy(tmXY.returnType, tmXY.numOfRows, tmXY.pageNo, tmXY.umdName)

    suspend fun getStation(stationData: StationData) = retrofitService.getStation(
        stationData.returnType,
        stationData.tmX,
        stationData.tmY,
       )

}