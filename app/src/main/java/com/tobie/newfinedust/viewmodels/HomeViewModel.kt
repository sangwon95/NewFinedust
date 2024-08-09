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
    private val _firstAddress = MutableLiveData<TmCoordinates>()
    private val _addressList = MutableLiveData<List<String>>()

    private val loading = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()
    var currentTmCoordinates: TmCoordinates? = null

    val addressList: MutableLiveData<List<String>> = _addressList
    val dustCombinedData: MutableLiveData<DustCombinedData> get() = _dustCombinedData
    val tmCoordinates: MutableLiveData<TmCoordinates> get() = _tmCoordinates
    val firstAddress: MutableLiveData<TmCoordinates> get() = _firstAddress

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
                val tmCoordinatesList = regionDBList.map {
                    TmCoordinates(it.tmX.toDouble(), it.tmY.toDouble(), it.region)
                } as ArrayList<TmCoordinates>
                firstAddress.postValue(tmCoordinatesList[0])
            } else {
                Log.d(TAG, "getAllRegion: RoomDB에 저장된 주소가 없습니다.")
            }
        }
    }

    fun insertRegion(tmPoint: TmCoordinates, roomDB: RegionDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val region = RegionEntity(null, tmPoint.address, tmPoint.tmX.toString(), tmPoint.tmY.toString())
                roomDB.regionDAO().insert(region)
                Log.d(TAG, "insertRegion: [ $tmPoint.address ] RoomDB에 저장")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting region: ${e.message}")
            }
        }
    }

    fun getIntegrated(tmCoordinates: TmCoordinates) {
        currentTmCoordinates = tmCoordinates
        Log.d(TAG, "getIntegrated: ${currentTmCoordinates?.address} / tmX: ${tmCoordinates.tmX} / tmY: ${tmCoordinates.tmY}")
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

                _dustCombinedData.postValue(DustCombinedData(dustItem, forecastItem, tmCoordinates.address))
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
                val addressList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 2) ?: emptyList()
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 2) ?: emptyList()
                }
                //Etc.translationAddress(addressList)

                withContext(Dispatchers.Main) {
                    _tmCoordinates.value =
                        Etc.convertWGS84ToTM(latitude, longitude, "대전광역시 유성구 송강동")
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