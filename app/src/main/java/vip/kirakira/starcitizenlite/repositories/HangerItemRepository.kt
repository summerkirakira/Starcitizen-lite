package vip.kirakira.starcitizenlite.repositories

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.database.HangerItem
import vip.kirakira.starcitizenlite.database.HangerPackage
import vip.kirakira.starcitizenlite.database.HangerPackageWithItems
import vip.kirakira.starcitizenlite.database.ShopItemDatabase
import vip.kirakira.starcitizenlite.network.CirnoApi
import vip.kirakira.starcitizenlite.network.CirnoProperty.AddNotTranslationBody
import vip.kirakira.starcitizenlite.network.hanger.HangerService
import vip.kirakira.starcitizenlite.network.rsi_cookie
import vip.kirakira.starcitizenlite.ui.home.UpgradeInfo
import vip.kirakira.starcitizenlite.util.Translation

class HangerItemRepository(private val database: ShopItemDatabase) {
//    val allItems: LiveData<List<HangerItem>> = database.hangerItemDao.getAllItems()
    val allPackagesAndItems: LiveData<List<HangerPackageWithItems>> = database.hangerItemDao.getAll()
    var hangerValue: Int = 0
    val translationRepository = TranslationRepository(database)

    var isRefreshing = MutableLiveData<Boolean>(false)

    suspend fun refreshItems(application: Application) {
        withContext(Dispatchers.IO) {
            var page = 1
            isRefreshing.postValue(true)
            val notTranslatedItems: MutableList<AddNotTranslationBody> = mutableListOf()
            val pref = application.getSharedPreferences(
                application.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE)
            val isTranslationEnabled = pref.getBoolean(application.getString(R.string.enable_localization_key), false)
            if (rsi_cookie.contains("_rsi_device")) {
                val getTime = System.currentTimeMillis()
                try {
                    translationRepository.refreshTranslation(application)
                    while (true) {
                        val data = HangerService().getHangerInfo(page)
                        if (data.hangerPackages.isEmpty()) {
                            break
                        }

                        for(hangerPackage in data.hangerPackages) {
                            var currentPrice = 0
                            for(hangerItem in data.hangerItems) {
                                if (hangerItem.package_id == hangerPackage.id) {
                                    if(hangerItem.kind == "Ship") {
                                        val shipUpgrade = database.shipUpgradeDao.getByName(hangerItem.title)
                                        if (shipUpgrade != null) {
                                            currentPrice += shipUpgrade.price
                                        }
                                    }
                                }
                            }
                            hangerPackage.currentPrice = currentPrice
                        }

                        if (isTranslationEnabled) {
                            for (hangerPackage in data.hangerPackages) {
                                val contains: MutableList<String> = mutableListOf()
                                val content: MutableList<String> = mutableListOf()
                                val alsoContains: MutableList<String> = mutableListOf()
                                for (hangerItem in data.hangerItems) {
                                    if (hangerItem.package_id == hangerPackage.id) {
                                        content.add(hangerItem.title)
                                        val contentTranslation = database.translationDao.getByEnglishTitle(hangerItem.title.trim())
                                        if (contentTranslation != null)
                                            hangerItem.chineseTitle = contentTranslation.title
                                        hangerItem.chineseSubtitle = Translation().translateHangerItemType(hangerItem.kind)
                                        if (hangerPackage.is_upgrade) {
                                            val upgradeInfo: UpgradeInfo = Gson().fromJson(hangerPackage.upgrade_info, UpgradeInfo::class.java)
                                            val fromShip = database.translationDao.getByEnglishTitle(upgradeInfo.match_items[0].name)
                                            val toShip = database.translationDao.getByEnglishTitle(upgradeInfo.target_items[0].name)

                                            hangerPackage.chineseTitle = "升级包 - "
                                            if (fromShip != null) {
                                                hangerPackage.chineseTitle += fromShip.title.replace(" ","")
                                            } else {
                                                hangerPackage.chineseTitle += upgradeInfo.match_items[0].name
                                            }
                                            hangerPackage.chineseTitle += " 到 "
                                            if (toShip != null) {
                                                hangerPackage.chineseTitle += toShip.title.replace(" ","")
                                            } else {
                                                hangerPackage.chineseTitle += upgradeInfo.target_items[0].name
                                            }

                                            if (hangerPackage.title.contains("Warbond")) {
                                                hangerPackage.chineseTitle += " (战争债券版)"
                                            }
                                            if (hangerItem.title.contains("Upgrade")) {
                                                hangerItem.chineseTitle = hangerPackage.chineseTitle
                                                alsoContains.add(hangerPackage.chineseTitle!!)
                                            }
                                            if (fromShip == null) {
                                                notTranslatedItems.add(AddNotTranslationBody(upgradeInfo.match_items[0].id + 400000,
                                                    english_title = upgradeInfo.match_items[0].name))
                                            }
                                            if (toShip == null) {
                                                notTranslatedItems.add(AddNotTranslationBody(upgradeInfo.target_items[0].id + 400000,
                                                    english_title = upgradeInfo.target_items[0].name))
                                            }
                                        }
                                    }
                                }
                                contains.addAll(hangerPackage.also_contains.split("#"))

                                if (hangerPackage.title.startsWith("Standalone Ship -")) {
                                    val shipName = hangerPackage.title.replace("Standalone Ship - ", "")
                                        .replace("Upgrades", "").trim()
                                    val ship = database.translationDao.getByEnglishTitle(shipName)
                                    if (ship != null) {
                                        if (hangerPackage.title.contains("Upgrades")) {
                                            hangerPackage.chineseTitle = "单船 - ${ship.title}" + " 升级"
                                        } else {
                                            hangerPackage.chineseTitle = "单船 - ${ship.title}"
                                        }
                                    } else {
                                        hangerPackage.chineseTitle = "单船 - $shipName"
                                        notTranslatedItems.add(AddNotTranslationBody(hangerPackage.id + 600000,
                                            english_title = shipName))
                                    }
                                }

                                var translationKey = hangerPackage.title.replace("Upgrades", "").trim()

                                if (hangerPackage.title.contains("nameable ship") && hangerPackage.title.contains("Contains")) {
                                    translationKey = translationKey.split("Contains")[0].trim()
                                }

                                val packageTranslation = database.translationDao.getByEnglishTitle(translationKey)
                                if (packageTranslation == null) {
                                    notTranslatedItems.add(
                                        AddNotTranslationBody(
                                            product_id = hangerPackage.id + 300000,
                                            type = "hanger",
                                            english_title = translationKey,
                                            content = content,
                                            excerpt = "",
                                            contains = contains,
                                            from_ship = 0,
                                            to_ship = 0
                                        )
                                    )
                                } else {
                                    hangerPackage.chineseTitle = packageTranslation.title
                                    if (hangerPackage.title.endsWith("Upgrades ")) {
                                        hangerPackage.chineseTitle += "升级"
                                    }
                                }
                                hangerPackage.chineseContains = hangerPackage.also_contains.trim().split("#").joinToString("#") {
                                    val containsTranslation = database.translationDao.getByEnglishTitle(it.trim())
                                    containsTranslation?.title ?: it
                                }
                                if (alsoContains.size > 0) {
                                    alsoContains.addAll(hangerPackage.chineseContains!!.split("#"))
                                    for (item in alsoContains) {
                                        if (item.contains("Upgrade")) {
                                            alsoContains.remove(item)
                                        }
                                    }
                                    hangerPackage.chineseContains = alsoContains.joinToString("#")
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