package com.goldenowl.ecommerce.models.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    var settingManager: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var editor: SharedPreferences.Editor = settingManager.edit()

    fun setFirstLaunch(isFirstLaunch: Boolean) {
        editor.putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch)
        editor.commit()
    }

    fun setDarkTheme(darkTheme: Boolean) {
        editor.putBoolean(KEY_DARK_MODE, darkTheme)
        editor.commit()
    }

    fun getFirstLaunch() = settingManager.getBoolean(KEY_FIRST_LAUNCH, true)
    fun getDarkMode() = settingManager.getBoolean(KEY_DARK_MODE, false)

    fun saveUserSettings(settings: Map<String, Boolean>) {
        editor.putBoolean(KEY_NOTIFICATION_SALE, settings[KEY_NOTIFICATION_SALE]!!)
        editor.putBoolean(KEY_NOTIFICATION_ARRIVES, settings[KEY_NOTIFICATION_ARRIVES]!!)
        editor.putBoolean(KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE, settings[KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE]!!)
        editor.commit()
    }

    fun getUserSettings(): Map<String, Boolean> {
        return mapOf(
            KEY_NOTIFICATION_SALE to settingManager.getBoolean(KEY_NOTIFICATION_SALE, false),
            KEY_NOTIFICATION_ARRIVES to settingManager.getBoolean(KEY_NOTIFICATION_ARRIVES, false),
            KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE to settingManager.getBoolean(
                KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE,
                false
            )
        )
    }

    companion object {
        const val KEY_FIRST_LAUNCH = "firstLaunch"
        const val KEY_DARK_MODE = "darkMode"
        const val KEY_NOTIFICATION_SALE = "notification_sales"
        const val KEY_NOTIFICATION_ARRIVES = "notification_arrives"
        const val KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE = "notification_delivery_stt_change"
    }
}