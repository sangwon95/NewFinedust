//package com.tobie.newfinedust.adapter
//
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentActivity
//import androidx.viewpager2.adapter.FragmentStateAdapter
//import com.tobie.newfinedust.models.DustCombinedData
//
//class ViewPagerAdapter(
//    private var dustCombinedItemList: ArrayList<DustCombinedData>,
//    fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
//
//    private var fragments: ArrayList<Fragment> = arrayListOf()
//
//
//    override fun getItemCount(): Int {
//        return fragments.size
//    }
//
//    override fun createFragment(position: Int): Fragment {
//        for(value in dustCombinedItemList){
//            fragments.add(HomeFragment(value))
//        }
//
//        return fragments[position]
//    }
//
//    /**
//     * viewPage 미세먼지 수치 값을 업데이트 한다.
//     */
//    fun update(dustItemList: ArrayList<DustCombinedData>) {
//        for(value in dustItemList){
//            fragments.add(HomeFragment(value))
//        }
//        notifyItemChanged(fragments.size - 1)
//    }
//}