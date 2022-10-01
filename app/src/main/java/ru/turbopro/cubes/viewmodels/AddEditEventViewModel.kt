package ru.turbopro.cubes.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.turbopro.cubes.ERR_UPLOAD
import ru.turbopro.cubes.CubesApplication
import ru.turbopro.cubes.data.Event
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.data.CubsAppSessionManager
import ru.turbopro.cubes.data.utils.AddEditEventErrors
import ru.turbopro.cubes.data.utils.StoreEventDataStatus
import ru.turbopro.cubes.getEventId
import ru.turbopro.cubes.ui.AddEditEventViewErrors

private const val TAG = "AddEditEventViewModel"

class AddEditEventViewModel(application: Application) : AndroidViewModel(application) {

    private val eventsRepository =
        (application.applicationContext as CubesApplication).eventsRepository

    private val sessionManager = CubsAppSessionManager(application.applicationContext)

    private val currentUser = sessionManager.getUserIdFromSession()

    private val _eventId = MutableLiveData<String>()
    val eventId: LiveData<String> get() = _eventId

    private val _errorStatus = MutableLiveData<AddEditEventViewErrors>()
    val errorStatus: LiveData<AddEditEventViewErrors> get() = _errorStatus

    private val _dataStatus = MutableLiveData<StoreEventDataStatus>()
    val eventDataStatus: LiveData<StoreEventDataStatus> get() = _dataStatus

    private val _isEdit = MutableLiveData<Boolean>()
    val isEdit: LiveData<Boolean> get() = _isEdit

    private val _addEditEventErrors = MutableLiveData<AddEditEventErrors?>()
    val addEditEventErrors: LiveData<AddEditEventErrors?> get() = _addEditEventErrors

    private val _eventData = MutableLiveData<Event>()
    val eventData: LiveData<Event> get() = _eventData

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val newEventData = MutableLiveData<Event>()

    init {
        _errorStatus.value = AddEditEventViewErrors.NONE
    }

    fun setIsEdit(state: Boolean) {
        _isEdit.value = state
    }

    fun setEventData(eventId: String) {
        _eventId.value = eventId
        viewModelScope.launch {
            Log.d(TAG, "onLoad: Getting event Data")
            _dataStatus.value = StoreEventDataStatus.LOADING
            val res = async { eventsRepository.getEventById(eventId) }
            val evRes = res.await()
            if (evRes is Result.Success) {
                val evData = evRes.data
                _eventData.value = evData
                Log.d(TAG, "onLoad: Successfully retrieved event data")
                _dataStatus.value = StoreEventDataStatus.DONE
            } else if (evRes is Result.Error) {
                _dataStatus.value = StoreEventDataStatus.ERROR
                Log.d(TAG, "onLoad: Error getting event data")
                _eventData.value = Event()
            }
        }
    }

    fun submitEvent(
        name: String,
        date: String,
        desc: String,
        imgList: List<Uri>,
    ) {
        if (name.isBlank() || date.isBlank() || desc.isBlank() || imgList.isNullOrEmpty()) {
            _errorStatus.value = AddEditEventViewErrors.EMPTY
        } else {
            _errorStatus.value = AddEditEventViewErrors.NONE
            val evId = if (_isEdit.value == true) _eventId.value!! else
                getEventId(currentUser!!)
            val newEvent =
                Event(
                    evId,
                    name.trim(),
                    desc.trim(),
                    date.trim(),
                    emptyList()
                )
            newEventData.value = newEvent
            Log.d(TAG, "ev = $newEvent")
            if (_isEdit.value == true) {
                updateEvent(imgList)
            } else {
                insertEvent(imgList)
            }
        }
    }

    private fun updateEvent(imgList: List<Uri>) {
        viewModelScope.launch {
            if (newEventData.value != null && _eventData.value != null) {
                _addEditEventErrors.value = AddEditEventErrors.ADDING
                val resImg =
                    async { eventsRepository.updateImages(imgList, _eventData.value!!.images) }
                val imagesPaths = resImg.await()
                newEventData.value?.images = imagesPaths
                if (newEventData.value?.images?.isNotEmpty() == true) {
                    if (imagesPaths[0] == ERR_UPLOAD) {
                        Log.d(TAG, "error uploading images")
                        _addEditEventErrors.value = AddEditEventErrors.ERR_ADD_IMG
                    } else {
                        val updateRes =
                            async { eventsRepository.updateEvent(newEventData.value!!) }
                        val res = updateRes.await()
                        if (res is Result.Success) {
                            Log.d(TAG, "onUpdate: Success")
                            _addEditEventErrors.value = AddEditEventErrors.NONE
                        } else {
                            Log.d(TAG, "onUpdate: Some error occurred!")
                            _addEditEventErrors.value = AddEditEventErrors.ERR_ADD
                            if (res is Result.Error)
                                Log.d(TAG, "onUpdate: Error, ${res.exception}")
                        }
                    }
                } else {
                    Log.d(TAG, "Event images empty, Cannot Add Event")
                }
            } else {
                Log.d(TAG, "Event is Null, Cannot Add Event")
            }
        }
    }

    private fun insertEvent(imgList: List<Uri>) {
        viewModelScope.launch {
            if (newEventData.value != null) {
                _addEditEventErrors.value = AddEditEventErrors.ADDING
                val resImg = async { eventsRepository.insertImages(imgList) }
                val imagesPaths = resImg.await()
                newEventData.value?.images = imagesPaths
                if (newEventData.value?.images?.isNotEmpty() == true) {
                    if (imagesPaths[0] == ERR_UPLOAD) {
                        Log.d(TAG, "error uploading images")
                        _addEditEventErrors.value = AddEditEventErrors.ERR_ADD_IMG
                    } else {
                        val deferredRes = async {
                            eventsRepository.insertEvent(newEventData.value!!)
                        }
                        val res = deferredRes.await()
                        if (res is Result.Success) {
                            Log.d(TAG, "onInsertEvent: Success")
                            _addEditEventErrors.value = AddEditEventErrors.NONE
                        } else {
                            _addEditEventErrors.value = AddEditEventErrors.ERR_ADD
                            if (res is Result.Error)
                                Log.d(TAG, "onInsertEvent: Error Occurred, ${res.exception}")
                        }
                    }
                } else {
                    Log.d(TAG, "Event images empty, Cannot Add Event")
                }
            } else {
                Log.d(TAG, "Event is Null, Cannot Add Event")
            }
        }
    }
}