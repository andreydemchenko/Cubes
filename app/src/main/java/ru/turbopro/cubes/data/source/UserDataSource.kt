package ru.turbopro.cubes.data.source

import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.UserData
import ru.turbopro.cubes.data.utils.EmailMobileData

interface UserDataSource {
	suspend fun addUser(userData: UserData)

	suspend fun getUserById(userId: String): Result<UserData?>

	fun updateEmailsAndMobiles(email: String, mobile: String) {}

	suspend fun getEmailsAndMobiles(): EmailMobileData? {
		return null
	}

	suspend fun getUserByMobileAndPassword(
		mobile: String,
		password: String
	): MutableList<UserData> {
		return mutableListOf()
	}

	suspend fun getUserByEmailAndPassword(
		email: String,
		password: String
	): MutableList<UserData> {
		return mutableListOf()
	}

	suspend fun getLessonNameByCode(qrCode: String): String? {
		return null
	}

	suspend fun isUserNotVisitedLesson(lessonName: String, userId: String): Boolean {
		return false
	}

	suspend fun addUserToLesson(lessonName: String, userId: String) {}

	suspend fun addPointsForVisiting(points: Int, userId: String) {}

	suspend fun likeProduct(productId: String, userId: String) {}

	suspend fun dislikeProduct(productId: String, userId: String) {}

	suspend fun insertAddress(newAddress: UserData.Address, userId: String) {}

	suspend fun updateAddress(newAddress: UserData.Address, userId: String) {}

	suspend fun deleteAddress(addressId: String, userId: String) {}

	suspend fun insertCartItem(newItem: UserData.CartItem, userId: String) {}

	suspend fun updateCartItem(item: UserData.CartItem, userId: String) {}

	suspend fun deleteCartItem(itemId: String, userId: String) {}

	suspend fun placeOrder(newOrder: UserData.OrderItem, userId: String) {}

	suspend fun setStatusOfOrderByUserId(orderId: String, userId: String, status: String) {}

	suspend fun clearUser() {}

	suspend fun getUserByMobile(phoneNumber: String): UserData? {
		return null
	}

	suspend fun getUserByEmail(email: String): UserData? {
		return null
	}

	suspend fun getOrdersByUserId(userId: String): Result<List<UserData.OrderItem>?>

	suspend fun getAddressesByUserId(userId: String): Result<List<UserData.Address>?>

	suspend fun getLikesByUserId(userId: String): Result<List<String>?>
}