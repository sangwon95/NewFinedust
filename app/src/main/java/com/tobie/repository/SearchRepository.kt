package com.tobie.repository

import com.tobie.newfinedust.models.SubAddressRequestData
import com.tobie.newfinedust.service.RetrofitAddrService

class SearchRepository constructor(private val retrofitService: RetrofitAddrService) {

    //읍면동 주소 가져오기
    suspend fun getSubAddress(subAddressRequestData: SubAddressRequestData)
     = retrofitService.getSubAddress(
        subAddressRequestData.request,
        subAddressRequestData.format,
        subAddressRequestData.data,
        subAddressRequestData.attrfilter,
        subAddressRequestData.columns,
        subAddressRequestData.geometry,
        subAddressRequestData.attribute,
        subAddressRequestData.crs,
        subAddressRequestData.domain,
     )

}