package ru.turbopro.cubes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.turbopro.cubes.CubesApplication
import ru.turbopro.cubes.data.CubsAppSessionManager
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.UserData

class QRCodeScannerViewModel(application: Application) : AndroidViewModel(application) {

    val authRepository = (application as CubesApplication).authRepository
    private val sessionManager = CubsAppSessionManager(application.applicationContext)
    private val currentUser = sessionManager.getUserIdFromSession()

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> get() = _userData

    init {
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
