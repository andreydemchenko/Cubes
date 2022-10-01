package ru.turbopro.cubes.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import ru.turbopro.cubes.CubesApplication
import ru.turbopro.cubes.data.Product
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.Result.Error
import ru.turbopro.cubes.data.Result.Success
import ru.turbopro.cubes.data.CubsAppSessionManager
import ru.turbopro.cubes.data.UserData
import ru.turbopro.cubes.data.utils.StoreProductDataStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.Month
import java.util.*

class ShopViewModel(application: Application) : AndroidViewModel(application) {

	private val TAG = OrderViewModel::class.java.simpleName

	private val productsRepository =
		(application.applicationContext as CubesApplication).productsRepository
	private val authRepository =
		(application.applicationContext as CubesApplication).authRepository

	private val sessionManager = CubsAppSessionManager(application.applicationContext)
	private val currentUser = sessionManager.getUserIdFromSession()
	val isUserASeller = sessionManager.isUserSeller()

	private var _products = MutableLiveData<List<Product>>()
	val products: LiveData<List<Product>> get() = _products

	private lateinit var _allProducts: MutableLiveData<List<Product>>
	val allProducts: LiveData<List<Product>> get() = _allProducts

	private var _userProducts = MutableLiveData<List<Product>>()
	val userProducts: LiveData<List<Product>> get() = _userProducts

	private var _userOrders = MutableLiveData<List<UserData.OrderItem>>()
	val userOrders: LiveData<List<UserData.OrderItem>> get() = _userOrders

	private var _userAddresses = MutableLiveData<List<UserData.Address>>()
	val userAddresses: LiveData<List<UserData.Address>> get() = _userAddresses

	private var _selectedOrder = MutableLiveData<UserData.OrderItem?>()
	val selectedOrder: LiveData<UserData.OrderItem?> get() = _selectedOrder

	private var _orderProducts = MutableLiveData<List<Product>>()
	val orderProducts: LiveData<List<Product>> get() = _orderProducts

	private var _likedProducts = MutableLiveData<List<Product>>()
	val likedProducts: LiveData<List<Product>> get() = _likedProducts

	private var _userLikes = MutableLiveData<List<String>>()
	val userLikes: LiveData<List<String>> get() = _userLikes

	private var _filterCategory = MutableLiveData("All")
	val filterCategory: LiveData<String> get() = _filterCategory

	private val _storeDataStatus = MutableLiveData<StoreProductDataStatus>()
	val storeProductDataStatus: LiveData<StoreProductDataStatus> get() = _storeDataStatus

	private val _dataStatus = MutableLiveData<StoreProductDataStatus>()
	val productDataStatus: LiveData<StoreProductDataStatus> get() = _dataStatus

	private val _userData = MutableLiveData<UserData?>()
	val userData: LiveData<UserData?> get() = _userData

	init {
		viewModelScope.launch {
			authRepository.hardRefreshUserData()
			getUserLikes()
		}

		if (isUserASeller)
			getProductsByOwner()
		else
			getProducts()
	}

	fun setDataLoaded() {
		_storeDataStatus.value = StoreProductDataStatus.DONE
	}

	fun isProductLiked(productId: String): Boolean {
		return _userLikes.value?.contains(productId) == true
	}

	fun toggleLikeByProductId(productId: String) {
		Log.d(TAG, "Toggling Like")
		viewModelScope.launch {
			val isLiked = isProductLiked(productId)
			val allLikes = _userLikes.value?.toMutableList() ?: mutableListOf()
			val deferredRes = async {
				if (isLiked) {
					authRepository.removeProductFromLikes(productId, currentUser!!)
				} else {
					authRepository.insertProductToLikes(productId, currentUser!!)
				}
			}
			val res = deferredRes.await()
			if (res is Success) {
				if (isLiked) {
					allLikes.remove(productId)
				} else {
					allLikes.add(productId)
				}
				_userLikes.value = allLikes
				val proList = _likedProducts.value?.toMutableList() ?: mutableListOf()
				val pro = proList.find { it.productId == productId }
				if (pro != null) {
					proList.remove(pro)
				}
				_likedProducts.value = proList
				Log.d(TAG, "onToggleLike: Success")
			} else {
				if (res is Error) {
					Log.d(TAG, "onToggleLike: Error, ${res.exception}")
				}
			}
		}
	}

