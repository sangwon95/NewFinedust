package com.tobie.newfinedust

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.tobie.newfinedust.adapter.ViewPager2Adapter
import com.tobie.newfinedust.databinding.ActivityMainBinding
import com.tobie.newfinedust.models.DustCombinedData
import com.tobie.newfinedust.models.IntentListener
import com.tobie.newfinedust.room.RegionDatabase
import com.tobie.newfinedust.room.RegionEntity
import com.tobie.newfinedust.room.RoomListener
import com.tobie.newfinedust.service.Permission
import com.tobie.newfinedust.service.RetrofitAirService
import com.tobie.newfinedust.viewmodels.MainViewModel
import com.tobie.newfinedust.viewmodels.MainViewModelFactory
import com.tobie.repository.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * 1. 현재 자신의 위치를 가져온다.
 * 2. 위도 경도 값을 이용하여 현주소를 가져온다.
 * 3. ex) [대전광역시 유성구 장동] 시구동 3개의 어구로 tmX, tmY값을 구한다.
 * 4. tmX, tmY기반으로 근접측정소 목록을 불러온다.
 * 5. stationName을 가지고 미세먼지 수치값을 가져온다.
 *
 * -------<Sequence>------
 * checkPermission: 자신의 위치 데이터를 가져오기 위해 퍼미션 권한 확인이 필요하다
 * getLocation(this, this) 실행하여 현재위치값(lat, log) 주소값으로 변환
 *
 * configureMainViewModel: address.observe-> 주소값(시, 구, 동 군..)
 * getTmxy()을 통해 TmX, TmY 값을 구한다.
 *
 *
 */

/**
 * TODO
 *  1.미세먼지 예보불러올때 timeout error issue
 *  2.검색시 특별자치도, 00구 삭제, 검색 에러 추가적으로 찾기
 */
class MainActivity : AppCompatActivity(), OnClickListener, RoomListener, IntentListener {

    companion object {
        const val TAG: String = "MainActivity - 로그"
        const val LOCATION_PERMISSION_REQUEST_CODE = 100 // 위치 권한 요청 코드
    }

    /**
     * isMoveLastPage는 false로 초기화된다.
     * 지역이 새롭게 추가되었을 경우 true로 변경되어
     * 추가된 마지막 ViewPager를 보여주게 된다.
     */
    private var isMoveLastPage: Boolean = false

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ViewPager2Adapter
    private lateinit var roomDB: RegionDatabase //Room Database

    private var regionList = listOf<RegionEntity>()
    private var addressList: ArrayList<String> = arrayListOf() //지역 리스트
    private var dustCombinedItemList: ArrayList<DustCombinedData> = arrayListOf()

    // 변수명 예시: isLocationPermissionGranted
    private var isLocationPermissionGranted: Boolean = false

