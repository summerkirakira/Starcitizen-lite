package vip.kirakira.starcitizenlite.repositories

import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.RefugeApplication
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.network.main.getRandomBannerURL
import java.security.AccessControlContext

class BannerRepository(private val database: ShopItemDatabase) {
    val allItems = database.bannerDao.getAll()
    val isRefreshing = MutableLiveData<Boolean>(true)

    suspend fun refresh() {
        withContext(Dispatchers.IO) {
            isRefreshing.postValue(true)
            try {
                val items = getRandomBannerURL()
                for (item in items) {
                    Glide.with(RefugeApplication.getInstance()).downloadOnly().load(item.url).submit()
                    database.bannerDao.insert(item)
                }
//                database.bannerDao.insertAll(items)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isRefreshing.postValue(false)
        }
    }

}