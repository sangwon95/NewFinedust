package com.tobie.newfinedust.utils

import android.location.Address
import android.util.Log
import com.tobie.newfinedust.R
import com.tobie.newfinedust.activity.HomeActivity
import com.tobie.newfinedust.models.TmCoordinates
import kr.hyosang.coordinate.CoordPoint
import kr.hyosang.coordinate.TransCoord
import kotlin.math.max

class Etc {
    companion object{
        /**
         * 위도, 경도로 추출된 주소리스트를 가공하여 보다
         * 정확한 주소를 추출한다.
         */
        fun translationAddress (addressList: List<Address>) : String {
             var returnAddress = "알 수 없음"

                for(value in addressList){
                    Log.d(HomeActivity.TAG, value.toString())
                    if(value.countryCode == "KR"){
                        val address = value.getAddressLine(0).split(" ")

                        if(isEndingWithEupMyeonDong(address, 4)){
                            returnAddress = "${address[1]} ${address[2]} ${address[3]} ${address[4]}"
                            Log.d("TAG - 로그", "returnAddress: index:4 / $returnAddress")
                        } else if(isEndingWithEupMyeonDong(address, 3)){
                            returnAddress = "${address[1]} ${address[2]} ${address[3]}"
                            Log.d("TAG - 로그", "returnAddress: index:3 / $returnAddress")
                        } else if(isEndingWithEupMyeonDong(address, 2)){
                            returnAddress = "${address[1]} ${address[2]}"
                            Log.d("TAG - 로그", "returnAddress: index:4 / $returnAddress")
                        }  else {
                            returnAddress = ""
                            Log.d("TAG - 로그", "읍 면 동이 없는 주소입니다.")
                        }
                    }
                    if(returnAddress != ""){
                        break
                    }
                }
            return returnAddress
        }

        private fun isEndingWithEupMyeonDong(address: List<String>, position: Int): Boolean {
            val element = address[position]
            return element.endsWith("동") || element.endsWith("읍") || element.endsWith("면")
        }


        /**
         * 통합 대기환경지수 계산
         */
        fun calculateAtmosphericEnvironment(pm10: Int, pm25: Int): String {
            val pm10Rating = when (pm10) {
                0 -> 0 //측정 불가
                in 1..40 -> 1 //좋음
                in 41 .. 50 -> 2 //보통
                in 51 .. 75 -> 3 //나쁨
                in 76 .. 150 -> 4 //매우 나쁨
                else  -> 5 // 최악
            }

            val pm25Rating = when (pm25) {
                0 -> 0 //측정 불가
                in 1..20 -> 1 //좋음
                in 21 .. 25-> 2 //보통
                in 26 .. 37 -> 3 //나쁜
                in 38 .. 75 -> 4 //매우 나쁨
                else  //76 이상
                -> 5
            }

            return when(max(pm10Rating, pm25Rating)) {
                0 -> "측정 불가"
                1 -> "좋음"
                2 -> "보통"
                3 -> "나쁨"
                4 -> "매우 나쁨"
                5 -> "최악"
                else -> "알수 없음"
            }
        }

        /**
         * 미세먼지 대기 상태값을 통해 statusBar color 값을 리턴한다.
         */
        fun getTextForStatusBarColor(status: String): Int {
            return when(status) {
                "측정 불가" -> R.color.statusbar_good_color
                "좋음" -> R.color.statusbar_good_color
                "보통" -> R.color.statusbar_normal_color
                "나쁨" -> R.color.statusbar_verybad_color
                "매우 나쁨" -> R.color.statusbar_worst_color
                "최악" -> R.color.statusbar_worst_color
                else -> R.color.statusbar_good_color
            }
        }

        /**
         * 미세먼지 대기 상태값을 통해 컬러 text를 반환한다.
         */
        fun getTextForStatus(status: String): Int {
            return when(status) {
                "측정 불가" -> R.drawable.ba_gradient_good
                "좋음" -> R.drawable.ba_gradient_good
                "보통" -> R.drawable.ba_gradient_normal
                "나쁨" -> R.drawable.ba_gradient_verybad
                "매우 나쁨" -> R.drawable.ba_gradient_worst
                "최악" -> R.drawable.ba_gradient_worst
                else -> R.drawable.ba_gradient_good
            }
        }

        fun getTextForStatusIconImage(status: String): Int {
            return when(status) {
                "측정 불가" -> R.drawable.ic_normal
                "좋음" -> R.drawable.ic_good
                "보통" -> R.drawable.ic_normal
                "나쁨" -> R.drawable.ic_bad
                "매우 나쁨" -> R.drawable.ic_verybad
                "최악" -> R.drawable.ic_verybad
                else -> R.drawable.ic_normal
            }
        }

        /**
         * 이산화 질소 오염도 단계
         */
       fun getNo2ValueAirQualityLevel(value: String) : String {
            return if (value != "-") {
                when (value.toDouble()) {
                    in 0.0..0.03 -> "좋음"
                    in 0.31..0.06 -> "보통"
                    in 0.061..0.15 -> "나쁨"
                    else -> "매우 나쁨"
                }
            } else {
                value
            }
        }

        /**
         * 오존 오염도 단계
         */
        fun getO3GradeAirQualityLevel(value: String) : String {
            return if (value != "-") {
                when (value.toDouble()) {
                    in 0.0 .. 0.03 -> "좋음"
                    in 0.031 .. 0.09 -> "보통"
                    in 0.091 .. 0.15 -> "나쁨"
                    else -> "매우 나쁨"
                }
            } else {
                value
            }
        }

        /**
         * 일산화탄소 오염도 단계
         */
        fun getCoValueAirQualityLevel(value: String) : String {
            return if (value != "-") {
                when (value.toDouble()) {
                    in 0.0 .. 2.0 -> "좋음"
                    in 2.01 .. 9.0 -> "보통"
                    in 9.01 .. 15.0 -> "나쁨"
                    else -> "매우 나쁨"
                }
            } else {
                value
            }
        }

        /**
         * 이황산가스 오염도 단계
         */
        fun getSo2ValueAirQualityLevel(value: String) : String {
            return if (value != "-") {
                when (value.toDouble()) {
                    in 0.0 .. 0.02 -> "좋음"
                    in 0.021 .. 0.05 -> "보통"
                    in 0.051 .. 0.15 -> "나쁨"
                    else -> "매우 나쁨"
                }
            } else {
                value
            }
        }

        /**
         * WGS84 to Tm 좌표 변환
         */
        fun convertWGS84ToTM(latitude: Double, longitude: Double, address: String): TmCoordinates {
            val tmPt = CoordPoint(longitude, latitude)
            val wgsPt = TransCoord.getTransCoord(
                tmPt,
                TransCoord.COORD_TYPE_WGS84,
                TransCoord.COORD_TYPE_TM
            )
            Log.i("wgscoorToTm:", "$address /tmx: ${wgsPt.x} tmy: ${wgsPt.y}")
            return TmCoordinates(wgsPt.x, wgsPt.y, address)
        }

    }

    /**
     * 특별자치도를 시로 변경한다.
     */
    fun addSpaceAfterCityName(fullNm: String): String {
        // 특별자치도 이름을 일반 도 이름으로 변경
        fun replaceSpecialRegions(text: String): String {
            return text
                .replace("전북특별자치도", "전라북도")
                .replace("강원특별자치도", "강원도")
        }

        // 정규 표현식으로 "시" 뒤에 띄어쓰기가 있는지 확인하고 없으면 추가
        val regex = Regex("([가-힣]+시)([가-힣])")

        // 먼저 특별자치도 이름을 변경한 후, 시 이름 뒤에 띄어쓰기를 추가
        val replacedRegions = replaceSpecialRegions(fullNm)
        return regex.replace(replacedRegions) { matchResult ->
            "${matchResult.groupValues[1]} ${matchResult.groupValues[2]}"
        }
    }
}