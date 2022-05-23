package com.goldenowl.ecommerce.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


abstract class BaseFragment<VBinding:ViewBinding> : Fragment() {

    protected lateinit var binding: VBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setViewBinding()
        init()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setViews()
        setUpListener()
        observeViews()

        return binding.root
    }

    open fun setUpListener() {
    }


    /*
    * set binding variable */
    abstract fun setViewBinding(): VBinding

    /*
    * set text, set title ...
    * */
    abstract fun  setViews()

    /*
    * set viewModel.property.observe
    * */
    abstract fun observeViews()
    open fun init(){}
}