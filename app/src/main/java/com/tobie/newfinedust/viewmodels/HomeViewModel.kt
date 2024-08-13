package com.tobie.newfinedust.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
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
import com.tobie.newfinedust.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {
    companion object {
        const val TAG = "HomeViewModel - 로그"
    }

    // 홈 리포지토리 인스턴스 생성
    private val repository: HomeRepository = HomeRepository(RetrofitAirService.getInstance())

    // LiveData 객체 초기화 - 먼지 데이터, TM 좌표, 첫 번째 주소 등
    private val _dustCombinedData = MutableLiveData<DustCombinedData>()
    private val _tmCoordinates = MutableLiveData<TmCoordinates>()
    private val _firstAddress = MutableLiveData<TmCoordinates>()

    // 로딩 및 에러 메시지를 관리하기 위한 LiveData 객체 초기화
    private val loading = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()
    var currentTmCoordinates: TmCoordinates? = null

    // 외부에서 접근할 수 있는 LiveData Getter
    val dustCombinedData: MutableLiveData<DustCombinedData> get() = _dustCombinedData
    val tmCoordinates: MutableLiveData<TmCoordinates> get() = _tmCoordinates
    val firstAddress: MutableLiveData<TmCoordinates> get() = _firstAddress

    // 현재 날짜와 시간을 저장할 변수
    private lateinit var currentDateTime: String

    // 현재 날짜와 시간을 초기화하는 함수
    private fun initCurrentDateTime() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        currentDateTime = dateFormat.format(Date())
    }

    /**
     * Room 데이터베이스에서 첫 번째 지역 정보를 가져오는 함수
     * 위치 퍼미션 거절 후 localdb에 저장된 첫번째 주소를 가져온다.
     */
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

    /**
     * 새 지역 정보를 Room 데이터베이스에 저장
     */
    private fun insertRegion(tmPoint: TmCoordinates, roomDB: RegionDatabase) {
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

    /**
     * 통합된 미세먼지 데이터 및 예보 데이터 조회
     */
    fun getIntegrated(tmCoordinates: TmCoordinates) {
        currentTmCoordinates = tmCoordinates
        Log.d(TAG, "getIntegrated - tmCoordinates: $currentTmCoordinates")
        initCurrentDateTime()

        viewModelScope.launch {
            try {
                // 측정소 정보를 가져오기 위한 API 호출
                val stationResponse = repository.getStation(
                    StationData(
                        tmX = tmCoordinates.tmX.toString(),
                        tmY = tmCoordinates.tmY.toString()
                    )
                )
                val stationName =
                    stationResponse.body()?.response?.body?.stationItems?.get(0)?.stationName
                        ?: throw Exception("Station not found")

                // 미세먼지 및 예보 데이터를 가져오기 위한 API 호출
                val dustResponse = repository.getFineDust(FineDustRequestData(stationName = stationName))
                val forecastResponse = repository.getForecast(currentDateTime)

                // 결과를 결합하여 LiveData에 설정
                val dustItem = dustResponse.body()?.response?.dustBody?.dustItem?.get(0)
                    ?: throw Exception("Dust data not found")
                val forecastItem = forecastResponse.body()?.response?.forecastBody?.forecastItem?.get(0)
                        ?: throw Exception("Forecast data not found")

                _dustCombinedData.postValue(
                    DustCombinedData(
                        dustItem,
                        forecastItem,
                        tmCoordinates.address
                    )
                )
            } catch (e: Exception) {
                handleError("Error in getIntegrated: ${e.message}")
            }
        }
    }

    /**
     * ActivityResultLauncher를 사용하여 즐겨찾기, 주소 추가하기, 주소 검색하기 결과처리
     */
    fun handleActivityResult(result: ActivityResult, isSearch: Boolean, roomDB: RegionDatabase) {
        Log.d(TAG, "handleActivityResult: $result")
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val address = data?.getStringExtra("address") ?: "대한민국"
            val tmX = data?.getDoubleExtra("tmX", 0.0) ?: 0.0
            val tmY = data?.getDoubleExtra("tmY", 0.0) ?: 0.0
            val longitude = data?.getStringExtra("x")?.toDoubleOrNull()
            val latitude = data?.getStringExtra("y")?.toDoubleOrNull()

            val tmPoint = if (isSearch) {
                Etc.convertWGS84ToTM(latitude!!, longitude!!, address)
            } else {
                TmCoordinates(tmX, tmY, address)
            }

            Log.d(TAG, "address: $address / tmX: $tmX / tmY: $tmY")
            if (isSearch) {
                insertRegion(tmPoint, roomDB)
                getIntegrated(tmPoint)
            } else {
                getIntegrated(tmPoint)
            }
        } else {
            Log.d(TAG, "RESULT_CANCELED")
        }
    }

    /**
     * 현재 위치를 가져오는 함수.
     * 위치 권한을 확인하고, 권한이 있는 경우 위치 정보를 가져옴.
     */
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

    /**
     * 위치 권한 확인
     */
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

    /**
     * 주어진 좌표에서 주소를 가져오는 함수
     */
    private fun getAddressFromLocation(geocoder: Geocoder, latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "getAddressFromLocation: $latitude, $longitude")
            try {
                // Geocoder를 사용하여 좌표에서 주소 목록을 가져옴
                val addressList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 2) ?: emptyList()
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 2) ?: emptyList()
                }

                withContext(Dispatchers.Main) {
                    _tmCoordinates.value =
                        Etc.convertWGS84ToTM(latitude, longitude, Etc.translationAddress(addressList))
                }
            } catch (e: Exception) {
                handleError("Error getting address: ${e.message}")
            }
        }
    }

    /**
     * 에러 발생 시 처리하는 함수
     */
    private fun handleError(message: String) {
        Log.e(TAG, message)
        errorMessage.postValue(message)
        loading.postValue(false)
    }
}
