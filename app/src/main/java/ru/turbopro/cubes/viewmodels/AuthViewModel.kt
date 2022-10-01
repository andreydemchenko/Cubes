package ru.turbopro.cubes.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ru.turbopro.cubes.*
import ru.turbopro.cubes.data.UserData
import ru.turbopro.cubes.data.utils.LogInErrors
import ru.turbopro.cubes.data.utils.SignUpErrors
import ru.turbopro.cubes.data.utils.UserType
import ru.turbopro.cubes.ui.LoginViewErrors
import ru.turbopro.cubes.ui.SignUpViewErrors
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

class AuthViewModel(application: Application) : AndroidViewModel(application) {

	private val authRepository = (application as CubesApplication).authRepository
	private val productsRepository = (application as CubesApplication).productsRepository

	private val _userData = MutableLiveData<UserData>()
	val userData: LiveData<UserData> get() = _userData

	private val _signErrorStatus = MutableLiveData<SignUpErrors?>()
	val signErrorStatus: LiveData<SignUpErrors?> get() = _signErrorStatus

	private val _errorStatus = MutableLiveData<SignUpViewErrors>()
	val errorStatus: LiveData<SignUpViewErrors> get() = _errorStatus

	private val _errorStatusLoginFragment = MutableLiveData<LoginViewErrors>()
	val errorStatusLoginFragment: LiveData<LoginViewErrors> get() = _errorStatusLoginFragment

	private val _loginErrorStatus = MutableLiveData<LogInErrors?>()
	val loginErrorStatus: LiveData<LogInErrors?> get() = _loginErrorStatus

	var isUserLoggedIn = MutableLiveData(false)

	init {
		_errorStatus.value = SignUpViewErrors.NONE
		_errorStatusLoginFragment.value = LoginViewErrors.NONE
		refreshStatus()
	}

	private fun refreshStatus() {
		viewModelScope.launch {
			getCurrUser()
			productsRepository.refreshProducts()
		}
	}

	fun login(uData: UserData, rememberMe: Boolean, context: Context) {
		authRepository.signInWithEmailAndPassword(uData.email, uData.password, isUserLoggedIn, context)
		viewModelScope.launch {
			authRepository.login(uData, rememberMe)
		}
	}

	fun signUp(uData: UserData) {
		viewModelScope.launch {
			authRepository.signUp(uData)
		}
	}

	fun signUpSubmitData(
		name: String,
		mobile: String,
		email: String,
		pwd1: String,
		pwd2: String,
		isAccepted: Boolean,
		isSeller: Boolean
	) {
		if (name.isBlank() || mobile.isBlank() || email.isBlank() || pwd1.isBlank() || pwd2.isBlank()) {
			_errorStatus.value = SignUpViewErrors.ERR_EMPTY
		} else {
			if (pwd1 != pwd2) {
				_errorStatus.value = SignUpViewErrors.ERR_PWD12NS
			} else {
				if (!isAccepted) {
					_errorStatus.value = SignUpViewErrors.ERR_NOT_ACC
				} else {
					var err = ERR_INIT
					if (!isEmailValid(email)) {
						err += ERR_EMAIL
					}
					if (!isPhoneValid(mobile)) {
						err += ERR_MOBILE
					}
					when (err) {
						ERR_INIT -> {
							_errorStatus.value = SignUpViewErrors.NONE
							val uId = getRandomString(32, "+7" + mobile.trim(), 6)
							val newData =
								UserData(
									uId,
									name.trim(),
									"+7" + mobile.trim(),
									email.trim(),
									pwd1.trim(),
									0,
									0,
									0,
									"cardId",
									"imageUrl",
									ArrayList(),
									ArrayList(),
									ArrayList(),
									ArrayList(),
									ArrayList(),
									if (isSeller) UserType.SELLER.name else UserType.CUSTOMER.name
								)
							_userData.value = newData
							checkUniqueUser(newData)
						}
						(ERR_INIT + ERR_EMAIL) -> _errorStatus.value = SignUpViewErrors.ERR_EMAIL
						(ERR_INIT + ERR_MOBILE) -> _errorStatus.value = SignUpViewErrors.ERR_MOBILE
						(ERR_INIT + ERR_EMAIL + ERR_MOBILE) -> _errorStatus.value =
							SignUpViewErrors.ERR_EMAIL_MOBILE
					}
				}
			}
		}

	}

	private fun checkUniqueUser(uData: UserData) {
		viewModelScope.launch {
			val res = async { authRepository.checkEmailAndMobile(uData.email, uData.mobile, getApplication<CubesApplication>().applicationContext) }
			_signErrorStatus.value = res.await()
			println("${uData.mobile} \n _signErrorStatus.value = ${_signErrorStatus.value}")
		}
	}

	fun loginSubmitData(email: String, password: String) {
		if (email.isBlank() || password.isBlank()) {
			_errorStatusLoginFragment.value = LoginViewErrors.ERR_EMPTY
		} else {
			if (!isEmailValid(email)) {
				_errorStatusLoginFragment.value = LoginViewErrors.ERR_EMAIL
			} else {
				_errorStatusLoginFragment.value = LoginViewErrors.NONE
				login(email.trim(), password)
			}
		}
	}

	private fun login(email: String, pwd: String) {
		viewModelScope.launch {
			val res = async { authRepository.checkLogin(email, pwd) }
			_userData.value = res.await()
			if (_userData.value != null) {
				_loginErrorStatus.value = LogInErrors.NONE
				println("userData.value !!!!!!!!!!!!!======= null")
			} else {
				_loginErrorStatus.value = LogInErrors.LERR
				println("userData.value ======= null")
			}
		}
	}

	private suspend fun getCurrUser() {
		Log.d(TAG, "refreshing data...")
		authRepository.refreshData()
	}
}