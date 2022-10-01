package ru.turbopro.cubes.data.utils

import java.util.*

enum class SignUpErrors { NONE, SERR }

enum class LogInErrors { NONE, LERR }

enum class AddEditProductErrors { NONE, ERR_ADD, ERR_ADD_IMG, ADDING }

enum class AddAchievementErrors { NONE, ERR_ADD, ERR_ADD_IMG, ADDING }

enum class AddEditEventErrors { NONE, ERR_ADD, ERR_ADD_IMG, ADDING }

enum class AddProductObjectStatus { DONE, ERR_ADD, ADDING }

enum class AddAchievementObjectStatus { DONE, ERR_ADD, ADDING }

enum class AddEventObjectStatus { DONE, ERR_ADD, ADDING }

enum class UserType { CUSTOMER, SELLER }

enum class OrderStatus { CONFIRMED, PACKAGING, PACKED, SHIPPING, SHIPPED, ARRIVING, DELIVERED }

enum class StoreProductDataStatus { LOADING, ERROR, DONE }

enum class StoreAchievementDataStatus { LOADING, ERROR, DONE }

enum class StoreEventDataStatus { LOADING, ERROR, DONE }

fun getISOCountriesMap(): Map<String, String> {
	val result = mutableMapOf<String, String>()
	val isoCountries = Locale.getISOCountries()
	val countriesList = isoCountries.map { isoCountry ->
		result[isoCountry] = Locale("", isoCountry).displayCountry
	}
	return result
}