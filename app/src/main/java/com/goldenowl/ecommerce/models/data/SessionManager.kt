package com.goldenowl.ecommerce.models.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

	var userSession: SharedPreferences =
		context.getSharedPreferences("userSessionData", Context.MODE_PRIVATE)
	var editor: SharedPreferences.Editor = userSession.edit()


	fun createLoginSession(
		id: String,
		name: String,
		email: String,
	) {
		editor.putBoolean(IS_LOGIN, true)
		editor.putString(KEY_ID, id)
		editor.putString(KEY_NAME, name)
		editor.putString(KEY_EMAIL, email)
		editor.commit()
	}

	fun getPhoneNumber(): String? = userSession.getString(KEY_MOBILE, null)

	fun getUserDataFromSession(): HashMap<String, String?> {
		return hashMapOf(
			KEY_ID to userSession.getString(KEY_ID, null),
			KEY_NAME to userSession.getString(KEY_NAME, null),
			KEY_EMAIL to userSession.getString(KEY_EMAIL, null)
		)
	}

	fun getUserIdFromSession(): String? = userSession.getString(KEY_ID, null)

	fun isLoggedIn(): Boolean = userSession.getBoolean(IS_LOGIN, false)

	fun logoutFromSession() {
		editor.clear()
		editor.commit()
	}

	companion object {
		private const val IS_LOGIN = "isLoggedIn"
		private const val KEY_NAME = "userName"
		private const val KEY_MOBILE = "userMobile"
		private const val KEY_EMAIL = "userEmail"
		private const val KEY_ID = "userId"
	}
}