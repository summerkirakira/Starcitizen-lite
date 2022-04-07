package vip.kirakira.viewpagertest.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.database.ShopItem
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.database.toShopItem
import vip.kirakira.starcitizenlite.network.RSIApi
import vip.kirakira.viewpagertest.network.graphql.UpdateCatalogMutation


class ShopItemRepository(private val database: ShopItemDatabase) {

    val allItems: LiveData<List<ShopItem>> = database.shopItemDao.getAll()

    suspend fun getAll() = database.shopItemDao.getAll()
    suspend fun getShopItem(id: Int) = database.shopItemDao.getById(id)
    suspend fun insertShopItems(shopItems: List<ShopItem>) = database.shopItemDao.insertAll(shopItems)

    suspend fun refreshItems() {
        withContext(Dispatchers.IO) {
            var page = 1;
            while (true) {
                val data = RSIApi.retrofitService.getCatalog(
                    UpdateCatalogMutation().getRequestBody(page)
                ).data.store.listing.resources
                if (data.isEmpty()) {
                    break
                }
                database.shopItemDao.insertAll(data.toShopItem())
                println("Crawling page: $page")
                page++
            }
        }
    }

}