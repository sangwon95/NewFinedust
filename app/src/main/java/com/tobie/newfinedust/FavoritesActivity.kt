package com.tobie.newfinedust

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tobie.newfinedust.activity.HomeActivity
import com.tobie.newfinedust.adapter.FavoriteAdapter
import com.tobie.newfinedust.databinding.ActivityFavoritesBinding
import com.tobie.newfinedust.databinding.CustomAlertDialogBinding
import com.tobie.newfinedust.models.FavoritesListEventListener
import com.tobie.newfinedust.models.GpsAddrssManager
import com.tobie.newfinedust.room.RegionDatabase
import com.tobie.newfinedust.viewmodels.FavoritesViewModel


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

    private lateinit var roomDB: RegionDatabase //Room Database
    private val viewModel: FavoritesViewModel by viewModels()

    private var addressFromHome: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        registerObservers()

        roomDB = RegionDatabase.getInstance(this)!! //Room Database 초기화
        viewModel.getAllRegion(roomDB)

//        val receivedAddress: String? = intent.getStringExtra("NotGpsAddress")
//        if (receivedAddress != null) {
//            addressFromHome = receivedAddress
//        }

        if(GpsAddrssManager.getAddress() != null){
            "현재위치: ${GpsAddrssManager.getAddress()}".also { binding.gpsTextView.text = it }
        } else {
            binding.gpsLinearLayout.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        binding.backImageView.setOnClickListener {
            setResult(RESULT_CANCELED, Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.gpsLinearLayout.setOnClickListener {
            selectListener(GpsAddrssManager.getAddress()!!)
        }
    }

    private fun registerObservers() {
        viewModel.addressLiveData.observe(this) {
            Log.d(TAG, "registerObservers: RoomDB에서 가져온 주소 리스트: $it")
            setFavoriteAdapter(it)
        }
    }


    private fun setFavoriteAdapter(addressList: ArrayList<String>) {
        favoriteAdapter = FavoriteAdapter(addressList, this)
        binding.favoriteRecyclerView.adapter = favoriteAdapter

//        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
//        binding.favoriteRecyclerView.addItemDecoration(dividerItemDecoration)

//        val callback = FavoriteListHelper(favoriteAdapter) { removePosition ->
//            Log.i(TAG, "removePosition: $removePosition")
//        }
//
//        val touchHelper = ItemTouchHelper(callback)
//        touchHelper.attachToRecyclerView(binding.favoriteRecyclerView)
    }


    // onSupportNavigateUp() 메서드를 오버라이드하여
    // 뒤로가기 버튼을 눌렀을 때 동작을 정의
    override fun onSupportNavigateUp(): Boolean {
        @Suppress("DEPRECATION")
        onBackPressed()
        return true
    }

    override fun deleteListener(address: String, position: Int) {
        deleteAlert(address, position)
    }

    override fun selectListener(address: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("selectedAddress", address)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * 삭제 알림창
     */
    private fun deleteAlert(address: String, position: Int) {
        val dialogBinding: CustomAlertDialogBinding = CustomAlertDialogBinding.inflate(layoutInflater)
        val dialogView = dialogBinding.root
        dialogBinding.alertTitle.text = "즐겨찾기"
        dialogBinding.alertMessage.text = "\"${address}\"\n삭제 하시겠습니까?"

        val dialog = AlertDialog.Builder(this).apply {
            setView(dialogView)
        }.create()

        // 삭제 버튼
        dialogBinding.positiveButton.setOnClickListener {
            favoriteAdapter.removeDataAt(position)
            viewModel.deleteAddress(address, roomDB)
            dialog.dismiss()
        }

        // 취소 버튼
        dialogBinding.negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        // Dialog의 배경을 둥근 모서리로 설정
        dialog.window?.setLayout(750, ViewGroup.LayoutParams.WRAP_CONTENT) // 너비를 600dp로 설정
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


}
