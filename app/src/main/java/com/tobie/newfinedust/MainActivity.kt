package com.tobie.newfinedust

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tobie.newfinedust.adapter.ViewPager2Adapter
import com.tobie.newfinedust.databinding.ActivityMainBinding

/**
 * The number of pages (wizard steps) to show in this demo.
 */
private const val NUM_PAGES = 5

class MainActivity : AppCompatActivity(), OnClickListener {

    companion object {
        const val TAG: String = "MainActivity 로그"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ViewPager2Adapter

    private var data: ArrayList<Int> = arrayListOf(1,2,3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        // 자동으로 완성된 액티비티 메인 바인딩 클스 인스턴스를 가져온다.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Add Button
         */
        binding.button.setOnClickListener(this)

        /**
         * ViewPager2
         */
        adapter = ViewPager2Adapter(data)
        binding.viewPager2.adapter = adapter // 어뎁터 생성
        binding.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 가로 방향 스크롤


    }

    // 뷰 페이저에 들어갈 아이템
    private fun getIdolList(): ArrayList<Int> {
        return arrayListOf(1, 2, 3)
    }

    private fun update() {
        data.add(4)
        adapter.update(data)
        Toast.makeText(this@MainActivity, "추가 완료", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(view: View?) {
        when(view){
            binding.button -> update()
        }

    }
}