package ru.turbopro.cubes.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ru.turbopro.cubes.ERR_UPLOAD
import ru.turbopro.cubes.CubesApplication
import ru.turbopro.cubes.data.Product
import ru.turbopro.cubes.data.Result.Error
import ru.turbopro.cubes.data.Result.Success
import ru.turbopro.cubes.data.CubsAppSessionManager
import ru.turbopro.cubes.data.utils.AddEditProductErrors
import ru.turbopro.cubes.data.utils.StoreProductDataStatus
import ru.turbopro.cubes.getProductId
import ru.turbopro.cubes.ui.AddProductViewErrors
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "AddEditViewModel"

class AddEditProductViewModel(application: Application) : AndroidViewModel(application) {

	private val productsRepository =
		(application.applicationContext as CubesApplication).productsRepository

	private val sessionManager = CubsAppSessionManager(application.applicationContext)

	private val currentUser = sessionManager.getUserIdFromSession()

	private val _selectedCategory = MutableLiveData<String>()
	val selectedCategory: LiveData<String> get() = _selectedCategory

	private val _productId = MutableLiveData<String>()
	val productId: LiveData<String> get() = _productId

	private val _isEdit = MutableLiveData<Boolean>()
	val isEdit: LiveData<Boolean> get() = _isEdit

	private val _errorStatus = MutableLiveData<AddProductViewErrors>()
	val errorStatus: LiveData<AddProductViewErrors> get() = _errorStatus

	private val _dataStatus = MutableLiveData<StoreProductDataStatus>()
	val productDataStatus: LiveData<StoreProductDataStatus> get() = _dataStatus

	private val _addProductErrors = MutableLiveData<AddEditProductErrors?>()
	val addEditProductErrors: LiveData<AddEditProductErrors?> get() = _addProductErrors

	private val _productData = MutableLiveData<Product>()
	val productData: LiveData<Product> get() = _productData

	@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
	val newProductData = MutableLiveData<Product>()

	init {
		_errorStatus.value = AddProductViewErrors.NONE
	}

	fun setIsEdit(state: Boolean) {
		_isEdit.value = state
	}

	fun setCategory(catName: String) {
		_selectedCategory.value = catName
	}

	fun setProductData(productId: String) {
		_productId.value = productId
		viewModelScope.launch {
			Log.d(TAG, "onLoad: Getting product Data")
			_dataStatus.value = StoreProductDataStatus.LOADING
			val res = async { productsRepository.getProductById(productId) }
			val proRes = res.await()
			if (proRes is Success) {
				val proData = proRes.data
				_productData.value = proData
				_selectedCategory.value = _productData.value!!.category
				Log.d(TAG, "onLoad: Successfully retrieved product data")
				_dataStatus.value = StoreProductDataStatus.DONE
			} else if (proRes is Error) {
				_dataStatus.value = StoreProductDataStatus.ERROR
				Log.d(TAG, "onLoad: Error getting product data")
				_productData.value = Product()
			}
		}
	}

	fun submitProduct(
		name: String,
		price: Double?,
		mrp: Double?,
		desc: String,
		sizes: List<Int>,
		colors: List<String>,
		imgList: List<Uri>,
	) {
		if (name.isBlank() || price == null || mrp == null || desc.isBlank() || sizes.isNullOrEmpty() || colors.isNullOrEmpty() || imgList.isNullOrEmpty()) {
			_errorStatus.value = AddProductViewErrors.EMPTY
		} else {
			if (price == 0.0 || mrp == 0.0) {
				_errorStatus.value = AddProductViewErrors.ERR_PRICE_0
			} else {
				_errorStatus.value = AddProductViewErrors.NONE
				val proId = if (_isEdit.value == true) _productId.value!! else
					getProductId(currentUser!!, selectedCategory.value!!)
				val newProduct =
					Product(
						proId,
						name.trim(),
						currentUser!!,
						desc.trim(),
						_selectedCategory.value!!,
						price,
						mrp,
						sizes,
						colors,
						emptyList(),
						0.0
					)
				newProductData.value = newProduct
				Log.d(TAG, "pro = $newProduct")
				if (_isEdit.value == true) {
					updateProduct(imgList)
				} else {
					insertProduct(imgList)
				}
			}
		}
	}

	private fun updateProduct(imgList: List<Uri>) {
		viewModelScope.launch {
			if (newProductData.value != null && _productData.value != null) {
				_addProductErrors.value = AddEditProductErrors.ADDING
				val resImg =
					async { productsRepository.updateImages(imgList, _productData.value!!.images) }
				val imagesPaths = resImg.await()
				newProductData.value?.images = imagesPaths
				if (newProductData.value?.images?.isNotEmpty() == true) {
					if (imagesPaths[0] == ERR_UPLOAD) {
						Log.d(TAG, "error uploading images")
						_addProductErrors.value = AddEditProductErrors.ERR_ADD_IMG
					} else {
						val updateRes =
							async { productsRepository.updateProduct(newProductData.value!!) }
						val res = updateRes.await()
						if (res is Success) {
							Log.d(TAG, "onUpdate: Success")
							_addProductErrors.value = AddEditProductErrors.NONE
						} else {
							Log.d(TAG, "onUpdate: Some error occurred!")
							_addProductErrors.value = AddEditProductErrors.ERR_ADD
							if (res is Error)
								Log.d(TAG, "onUpdate: Error, ${res.exception}")
						}
					}
				} else {
					Log.d(TAG, "Product images empty, Cannot Add Product")
				}
			} else {
				Log.d(TAG, "Product is Null, Cannot Add Product")
			}
		}
	}

	private fun insertProduct(imgList: List<Uri>) {
		viewModelScope.launch {
			if (newProductData.value != null) {
				_addProductErrors.value = AddEditProductErrors.ADDING
				val resImg = async { productsRepository.insertImages(imgList) }
				val imagesPaths = resImg.await()
				newProductData.value?.images = imagesPaths
				if (newProductData.value?.images?.isNotEmpty() == true) {
					if (imagesPaths[0] == ERR_UPLOAD) {
						Log.d(TAG, "error uploading images")
						_addProductErrors.value = AddEditProductErrors.ERR_ADD_IMG
					} else {
						val deferredRes = async {
							productsRepository.insertProduct(newProductData.value!!)
						}
						val res = deferredRes.await()
						if (res is Success) {
							Log.d(TAG, "onInsertProduct: Success")
							_addProductErrors.value = AddEditProductErrors.NONE
						} else {
							_addProductErrors.value = AddEditProductErrors.ERR_ADD
							if (res is Error)
								Log.d(TAG, "onInsertProduct: Error Occurred, ${res.exception}")
						}
					}
				} else {
					Log.d(TAG, "Product images empty, Cannot Add Product")
				}
			} else {
				Log.d(TAG, "Product is Null, Cannot Add Product")
			}
		}
	}
}