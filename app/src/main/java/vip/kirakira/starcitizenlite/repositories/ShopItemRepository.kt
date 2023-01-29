package vip.kirakira.viewpagertest.repositories

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.*
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.CirnoProperty.AddNotTranslationBody
import vip.kirakira.starcitizenlite.network.RSIApi
import vip.kirakira.starcitizenlite.network.shop.InitShipUpgradeProperty
import vip.kirakira.starcitizenlite.repositories.TranslationRepository
import vip.kirakira.starcitizenlite.util.Translation
import vip.kirakira.viewpagertest.network.graphql.FilterShipsQuery
import vip.kirakira.viewpagertest.network.graphql.UpdateCatalogMutation
import vip.kirakira.viewpagertest.network.graphql.initShipUpgradeQuery


class ShopItemRepository(private val database: ShopItemDatabase) {

    val allItems: LiveData<List<ShopItem>> = database.shopItemDao.getAll()
    var isRefreshing: MutableLiveData<Boolean> = MutableLiveData(false)
    val translationRepository = TranslationRepository(database)

//    suspend fun getAll() = database.shopItemDao.getAll()
//    suspend fun getShopItem(id: Int) = database.shopItemDao.getById(id)
//    suspend fun insertShopItems(shopItems: List<ShopItem>) = database.shopItemDao.insertAll(shopItems)

    suspend fun refreshItems(application: Application) {
        val pref = application.getSharedPreferences(
            application.getString(R.string.preference_file_key),
            AppCompatActivity.MODE_PRIVATE
        )
        withContext(Dispatchers.IO) {
            var page = 1;
            isRefreshing.postValue(true)
            try {
                val notTranslatedItems: MutableList<AddNotTranslationBody> = mutableListOf()
                while (true) {
                    val data = RSIApi.retrofitService.getCatalog(
                        UpdateCatalogMutation().getRequestBody(page)
                    ).data.store.listing.resources
                    if (data.isEmpty()) {
                        break
                    }
                    val shopItems = data.toShopItem()
                    val isTranslationEnabled = pref.getBoolean(
                        application.getString(R.string.enable_localization_key),
                        false
                    )
                    if (isTranslationEnabled) {
                        for (shopItem in shopItems) {
                            if (!database.translationDao.isProductExist(shopItem.id)) {
                                notTranslatedItems.add(
                                    AddNotTranslationBody(
                                        product_id = shopItem.id,
                                        type = "product",
                                        english_title = shopItem.name,
                                        content = listOf(),
                                        excerpt = shopItem.excerpt,
                                        contains = listOf(),
                                        from_ship = 0,
                                        to_ship = 0
                                    )
                                )
                            } else {
                                val translation = database.translationDao.getByProductId(shopItem.id)
                                shopItem.chineseName = translation!!.title
                                shopItem.chineseDescription = translation.excerpt
                            }
                            shopItem.chineseSubtitle = Translation().translateShopItemSubtitle(shopItem.subtitle)
                        }
                    }
                    database.shopItemDao.insertAll(shopItems)
                    page++
                }
                CirnoApi.retrofitService.addNotTranslation(notTranslatedItems)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isRefreshing.postValue(false)
            translationRepository.refreshTranslation(application)
        }
    }

    suspend fun initShipUpgrades(application: Application) {
        val pref = application.getSharedPreferences(
            application.getString(R.string.preference_file_key),
            AppCompatActivity.MODE_PRIVATE
        )
        withContext(Dispatchers.IO) {
            try {
                isRefreshing.postValue(true)
                RSIApi.setAuthToken()
                RSIApi.setUpgradeToken()
                val data: InitShipUpgradeProperty = RSIApi.retrofitService.initShipUpgrade(initShipUpgradeQuery().getRequestBody())
                val notTranslatedItems: MutableList<AddNotTranslationBody> = mutableListOf()
                val shopUpgradeItems = data.data.ships.toUpgradeShopItem()
                val isTranslationEnabled = pref.getBoolean(
                    application.getString(R.string.enable_localization_key),
                    false
                )
                if (isTranslationEnabled) {
                    for (shopUpgradeItem in shopUpgradeItems) {
                        if (!database.translationDao.isProductExist(shopUpgradeItem.id)) {
                            notTranslatedItems.add(
                                AddNotTranslationBody(
                                    product_id = shopUpgradeItem.id + 100000,
                                    type = "product",
                                    english_title = shopUpgradeItem.name,
                                    content = listOf(),
                                    excerpt = "",
                                    contains = listOf(),
                                    from_ship = 0,
                                    to_ship = 0
                                )
                            )
                        } else {
                            val translation = database.translationDao.getByProductId(shopUpgradeItem.id)
                            shopUpgradeItem.chineseName = translation!!.title
                            shopUpgradeItem.chineseDescription = translation.excerpt
                        }
                        shopUpgradeItem.chineseSubtitle = "升级"
                    }
                }
                val canUpgrade = RSIApi.retrofitService.filterShips(FilterShipsQuery().getRequestBody())
                val canUpgradeItemIds = canUpgrade.data.to.ships.map { it.id }
                shopUpgradeItems.forEach {
                    if (canUpgradeItemIds.contains(it.id - 100000)) {
                        it.isUpgradeAvailable = true
                    }
                }
                database.shopItemDao.insertAll(shopUpgradeItems)
//                CirnoApi.retrofitService.addNotTranslation(notTranslatedItems)

                database.shipUpgradeDao.insertAll(convertToShipUpgradeRepoItem(data.data.ships))
                canUpgrade.data.to.ships.map {
                    for(sku in it.skus) {
                        database.shipUpgradeDao.updateIsAvailable(sku.id, true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isRefreshing.postValue(false)
        }
    }

    private fun convertToShipUpgradeRepoItem(ships: List<InitShipUpgradeProperty.Data.Ship>): List<ShipUpgrade> {
        val shipUpgrades: MutableList<ShipUpgrade> = mutableListOf()
        for(ship in ships) {
            if(ship.skus == null) {
                shipUpgrades.add(InitShipUpgradeProperty.Data.Ship.toShipUpgradeRepoItem(ship))
            } else {
                shipUpgrades.addAll(InitShipUpgradeProperty.Data.Ship.toShipUpgradeRepoItems(ship))
            }
        }
        return shipUpgrades
    }

}