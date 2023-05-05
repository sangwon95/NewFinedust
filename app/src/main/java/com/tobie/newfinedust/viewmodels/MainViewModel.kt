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
import com.tobie.repository.MainRepository
import kotlinx.coroutines.*
import java.util.*

/*
1."Room Data" 조회를 통해서 저장된 지역 데이터가 있는 확인한다.
2. 리스트 갯수 만큼 에어코리아 API 호출 하여 미세먼지 데이터를 가져온다.
 */
class MainViewModel(private val repository: MainRepository) : ViewModel() {

    companion object {
        const val TAG = "MainViewModel - 로그"
    }

    init {
        Log.d(TAG, "생성자 호출");
    }

    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }

    private val _dustValue = MutableLiveData<DustItem>()
    private val _tmxyValue = MutableLiveData<List<TmxyItem>>()
    private val _stationValue = MutableLiveData<String>()
    private val _address = MutableLiveData<String>()

    val loading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    val dustValue: MutableLiveData<DustItem> get() = _dustValue
    val tmxyValue: MutableLiveData<List<TmxyItem>> get() = _tmxyValue
    val stationValue: MutableLiveData<String> get() = _stationValue
    val address: MutableLiveData<String> get() = _address


    /**
     * 에어코리아 API를 통해서 미세먼지 수치(데이터)를 가져온다.
     */
    fun getFineDust(stationName: String) {
        job = viewModelScope.launch {
            try {
                val fineDustRequestData = FineDustRequestData(stationName= stationName)
                val response = repository.getFineDust(fineDustRequestData)

                withContext(Dispatchers.IO + exceptionHandler) {
                    if (response.isSuccessful) {
                        _dustValue.postValue(response.body()!!.response.dustBody.dustItem!![0])

                        loading.postValue(false)
                    } else {
                        Log.d(TAG+ "Error", response.body().toString())
                        onError("Error : ${response.message()} ")
                    }
                }
            }
            catch (e: Exception) {
                Log.e(TAG+ "Exception Error:", e.toString())
                // Show AlertDialog..
            }
        }
    }


    /**
     * 에어코리아 API를 통해서 Tmx, Tmy 값을 가져온다.
     */
    fun getTmxy(address: String) {
        job = viewModelScope.launch {
            try {
                val response = repository.getTmxy(TmxyData(umdName = address))
                withContext(Dispatchers.IO + exceptionHandler) {
                    if (response.isSuccessful) {
                        _tmxyValue.postValue(response.body()!!.response.body.tmxyItems)
                        loading.postValue(false)
                    } else {
                        Log.d(TAG+ "Error", response.body().toString())
                        onError("Error : ${response.message()} ")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG+ "Exception Error:", e.toString())
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
                val response = repository.getStation(StationData(tmX = tmX, tmY = tmY))
                withContext(Dispatchers.IO + exceptionHandler) {
                    if (response.isSuccessful) {
                        _stationValue.postValue(response.body()!!.response.body.stationItems[0].stationName)
                        loading.postValue(false)
                    } else {
                        Log.d(TAG+ "Error", response.body().toString())
                        onError("Error : ${response.message()} ")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG+ "Exception Error:", e.toString())
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
                            val address = addressList[0].getAddressLine(0).split(" ")
                            _address.postValue("${address[1]} ${address[2]} ${address[3]}")
                        }
                    }

                    else {
                        Log.i(TAG, "NOT Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU")
                        val addressList = geocoder.getFromLocation(location.latitude, location.longitude,1)
                        if (addressList != null) {
                            val address = addressList[0].getAddressLine(0).split(" ")
                            _address.postValue("${address[1]} ${address[2]} ${address[3]}")
                        }
                        else {
                            Log.i(TAG, "address null!")
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