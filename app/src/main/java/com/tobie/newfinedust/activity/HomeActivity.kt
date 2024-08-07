package com.tobie.newfinedust.activity

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tobie.newfinedust.FavoritesActivity
import com.tobie.newfinedust.R
import com.tobie.newfinedust.SearchActivity
import com.tobie.newfinedust.adapter.RemainAdapter
import com.tobie.newfinedust.databinding.ActivityHomeBinding
import com.tobie.newfinedust.models.DustCombinedData
import com.tobie.newfinedust.models.GpsAddrssManager
import com.tobie.newfinedust.models.Remain
import com.tobie.newfinedust.room.RegionDatabase
import com.tobie.newfinedust.service.Permission
import com.tobie.newfinedust.utils.Etc
import com.tobie.newfinedust.viewmodels.HomeViewModel
import kotlin.math.log

/**
 * 홈 화면 액티비티
 */
class HomeActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    companion object {
        const val TAG = "HomeAcitivity - 로그"
        const val LOCATION_PERMISSION_REQUEST_CODE = 100 // 위치 권한 요청 코드
    }
    private lateinit var splashScreen: SplashScreen
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    private var isLocationPermissionGranted: Boolean = false // 변수명 예시: isLocationPermissionGranted
    private lateinit var roomDB: RegionDatabase //Room Database


    // 주소 리스트 수정 StartActivityForResult
    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedAddress = result.data?.getStringExtra("selectedAddress")

                if (selectedAddress != null) {
                    Log.d(TAG, "selectedAddress: $selectedAddress")
                    //viewModel.currentAddress = selectedAddress
                    viewModel.getIntegrated(selectedAddress) // 가져온 주소의 미세먼지 정보 가져오기
                }
            } else {
                Log.d(TAG, "RESULT_CANCELED")
            }
        }

    private val addLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedAddress = result.data?.getStringExtra("selectedAddress")
                if (selectedAddress != null) {
                    Log.d(TAG, "selectedAddress: $selectedAddress")

                    viewModel.addAddress(selectedAddress)
                    viewModel.insertRegion(selectedAddress, roomDB) // RoomDB 저장
                    viewModel.getIntegrated(selectedAddress) // 가져온 주소의 미세먼지 정보 가져오기
                }
            } else {
                Log.d(TAG, "RESULT_CANCELED")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen = installSplashScreen()

        binding = ActivityHomeBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        Permission(this,{ // 위치 권한이 허용되었을 때 수행할 작업
            isLocationPermissionGranted = true
            viewModel.getLocation(this, this)
        },{ // 계속 위치권한 거절했을 때 수행할 작업
            handleLocationPermissionDenied()
        }).checkPermission()

        roomDB = RegionDatabase.getInstance(this)!! //Room Database 초기화
        viewModel.getFirstRegion(roomDB)

        registerObservers()
        binding.swipeLayout.setOnRefreshListener(this)
    }

    override fun onResume() {
        super.onResume()
        binding.addImageView.setOnClickListener { // 지역 추가하기
            Intent(this, SearchActivity::class.java)
                .putExtra("impossibleBack", true).apply {
                    addLauncher.launch(this)
                }
        }

        binding.editImageView.setOnClickListener { // 즐겨찾기 화면으로 이동
            editLauncher.launch(Intent(this, FavoritesActivity::class.java))
        }
    }

