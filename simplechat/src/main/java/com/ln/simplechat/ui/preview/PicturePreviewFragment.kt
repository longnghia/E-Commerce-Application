package com.ln.simplechat.ui.preview

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.ln.simplechat.R
import com.ln.simplechat.databinding.PreviewFragmentBinding
import com.ln.simplechat.model.ChatMedia
import com.ln.simplechat.ui.viewBindings

class PicturePreviewFragment : Fragment(R.layout.preview_fragment) {
    private val binding by viewBindings(PreviewFragmentBinding::bind)
    private lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val loadAnimation: Animation
        if (enter) {
            loadAnimation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.ps_anim_alpha_enter)

            loadAnimation.duration = 100
        } else {
            loadAnimation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.ps_anim_alpha_exit)
        }
        return loadAnimation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position = arguments?.getInt(EXTRA_POSITION) ?: 0
        val data = (arguments?.get(EXTRA_DATA) ?: return) as ArrayList<ChatMedia>
        val dataSize = data.size

        viewPager = binding.viewpager
        viewPager.adapter = PreviewPagerAdapter(data)
        viewPager.setCurrentItem(position, false)
        binding.tvTitle.text = "${position + 1}/$dataSize"
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tvTitle.text = "${position + 1}/$dataSize"
            }
        })

        if (dataSize == 1)
            binding.tabLayout.visibility = View.INVISIBLE
        else
            TabLayoutMediator(binding.tabLayout, viewPager) { _, position ->
            }.attach()

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        private const val EXTRA_POSITION = "EXTRA_POSITION"
        private const val EXTRA_DATA = "EXTRA_DATA"
        const val TAG = "PREVIEW_FRAGMENT"

        fun newInstance(currentPosition: Int = 0, data: ArrayList<ChatMedia>) = PicturePreviewFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_POSITION, currentPosition)
                putParcelableArrayList(EXTRA_DATA, data)
            }
        }
    }
}