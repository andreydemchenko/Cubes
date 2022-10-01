package ru.turbopro.cubes.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.turbopro.cubes.data.utils.ObjectListTypeConvertor
import ru.turbopro.cubes.data.utils.OrderStatus
import ru.turbopro.cubes.data.utils.UserType
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
@Entity(tableName = "users")
data class UserData(
	@PrimaryKey
	var userId: String = "",
	var name: String = "",
	var mobile: String = "",
	var email: String = "",
	var password: String = "",
	var hours: Int = 0,
	var points: Int = 0,
	var allPoints: Int = 0,
	var cardId: String = "",
	var userImageUrl: String = "",
	var likes: List<String> = ArrayList(),
	@TypeConverters(ObjectListTypeConvertor::class)
	var addresses: List<Address> = ArrayList(),
	@TypeConverters(ObjectListTypeConvertor::class)
	var progressAchiv: List<String> = ArrayList(),
	@TypeConverters(ObjectListTypeConvertor::class)
	var cart: List<CartItem> = ArrayList(),
	@TypeConverters(ObjectListTypeConvertor::class)
	var orders: List<OrderItem> = ArrayList(),
	var userType: String = UserType.CUSTOMER.name
) : Parcelable {
	fun toHashMap(): HashMap<String, Any> {
		return hashMapOf(
			"userId" to userId,
			"name" to name,
			"email" to email,
			"mobile" to mobile,
			"password" to password,
			"hours" to hours,
			"points" to points,
			"allPoints" to allPoints,
			"cardId" to cardId,
			"userImageUrl" to userImageUrl,
			"likes" to likes,
			"addresses" to addresses.map { it.toHashMap() },
			"achievements" to progressAchiv,
			"userType" to userType
		)
	}

	@Parcelize
	data class OrderItem(
		var orderId: String = "",
		var customerId: String = "",
		var items: List<CartItem> = ArrayList(),
		var itemsPrices: Map<String, Double> = mapOf(),
		var deliveryAddress: Address = Address(),
		var shippingCharges: Double = 0.0,
		var paymentMethod: String = "",
		var orderDate: Date = Date(),
		var status: String = OrderStatus.CONFIRMED.name
	) : Parcelable {
		fun toHashMap(): HashMap<String, Any> {
			return hashMapOf(
				"orderId" to orderId,
				"customerId" to customerId,
				"items" to items.map { it.toHashMap() },
				"itemsPrices" to itemsPrices,
				"deliveryAddress" to deliveryAddress.toHashMap(),
				"shippingCharges" to shippingCharges,
				"paymentMethod" to paymentMethod,
				"orderDate" to orderDate,
				"status" to status
			)
		}
	}

	@Parcelize
	data class Address(
		var addressId: String = "",
		var fName: String = "",
		var lName: String = "",
		var countryISOCode: String = "",
		var streetAddress: String = "",
		var streetAddress2: String = "",
		var city: String = "",
		var state: String = "",
		var zipCode: String = "",
		var phoneNumber: String = ""
	) : Parcelable {
		fun toHashMap(): HashMap<String, Any> {
			return hashMapOf(
				"addressId" to addressId,
				"fName" to fName,
				"lName" to lName,
				"countryISOCode" to countryISOCode,
				"streetAddress" to streetAddress,
				"streetAddress2" to streetAddress2,
				"city" to city,
				"state" to state,
				"zipCode" to zipCode,
				"phoneNumber" to phoneNumber
			)
		}
	}

	@Parcelize
	data class CartItem(
		var itemId: String = "",
		var productId: String = "",
		var ownerId: String = "",
		var quantity: Int = 0,
		var color: String?,
		var size: Int?
	) : Parcelable {
		constructor() : this("", "", "", 0, "NA", -1)

		fun toHashMap(): HashMap<String, Any> {
			val hashMap = hashMapOf<String, Any>()
			hashMap["itemId"] = itemId
			hashMap["productId"] = productId
			hashMap["ownerId"] = ownerId
			hashMap["quantity"] = quantity
			if (color != null)
				hashMap["color"] = color!!
			if (size != null)
				hashMap["size"] = size!!
			return hashMap
		}
	}

	@Parcelize
	data class EventItem(
		var eventId: String = "",
		var name: String = "",
		var description: String = "",
		var date: String?,
		var images: List<String> = ArrayList()
	) : Parcelable {
		constructor() : this("", "", "", "", listOf(""))

		fun toHashMap(): HashMap<String, String> {
			val hashMap = hashMapOf<String, String>()
			hashMap["eventId"] = eventId
				hashMap["name"] = name
				hashMap["description"] = description
				hashMap["date"] = date!!
				hashMap["images"] = images.toString()
				return hashMap
		}
	}
}