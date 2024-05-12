package vip.kirakira.starcitizenlite.repositories

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.CirnoProperty.AddNotTranslationBody
import vip.kirakira.starcitizenlite.network.hanger.HangerService
import vip.kirakira.starcitizenlite.ui.home.Parser
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

class BuyBackItemRepository(private val database: ShopItemDatabase) {
    val allItems: LiveData<List<BuybackItem>> = database.buybackItemDao.getAll()
    var isRefreshing = MutableLiveData<Boolean>(false)
    suspend fun refreshItems(application: Application) {

        withContext(Dispatchers.IO) {
            isRefreshing.postValue(true)
            val pref = application.getSharedPreferences(
                application.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE)
            val isTranslationEnabled = pref.getBoolean(application.getString(R.string.enable_localization_key), false)
            try {
                val allData: CopyOnWriteArrayList<BuybackItem> = CopyOnWriteArrayList()

                var index = 1

                var isEnd = false

                while (true) {
                    val executor = Executors.newFixedThreadPool(10)
                    for (page in index..index + 10) {
                        executor.submit {
                            val data =  runBlocking { HangerService().getBuybackInfo(page) }
                            if (data.isEmpty()) {
                                isEnd = true
                            }
                            if (isTranslationEnabled) {
                                for (hangerPackage in data) {
                                    val isUpgraded = hangerPackage.title.contains(" - upgraded")
                                    val itemName = hangerPackage.title.replace(" - upgraded", "")
                                    if (itemName.startsWith("Upgrade - ")) {
                                        val shipUpgradeAliasList = Parser.getFullUpgradeName(itemName)
                                        val upgradeFromShipAlias = RepoUtil.getShipAlias(shipUpgradeAliasList[0])
                                        val upgradeToShipAlias = RepoUtil.getShipAlias(shipUpgradeAliasList[1])
                                        if (upgradeFromShipAlias != null && upgradeToShipAlias != null) {
                                            val upgradeFromShipChineseName =
                                                database.translationDao.getByEnglishTitle(upgradeFromShipAlias.name)
                                            val upgradeToShipChineseName =
                                                database.translationDao.getByEnglishTitle(upgradeToShipAlias.name)
                                            //                                    currentPrice = RepoUtil.getHighestAliasPrice(upgradeToShipAlias) - RepoUtil.getHighestAliasPrice(upgradeFromShipAlias)
                                            if (upgradeFromShipChineseName != null && upgradeToShipChineseName != null) {
                                                hangerPackage.chinesName =
                                                    "升级包 - ${upgradeFromShipChineseName.title} 到 ${upgradeToShipChineseName.title}"
                                            }
                                        }
                                    } else if (itemName.startsWith("Standalone Ship - ")) {
                                        val translation = database.translationDao.getByEnglishTitle(
                                            itemName.replace(
                                                "Standalone Ship - ",
                                                ""
                                            )
                                        )
                                        if (translation != null) {
                                            hangerPackage.chinesName = "单船 - " + translation.title
                                        }
                                    } else {
                                        val chineseItemName = database.translationDao.getByEnglishTitle(itemName)
                                        if (chineseItemName != null) {
                                            hangerPackage.chinesName = chineseItemName.title
                                        }
                                    }
                                    if (isUpgraded) {
                                        hangerPackage.chinesName = "[已升级]" + hangerPackage.chinesName
                                    }
                                }
                            }
                            allData.addAll(data)
                        }
                    }
                    index += 10

                    executor.shutdown()

                    while (!executor.isTerminated) {
                        delay(100)
                    }

                    if (isEnd) {
                        break
                    }
                }
                database.buybackItemDao.deleteAll()
                database.buybackItemDao.insertAll(allData)
//                CirnoApi.retrofitService.addNotTranslation(notTranslatedItems)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isRefreshing.postValue(false)
    }

}