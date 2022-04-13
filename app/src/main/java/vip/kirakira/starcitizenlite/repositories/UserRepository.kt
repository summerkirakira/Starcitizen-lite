package vip.kirakira.starcitizenlite.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.database.User

class UserRepository(private val database: ShopItemDatabase) {
    val user: LiveData<List<User>> = database.userDao.getAll()
    fun getUserById(id: Int): LiveData<User> {
        return database.userDao.getById(id)
    }

    suspend fun insertUser(user: User): Result<Int> {
        return withContext(Dispatchers.IO) {
            val id = database.userDao.insert(user)
            Result.success(user.id)
        }
    }

}