package com.ln.simplechat.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ln.simplechat.R
import com.ln.simplechat.databinding.MainFragmentBinding
import com.ln.simplechat.ui.chat.ChatFragment
import com.ln.simplechat.ui.viewBindings
import com.ln.simplechat.utils.MyResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment) {
    private val binding by viewBindings(MainFragmentBinding::bind)
    private lateinit var adapter: ChannelAdapter
    private val viewModel: MainViewModel by viewModels()

    val db: FirebaseFirestore = Firebase.firestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChannelAdapter { channel ->
            activity?.supportFragmentManager?.commit {
                addToBackStack(ChatFragment.TAG)
                replace(R.id.container, ChatFragment.newInstance(channel))
            }
        }
        binding.contacts.apply {
            setHasFixedSize(true)
            adapter = this@MainFragment.adapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.listChannel.collect { result ->
                    when (result) {
                        is MyResult.Success -> adapter.submitList(result.data)
                        is MyResult.Error -> Toast.makeText(
                            requireContext(),
                            result.exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    }
}