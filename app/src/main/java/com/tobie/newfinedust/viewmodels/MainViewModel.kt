package com.tobie.newfinedust.viewmodels

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.tobie.newfinedust.MainActivity
import com.tobie.newfinedust.models.*
import com.tobie.newfinedust.room.RegionDatabase
import com.tobie.newfinedust.utils.Etc
import com.tobie.repository.MainRepository
import kotlinx.coroutines.*
import java.util.*
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

/*
1."Room Data" 조회를 통해서 저장된 지역 데이터가 있는 확인한다.
2. 리스트 갯수 만큼 에어코리아 API 호출 하여 미세먼지 데이터를 가져온다.
 */
class MainViewModel(private val repository: MainRepository) : ViewModel() {

    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }

    private val _dustCombinedData = MutableLiveData<DustCombinedData>()
    private val _tmxyValue = MutableLiveData<List<TmxyItem>>()
    private val _stationValue = MutableLiveData<String>()
    private val _address = MutableLiveData<String>()

    val loading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    val dustCombinedData: MutableLiveData<DustCombinedData> get() = _dustCombinedData
    val tmxyValue: MutableLiveData<List<TmxyItem>> get() = _tmxyValue
    val stationValue: MutableLiveData<String> get() = _stationValue
    val address: MutableLiveData<String> get() = _address

    private lateinit var tempAddress: String

    companion object {
        const val TAG = "MainViewModel - 로그"
    }

    init {
        Log.d(TAG, "생성자 호출");
    }

    fun getIntegrated(address: String) {
        viewModelScope.launch {
            Log.d(TAG, "getFineDust $address start")
            try {
                val tmxyResponse = repository.getTmxy(TmxyData(umdName = address))

                if (tmxyResponse.isSuccessful) {
                    val tmxyItems = tmxyResponse.body()!!.response.body.tmxyItems
                    val stationResponse = repository.getStation(StationData(tmX = tmxyItems[0].tmX, tmY = tmxyItems[0].tmY))

                    if (stationResponse.isSuccessful) {
                        val stationItems = stationResponse.body()!!.response.body.stationItems
                        val stationName = stationItems[0].stationName

                        val dustRequestData = FineDustRequestData(stationName = stationName)
                        val dustResponse = repository.getFineDust(dustRequestData) // 미세먼지 정보
                        val forecastResponse = repository.getForecast("2024-01-29") // 예보정보

                        if (dustResponse.isSuccessful && forecastResponse.isSuccessful) {
                            val dustItem = dustResponse.body()!!.response.dustBody.dustItem!![0]
                            val forecastItem = forecastResponse.body()!!.response.forecastBody.forecastItem!![0]

                            val dustCombinedData = DustCombinedData(
                                dustItem = dustItem,
                                forecastItem = forecastItem,
                                address = address
                            )
                            _dustCombinedData.postValue(dustCombinedData)
                            Log.d(TAG, "getFineDust $address end")

                        } else {
                            handleApiError("미세먼지 정보, 예보를 불러오지 못했습니다.")
                        }
                    } else {
                        handleApiError("StationData ${stationResponse.body().toString()}")
                    }

                } else {
                    handleApiError("getTmxy ${tmxyResponse.body().toString()}")
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    private fun handleApiError(errorMessage: String) {
        Log.d(TAG + "Error", errorMessage)
        onError("Error: $errorMessage")
    }

    private fun handleException(exception: Exception) {
        Log.e(TAG, "Exception during getIntegrated: $exception")
        onError("Error: ${exception.message}")
    }


    /**
     * 에어코리아 API를 통해서 미세먼지 수치(데이터)를 가져온다.
     */
    fun getFineDust(stationName: String) {
        Log.d(TAG,"getFineDust start")
        job = viewModelScope.launch {
            try {
                val fineDustRequestData = FineDustRequestData(stationName = stationName)
                val responseDust = repository.getFineDust(fineDustRequestData) // 미세먼지 정보
                val responseForecast = repository.getForecast("2024-01-26") // 예보정보

                val isDustCombinedResponse = responseDust.isSuccessful && responseForecast.isSuccessful

                withContext(Dispatchers.IO + exceptionHandler) {
                    if (isDustCombinedResponse) {
                        val dustCombinedData = DustCombinedData(
                            dustItem = responseDust.body()!!.response.dustBody.dustItem!![0],
                            forecastItem = responseForecast.body()!!.response.forecastBody.forecastItem!![0],
                            address = tempAddress
                        )
                        _dustCombinedData.postValue(dustCombinedData)
                        Log.d(TAG,"getFineDust end")

                        loading.postValue(false)
                    } else {
                        Log.d(TAG + "Error", "미세먼지 정보, 예보를 불러오지 못했습니다.")
                        onError("미세먼지 정보, 예보를 불러오지 못했습니다.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG + "getFineDust Exception Error:", e.toString())
                // Show AlertDialog..
            }
        }
    }


    /**
     * 에어코리아 API를 통해서 Tmx, Tmy 값을 가져온다.
     */
    fun getTmxy(address: String) {
        tempAddress = address
        job = viewModelScope.launch {
            try {
                Log.d(TAG,"getTmxy start")
                val response = repository.getTmxy(TmxyData(umdName = address))

                withContext(Dispatchers.IO + exceptionHandler) {
                    if (response.isSuccessful) {
                        /**
                         * 테스트
                         */
                        val station = response.body()!!.response.body.tmxyItems
                        getStationName(station[0].tmX, station[0].tmY)
                        Log.d(TAG,"getTmxy end")

                        //_tmxyValue.postValue(response.body()!!.response.body.tmxyItems)
                        loading.postValue(false)
                    } else {
                        Log.d(TAG+ "Error", "getTmxy${response.body().toString()}")
                        onError("Error : ${response.message()} ")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG+ "getTmxy Exception Error:", e.toString())
                // Show AlertDialog..
            }
        }
    }


    /**
     * 에어코리아 API를 통해서 근접 측정소 목록값을 가져온다.
     */
    fun getStationName(tmX: String, tmY: String) {
        job = viewModelScope.launch {
            try {
                Log.d(TAG,"getStationName start")

                val response = repository.getStation(StationData(tmX = tmX, tmY = tmY))

                withContext(Dispatchers.IO + exceptionHandler) {
                    if (response.isSuccessful) {
                        /**
                         *  테스트중
                         */
                        Log.d(TAG,"getStationName: ${response.body()!!.response.body.stationItems.size}")
                        val stationName = response.body()!!.response.body.stationItems[0].stationName
                        Log.d(TAG,"getStationName $stationName end")
                        getFineDust(stationName = stationName)

                        // _stationValue.postValue(response.body()!!.response.body.stationItems[0].stationName)
                        loading.postValue(false)
                    } else {
                        Log.d(TAG+ "Error!!", response.body().toString())
                        onError("Error!! : ${response.message()} ")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG+ "getStationName Exception Error:getStationName", e.toString())
                // Show AlertDialog..
            }
        }
    }


    /**
     * 자신의 위치 정보를 가져와 주소명을 가져온다.
     */
    fun getLocation(context: Context, activity: MainActivity) {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val geocoder = Geocoder(context, Locale.getDefault())

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MainActivity.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if(location != null) {
                    Log.i(TAG, "${location.latitude} / ${location.longitude}")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Log.i(TAG, "Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU")
                        geocoder.getFromLocation(location.latitude, location.longitude,1) { addressList ->
                            _address.postValue(Etc.translationAddress(addressList))
                        }
                    }
                    // 36.6137, 127.4364,
                    else {
                        Log.i(TAG, "NOT Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU")
                        val addressList = geocoder.getFromLocation(location.latitude, location.longitude,3)

                        if (addressList != null) {
                            _address.postValue(Etc.translationAddress(addressList))
                        }
                        else {
                            Log.e(TAG, "가져온 위도, 경도로 주소값 못 가져옴")
                        }
                    }
                }
        }
    }



    /**
     * 1. SQLite를 통해서 즐겨찾기 추가된 데이터가 있는지 확인한다.
     * 2. 첫번쨰 지역은 현재위치를 가져와 데이터를 보여준다.
     */
    fun getRegionFromRoom(){
    }


    private fun onError(message: String) {
        errorMessage.postValue(message)
        loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
