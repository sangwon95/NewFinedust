package com.tobie.newfinedust.activity

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tobie.newfinedust.R
import com.tobie.newfinedust.adapter.RemainAdapter
import com.tobie.newfinedust.databinding.ActivityHomeBinding
import com.tobie.newfinedust.models.DustCombinedData
import com.tobie.newfinedust.models.GpsAddrssManager
import com.tobie.newfinedust.models.Remain
import com.tobie.newfinedust.models.TmCoordinates
import com.tobie.newfinedust.room.RegionDatabase
import com.tobie.newfinedust.service.Permission
import com.tobie.newfinedust.utils.Etc
import com.tobie.newfinedust.viewmodels.HomeViewModel

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

    private lateinit var favoritesAddressLauncher : ActivityResultLauncher<Intent>
    private lateinit var searchAddressLauncher : ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen = installSplashScreen()

        binding = ActivityHomeBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        // 위치 권한이 허용되었을 때 수행할 작업
        Permission(this,{
            isLocationPermissionGranted = true
            viewModel.getLocation(this, this)
        },{
            handleLocationPermissionDenied() // 계속 위치권한 거절했을 때 수행할 작업
        }).checkPermission()

        // Room Database 초기화
        roomDB = RegionDatabase.getInstance(this)!!

        // local db에 저장된 첫번째 주소 조회
        viewModel.getFirstRegion(roomDB)

        // 옵저버 등록
        registerObservers()

        // 즐겨찾기, 주소 추가 ActivityResultLauncher 초기화
        favoritesAddressLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "즐겨찾기 화면에서 돌아옴")
            viewModel.handleActivityResult(result, isSearch = false, roomDB)
        }
        searchAddressLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "주소 추가 화면에서 돌아옴")
            viewModel.handleActivityResult(result, isSearch = true, roomDB)
        }

        // 새로고침 리스너
        binding.swipeLayout.setOnRefreshListener(this)
    }

    override fun onResume() {
        super.onResume()
        binding.addImageView.setOnClickListener { // 지역 추가하기
            Intent(this, SearchActivity::class.java)
                .putExtra("impossibleBack", true).apply {
                    searchAddressLauncher.launch(this)
                }
        }

        binding.favoriteImageView.setOnClickListener { // 즐겨찾기 화면으로 이동
            favoritesAddressLauncher.launch(Intent(this, FavoritesActivity::class.java))
        }
    }

    /**
     * viewModel 옵저버 등록
     */
    private fun registerObservers(){
        // 현재 위치기반 주소 수신
        viewModel.tmCoordinates.observe(this) { tmCoordinates ->
            Log.d(TAG, "가져온 GPS 좌표 및 주소: $tmCoordinates")

            if(tmCoordinates.address == "알 수 없음") {
                binding.loadingMessageTextView.text = "현재 위치하신곳은 서비스를 제공하고 있지 않습니다."
            } else {
                GpsAddrssManager.set(tmCoordinates.tmX, tmCoordinates.tmY, tmCoordinates.address)
                viewModel.getIntegrated(tmCoordinates)
            }

        }

        // 미세먼지 데이터 수신
        viewModel.dustCombinedData.observe(this) {
            Log.i(TAG, "화면에 보여줄 미세먼지 데이터 결과:$it")
            setHomeView(it)
        }
    }


    /**
     * 홈 화면 뷰 설정
     */
    private fun setHomeView(dustData: DustCombinedData) {
       // hideLoading
        binding.loadingLayout.visibility = View.GONE
        binding.swipeLayout.isRefreshing = false

        updateAirQualityInfo(dustData)
        updateBackgroundAndStatusBar(dustData)
        setupRemainDataRecyclerView(dustData)
        updateForecastInfo(dustData)
    }

    /**
     * 미세먼지 정보 업데이트
     */
    private fun updateAirQualityInfo(dustData: DustCombinedData) {
        val pm10Value = dustData.dustItem.pm10Value?.toIntOrNull() ?: 0
        val pm25Value = dustData.dustItem.pm25Value?.toIntOrNull() ?: 0
        val dateTime = dustData.dustItem.dataTime ?: "-"
        val txtState = Etc.calculateAtmosphericEnvironment(pm10Value, pm25Value)

        binding.pm10TextView.text = getString(R.string.pm_unit, "미세먼지", pm10Value.toString())
        binding.pm25TextView.text = getString(R.string.pm_unit, "초 미세먼지", pm25Value.toString())
        binding.dateTimeTextView.text = dateTime
        binding.addressTextView.text = dustData.address
        binding.stateTextView.text = txtState
    }

    /**
     * 배경 및 상태바 업데이트
     */
    private fun updateBackgroundAndStatusBar(dustData: DustCombinedData) {
        val txtState = Etc.calculateAtmosphericEnvironment(
            dustData.dustItem.pm10Value?.toIntOrNull() ?: 0,
            dustData.dustItem.pm25Value?.toIntOrNull() ?: 0
        )

        window.statusBarColor = ContextCompat.getColor(this, Etc.getTextForStatusBarColor(txtState))
        binding.mainFrame.setBackgroundResource(Etc.getTextForStatus(txtState))
        binding.mainImageView.setImageDrawable(ContextCompat.getDrawable(this, Etc.getTextForStatusIconImage(txtState)))
    }

    /**
     * 남은 데이터 리사이클러뷰 설정
     * 이산화질소, 오존, 일산화탄소, 이황산가스
     */
    private fun setupRemainDataRecyclerView(dustData: DustCombinedData) {
        val remainData = arrayListOf(
            createRemainItem("이산화 질소", dustData.dustItem.no2Value ?: "-", Etc::getNo2ValueAirQualityLevel),
            createRemainItem("오존", dustData.dustItem.o3Value ?: "-", Etc::getO3GradeAirQualityLevel),
            createRemainItem("일산화탄소", dustData.dustItem.coValue ?: "-", Etc::getCoValueAirQualityLevel),
            createRemainItem("이황산가스", dustData.dustItem.so2Value ?: "-", Etc::getSo2ValueAirQualityLevel)
        )

        Log.d(TAG, "remainData: $remainData")
        binding.recyclerView.adapter = RemainAdapter(remainData)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    /**
     * 남은 데이터 아이템 생성
     * 이산화질소, 오존, 일산화탄소, 이황산가스
     */
    private fun createRemainItem(name: String, value: String, levelFunction: (String) -> String): Remain {
        val level = levelFunction(value)
        return Remain(name, level, "$value ppm", Etc.getTextForStatusIconImage(level))
    }

    /**
     * 미세먼지 예보 정보 업데이트
     */
    private fun updateForecastInfo(dustData: DustCombinedData) {
        binding.forecastDateTextView.text = dustData.forecastItem.informData
        binding.forecastContentTextView.text = getString(
            R.string.forecast_unit,
            dustData.forecastItem.informCause,
            dustData.forecastItem.informOverall
        )
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
        when(requestCode) {
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
                        searchAddressLauncher.launch(this)
                }
        }
    }

    /**
     * 새로고침 리스너
     */
    override fun onRefresh() {
        viewModel.currentTmCoordinates?.let {
            viewModel.getIntegrated(it)
            Log.d(TAG, "onRefresh currentAddress: $it")
        }
    }
}

