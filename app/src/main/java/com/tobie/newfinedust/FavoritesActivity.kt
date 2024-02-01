package com.tobie.newfinedust

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import com.tobie.newfinedust.adapter.FavoriteAdapter
import com.tobie.newfinedust.adapter.ViewPager2Adapter
import com.tobie.newfinedust.databinding.ActivityFavoritesBinding
import com.tobie.newfinedust.models.FavoritesListEventListener
import com.tobie.newfinedust.utils.FavoriteListHelper


/**
 * 즐겨찾기 화면
 */
class FavoritesActivity : AppCompatActivity(), FavoritesListEventListener {

    companion object {
        const val TAG: String = "FavoritesActivity - 로그"
    }
    private lateinit var binding: ActivityFavoritesBinding
    private  var favoritesAddressList = arrayListOf<String>()
    private  var updatedFavoritesAddressList: ArrayList<String>? = null
    private lateinit var favoriteAdapter: FavoriteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backImageView.setColorFilter(Color.parseColor("#FFFFFF"))

        binding.backImageView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("updateList", updatedFavoritesAddressList)
            }
            setResult(RESULT_OK, intent)
            finish()
        }


        val receivedList: ArrayList<String>? = intent.getStringArrayListExtra("addressList")
        if (receivedList != null) {
            for (item in receivedList) {
                favoritesAddressList.add(item)
            }
        }

        setFavoriteAdapter()
    }

    private fun setFavoriteAdapter() {
        favoriteAdapter = FavoriteAdapter(favoritesAddressList, this)
        binding.favoriteRecyclerView.adapter = favoriteAdapter

        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.favoriteRecyclerView.addItemDecoration(dividerItemDecoration)

        val callback = FavoriteListHelper(favoriteAdapter) { removePosition ->
            Log.i(TAG, "removePosition: $removePosition")
        }

        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.favoriteRecyclerView)
    }


    // onSupportNavigateUp() 메서드를 오버라이드하여
    // 뒤로가기 버튼을 눌렀을 때 동작을 정의
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun changedFavoritesListListener(updatedList: ArrayList<String>) {
        updatedFavoritesAddressList = updatedList
        for (value in updatedList) {
            Log.d("업데이트된 리스트: ", value)
        }
    }
}
