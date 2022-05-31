package com.goldenowl.ecommerce.ui.global.bag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.goldenowl.ecommerce.databinding.FragmentBagBinding

class BagFragment : Fragment() {
    private  lateinit var binding: FragmentBagBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBagBinding.inflate(inflater, container, false)
        setViews()
        return binding.root
    }

    private fun setViews() {
    }

}