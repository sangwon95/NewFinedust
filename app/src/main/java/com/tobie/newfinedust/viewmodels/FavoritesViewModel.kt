package com.tobie.newfinedust.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tobie.newfinedust.activity.HomeActivity
import com.tobie.newfinedust.models.TmCoordinates
import com.tobie.newfinedust.room.RegionDatabase
import com.tobie.newfinedust.room.RegionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class FavoritesViewModel: ViewModel() {
    companion object {
        const val TAG: String = "FavoritesViewModel - 로그"
    }

    private val _addressLiveData = MutableLiveData<ArrayList<TmCoordinates>>()
    val addressLiveData: MutableLiveData<ArrayList<TmCoordinates>> get() = _addressLiveData

    var regionDBList: List<RegionEntity> = ArrayList()
    var tmCoordinatesList = ArrayList<TmCoordinates>()

    /**
     * RoomDB에서 가져온 주소를 저장할 리스트
     */
    @SuppressLint("StaticFieldLeak")
    fun getAllRegion(roomDB: RegionDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            regionDBList = roomDB.regionDAO().getAll()

            if(regionDBList.isEmpty()){
                Log.d(HomeActivity.TAG, "getAllRegion: RoomDB에 저장된 주소가 없습니다.")
            }

            for (value in regionDBList) {
                tmCoordinatesList = regionDBList.map {
                    TmCoordinates(it.tmX.toDouble(), it.tmY.toDouble(), it.region)
                } as ArrayList<TmCoordinates>
                Log.d(TAG, "getAllRegion: RoomDB에서 가져온 주소: ${value.region}")
            }
            addressLiveData.postValue(tmCoordinatesList)
        }.start()
    }

    /**
     * RoomDB 주소 삭제
     */
    fun deleteAddress(tmCoordinates: TmCoordinates, roomDB: RegionDatabase) {
        val addressToDelete = regionDBList.find { it.region == tmCoordinates.address }

        if(addressToDelete == null){
            Log.d(TAG, "deleteAddress: 삭제할 주소가 없습니다.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            roomDB.regionDAO().delete(addressToDelete)
            Log.d(TAG, "deleteAddress: RoomDB 주소 삭제")
        }
    }
}