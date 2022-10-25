package vip.kirakira.starcitizenlite.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.database.HangerItem
import vip.kirakira.starcitizenlite.database.HangerPackage
import vip.kirakira.starcitizenlite.database.HangerPackageWithItems
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.CirnoProperty.AddNotTranslationBody
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
            val notTranslatedItems: MutableList<AddNotTranslationBody> = mutableListOf()
            if (rsi_cookie.contains("_rsi_device")) {
                val getTime = System.currentTimeMillis()
                try {
                    while (true) {
                        val data = HangerService().getHangerInfo(page)
                        if (data.hangerPackages.isEmpty()) {
                            break
                        }
                        for (hangerPackage in data.hangerPackages) {
                            val contains: MutableList<String> = mutableListOf()
                            val content: MutableList<String> = mutableListOf()
                            for (hangerItem in data.hangerItems) {
                                if (hangerItem.package_id == hangerPackage.id) {
                                    content.add(hangerItem.title)
                                    contains.addAll(hangerPackage.also_contains.split("#"))
                                    notTranslatedItems.add(
                                        AddNotTranslationBody(
                                            product_id = hangerItem.package_id + 300000,
                                            type = "hanger",
                                            english_title = hangerItem.title,
                                            content = content,
                                            excerpt = "",
                                            contains = contains,
                                            from_ship = 0,
                                            to_ship = 0
                                        )
                                    )
                                    break
                                }
                            }
                        }
                        database.hangerItemDao.insertAllItems(data.hangerItems)
                        database.hangerItemDao.insertAllPackages(data.hangerPackages)
                        page++
                    }
                    CirnoApi.retrofitService.addNotTranslation(notTranslatedItems)
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