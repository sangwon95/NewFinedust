package com.tobie.newfinedust.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

class LocationService : Service() {

    companion object {
        const val TAG = "LocationService - 로그"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private val binder = MyBinder()

    inner class MyBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate 실행")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "LocationService Stop")

        super.onDestroy()
    }

//    fun getCurrentAddress(): List<String> {
//        var currentAddress: List<String> = listOf("서울특별시", "중구", "명동")
//
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            //주소 초기화
//
//            Log.i(TAG,"위치권한이 있습니다.")
//            fusedLocationClient.lastLocation.addOnSuccessListener {
//                it.let {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        Log.i(TAG,"latitude: ${it.latitude} / ${it.longitude}")
//                        geocoder.getFromLocation(it.latitude, it.longitude,1) { addressList ->
//                            for (address in addressList) {
//                                val addressLine = address.getAddressLine(0).split(" ")
//                                currentAddress = addressLine
//                            }
//                        }
//                    }
//                }
//
////            val address: Address = addresses[0]
////            val currentAddress = address.getAddressLine(0)
//                Log.d(TAG, "Current Address$currentAddress")
//            }
//        } else {
//            Log.i(TAG,"위치권한이 없습니다.")
//            Toast.makeText(this, "위치권한이 없습니다..", Toast.LENGTH_SHORT).show()
//        }
//        return currentAddress
//    }
}


