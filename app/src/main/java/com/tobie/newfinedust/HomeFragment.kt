package com.tobie.newfinedust

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tobie.newfinedust.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    companion object {
        const val TAG: String = "HomeFragment 로그"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "HomeFragment - onCreate() celled")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        var binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

}