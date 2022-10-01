package ru.turbopro.cubes.data

import android.content.Context
import android.content.SharedPreferences

class CubsAppSessionManager(context: Context) {

	var userSession: SharedPreferences =
		context.getSharedPreferences("userSessionData", Context.MODE_PRIVATE)
	var editor: SharedPreferences.Editor = userSession.edit()


	fun createLoginSession(
		id: String,
		name: String,
		mobile: String,
		email: String,
		points: Int,
		allPoints: Int,
		hours: Int,
		cardId: String,
		userImageUrl: String,
		isRemOn: Boolean,
		isSeller: Boolean
	) {
		editor.putBoolean(IS_LOGIN, true)
		editor.putString(KEY_ID, id)
		editor.putString(KEY_NAME, name)
		editor.putString(KEY_MOBILE, mobile)
		editor.putString(KEY_EMAIL, email)
		editor.putInt(KEY_POINTS, points)
		editor.putInt(KEY_ALL_POINTS, allPoints)
		editor.putInt(KEY_HOURS, hours)
		editor.putString(KEY_CARD_ID, cardId)
		editor.putString(KEY_USER_IMAGE_URL, userImageUrl)
		editor.putBoolean(KEY_REMEMBER_ME, isRemOn)
		editor.putBoolean(KEY_IS_SELLER, isSeller)

		editor.commit()
	}

	fun isUserSeller(): Boolean = userSession.getBoolean(KEY_IS_SELLER, false)

	fun isRememberMeOn(): Boolean = userSession.getBoolean(KEY_REMEMBER_ME, false)

	fun getPhoneNumber(): String? = userSession.getString(KEY_MOBILE, null)

	fun getEmailFromSession(): String? = userSession.getString(KEY_EMAIL, null)

	fun getUserDataFromSession(): HashMap<String, String?> {
		return hashMapOf(
			KEY_ID to userSession.getString(KEY_ID, null),
			KEY_NAME to userSession.getString(KEY_NAME, null),
			KEY_MOBILE to userSession.getString(KEY_MOBILE, null),
			KEY_EMAIL to userSession.getString(KEY_EMAIL, null)
		)
	}

	fun getUserIdFromSession(): String? = userSession.getString(KEY_ID, null)

	fun getUserPointsFromSession(): String? = userSession.getString(KEY_POINTS, null)

	fun getWhenUpcomingEventIsFromSession(): String? = userSession.getString(KEY_WHEN_UPCOMING_EVEN_IS, null)

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
		private const val KEY_POINTS = "points"
		private const val KEY_ALL_POINTS = "allPoints"
		private const val KEY_CARD_ID = "cardId"
		private const val KEY_HOURS = "hours"
		private const val KEY_USER_IMAGE_URL = "userImageUrl"
		private const val KEY_ACHIEVEMENTS = "achievements"
		private const val KEY_WHEN_UPCOMING_EVEN_IS = "upcomingEvent"
		private const val KEY_REMEMBER_ME = "isRemOn"
		private const val KEY_IS_SELLER = "isSeller"
	}
}