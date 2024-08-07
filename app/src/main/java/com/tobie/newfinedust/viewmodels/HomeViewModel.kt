package com.tobie.newfinedust.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.tobie.newfinedust.activity.HomeActivity
import com.tobie.newfinedust.models.*
import com.tobie.newfinedust.room.RegionDatabase
import com.tobie.newfinedust.room.RegionEntity
import com.tobie.newfinedust.service.RetrofitAirService
import com.tobie.newfinedust.utils.Etc
import com.tobie.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kr.hyosang.coordinate.*

class HomeViewModel : ViewModel() {
    private val _dustCombinedData = MutableLiveData<DustCombinedData>()
    private val _tmCoordinates = MutableLiveData<TmCoordinates>()
    private val _firstAddress = MutableLiveData<String>()
    private val _addressList = MutableLiveData<List<String>>()

    val loading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    var currentAddress: String? = null

    val addressList: MutableLiveData<List<String>> = _addressList
    val dustCombinedData: MutableLiveData<DustCombinedData> get() = _dustCombinedData
    val tmCoordinates: MutableLiveData<TmCoordinates> get() = _tmCoordinates
    val firstAddress: MutableLiveData<String> get() = _firstAddress

    private lateinit var currentDateTime: String
    private val repository: MainRepository = MainRepository(RetrofitAirService.getInstance())

    companion object {
        const val TAG = "HomeViewModel - 로그"
    }

    fun addAddress(address: String) {
        val currentList = _addressList.value?.toMutableList() ?: mutableListOf()
        if (!currentList.contains(address)) {
            currentList.add(address)
            _addressList.postValue(currentList)
        }
    }

    fun updateAddressList(newList: List<String>) {
        _addressList.postValue(newList)
    }

   private fun initCurrentDateTime() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        currentDateTime = dateFormat.format(Date())
    }


    fun getFirstRegion(roomDB: RegionDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            val regionDBList = roomDB.regionDAO().getAll()
            if (regionDBList.isNotEmpty()) {
                firstAddress.postValue(regionDBList[0].region)
            } else {
                Log.d(TAG, "getAllRegion: RoomDB에 저장된 주소가 없습니다.")
            }
        }
    }

    fun insertRegion(address: String, roomDB: RegionDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val region = RegionEntity(null, address)
                roomDB.regionDAO().insert(region)
                Log.d(TAG, "insertRegion: [ $address ] RoomDB에 저장")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting region: ${e.message}")
            }
        }
    }

    fun getIntegrated(tmCoordinates: TmCoordinates) {
        currentAddress = tmCoordinates.address
        Log.d(TAG, "getIntegrated: $currentAddress")
        initCurrentDateTime()

        viewModelScope.launch {
            try {
//                val tmxyResponse = repository.getTmxy(TmxyData(umdName = address))
//                val tmxyItems = tmxyResponse.body()?.response?.body?.tmxyItems ?: throw Exception("TmxyItems not found")

                val stationResponse = repository.getStation(StationData(tmX = tmCoordinates.tmX.toString(), tmY = tmCoordinates.tmY.toString()))
                val stationName = stationResponse.body()?.response?.body?.stationItems?.get(0)?.stationName ?: throw Exception("Station not found")

                val dustResponse = repository.getFineDust(FineDustRequestData(stationName = stationName))
                val forecastResponse = repository.getForecast(currentDateTime)

                val dustItem = dustResponse.body()?.response?.dustBody?.dustItem?.get(0) ?: throw Exception("Dust data not found")
                val forecastItem = forecastResponse.body()?.response?.forecastBody?.forecastItem?.get(0) ?: throw Exception("Forecast data not found")

                _dustCombinedData.postValue(DustCombinedData(dustItem, forecastItem, currentAddress))
            } catch (e: Exception) {
                handleError("Error in getIntegrated: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation(context: Context, activity: HomeActivity) {
        if (!checkLocationPermission(context, activity)) return

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val geocoder = Geocoder(context, Locale.getDefault())

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                getAddressFromLocation(geocoder, it.latitude, it.longitude)
            } ?: Log.d(TAG, "location null!")
        }
    }

    private fun checkLocationPermission(context: Context, activity: HomeActivity): Boolean {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                HomeActivity.LOCATION_PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    private fun getAddressFromLocation(geocoder: Geocoder, latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "getAddressFromLocation: $latitude, $longitude")
            try {
                // CoordPoint 객체 생성
                val tmPt = CoordPoint(longitude, latitude)
                val wgsPt = TransCoord.getTransCoord(
                    tmPt,
                    TransCoord.COORD_TYPE_WGS84,
                    TransCoord.COORD_TYPE_TM
                )

                Log.i("wgscoor", "tmx: $wgsPt.x tmy: $wgsPt.y")
                val addressList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 2) ?: emptyList()
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 2) ?: emptyList()
                }
                withContext(Dispatchers.Main) {
                    _tmCoordinates.value =
                        TmCoordinates(wgsPt.x, wgsPt.y, Etc.translationAddress(addressList))
                }


            } catch (e: Exception) {
                handleError("Error getting address: ${e.message}")
            }
        }
    }

    private fun handleError(message: String) {
        Log.e(TAG, message)
        errorMessage.postValue(message)
        loading.postValue(false)
    }
}