package com.goldenowl.ecommerce.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.goldenowl.ecommerce.models.auth.UserManager
import com.goldenowl.ecommerce.viewmodels.AuthViewModel
import com.goldenowl.ecommerce.viewmodels.TextInputViewModel


abstract class BaseAuthFragment<VBiding : ViewBinding> : Fragment() {
    private val TAG = "BaseAuthFragment"

    lateinit var binding: VBiding
    private lateinit var userManager: UserManager

    val textInputViewModel: TextInputViewModel by activityViewModels()
    val viewModel: AuthViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userManager = UserManager.getInstance(requireContext())
        binding = getViewBinding()

        setAppBar()
        setViews()
        setObservers()
        setupListeners()

        return binding.root
    }

    abstract fun getViewBinding(): VBiding
    abstract fun setAppBar()
    abstract fun setObservers()
    abstract fun setupListeners()
    abstract fun setViews()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        viewModel.googleCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

}