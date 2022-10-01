package ru.turbopro.cubes.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.turbopro.cubes.data.Product
import ru.turbopro.cubes.data.UserData
import ru.turbopro.cubes.data.Achievement
import ru.turbopro.cubes.data.Event
import ru.turbopro.cubes.data.utils.DateTypeConvertors
import ru.turbopro.cubes.data.utils.ListTypeConverter
import ru.turbopro.cubes.data.utils.ObjectListTypeConvertor

@Database(entities = [UserData::class, Product::class, Achievement::class, Event::class], version = 3)
@TypeConverters(ListTypeConverter::class, ObjectListTypeConvertor::class, DateTypeConvertors::class)
abstract class ShoppingAppDatabase : RoomDatabase() {
	abstract fun userDao(): UserDao
	abstract fun productsDao(): ProductsDao
	abstract fun achievementsDao(): AchievementsDao
	abstract fun eventsDao(): EventsDao

	companion object {
		@Volatile
		private var INSTANCE: ShoppingAppDatabase? = null

		fun getInstance(context: Context): ShoppingAppDatabase =
			INSTANCE ?: synchronized(this) {
				INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
			}

		private fun buildDatabase(context: Context) =
			Room.databaseBuilder(
				context.applicationContext,
				ShoppingAppDatabase::class.java, "ShoppingAppDb"
			)
				.fallbackToDestructiveMigration()
				.allowMainThreadQueries()
				.build()
	}
}