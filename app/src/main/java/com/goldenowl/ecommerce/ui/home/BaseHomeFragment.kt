package com.goldenowl.ecommerce.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.goldenowl.ecommerce.viewmodels.AuthViewModel


abstract class BaseHomeFragment<VBinding:ViewBinding> : Fragment() {
    private val TAG = "ProfileFragment"

    protected lateinit var binding: VBinding
    protected val viewModel: AuthViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        binding = setViewBinding()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setViews()
        observeViews()

        return binding.root
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

    /*
    * init firebase ...
    * */
    open fun init(){}
}