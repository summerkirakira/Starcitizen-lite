package vip.kirakira.starcitizenlite.repositories

import androidx.lifecycle.LiveData
import vip.kirakira.starcitizenlite.database.HangerItem
import vip.kirakira.starcitizenlite.database.HangerPackage
import vip.kirakira.starcitizenlite.database.HangerPackageWithItems
import vip.kirakira.starcitizenlite.database.ShopItemDatabase

class HangerItemRepository(private val database: ShopItemDatabase) {
    val allItems: LiveData<List<HangerItem>> = database.hangerItemDao.getAllItems()
    val allPackagesAndItems: LiveData<List<HangerPackageWithItems>> = database.hangerItemDao.getAll()

    fun refresh() {
        TODO()
    }

}