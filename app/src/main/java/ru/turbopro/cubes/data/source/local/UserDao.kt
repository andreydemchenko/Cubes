package ru.turbopro.cubes.data.source.local

import androidx.room.*
import ru.turbopro.cubes.data.UserData

@Dao
interface UserDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(uData: UserData)

	@Query("SELECT * FROM users WHERE userId = :userId")
	suspend fun getById(userId: String): UserData?

	@Query("SELECT * FROM users WHERE mobile = :mobile")
	suspend fun getByMobile(mobile: String): UserData?

	@Query("SELECT * FROM users WHERE mobile = :email")
	suspend fun getByEmail(email: String): UserData?

	@Update(entity = UserData::class)
	suspend fun updateUser(obj: UserData)

	@Query("DELETE FROM users")
	suspend fun clear()
}