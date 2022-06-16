package com.goldenowl.ecommerce.ui.tutorial

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.goldenowl.ecommerce.adapter.TutorialPagerAdapter
import com.goldenowl.ecommerce.databinding.ActivityTutorialBinding
import com.goldenowl.ecommerce.models.data.SettingsManager
import com.goldenowl.ecommerce.utils.Utils.launchHome
import com.google.android.material.tabs.TabLayoutMediator

class TutorialActivity : AppCompatActivity() {
    private val TAG = "TutorialActivity"
    private lateinit var binding: ActivityTutorialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTutorialBinding.inflate(layoutInflater)

        setViews()
        setContentView(binding.root)
    }

    private fun setFirstLaunch(firstLaunch: Boolean) {
        val settingsManager = SettingsManager(this)
        settingsManager.setFirstLaunch(firstLaunch)
    }

    private fun setViews() {
        val viewPager = binding.viewPagerTut
        val tabLayout = binding.tabIndicator
        val btnNext = binding.btnNext
        val btnSkip = binding.btnSkip
        val btnGetStarted = binding.btnGetStarted

        btnSkip.setOnClickListener { skipTutorial() }
        btnGetStarted.setOnClickListener { skipTutorial() }

        val adapter = TutorialPagerAdapter()

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
        }.attach()

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == 3) {
                    binding.tutNextLayout.visibility = View.INVISIBLE
                    binding.btnGetStarted.visibility = View.VISIBLE
                } else {
                    binding.tutNextLayout.visibility = View.VISIBLE
                    binding.btnGetStarted.visibility = View.INVISIBLE
                }

                btnNext.setOnClickListener {
                    viewPager.setCurrentItem(viewPager.currentItem + 1)
                }
            }
        })

    }

    private fun skipTutorial() {
        setFirstLaunch(false)
        launchHome(this)
        finish()
    }

}