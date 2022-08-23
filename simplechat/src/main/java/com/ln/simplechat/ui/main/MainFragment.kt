package com.ln.simplechat.ui.main

import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ln.simplechat.R
import com.ln.simplechat.SimpleChatActivity
import com.ln.simplechat.databinding.MainFragmentBinding
import com.ln.simplechat.ui.chat.ChatFragment
import com.ln.simplechat.ui.viewBindings
import com.ln.simplechat.utils.MyResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment) {
    private val binding by viewBindings(MainFragmentBinding::bind)
    private lateinit var adapter: ChannelAdapter
    private val viewModel: MainViewModel by viewModels()

    val db: FirebaseFirestore = Firebase.firestore

    override fun onStart() {
        super.onStart()
        (activity as SimpleChatActivity).setSystemBarColor(R.color.colorPrimary)
    }

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

        binding.icAdd.setOnClickListener {
            showMenu(it, R.menu.popup_menu)
        }

        viewModel.listChannelAndMembers.observe(viewLifecycleOwner) { result ->
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

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(context!!, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, ICON_MARGIN.toFloat(), resources.displayMetrics
                    )
                        .toInt()
                if (item.icon != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                    } else {
                        item.icon =
                            object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                override fun getIntrinsicWidth(): Int {
                                    return intrinsicHeight + iconMarginPx + iconMarginPx
                                }
                            }
                    }
                }
            }
        }

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when(menuItem.itemId){
                R.id.option_create_group->{
                }
            }
            true
        }
        popup.setOnDismissListener {
        }

        popup.show()
    }

    companion object {
        const val ICON_MARGIN = 8
    }
}
