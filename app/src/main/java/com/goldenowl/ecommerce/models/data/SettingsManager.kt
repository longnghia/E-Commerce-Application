package com.goldenowl.ecommerce.models.data

import android.content.Context
import android.content.SharedPreferences
import com.goldenowl.ecommerce.utils.Constants

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

    fun clear() {
        editor.remove(KEY_NOTIFICATION_SALE)
        editor.remove(KEY_NOTIFICATION_ARRIVES)
        editor.remove(KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE)
        editor.commit()
    }

    fun setDefaultCheckOut(default: Map<String, Int?>) {
        default[Constants.DEFAULT_CARD]?.let { editor.putInt(Constants.DEFAULT_CARD, it) }
        default[Constants.DEFAULT_ADDRESS]?.let { editor.putInt(Constants.DEFAULT_ADDRESS, it) }
        editor.commit()
    }

    fun getDefaultAddress(): Int {
        return settingManager.getInt(Constants.DEFAULT_ADDRESS, -1)
    }

    fun getDefaultCard(): Int {
        return settingManager.getInt(Constants.DEFAULT_ADDRESS, -1)
    }

    fun setLastNetwork(networkAvailable: Boolean) {
        editor.putBoolean(KEY_LAST_NETWORK, networkAvailable)
        editor.commit()
    }

    fun getLastNetwork(): Boolean {
        return settingManager.getBoolean(KEY_LAST_NETWORK, true)
    }

    fun getListHistory(): MutableSet<String>? {
        return settingManager.getStringSet(KEY_HISTORY, emptySet())
    }

    fun setListHistory(stack: MutableList<String>) {
        editor.putStringSet(KEY_HISTORY, stack.toSet())
        editor.commit()
    }

    companion object {
        const val KEY_FIRST_LAUNCH = "firstLaunch"
        const val KEY_LAST_NETWORK = "lastNetwork"
        const val KEY_DARK_MODE = "darkMode"
        const val KEY_NOTIFICATION_SALE = "notification_sales"
        const val KEY_NOTIFICATION_ARRIVES = "notification_arrives"
        const val KEY_NOTIFICATION_DELIVERY_STATUS_CHANGE = "notification_delivery_stt_change"
        const val KEY_HISTORY = "history"
    }
}