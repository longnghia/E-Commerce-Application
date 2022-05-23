package com.goldenowl.ecommerce.ui.auth

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ActivityLoginSignupBinding

class LoginSignupActivity : AppCompatActivity() {
    private val TAG = "LoginSignupActivity"
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityLoginSignupBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setWindow()
        Log.d(TAG, "onCreate: ")
        binding = ActivityLoginSignupBinding.inflate(layoutInflater)

        val toolbar = binding.topAppBar.toolbar
        Log.d(TAG, "onCreate: " + toolbar.title)
        setSupportActionBar(toolbar)

        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.auth_nav_host_fragment) as NavHostFragment? ?: return

        navController = host.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)
//
        setupToolBarLayout(toolbar, navController, appBarConfiguration)
//
        setupActionBar(navController, appBarConfiguration)

        setDestinationChangedListener(navController)

        setContentView(binding.root)
    }

    private fun setWindow() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }
    }

    private fun setDestinationChangedListener(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val dest: String = try {
                resources.getResourceName(destination.id)
            } catch (e: Resources.NotFoundException) {
                destination.id.toString()
            }
            Log.d("NavigationActivity", "Navigated to $dest")
        }
    }


    private fun setupToolBarLayout(
        toolbar: Toolbar,
        navController: NavController,
        appBarConfiguration: AppBarConfiguration
    ) {
        val toolbarLayout = binding.topAppBar.collapsingToolbarLayout
        toolbarLayout.setupWithNavController(toolbar, navController, appBarConfiguration)
    }

    private fun setupActionBar(navController: NavController, appBarConfiguration: AppBarConfiguration) {
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
}