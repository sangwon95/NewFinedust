package com.tobie.newfinedust

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

import androidx.viewpager2.widget.ViewPager2
import com.tobie.newfinedust.adapter.ViewPager2Adapter
import com.tobie.newfinedust.databinding.ActivityMainBinding
import com.tobie.newfinedust.models.DustItem
import com.tobie.newfinedust.service.RetrofitService
import com.tobie.newfinedust.utils.Constants
import com.tobie.newfinedust.viewmodels.MainViewModel
import com.tobie.newfinedust.viewmodels.MainViewModelFactory
import com.tobie.repository.MainRepository
import eightbitlab.com.blurview.RenderScriptBlur
import kotlin.collections.ArrayList

/**
 * 1. 현재 자신의 위치를 가져온다.
 * 2. 위도 경도 값을 이용하여 현주소를 가져온다.
 * 3. ex) [대전광역시 유성구 장동] 시구동 3개의 어구로 tmX, tmY값을 구한다.
 * 4. tmX, tmY기반으로 근접측정소 목록을 불러온다.
 * 5. stationName을 가지고 미세먼지 수치값을 가져온다.
 *
 * -------<Sequence>------
 * [checkPermission]: 자신의 위치 데이터를 가져오기 위해 퍼미션 권한 확인이 필요하다
 * getLocation(this, this) 실행하여 현재위치값(lat, log) 주소값으로 변환
 *
 * [configureMainViewModel]: address.observe-> 주소값(시, 구, 동 군..)
 * getTmxy()을 통해 TmX, TmY 값을 구한다.
 *
 *
 */
class MainActivity : AppCompatActivity(), OnClickListener {

    companion object {
        const val TAG: String = "MainActivity - 로그"
        const val LOCATION_PERMISSION_REQUEST_CODE = 100 // 위치 권한 요청 코드
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ViewPager2Adapter

    private var address: ArrayList<String> = arrayListOf("송강동", "관평동", "전민동")

    //private var data: ArrayList<Int> = arrayListOf(1, 2, 3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewModel
        configureMainViewModel()

        // 위치 권한 퍼미션 확인
        checkPermission()

        // view binding
        // 자동으로 완성된 액티비티 메인 바인딩 클스 인스턴스를 가져온다.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Add Button
        // binding.button.setOnClickListener(this)


    }


    /**
     * 메인 액티비티 뷰모델 구성
     */
    private fun configureMainViewModel() {
        val retrofitService = RetrofitService.getInstance()
        val mainRepository = MainRepository(retrofitService)

        // MainViewModelFactory: MainViewModel을 통해 전달되는 인자가 있을때 사용됩니다.
        // 생성자나 매개변수를 사용하지 않고 MainViewModel 객체를 인스턴스화한다.
        // mainRepository 지정
        viewModel = ViewModelProvider(this,
            MainViewModelFactory(mainRepository))[MainViewModel::class.java]

        // 현재 위치 주소 수신
        viewModel.address.observe(this) {
            Log.d(TAG, "가져온 주소값: $it")
            viewModel.getTmxy(it)
        }


        // Tmx, Tmy값 수신
        viewModel.tmxyValue.observe(this) {
            Log.d(TAG, it.toString())
            viewModel.getStationName(it[0].tmX, it[0].tmY)
        }


        // 미세먼지 수치 수신
        viewModel.dustValue.observe(this) {
            Log.d(TAG, it.toString())
            var dustItemList: ArrayList<DustItem> = arrayListOf(it, it, it)

            // ViewPager2
            adapter = ViewPager2Adapter(dustItemList, this, address)
            binding.viewPager2.adapter = adapter // 어뎁터 생성
            binding.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 가로 방향 스크롤
        }


        // station 수신
        viewModel.stationValue.observe(this) {
            Log.d(TAG, "관측소: $it")
            viewModel.getFineDust(stationName = it)
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

        // 에어코리아 api를 통해 미세먼지 데이터를 가져온다.
//        viewModel.getFineDust(stationName = "증평읍")
//        viewModel.getStationName("232285.907431","321797.554715")
    }



    /**
     * 위치 권한 퍼미션 확인
     */
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Constants.FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 권한이 허용되어 있는 경우
            viewModel.getLocation(this, this)
        }
        else {
            // 위치 권한이 허용되어 있지 않은 경우
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Constants.FINE_LOCATION,
                    Constants.COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }



    /**
     * 퍼미션 요청 처리
     */
    override  fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 위치 권한이 허용되었을 때 수행할 작업
                    viewModel.getLocation(this, this)
                }
                else {
                    // 위치 권한이 거부되었을 때 수행할 작업
                    // 즐겨찾기 리스트 추가 하여 앱 진행 할수 있게 한다.
                    Log.i(TAG, "위치 권한이 거부되었습니다.")
                }
                return
            }
            // 다른 권한 요청 코드에 대한 처리
        }
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
        when(view){
           // binding.button -> update()
        }
    }
}