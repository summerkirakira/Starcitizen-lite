package vip.kirakira.starcitizenlite.repositories

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.CirnoProperty.AddNotTranslationBody
import vip.kirakira.starcitizenlite.network.hanger.HangerService
import vip.kirakira.starcitizenlite.ui.home.Parser

class BuyBackItemRepository(private val database: ShopItemDatabase) {
    val allItems: LiveData<List<BuybackItem>> = database.buybackItemDao.getAll()
    var isRefreshing = MutableLiveData<Boolean>(false)
    suspend fun refreshItems() {

        withContext(Dispatchers.IO) {
            var page = 1
            isRefreshing.postValue(true)
            val notTranslatedItems: MutableList<AddNotTranslationBody> = mutableListOf()
            try {
                while (true) {
                    val data = HangerService().getBuybackInfo(page)
                    if (data.isEmpty()) {
                        break
                    }
                    View.GONE
//                    for(hangerPackage in data) {
//                        if(hangerPackage.title.startsWith("Upgrade - ")) {
//                            val upgradeShips = Parser.getUpgradeOriginalName(hangerPackage.title)
//                            val upgradeFromShip = database.shipUpgradeDao.getByNameLike(upgradeShips[0].name)
//                            val upgradeToShip = database.shipUpgradeDao.getByNameLike(upgradeShips[1].name)
//                            if (upgradeFromShip != null && upgradeToShip != null) {
//
//                            } else {
//                                val fromShip = database.shipDetailDao.getByName(upgradeShips[0].name)
//                                val toShip = database.shipDetailDao.getByName(upgradeShips[1].name)
//                                if (fromShip?.lastPledgePrice != null && toShip?.lastPledgePrice != null) {
//                                } else {
//                                    Log.d("BuybackItemRepository", upgradeShips[0].name + " " + upgradeShips[1].name)
//                                }
//                            }
//                        }
//                    }

                    database.buybackItemDao.insertAll(data)
//                    for (buybackItem in data) {
//                        val translation = database.translationDao.getByEnglishTitle(buybackItem.title)
//                        if (translation == null) {
//                            notTranslatedItems.add(
//                                AddNotTranslationBody(
//                                    product_id = buybackItem.id + 200000,
//                                    type = "buyback",
//                                    english_title = buybackItem.title,
//                                    content = listOf(),
//                                    excerpt = "",
//                                    contains = listOf(buybackItem.contains),
//                                    from_ship = 0,
//                                    to_ship = 0
//                                )
//                            )
//                        }
//                    }
                    page++
                }
//                CirnoApi.retrofitService.addNotTranslation(notTranslatedItems)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isRefreshing.postValue(false)
    }

}