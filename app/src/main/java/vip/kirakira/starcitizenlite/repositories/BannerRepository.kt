package vip.kirakira.starcitizenlite.repositories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.network.main.getRandomBannerURL
import vip.kirakira.starcitizenlite.network.search.getPlayerSearchResult

class BannerRepository(private val database: ShopItemDatabase) {
    val allItems = database.bannerDao.getAll()
    val isRefreshing = MutableLiveData<Boolean>(true)

    suspend fun refresh() {
        withContext(Dispatchers.IO) {
            isRefreshing.postValue(true)
            try {
                val items = getRandomBannerURL()
                database.bannerDao.insertAll(items)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isRefreshing.postValue(false)
        }
    }

}