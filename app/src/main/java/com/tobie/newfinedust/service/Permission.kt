package com.tobie.newfinedust.service

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tobie.newfinedust.MainActivity

class Permission(
    private val mainActivity: MainActivity,
    private val getLocation: () -> Unit,
    private val keepNegativeAction: () -> Unit
) {

     fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                mainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 위치 권한이 허용되어 있는 경우
                getLocation()
            }

            // 교육용 팝업을 띄어야할때
            // 최초 퍼미션(거절) 이후 팝업을 띄워 왜필요한지 설명 다이얼로그가 띄워진다.
//            mainActivity.shouldShowRequestPermissionRationale(
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) -> {
//                showPermissionInfoDialog()
//            }
            else -> {
                requestAccessFineLocation()
            }
        }
    }


    /**
     * 외부 저장소 읽기 퍼미션에 대한 교육용 다이얼로그
     */
    private fun showPermissionInfoDialog(){
        AlertDialog.Builder(mainActivity).apply {
            setMessage("현재위치를 가져오기 위해서, 위치 권한이 필요합니다.")
            setNegativeButton("취소") { _, _ ->
                keepNegativeAction()
            }
            setPositiveButton("동의") { _, _ ->
                requestAccessFineLocation()
            }
        }.show()
    }

    /**
     * 실제 퍼미션 요청 메소드
     */
    private fun requestAccessFineLocation() {
        ActivityCompat.requestPermissions(
            mainActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            MainActivity.LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * 퍼미션 요청 처리
     */
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray,
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            MainActivity.LOCATION_PERMISSION_REQUEST_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    // 위치 권한이 거부되었을 때 수행할 작업
//                    // 즐겨찾기 리스트 추가 하여 앱 진행 할수 있게 한다.
//                    Log.i(MainActivity.TAG, "위치 권한이 거부되었습니다.")
//                }
//                return
//            }
//            // 다른 권한 요청 코드에 대한 처리
//        }
//    }
}