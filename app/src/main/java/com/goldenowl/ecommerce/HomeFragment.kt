package com.goldenowl.ecommerce

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    val TAG: String = "HomeFragment"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: " + TAG)
        val view: View = inflater.inflate(R.layout.home_fragment, container, false);


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