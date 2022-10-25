package vip.kirakira.starcitizenlite.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.CirnoProperty.AddNotTranslationBody
import vip.kirakira.starcitizenlite.network.hanger.HangerService

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
                    database.buybackItemDao.insertAll(data)
                    for (buybackItem in data) {
                        if (!database.translationDao.isProductExist(buybackItem.id)) {
                            notTranslatedItems.add(
                                AddNotTranslationBody(
                                    product_id = buybackItem.id + 200000,
                                    type = "buyback",
                                    english_title = buybackItem.title,
                                    content = listOf(),
                                    excerpt = "",
                                    contains = listOf(buybackItem.contains),
                                    from_ship = 0,
                                    to_ship = 0
                                )
                            )
                        }
                    }
                    page++
                }
                CirnoApi.retrofitService.addNotTranslation(notTranslatedItems)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isRefreshing.postValue(false)
    }

}