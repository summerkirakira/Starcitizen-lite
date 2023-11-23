package vip.kirakira.starcitizenlite.ui.home

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.database.HangerItem
import vip.kirakira.starcitizenlite.database.HangerPackageWithItems
import vip.kirakira.starcitizenlite.network.convertLongToDate
import vip.kirakira.starcitizenlite.network.hanger.HangerProcess
import vip.kirakira.starcitizenlite.repositories.RepoUtil


data class HangerItemProperty(
    val id: Int,
    var idList: String = "",
    val name: String,
    val image: String,
    var number: Int,
    var status: String,
    val tags: List<Tag>,
    val date: String,
    val contains: String,
    val price: Int,
    var insurance: String,
    val alsoContains: String,
    val items: List<HangerItem>,
    val isUpgrade: Boolean = false,
    val formShipId: Int = 0,
    val toShipId: Int = 0,
    val toSkuId: Int = 0,
    val chineseAlsoContains: String? = null,
    val savingString: String? = null,
    val currentPrice: Int? = null
) {
    data class Tag(
        val name: String,
        val color: String
    )
}

data class UpgradeInfo(
    val id: Int,
    val name: String,
    val upgrade_type: String,
    val match_items: List<MatchItem>,
    val target_items: List<MatchItem>
) {
    data class MatchItem(val id: Int, val name: String)
    data class TargetItem(val id: Int, val name: String)
}

fun List<BuybackItem>.toItemProperty(): List<HangerItemProperty> {
    val map = mutableMapOf<String, HangerItemProperty>()
    for (item in this) {
        if(map[item.title] == null){

            var currentPrice = -1

            if(item.title.startsWith("Upgrade - ")) {
                val shipUpgradeAliasList = Parser.getFullUpgradeName(item.title)
                val upgradeFromShipAlias = RepoUtil.getShipAlias(shipUpgradeAliasList[0])
                val upgradeToShipAlias = RepoUtil.getShipAlias(shipUpgradeAliasList[1])
                if(upgradeFromShipAlias != null && upgradeToShipAlias != null) {
                    currentPrice = RepoUtil.getHighestAliasPrice(upgradeToShipAlias) - RepoUtil.getHighestAliasPrice(upgradeFromShipAlias)
                }
            }

            if(item.title.startsWith("Standalone Ship - ")) {
                val shipName = item.title.replace("Standalone Ship - ", "")
                val ship= RepoUtil.getShipAlias(shipName)
                if(ship != null) {
                    currentPrice = RepoUtil.getHighestAliasPrice(ship)
                }
            }

            map[item.title] = HangerItemProperty(
                id = item.id,
                name = item.chinesName?:item.title,
                image = item.image,
                number = 1,
                status = "回购中",
                tags = listOf(),
                date = convertLongToDate(item.date),
                contains = item.contains,
                price = currentPrice,
                insurance = "",
                alsoContains = item.contains,
                items = listOf(),
                isUpgrade = item.isUpgrade,
                formShipId = item.formShipId,
                toShipId = item.toShipId,
                toSkuId = item.toSkuId,
                idList = item.id.toString()
            )
        } else {
            map[item.title]!!.number++
            map[item.title]!!.idList += ",${item.id}"
        }
    }
    return map.values.toList()
}

fun List<HangerPackageWithItems>.toItemPropertyList(): List<HangerItemProperty> {
    val map = mutableMapOf<String, HangerItemProperty>()
    for(packageWithItems in this) {
        val packageItemString = packageWithItems.hangerItems.map { it.title }.joinToString("#")
        val saveKey = packageWithItems.hangerPackage.title +
                packageWithItems.hangerPackage.status +
                packageItemString +
                packageWithItems.hangerPackage.also_contains  +
                packageWithItems.hangerPackage.image +
                packageWithItems.hangerPackage.can_gift.toString()
        for (item in packageWithItems.hangerItems) {
            saveKey + item.title
        }
        if(map[saveKey] == null){
            var insuranceTime = 0
            var insuranceString = ""
            packageWithItems.hangerPackage.also_contains.split("#").forEach {
                var eachInsuranceTime = 0
                try {
                    if(it.contains("Insurance")){
//                        Log.d("HangerItemProperty", it)
                        val formattedInsurance = it.replace("-", " ")
                        if(formattedInsurance.contains("Lifetime")){
                            insuranceString = "LTI"
                        } else {
                            eachInsuranceTime = formattedInsurance.split(" ")[0].toInt()
                        }
                        if(formattedInsurance.split(" ")[1] == "Year"){
                            eachInsuranceTime *= 10
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    eachInsuranceTime = 0
//                    Log.d("HangerItemProperty", packageWithItems.hangerPackage.toString())
                }

                if(eachInsuranceTime > insuranceTime){
                    insuranceTime = eachInsuranceTime
                }
            }
            if(insuranceString.isEmpty() && insuranceTime != 0){
                insuranceString = if(insuranceTime % 12 == 0){
                    "${insuranceTime / 12}Y"
                } else {
                    "${insuranceTime}M"
                }
            }

            val tagList = mutableListOf<HangerItemProperty.Tag>()
            if(packageWithItems.hangerPackage.can_gift) {
                tagList.add(HangerItemProperty.Tag("可赠送", "#FF0000"))
            }
            if(packageWithItems.hangerPackage.exchangeable) {
                tagList.add(HangerItemProperty.Tag("可融", "#FF0000"))
            }
            if(packageWithItems.hangerPackage.is_upgrade) {
                tagList.add(HangerItemProperty.Tag("可升级", "#FF0000"))
            }
            var status = "未知"
            if(packageWithItems.hangerPackage.status == "Attributed") {
                status = "库存中"
            } else if(packageWithItems.hangerPackage.status == "Gifted") {
                status = "已礼物"
            }

            var savingString: String? = null
            if(packageWithItems.hangerPackage.currentPrice != 0){
//                if((
//                            (packageWithItems.hangerPackage.currentPrice - packageWithItems.hangerPackage.value).toFloat()
//                                    / packageWithItems.hangerPackage.currentPrice.toFloat() * 100
//                            ).toInt() > 100) {
//                    Log.d("HangerItemProperty", "${packageWithItems.hangerPackage}")
//                }
                savingString = "-${
                    (
                            (packageWithItems.hangerPackage.currentPrice - packageWithItems.hangerPackage.value).toFloat()
                                    / packageWithItems.hangerPackage.currentPrice.toFloat() * 100
                            ).toInt()
                }%"
            }

            val currentPrice = if(packageWithItems.hangerPackage.currentPrice == 0) null else packageWithItems.hangerPackage.currentPrice


            map[saveKey] = HangerItemProperty(
                packageWithItems.hangerPackage.id,
                packageWithItems.hangerPackage.id.toString(),
                packageWithItems.hangerPackage.chineseTitle?:packageWithItems.hangerPackage.title,
                packageWithItems.hangerPackage.image,
                1,
                status,
                tagList,
                convertLongToDate(packageWithItems.hangerPackage.date),
                packageWithItems.hangerPackage.contains,
                packageWithItems.hangerPackage.value,
                insuranceString,
                packageWithItems.hangerPackage.also_contains,
                packageWithItems.hangerItems,
                chineseAlsoContains = packageWithItems.hangerPackage.chineseContains,
                savingString = savingString,
                currentPrice = currentPrice
            )
        } else {
            map[saveKey]!!.number++
            map[saveKey]!!.idList += ",${packageWithItems.hangerPackage.id}"
        }
    }
    return map.values.toList()
}