//    override fun onRestart() {
//        super.onRestart()
//        Log.d(TAG, "onRestart() 호출됨");
//        onRefresh()
//    }

    /**
     * 옵저버 등록
     */
    private fun registerObservers(){
        // 현재 위치기반 주소 수신
        viewModel.tmCoordinates.observe(this) { tmCoordinates ->
            Log.d(TAG, "가져온 GPS 좌표 및 주소: $tmCoordinates")

            GpsAddrssManager.set(tmCoordinates.tmX, tmCoordinates.tmY, tmCoordinates.address)
            viewModel.getIntegrated(tmCoordinates)
        }

        // 미세먼지 데이터 수신
        viewModel.dustCombinedData.observe(this) {
            Log.i(TAG, "화면에 보여줄 미세먼지 데이터 결과:$it")
            setHomeView(it)
        }

        // RoomDB에서 가져온 첫번째 주소
//        viewModel.firstAddress.observe(this){
//            Log.i(TAG, "RoomDB에서 가져온 첫번째 주소: $it")
//            if(it != null){
//                viewModel.getIntegrated(it)
//            }
//        }

    }

    private fun setHomeView(dustData: DustCombinedData) {
        binding.loadingLayout.visibility = View.GONE
        binding.swipeLayout.isRefreshing = false //새로 고침 완료

        val pm10Value = dustData.dustItem.pm10Value?.toIntOrNull() ?: 0
        val pm25Value = dustData.dustItem.pm25Value?.toIntOrNull() ?: 0
        val dateTime = dustData.dustItem.dataTime?: "-"
        val txtState = Etc.calculateAtmosphericEnvironment(pm10Value, pm25Value)

        window.apply {
            statusBarColor = ContextCompat.getColor(this@HomeActivity, Etc.getTextForStatusBarColor(txtState))
        }
        binding.mainFrame.setBackgroundResource(Etc.getTextForStatus(txtState)) // background color
        binding.mainImageView.setImageDrawable(ContextCompat.getDrawable(this, Etc.getTextForStatusIconImage(txtState)))
        binding.pm10TextView.text = this.getString(R.string.pm_unit, "미세먼지", pm10Value.toString())
        binding.pm25TextView.text = this.getString(R.string.pm_unit, "초 미세먼지", pm25Value.toString())
        binding.dateTimeTextView.text = dateTime
        binding.addressTextView.text = dustData.address
        binding.stateTextView.text = txtState

        val no2Value = dustData.dustItem.no2Value ?: "-"
        val o3Value = dustData.dustItem.o3Value ?: "-"
        val coValue = dustData.dustItem.coValue ?: "-"
        val so2Value = dustData.dustItem.so2Value ?: "-"

        val remainData: ArrayList<Remain> = arrayListOf(
            Remain("이산화 질소", Etc.getNo2ValueAirQualityLevel(no2Value), "$no2Value ppm", Etc.getTextForStatusIconImage(
                Etc.getNo2ValueAirQualityLevel(no2Value)
            )
            ),
            Remain("오존", Etc.getO3GradeAirQualityLevel(o3Value), "$o3Value ppm", Etc.getTextForStatusIconImage(
                Etc.getO3GradeAirQualityLevel(o3Value)
            )
            ),
            Remain("일산화탄소", Etc.getCoValueAirQualityLevel(coValue), "$coValue ppm", Etc.getTextForStatusIconImage(
                Etc.getCoValueAirQualityLevel(coValue)
            )
            ),
            Remain("이황산가스", Etc.getSo2ValueAirQualityLevel(so2Value), "$so2Value ppm", Etc.getTextForStatusIconImage(
                Etc.getSo2ValueAirQualityLevel(so2Value)
            )
            ),
        )
        Log.d(TAG, "remainData: $remainData")
        val remainAdapter = RemainAdapter(remainData)
        binding.recyclerView.adapter = remainAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) // 가로 정렬


        // 미세먼지 예보 정보
        val date = dustData.forecastItem.informData
        val informCause = dustData.forecastItem.informCause
        val informOverall = dustData.forecastItem.informOverall
        binding.forecastDateTextView.text = date
        binding.forecastContentTextView.text = this.getString(R.string.forecast_unit, informCause, informOverall)
    }

    /**
     * 퍼미션 요청 결과 처리
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                val resultCode = grantResults.firstOrNull() ?: PackageManager.PERMISSION_DENIED
                if (resultCode == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "PackageManager.PERMISSION_GRANTED 승인됨")
                    isLocationPermissionGranted = true
                    viewModel.getLocation(this, this)
                } else {
                    handleLocationPermissionDenied()
                }
                return
            }
        }
    }

    /**
     *  위치 권한이 거부되었을 때 수행할 작업
     *  즐겨찾기 리스트 추가 하여 앱 진행 할 수 있게한다.
     */
    private fun handleLocationPermissionDenied() {
        Log.i(TAG, "위치 권한이 거부되었습니다.")
        viewModel.firstAddress.value?.let {
            Log.i(TAG, "첫번째 주소가 있습니다.! / $it")
            viewModel.getIntegrated(it)
        } ?: run {
            Log.i(TAG, "첫번째 주소가 없습니다.")
                Intent(this, SearchActivity::class.java)
                    .putExtra("impossibleBack", false).apply {
                    addLauncher.launch(this)
                }
        }
    }

    override fun onRefresh() {
        viewModel.currentAddress?.let {
            viewModel.getIntegrated(it)
            Log.d(TAG, "onRefresh currentAddress: $it")
        }
    }
}

