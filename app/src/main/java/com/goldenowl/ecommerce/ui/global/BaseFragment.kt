package com.goldenowl.ecommerce.ui.global

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


abstract class BaseFragment<VBinding : ViewBinding> : Fragment() {

    protected lateinit var binding: VBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = getViewBinding()
        init()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        setAppbar()

        setUpListener()
        setObservers()
    }

    open fun setUpListener() {
    }


    /*
    * set binding variable */
    abstract fun getViewBinding(): VBinding

    /*
    * set text, set title ...
    * */
    abstract fun setViews()
    abstract fun setAppbar()

    /*
    * set viewModel.property.observe
    * */
    abstract fun setObservers()
    open fun init() {
        setHasOptionsMenu(true)
    }
}