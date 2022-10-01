package ru.turbopro.cubes

import android.content.Context
import androidx.annotation.VisibleForTesting
import ru.turbopro.cubes.data.CubsAppSessionManager
import ru.turbopro.cubes.data.source.AchievementDataSource
import ru.turbopro.cubes.data.source.EventDataSource
import ru.turbopro.cubes.data.source.ProductDataSource
import ru.turbopro.cubes.data.source.UserDataSource
import ru.turbopro.cubes.data.source.local.*
import ru.turbopro.cubes.data.source.remote.AchievementRemoteDataSource
import ru.turbopro.cubes.data.source.remote.AuthRemoteDataSource
import ru.turbopro.cubes.data.source.remote.EventRemoteDataSource
import ru.turbopro.cubes.data.source.remote.ProductsRemoteDataSource
import ru.turbopro.cubes.data.source.repository.*

object ServiceLocator {
	private var database: ShoppingAppDatabase? = null
	private val lock = Any()

	@Volatile
	var authRepository: AuthRepoInterface? = null
		@VisibleForTesting set

	@Volatile
	var productsRepository: ProductsRepoInterface? = null
		@VisibleForTesting set

	@Volatile
	var eventsRepository: EventsRepoInterface? = null
		@VisibleForTesting set

	@Volatile
	var achievementsRepository: AchievementsRepoInterface? = null
		@VisibleForTesting set

	fun provideAuthRepository(context: Context): AuthRepoInterface {
		synchronized(this) {
			return authRepository ?: createAuthRepository(context)
		}
	}

	fun provideProductsRepository(context: Context): ProductsRepoInterface {
		synchronized(this) {
			return productsRepository ?: createProductsRepository(context)
		}
	}

	fun provideEventsRepository(context: Context): EventsRepoInterface {
		synchronized(this) {
			return eventsRepository ?: createEventsRepository(context)
		}
	}

	fun provideAchievementsRepository(context: Context): AchievementsRepoInterface {
		synchronized(this) {
			return achievementsRepository ?: createAchievementsRepository(context)
		}
	}

	@VisibleForTesting
	fun resetRepository() {
		synchronized(lock) {
			database?.apply {
				clearAllTables()
				close()
			}
			database = null
			authRepository = null
		}
	}

	private fun createProductsRepository(context: Context): ProductsRepoInterface {
		val newRepo =
			ProductsRepository(ProductsRemoteDataSource(), createProductsLocalDataSource(context))
		productsRepository = newRepo
		return newRepo
	}

	private fun createAuthRepository(context: Context): AuthRepoInterface {
		val appSession = CubsAppSessionManager(context.applicationContext)
		val newRepo =
			AuthRepository(createUserLocalDataSource(context), AuthRemoteDataSource(), appSession)
		authRepository = newRepo
		return newRepo
	}

	private fun createEventsRepository(context: Context): EventsRepoInterface {
		val newRepo =
			EventsRepository(EventRemoteDataSource(), createEventsLocalDataSource(context))
		eventsRepository = newRepo
		return newRepo
	}

	private fun createAchievementsRepository(context: Context): AchievementsRepoInterface {
		val newRepo =
			AchievementsRepository(AchievementRemoteDataSource(), createAchievementsLocalDataSource(context))
		achievementsRepository = newRepo
		return newRepo
	}

	private fun createProductsLocalDataSource(context: Context): ProductDataSource {
		val database = database ?: ShoppingAppDatabase.getInstance(context.applicationContext)
		return ProductsLocalDataSource(database.productsDao())
	}

	private fun createUserLocalDataSource(context: Context): UserDataSource {
		val database = database ?: ShoppingAppDatabase.getInstance(context.applicationContext)
		return UserLocalDataSource(database.userDao())
	}

	private fun createEventsLocalDataSource(context: Context): EventDataSource {
		val database = database ?: ShoppingAppDatabase.getInstance(context.applicationContext)
		return EventsLocalDataSource(database.eventsDao())
	}

	private fun createAchievementsLocalDataSource(context: Context): AchievementDataSource {
		val database = database ?: ShoppingAppDatabase.getInstance(context.applicationContext)
		return AchievementsLocalDataSource(database.achievementsDao())
	}
}