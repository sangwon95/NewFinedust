package com.tobie.newfinedust.utils

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import java.lang.Math.abs

class CustomPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        // position 값은 현재 페이지의 위치를 나타내며 -1부터 1까지의 범위입니다.

        // 이 부분에서 원하는 동작을 구현하여 스크롤 속도를 조절할 수 있습니다.
        // 예를 들어, 스크롤 속도를 늦추려면 position 값을 조절하면 됩니다.
        val scaleFactor = 0.75f
        val alphaFactor = 0.5f

        // 현재 페이지의 위치(position)을 기반으로 원하는 동작을 적용
        page.translationX = -position * page.width
        page.scaleX = 1 - (scaleFactor * kotlin.math.abs(position))
        page.scaleY = 1 - (scaleFactor * kotlin.math.abs(position))
        page.alpha = 1 - (alphaFactor * kotlin.math.abs(position))
    }
}