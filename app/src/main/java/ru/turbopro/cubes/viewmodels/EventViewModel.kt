package ru.turbopro.cubes.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.turbopro.cubes.CubesApplication
import ru.turbopro.cubes.data.*
import ru.turbopro.cubes.data.utils.AddEventObjectStatus
import ru.turbopro.cubes.data.utils.StoreEventDataStatus
import ru.turbopro.cubes.ui.AddEventItemErrors

private const val TAG = "EventViewModel"

class EventViewModel(private val eventId: String, application: Application) :
    AndroidViewModel(application) {

    private val _eventData = MutableLiveData<Event?>()
    val eventData: LiveData<Event?> get() = _eventData

    private var _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    private val _dataStatus = MutableLiveData<StoreEventDataStatus>()
    val eventDataStatus: LiveData<StoreEventDataStatus> get() = _dataStatus

    private val _errorStatus = MutableLiveData<List<AddEventItemErrors>>()
    val errorStatus: LiveData<List<AddEventItemErrors>> get() = _errorStatus

    private val _addItemStatus = MutableLiveData<AddEventObjectStatus?>()
    val addEventItemStatus: LiveData<AddEventObjectStatus?> get() = _addItemStatus

    private val eventsRepository = (application as CubesApplication).eventsRepository
    private val authRepository = (application as CubesApplication).authRepository
    private val sessionManager = CubsAppSessionManager(application.applicationContext)
    private val currentUserId = sessionManager.getUserIdFromSession()

    init {
        _errorStatus.value = emptyList()
        viewModelScope.launch {
            Log.d(TAG, "init: eventId: $eventId")
            getEventDetails()
        }
    }

    fun isSeller() = sessionManager.isUserSeller()

    fun getCountOfEventsList() = _eventData.value!!.images.count()

    private fun getEventDetails() {
        viewModelScope.launch {
            _dataStatus.value = StoreEventDataStatus.LOADING
            try {
                Log.d(TAG, "getting event Data")
                val res = eventsRepository.getEventById(eventId)
                if (res is Result.Success) {
                    _eventData.value = res.data
                    _dataStatus.value = StoreEventDataStatus.DONE
                } else if (res is Result.Error) {
                    throw Exception("Error getting event")
                }
            } catch (e: Exception) {
                _eventData.value = Event()
                _dataStatus.value = StoreEventDataStatus.ERROR
            }
        }
    }
}