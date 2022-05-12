package com.goldenowl.ecommerce.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.goldenowl.ecommerce.R

class HomeFragment : Fragment() {
    val TAG: String = "HomeFragment"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: " + TAG)
        val view: View = inflater.inflate(R.layout.fragment_home, container, false);


        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")




//        val toolbar: Toolbar? = activity?.findViewById<Toolbar>(R.id.toolbar)
//        if (toolbar != null) {
//            Log.d(TAG, "onCreateView: tool bar ok")
//            toolbar.setTitle("123")
//
//        }
    }
}