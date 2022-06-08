/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goldenowl.ecommerce.ui.global

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.goldenowl.ecommerce.R
import com.goldenowl.ecommerce.databinding.ActivityMainBinding
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/*todo
* change password
* forgot password
* save info, settings to firestore
* change avatar
*
* bottom sheet check hash vs old password
*
* bottomsheet shadow
* searchview close on submit
*
* category toggle show all -show by
* homepage viewpager
*
* guest: hide add to favorite
* */


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val TAG: String = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: create")
        super.onCreate(savedInstanceState)
//        setWindow()
        binding = ActivityMainBinding.inflate(layoutInflater)


        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        val navController = host.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.home_dest, R.id.bag_dest, R.id.shop_dest, R.id.favorites_dest, R.id.profile_dest),
        )

        setupBottomNavMenu(navController)

        navController.addOnDestinationChangedListener { controller, destination, _ ->

//            val dest: String = try {
//                resources.getResourceName(destination.id)
//            } catch (e: Resources.NotFoundException) {
//                destination.id.toString()
//            }
            Log.d(
                "NavigationActivity",
                "Navigated to=${destination.label} , from= ${navController.previousBackStackEntry?.destination?.label}"
            )

            when (destination.id) {
                R.id.home_dest, R.id.bag_dest, R.id.shop_dest, R.id.favorites_dest, R.id.profile_dest, R.id.category_dest -> {
                    showNavBar(true)
                }
                else -> showNavBar(false)
            }
        }

//        callFirestore()

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

    private fun showNavBar(b: Boolean) {
        binding.bottomNavView?.visibility = if (b) View.VISIBLE else View.GONE
    }

    private fun callFirestore() {
        Log.d(TAG, "callFirestore: firestore")
        val db = Firebase.firestore

        val user = hashMapOf(
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815
        )

// Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun setupToolBarLayout(
        toolbar: Toolbar,
        toolbarLayout: CollapsingToolbarLayout,
        navController: NavController,
        appBarConfiguration: AppBarConfiguration
    ) {
        toolbarLayout?.setupWithNavController(toolbar, navController, appBarConfiguration)
    }

    private fun setupBottomNavMenu(navController: NavController) {
        binding.bottomNavView?.apply {
            setupWithNavController(navController)
            setOnItemSelectedListener { item ->
                NavigationUI.onNavDestinationSelected(item, navController)
                navController.popBackStack(item.itemId, inclusive = false)
                true
            }
        }

    }

    private fun setupActionBar(navController: NavController, appBarConfiguration: AppBarConfiguration) {
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }

    fun setBottomNavBarEnabled(enabled: Boolean) {
        binding.bottomNavView?.isEnabled = enabled
    }
}

