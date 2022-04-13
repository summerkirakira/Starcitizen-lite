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
import vip.kirakira.starcitizenlite.network.rsi_cookie

class HangerItemRepository(private val database: ShopItemDatabase) {
//    val allItems: LiveData<List<HangerItem>> = database.hangerItemDao.getAllItems()
    val allPackagesAndItems: LiveData<List<HangerPackageWithItems>> = database.hangerItemDao.getAll()
    var hangerValue: Int = 0

    var isRefreshing = MutableLiveData<Boolean>(false)

    suspend fun refreshItems() {
        withContext(Dispatchers.IO) {
            var page = 1
            isRefreshing.postValue(true)
            if (rsi_cookie.contains("_rsi_device")) {
                val getTime = System.currentTimeMillis()
                try {
                    while (true) {
                        val data = HangerService().getHangerInfo(page)
                        if (data.hangerPackages.isEmpty()) {
                            break
                        }
                        database.hangerItemDao.insertAllItems(data.hangerItems)
                        database.hangerItemDao.insertAllPackages(data.hangerPackages)
                        page++
                    }
                    database.hangerItemDao.deleteAllOldPackage(getTime)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            isRefreshing.postValue(false)
        }
    }
    init {
        allPackagesAndItems.observeForever(){
            hangerValue = getHangerValue(it)
        }
    }

    fun getHangerValue(hangerPackageWithItems: List<HangerPackageWithItems>): Int {
        var value = 0
        for (item in hangerPackageWithItems) {
            value += item.hangerPackage.value
        }
        return value
    }

    fun getTotalValue(): Int {
        return hangerValue
    }

}