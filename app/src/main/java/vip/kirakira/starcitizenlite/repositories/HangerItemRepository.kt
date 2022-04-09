package vip.kirakira.starcitizenlite.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.database.HangerItem
import vip.kirakira.starcitizenlite.database.HangerPackage
import vip.kirakira.starcitizenlite.database.HangerPackageWithItems
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.network.hanger.HangerService

class HangerItemRepository(private val database: ShopItemDatabase) {
    val allItems: LiveData<List<HangerItem>> = database.hangerItemDao.getAllItems()
    val allPackagesAndItems: LiveData<List<HangerPackageWithItems>> = database.hangerItemDao.getAll()

    var isRefreshing = MutableLiveData<Boolean>(false)

    suspend fun refreshItems() {
        withContext(Dispatchers.IO) {
            var page = 1
            isRefreshing.postValue(true)
            while (true) {
                val data = HangerService().getHangerInfo(page)
                if (data.hangerPackages.isEmpty()) {
                    break
                }
                database.hangerItemDao.insertAllItems(data.hangerItems)
                database.hangerItemDao.insertAllPackages(data.hangerPackages)
                page++
            }
            isRefreshing.postValue(false)
        }
    }

}