	fun checkProductInCart(productId: String) : Boolean {
		var isProductInCart = false
		viewModelScope.launch {
			val deferredRes = async { authRepository.getUserData(currentUser!!) }
			val userRes = deferredRes.await()
			if (userRes is Success) {
				val uData = userRes.data
				if (uData != null) {
					val cartList = uData.cart
					val idx = cartList.indexOfFirst { it.productId == productId }
					isProductInCart = idx >= 0
					Log.d(TAG, "Checking in Cart: Success, value = ${isProductInCart}, ${cartList.size}")
				} else {
					isProductInCart = false
				}
			} else {
				isProductInCart = false
			}
		}
		return isProductInCart
	}

	fun toggleProductInCart(product: Product) {

	}

	fun setDataLoading() {
		_dataStatus.value = StoreProductDataStatus.LOADING
	}

	private fun getProducts() {
		_allProducts = Transformations.switchMap(productsRepository.observeProducts()) {
			getProductsLiveData(it)
		} as MutableLiveData<List<Product>>
		viewModelScope.launch {
			_storeDataStatus.value = StoreProductDataStatus.LOADING
			val res = async { productsRepository.refreshProducts() }
			res.await()
			Log.d(TAG, "getAllProducts: status = ${_storeDataStatus.value}")
		}
	}

	fun getUserLikes() {
		viewModelScope.launch {
			val res = authRepository.getLikesByUserId(currentUser!!)
			if (res is Success) {
				val data = res.data ?: emptyList()
				if (data[0] != "") {
					_userLikes.value = data
				} else {
					_userLikes.value = emptyList()
				}
				Log.d(TAG, "Getting Likes: Success")
			} else {
				_userLikes.value = emptyList()
				if (res is Error)
					Log.d(TAG, "Getting Likes: Error, ${res.exception}")
			}
		}
	}

	fun getLikedProducts() {
		val res: List<Product> = if (_userLikes.value != null) {
			val allLikes = _userLikes.value ?: emptyList()
			if (!allLikes.isNullOrEmpty()) {
				Log.d(TAG, "alllikes = ${allLikes.size}")
				_dataStatus.value = StoreProductDataStatus.DONE
				allLikes.map { proId ->
					_allProducts.value?.find { it.productId == proId } ?: Product()
				}
			} else {
				_dataStatus.value = StoreProductDataStatus.ERROR
				emptyList()
			}
		} else {
			_dataStatus.value = StoreProductDataStatus.ERROR
			emptyList()
		}
		_likedProducts.value = res
	}

	private fun getProductsLiveData(result: Result<List<Product>?>?): LiveData<List<Product>> {
		val res = MutableLiveData<List<Product>>()
		if (result is Success) {
			Log.d(TAG, "result is success")
			_storeDataStatus.value = StoreProductDataStatus.DONE
			res.value = result.data ?: emptyList()
		} else {
			Log.d(TAG, "result is not success")
			res.value = emptyList()
			_storeDataStatus.value = StoreProductDataStatus.ERROR
			if (result is Error)
				Log.d(TAG, "getProductsLiveData: Error Occurred: ${result.exception}")
		}
		return res
	}

	private fun getProductsByOwner() {
		_allProducts =
			Transformations.switchMap(productsRepository.observeProductsByOwner(currentUser!!)) {
				Log.d(TAG, it.toString())
				getProductsLiveData(it)
			} as MutableLiveData<List<Product>>
		viewModelScope.launch {
			_storeDataStatus.value = StoreProductDataStatus.LOADING
			val res = async { productsRepository.refreshProducts() }
			res.await()
			Log.d(TAG, "getProductsByOwner: status = ${_storeDataStatus.value}")
		}
	}

	fun refreshProducts() {
		getProducts()
	}

	fun filterBySearch(queryText: String) {
		filterProducts(_filterCategory.value!!)
		_products.value = _products.value?.filter { product ->
			product.name.contains(queryText, true) or
					((queryText.toDoubleOrNull() ?: 0.0).compareTo(product.price) == 0)
		}
	}

	fun filterProducts(filterType: String) {
		Log.d(TAG, "filterType is $filterType")
		_filterCategory.value = filterType
		_products.value = when (filterType) {
			"None" -> emptyList()
			"All" -> _allProducts.value
			else -> _allProducts.value?.filter { product ->
				product.category == filterType
			}
		}
	}

	fun deleteProduct(productId: String) {
		viewModelScope.launch {
			val delRes = async { productsRepository.deleteProductById(productId) }
			when (val res = delRes.await()) {
				is Success -> Log.d(TAG, "onDelete: Success")
				is Error -> Log.d(TAG, "onDelete: Error, ${res.exception}")
				else -> Log.d(TAG, "onDelete: Some error occurred!")
			}
		}
	}

