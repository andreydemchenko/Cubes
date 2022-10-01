package ru.turbopro.cubes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ru.turbopro.cubes.CubesApplication
import ru.turbopro.cubes.data.CubsAppSessionManager

private const val TAG = "AchievementViewModel"

class AchievementViewModel(private val achievementId: String, application: Application) :
    AndroidViewModel(application) {



    private val achievementsRepository = (application as CubesApplication).achievementsRepository
    private val authRepository = (application as CubesApplication).authRepository
    private val sessionManager = CubsAppSessionManager(application.applicationContext)
    private val currentUserId = sessionManager.getUserIdFromSession()
}