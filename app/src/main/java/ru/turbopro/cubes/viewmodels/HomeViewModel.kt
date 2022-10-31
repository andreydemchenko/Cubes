package ru.turbopro.cubes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.turbopro.cubes.CubesApplication
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.CubsAppSessionManager
import ru.turbopro.cubes.data.UserData

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository =
        (application.applicationContext as CubesApplication).authRepository

    init {
        viewModelScope.launch {
            authRepository.hardRefreshUserData()
            getUserData()
        }
    }

    private val sessionManager = CubsAppSessionManager(application.applicationContext)
    private val currentUser = sessionManager.getUserIdFromSession()
    val isUserASeller = sessionManager.isUserSeller()

    private val _userData = MutableLiveData<UserData?>()
    val userData: LiveData<UserData?> get() = _userData

    //fun getUpcomingEvent() = sessionManager.getUserDataFromSession()

    fun getUserData() {
        viewModelScope.launch {
            val deferredRes = async { authRepository.getUserData(currentUser!!) }
            val res = deferredRes.await()
            if (res is Result.Success) {
                val uData = res.data
                _userData.value = uData
            } else {
                _userData.value = null
            }
        }
    }
}