    // 주소 리스트 수정 StartActivityForResult
    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {

                val resultList = result.data?.getStringArrayListExtra("updateList")
                if (resultList != null) {

                    isMoveLastPage = false
                    resultList.removeAt(0)
                    addressList.clear()
                    addressList = resultList

                    deleteAllRegion { isResult->
                        if(isResult){
                            for (address in addressList) { // 사용 예시: 받은 ArrayList<String>을 사용
                                insertRegion(address)
                            }
                        } else {
                            Log.d(TAG,"deleteAllRegion false 리턴됨")
                        }
                    }
                    reorderAndRemove(resultList)

                } else {
                    Toast.makeText(applicationContext, "변경된 사항이 없음", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val addLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedAddress = result.data?.getStringExtra("selectedAddress")

                if (selectedAddress != null) {
                    isMoveLastPage = true

                    addressList.add(selectedAddress)
                    Log.d(TAG, "selectedAddress: $selectedAddress")

                    insertRegion(selectedAddress) // RoomDB 저장

                    getFineDustData(selectedAddress) // 가져온 주소의 미세먼지 정보 가져오기
                } else {
                    Toast.makeText(applicationContext, "가져온 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private var gpsRequestText: String = "위치정보를 활성화해주세요."


    /**
     * 지역리스트를 수정및 제거 시 dustCombinedItemList를 재배열한다.
     */
    private fun reorderAndRemove(list: List<String>) {
        val tempList = dustCombinedItemList.toList()
        dustCombinedItemList.clear() // dustCombinedItemList을 list 따라 재배열하고 삭제합니다.

        list.forEach { element ->
            val dustCombinedData = tempList.find { it.address == element }
            dustCombinedData?.let {
                dustCombinedItemList.add(it)
            }
        }

        setupViewPager() // 위 변경사항 ViewPage2 adapter 재배치

        // 첫번째 ViewPage2로 이동
        if(isMoveLastPage){
            binding.viewPager2.setCurrentItemWithDuration(addressList.size, 1000)
        } else {
            binding.viewPager2.setCurrentItem(0, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * view binding
         * 자동으로 완성된 액티비티 메인 바인딩 클스래 인스턴스를 가져온다.
         */
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureMainViewModel() // ViewModel 초기화

        roomDB = RegionDatabase.getInstance(this)!! //Room Database 초기화
        getAllRegion()

        Permission(this,{
            // 위치 권한이 허용되었을 때 수행할 작업
            isLocationPermissionGranted = true
            viewModel.getLocation(this, this)
        },{
            // 계속 위치권한 거절했을 때
            handleLocationPermissionDenied()
        }).checkPermission()

        adapter = ViewPager2Adapter(dustCombinedItemList, this, this, this)
        binding.viewPager2.adapter = adapter // 어뎁터 생성
        binding.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 가로 방향 스크롤
    }

    private fun moveViewPagePosition(){
        if(isMoveLastPage){
            binding.viewPager2.setCurrentItemWithDuration(addressList.size, 1000)
        }

    }

    private fun ViewPager2.setCurrentItemWithDuration(
        item: Int,
        duration: Long,
        interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
        pagePxWidth: Int = width // Default value taken from getWidth() from ViewPager2 view
    ) {
        val pxToDrag: Int = pagePxWidth * (item - currentItem)
        val animator = ValueAnimator.ofInt(0, pxToDrag)
        var previousValue = 0
        animator.addUpdateListener { valueAnimator ->
            val currentValue = valueAnimator.animatedValue as Int
            val currentPxToDrag = (currentValue - previousValue).toFloat()
            fakeDragBy(-currentPxToDrag)
            previousValue = currentValue
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) { beginFakeDrag() }
            override fun onAnimationEnd(animation: Animator) { endFakeDrag() }
            override fun onAnimationCancel(animation: Animator) { /* Ignored */ }
            override fun onAnimationRepeat(animation: Animator) { /* Ignored */ }

        })
        animator.interpolator = interpolator
        animator.duration = duration
        animator.start()
    }


    /**
     * 메인 액티비티 뷰모델 구성 및 observe 등록
     */
    private fun configureMainViewModel() {
        val retrofitService = RetrofitAirService.getInstance()
        val mainRepository = MainRepository(retrofitService)

        /**
         * MainViewModelFactory: MainViewModel을 통해 전달되는 인자가 있을때 사용됩니다.
         * 생성자나 매개변수를 사용하지 않고 MainViewModel 객체를 인스턴스화한다.
         * mainRepository 지정
         */
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(mainRepository)
        )[MainViewModel::class.java]

        // 현재 위치기반 주소 수신
        viewModel.address.observe(this) { gpsAddress ->
            Log.d(TAG, "가져온 GPS주소: $gpsAddress")
            addressList.add(0, gpsAddress)

            viewModel.fetchDataBasedOnAddressList(addressList)
        }

        // Tmx, Tmy값 수신
        viewModel.tmxyValue.observe(this) {
            Log.d(TAG, it.toString())
            viewModel.getStationName(it[0].tmX, it[0].tmY)
        }

        // station 수신
        viewModel.stationValue.observe(this) {
            Log.d(TAG, "관측소: $it")
            viewModel.getFineDust(stationName = it)
        }

        viewModel.dustCombinedDataList.observe(this) {
            dustCombinedItemList = it
            reorderAndRemove(addressList) // 비동기 처리 후 바뀐순서를 재 배치
        }

        // 미세먼지 수치 수신
        viewModel.dustCombinedData.observe(this) {
            Log.i(TAG, "dustCombinedItemList value:$it")

            dustCombinedItemList.add(it)

            /**
             * 로컬에 저장된 주소리스트 값을 불러와 갯수에 맞게 ViewPager를 구성해둔다.
             * roomDB에서 데이터를 불러온다.
             * 불러온 데이터 길이 만큼 getFineDustData()를 수행하여 가져온데이터를 dustCombinedItemList
             * 에 추가하여 최종적으로 setupViewPager()를 호출하여 ViewPager를 생성하게 된다.
             */
//            if (dustCombinedItemList.size == 1) {
//                for (value in regionList) {
//                    getFineDustData(value.region)
//                    Log.d(TAG, "value.region: ${value.region}")
//                }
//            }
            Log.i(TAG, "dustCombinedItemList 길이:${dustCombinedItemList.size}")
            Log.i(TAG, "addressList 길이:${addressList.size}")

            if(dustCombinedItemList.size == addressList.size){
                reorderAndRemove(addressList) // 비동기 처리 후 바뀐순서를 재 배치
                //moveViewPagePosition()
            }

        }

        // 에러 observe
        viewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        // 로딩 observe
        viewModel.loading.observe(this) {
            if (it) {
                // binding.progressDialog.visibility = View.VISIBLE
            } else {
                // binding.progressDialog.visibility = View.GONE
            }
        }
    }

    /**
     * Airkorea를 통해 미세먼지 데이터를 불러온다.
     */
    private fun getFineDustData(address: String) {
        // viewModel.getFineDust(stationName = address)
        //viewModel.getTmxy(address)
        viewModel.getIntegrated(address)
        // 에어코리아 api를 통해 미세먼지 데이터를 가져온다.
        // viewModel.getFineDust(stationName = "증평읍")
        // viewModel.getStationName("232285.907431","321797.554715")
    }

    /**
     * ViewPager2Adapter 설정
     */
    private fun setupViewPager() {
        Log.i(TAG, "dustCombinedItemList: ${dustCombinedItemList.size} / ${addressList.size}")
        for (i in dustCombinedItemList) {
            Log.i(TAG, "dustCombinedItemList 값: $i")
        }
        adapter.update(dustCombinedItemList)
        binding.viewPager2.offscreenPageLimit = dustCombinedItemList.size
    }


    // 뷰 페이저에 들어갈 아이템
    private fun getIdolList(): ArrayList<Int> {
        return arrayListOf(1, 2, 3)
    }

    private fun update() {
//        data.add(4)
//        adapter.update(data)
//        Toast.makeText(this@MainActivity, "추가 완료", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(view: View?) {
        when (view) {
            // binding.button -> update()
        }
    }


    @SuppressLint("StaticFieldLeak")
    fun insertRegion(address: String) {
        Log.d(TAG, "insertRegion: [ $address ] RoomDB에 저장")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val region = RegionEntity(null, address)
                roomDB.regionDAO().insert(region)
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting region: ${e.message}")
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    fun getAllRegion() {
        CoroutineScope(Dispatchers.IO).launch {
            regionList = roomDB.regionDAO().getAll()
            if(regionList.isEmpty()){
                Log.d(TAG, "getAllRegion: RoomDB에 저장된 주소가 없습니다.")

            }
            for (value in regionList) {
                addressList.add(value.region)
                Log.d(TAG, "getAllRegion: RoomDB에서 가져온 주소: ${value.region}")
            }
        }.start()
    }

    /**
     * Region DB 전체 삭제
     */
    @SuppressLint("StaticFieldLeak")
    private fun deleteAllRegion(callback: (Boolean) -> Unit) {
        Log.d(TAG, "deleteAllRegion: RoomDB에 저장된 주소 모두 삭제")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                roomDB.regionDAO().deleteAll()
                callback(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleteAllRegion: ${e.message}")
                callback(false) // Callback to indicate insertion failure
            }

        }
    }

    fun deleteRegion() {
        var job = CoroutineScope(Dispatchers.IO).launch {
            var region = RegionEntity(null, "관평동")
            roomDB.regionDAO().delete(region)
        }
    }



    override fun onInsertListener(region: RegionEntity) {
        //insertRegion()
        //Log.d(TAG, "onInsertListener 실행")
    }

    override fun onGetAllListener() {
        getAllRegion()
        Log.d(TAG, "onGetAllListener 실행")
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
        Log.i(TAG, "addressList.size: ${addressList.size}")

        // Location Permission 미 허용시
        // Room 데이터에 저장된 주소값으로 데이터 불러오기
        if(addressList.size != 0) {
            Log.i(TAG, "addressList.isNotEmpty()")
            for (value in addressList) {
                getFineDustData(value)
            }
        }

        // Room 데이터에 저장된 주소값이 없을 시
        // 지역 추가 화면으로 전환
        else {
            Log.i(TAG, "firstRunAddLauncher")
            Intent(this, SearchActivity::class.java)
                .putExtra("impossibleBack", false).apply {
                    addLauncher.launch(this)
                }
        }
    }

    /**
     * 삭제된 리스트 위치값 계산
     */
    fun findDeletedValues(originalList: List<String>, modifiedList: List<String>): List<Int> {
        val deletedValueIndices = mutableListOf<Int>()

        for ((index, value) in originalList.withIndex()) {
            if (!modifiedList.contains(value)) {
                deletedValueIndices.add(index)
            }
        }
        return deletedValueIndices
    }

    override fun addIntentListener() {
        Intent(this, SearchActivity::class.java).apply {
            addLauncher.launch(this)
        }
    }

    override fun editIntentListener() {
//        val tempAddressList: ArrayList<String> = arrayListOf()
//        tempAddressList.add(gpsAddress) // gps 주소 텍스트를 따로 넣어준다.
//        tempAddressList.addAll(addressList)

        if(!isLocationPermissionGranted){
            addressList.add(0, gpsRequestText)
            Log.d(TAG,"isLocationPermissionGranted:$isLocationPermissionGranted")

        } else {
            Log.d(TAG,"isLocationPermissionGranted:$isLocationPermissionGranted")
        }

        Intent(this, FavoritesActivity::class.java)
            .putExtra("addressList", addressList).apply {
                editLauncher.launch(this)
            }
    }
}