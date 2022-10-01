package ru.turbopro.cubes.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.turbopro.cubes.CubesApplication
import ru.turbopro.cubes.data.Event
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.CubsAppSessionManager
import ru.turbopro.cubes.data.UserData
import ru.turbopro.cubes.data.utils.StoreEventDataStatus

private const val TAG = "EventsViewModel"

class EventsHomeViewModel(application: Application) : AndroidViewModel(application) {
    private val eventsRepository =
        (application.applicationContext as CubesApplication).eventsRepository
    private val authRepository =
        (application.applicationContext as CubesApplication).authRepository

    private val sessionManager = CubsAppSessionManager(application.applicationContext)
    private val currentUser = sessionManager.getUserIdFromSession()
    val isUserASeller = sessionManager.isUserSeller()

    private var _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    private lateinit var _allEvents: MutableLiveData<List<Event>>
    val allEvents: LiveData<List<Event>> get() = _allEvents

    private val _storeDataStatus = MutableLiveData<StoreEventDataStatus>()
    val storeEventDataStatus: LiveData<StoreEventDataStatus> get() = _storeDataStatus

    private val _dataStatus = MutableLiveData<StoreEventDataStatus>()
    val eventDataStatus: LiveData<StoreEventDataStatus> get() = _dataStatus

    private val _userData = MutableLiveData<UserData?>()
    val userData: LiveData<UserData?> get() = _userData

    init {
        viewModelScope.launch {
            authRepository.hardRefreshUserData()
        }
        getEvents()
    }

    fun setDataLoaded() {
        _storeDataStatus.value = StoreEventDataStatus.DONE
    }

    private fun getEvents() {
        _allEvents = Transformations.switchMap(eventsRepository.observeEvents()) {
            getEventsLiveData(it)
        } as MutableLiveData<List<Event>>
        viewModelScope.launch {
            _storeDataStatus.value = StoreEventDataStatus.LOADING
            val res = async { eventsRepository.refreshEvents() }
            res.await()
            Log.d(TAG, "getAllEvents: status = ${_storeDataStatus.value}")
        }
        _events.value = _allEvents.value
    }

    private fun getEventsLiveData(result: Result<List<Event>?>?): LiveData<List<Event>> {
        val res = MutableLiveData<List<Event>>()
        if (result is Result.Success) {
            Log.d(TAG, "result is success")
            _storeDataStatus.value = StoreEventDataStatus.DONE
            res.value = result.data ?: emptyList()
        } else {
            Log.d(TAG, "result is not success")
            res.value = emptyList()
            _storeDataStatus.value = StoreEventDataStatus.ERROR
            if (result is Result.Error)
                Log.d(TAG, "getEventsLiveData: Error Occurred: ${result.exception}")
        }
        return res
    }

    fun refreshEvents() {
        getEvents()
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            val delRes = async { eventsRepository.deleteEventById(eventId) }
            when (val res = delRes.await()) {
                is Result.Success -> Log.d(TAG, "onDelete: Success")
                is Result.Error -> Log.d(TAG, "onDelete: Error, ${res.exception}")
                else -> Log.d(TAG, "onDelete: Some error occurred!")
            }
        }
    }
}