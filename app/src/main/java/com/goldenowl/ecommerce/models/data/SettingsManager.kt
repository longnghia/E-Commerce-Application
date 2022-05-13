package com.goldenowl.ecommerce.models.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

	var settingsSession: SharedPreferences =
		context.getSharedPreferences("settings", Context.MODE_PRIVATE)
	var editor: SharedPreferences.Editor = settingsSession.edit()

	fun setFirstLaunch(isFirstLaunch: Boolean){
		editor.putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch)
		editor.commit()
	}
	fun setDarkTheme(darkTheme: Boolean){
		editor.putBoolean(KEY_DARK_MODE, darkTheme)
		editor.commit()
	}

	fun getFirstLaunch() = settingsSession.getBoolean(KEY_FIRST_LAUNCH, true)
	fun getDarkMode() = settingsSession.getBoolean(KEY_DARK_MODE, false)


	companion object {
		private const val KEY_FIRST_LAUNCH = "firstLaunch"
		private const val KEY_DARK_MODE = "darkMode"
	}
}