	fun signOut() {
		viewModelScope.launch {
			val deferredRes = async { authRepository.signOut() }
			deferredRes.await()
		}
	}

	fun getAllOrders() {
		viewModelScope.launch {
			_storeDataStatus.value = StoreProductDataStatus.LOADING
			val deferredRes = async { authRepository.getOrdersByUserId(currentUser!!) }
			val res = deferredRes.await()
			if (res is Success) {
				_userOrders.value = res.data ?: emptyList()
				_storeDataStatus.value = StoreProductDataStatus.DONE
				Log.d(TAG, "Getting Orders: Success")
			} else {
				_userOrders.value = emptyList()
				_storeDataStatus.value = StoreProductDataStatus.ERROR
				if (res is Error)
					Log.d(TAG, "Getting Orders: Error, ${res.exception}")
			}
		}
	}

	fun getOrderDetailsByOrderId(orderId: String) {
		viewModelScope.launch {
			_storeDataStatus.value = StoreProductDataStatus.LOADING
			if (_userOrders.value != null) {
				val orderData = _userOrders.value!!.find { it.orderId == orderId }
				if (orderData != null) {
					_selectedOrder.value = orderData
					_orderProducts.value =
						orderData.items.map {
							_allProducts.value?.find { pro -> pro.productId == it.productId }
								?: Product()
						}
					_storeDataStatus.value = StoreProductDataStatus.DONE
				} else {
					_selectedOrder.value = null
					_storeDataStatus.value = StoreProductDataStatus.ERROR
				}
			}
		}
	}

	fun onSetStatusOfOrder(orderId: String, status: String) {
		val currDate = Calendar.getInstance()
		val dateString =
			"${Month.values()[(currDate.get(Calendar.MONTH))].name} ${
				currDate.get(Calendar.DAY_OF_MONTH)
			}, ${currDate.get(Calendar.YEAR)}"
		Log.d(TAG, "Selected Status is $status ON $dateString")
		setStatusOfOrder(orderId, "$status ON $dateString")
	}

	private fun setStatusOfOrder(orderId: String, statusString: String) {
		viewModelScope.launch {
			_storeDataStatus.value = StoreProductDataStatus.LOADING
			val deferredRes = async {
				authRepository.setStatusOfOrder(orderId, currentUser!!, statusString)
			}
			val res = deferredRes.await()
			if (res is Success) {
				val orderData = _selectedOrder.value
				orderData?.status = statusString
				_selectedOrder.value = orderData
				getOrderDetailsByOrderId(orderId)
			} else {
				_storeDataStatus.value = StoreProductDataStatus.ERROR
				if (res is Error)
					Log.d(TAG, "Error updating status, ${res.exception}")
			}
		}
	}

	fun getUserAddresses() {
		Log.d(TAG, "Getting Addresses")
		_dataStatus.value = StoreProductDataStatus.LOADING
		viewModelScope.launch {
			val res = authRepository.getAddressesByUserId(currentUser!!)
			if (res is Success) {
				_userAddresses.value = res.data ?: emptyList()
				_dataStatus.value = StoreProductDataStatus.DONE
				Log.d(TAG, "Getting Addresses: Success")
			} else {
				_userAddresses.value = emptyList()
				_dataStatus.value = StoreProductDataStatus.ERROR
				if (res is Error)
					Log.d(TAG, "Getting Addresses: Error Occurred, ${res.exception.message}")
			}
		}
	}

	fun deleteAddress(addressId: String) {
		viewModelScope.launch {
			val delRes = async { authRepository.deleteAddressById(addressId, currentUser!!) }
			when (val res = delRes.await()) {
				is Success -> {
					Log.d(TAG, "onDeleteAddress: Success")
					val addresses = _userAddresses.value?.toMutableList()
					addresses?.let {
						val pos =
							addresses.indexOfFirst { address -> address.addressId == addressId }
						if (pos >= 0)
							it.removeAt(pos)
						_userAddresses.value = it
					}
				}
				is Error -> Log.d(TAG, "onDeleteAddress: Error, ${res.exception}")
				else -> Log.d(TAG, "onDeleteAddress: Some error occurred!")
			}
		}
	}

	fun getUserData() {
		viewModelScope.launch {
			_dataStatus.value = StoreProductDataStatus.LOADING
			val deferredRes = async { authRepository.getUserData(currentUser!!) }
			val res = deferredRes.await()
			if (res is Success) {
				val uData = res.data
				_userData.value = uData
				_dataStatus.value = StoreProductDataStatus.DONE
			} else {
				_dataStatus.value = StoreProductDataStatus.ERROR
				_userData.value = null
			}
		}
	}
}