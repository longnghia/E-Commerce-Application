package com.goldenowl.ecommerce.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    val TAG: String = "HomeFragment"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: " + TAG)
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        setAppBar()

        return binding.root;
    }

    private fun setAppBar() {
        binding.topAppBar.toolbar.title = getString(R.string.home)
    }
}