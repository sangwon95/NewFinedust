package com.tobie.newfinedust.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tobie.repository.MainRepository
import com.tobie.repository.SearchRepository

/**
 * MainViewModel 객체를 생성하는데 사용됩니다.
 * Activity or Fragment같은 UI 컨드롤러에서 사용되며 데이터를 보유하고 UI
 * 를 업데이트하기 위해 사용됩니다.
 *
 * ViewModelFactory를 사용하면 ViewModel 객체를 생성하는 로직을 중앙에서 관리할 수 있습니다.
 *  이를 통해 코드 중복을 줄이고 유지보수성을 향상시킬 수 있습니다.
 */
class SearchViewModelFactory constructor(private val repository: SearchRepository): ViewModelProvider.Factory {
    companion object {
        const val TAG: String = "SearchViewModelFactory - 로그"
    }

    // ViewModelProvider.Factory를 확장함.
    // 오버라이드 하면 아래와 같은 create 함수를 받을 수 있음.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // modelClass에 MainActivityViewModel이 상속이되었는지 확인
        return if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            SearchViewModel(this.repository) as T  // MainViewModel의 파라미터 값 반환
        } else {
            throw IllegalArgumentException("$TAG: ViewModel Not Found")
        }
    }
}