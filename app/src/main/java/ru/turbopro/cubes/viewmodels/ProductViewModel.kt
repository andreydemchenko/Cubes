package ru.turbopro.cubes.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ru.turbopro.cubes.CubesApplication
import ru.turbopro.cubes.data.Product
import ru.turbopro.cubes.data.Result.Error
import ru.turbopro.cubes.data.Result.Success
import ru.turbopro.cubes.data.CubsAppSessionManager
import ru.turbopro.cubes.data.UserData
import ru.turbopro.cubes.data.utils.AddProductObjectStatus
import ru.turbopro.cubes.data.utils.StoreProductDataStatus
import ru.turbopro.cubes.ui.AddProductItemErrors
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "ProductViewModel"

class ProductViewModel(private val productId: String, application: Application) :
	AndroidViewModel(application) {

	private val _productData = MutableLiveData<Product?>()
	val productData: LiveData<Product?> get() = _productData

	private val _dataStatus = MutableLiveData<StoreProductDataStatus>()
	val productDataStatus: LiveData<StoreProductDataStatus> get() = _dataStatus

	private val _errorStatus = MutableLiveData<List<AddProductItemErrors>>()
	val errorStatus: LiveData<List<AddProductItemErrors>> get() = _errorStatus

	private val _addItemStatus = MutableLiveData<AddProductObjectStatus?>()
	val addProductItemStatus: LiveData<AddProductObjectStatus?> get() = _addItemStatus

	private val _isLiked = MutableLiveData<Boolean>()
	val isLiked: LiveData<Boolean> get() = _isLiked

	private val _isItemInCart = MutableLiveData<Boolean>()
	val isItemInCart: LiveData<Boolean> get() = _isItemInCart

	private val productsRepository =(application as CubesApplication).productsRepository
	private val authRepository = (application as CubesApplication).authRepository
	private val sessionManager = CubsAppSessionManager(application.applicationContext)
	private val currentUserId = sessionManager.getUserIdFromSession()

	init {
		_isLiked.value = false
		_errorStatus.value = emptyList()
		viewModelScope.launch {
			Log.d(TAG, "init: productId: $productId")
			getProductDetails()
			checkIfInCart()
			setLike()
		}

	}

	private fun getProductDetails() {
		viewModelScope.launch {
			_dataStatus.value = StoreProductDataStatus.LOADING
			try {
				Log.d(TAG, "getting product Data")
				val res = productsRepository.getProductById(productId)
				if (res is Success) {
					_productData.value = res.data
					_dataStatus.value = StoreProductDataStatus.DONE
				} else if (res is Error) {
					throw Exception("Error getting product")
				}
			} catch (e: Exception) {
				_productData.value = Product()
				_dataStatus.value = StoreProductDataStatus.ERROR
			}
		}
	}

	fun setLike() {
		viewModelScope.launch {
			val res = authRepository.getLikesByUserId(currentUserId!!)
			if (res is Success) {
				val userLikes = res.data ?: emptyList()
				_isLiked.value = userLikes.contains(productId)
				Log.d(TAG, "Setting Like: Success, value = ${_isLiked.value}, ${res.data?.size}")
			} else {
				if (res is Error)
					Log.d(TAG, "Getting Likes: Error Occurred, ${res.exception.message}")
			}
		}
	}

	fun toggleLikeProduct() {
		Log.d(TAG, "toggling Like")
		viewModelScope.launch {
			val deferredRes = async {
				if (_isLiked.value == true) {
					authRepository.removeProductFromLikes(productId, currentUserId!!)
				} else {
					authRepository.insertProductToLikes(productId, currentUserId!!)
				}
			}
			val res = deferredRes.await()
			if (res is Success) {
				_isLiked.value = !_isLiked.value!!
			} else{
				if(res is Error)
					Log.d(TAG, "Error toggling like, ${res.exception}")
			}
		}
	}

	fun isSeller() = sessionManager.isUserSeller()

	fun getCountOfProductsList() = _productData.value!!.images.count()

	fun checkIfInCart() {
		viewModelScope.launch {
			val deferredRes = async { authRepository.getUserData(currentUserId!!) }
			val userRes = deferredRes.await()
			if (userRes is Success) {
				val uData = userRes.data
				if (uData != null) {
					val cartList = uData.cart
					val idx = cartList.indexOfFirst { it.productId == productId }
					_isItemInCart.value = idx >= 0
					Log.d(TAG, "Checking in Cart: Success, value = ${_isItemInCart.value}, ${cartList.size}")
				} else {
					_isItemInCart.value = false
				}
			} else {
				_isItemInCart.value = false
			}
		}
	}

	fun addToCart(size: Int?, color: String?) {
		val errList = mutableListOf<AddProductItemErrors>()
		if (size == null) errList.add(AddProductItemErrors.ERROR_SIZE)
		if (color.isNullOrBlank()) errList.add(AddProductItemErrors.ERROR_COLOR)

		if (errList.isEmpty()) {
			val itemId = UUID.randomUUID().toString()
			val newItem = UserData.CartItem(
				itemId, productId, productData.value!!.owner, 1, color, size
			)
			insertCartItem(newItem)
		}
	}

	private fun insertCartItem(item: UserData.CartItem) {
		viewModelScope.launch {
			_addItemStatus.value = AddProductObjectStatus.ADDING
			val deferredRes = async {
				authRepository.insertCartItemByUserId(item, currentUserId!!)
			}
			val res = deferredRes.await()
			if (res is Success) {
				Log.d(TAG, "onAddProductItem: Success")
				_addItemStatus.value = AddProductObjectStatus.DONE
			} else {
				_addItemStatus.value = AddProductObjectStatus.ERR_ADD
				if (res is Error) {
					Log.d(TAG, "onAddProductItem: Error, ${res.exception.message}")
				}
			}
		}
	}
}