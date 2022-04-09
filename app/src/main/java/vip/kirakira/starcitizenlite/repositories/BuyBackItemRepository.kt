package vip.kirakira.starcitizenlite.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.network.hanger.HangerService

class BuyBackItemRepository(private val database: ShopItemDatabase) {
    val allItems: LiveData<List<BuybackItem>> = database.buybackItemDao.getAll()
    var isRefreshing = MutableLiveData<Boolean>(false)
    suspend fun refreshItems() {

        withContext(Dispatchers.IO) {
            var page = 1
            isRefreshing.postValue(true)
            while (true) {
                val data = HangerService().getBuybackInfo(page)
                if (data.isEmpty()) {
                    break
                }
                database.buybackItemDao.insertAll(data)
                page++
            }
        }
        isRefreshing.postValue(false)
    }

}