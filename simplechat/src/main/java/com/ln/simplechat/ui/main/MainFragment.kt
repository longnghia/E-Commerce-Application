package com.ln.simplechat.ui.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ln.simplechat.R
import com.ln.simplechat.SimpleChatActivity
import com.ln.simplechat.databinding.MainFragmentBinding
import com.ln.simplechat.getNavigationController
import com.ln.simplechat.ui.viewBindings
import com.ln.simplechat.utils.MyResult
import com.ln.simplechat.utils.buildMenu
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment) {
    private val binding by viewBindings(MainFragmentBinding::bind)
    private lateinit var adapter: ChannelAdapter
    private val viewModel: MainViewModel by activityViewModels()
    val db: FirebaseFirestore = Firebase.firestore

    override fun onStart() {
        super.onStart()
        (activity as SimpleChatActivity).setSystemBarColor(R.color.colorPrimary)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navigator = getNavigationController()

        adapter = ChannelAdapter(viewModel.userId) { channel ->
            navigator.openChat(channel.id)
        }

        binding.shimmerMain.startShimmer()
        restoreData()

        binding.contacts.apply {
            setHasFixedSize(true)
            adapter = this@MainFragment.adapter
        }

        binding.icAdd.setOnClickListener {
            showMenu(it, R.menu.popup_menu)
        }

        viewModel.listChannelAndMembers.observe(viewLifecycleOwner) { result ->
            when (result) {
                is MyResult.Success -> {
                    viewModel.saveData(result.data)
                    adapter.submitList(result.data)
                }
                is MyResult.Error -> Toast.makeText(
                    requireContext(),
                    result.exception.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun restoreData() {
        val data = viewModel.restoreData()
        data?.let {
            adapter.submitList(it)
        }
        hideShimmer()
    }

    private fun hideShimmer() {
        requireActivity().runOnUiThread {
            binding.shimmerMain.stopShimmer()
            binding.shimmerMain.hideShimmer()
        }
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = buildMenu(v, menuRes)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.option_create_group -> {
//                    Implement later
                }
            }
            true
        }
        popup.show()
    }
}
