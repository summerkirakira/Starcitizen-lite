package vip.kirakira.starcitizenlite.ui.home

import vip.kirakira.starcitizenlite.database.BuybackItem
import vip.kirakira.starcitizenlite.database.HangerItem
import vip.kirakira.starcitizenlite.database.HangerPackageWithItems
import vip.kirakira.starcitizenlite.network.convertLongToDate
import vip.kirakira.starcitizenlite.network.hanger.HangerProcess


data class HangerItemProperty(
    val id: Int,
    val name: String,
    val image: String,
    var number: Int,
    var status: String,
    val tags: List<Tag>,
    val date: String,
    val contains: String,
    val price: Int,
    val insurance: String,
    val alsoContains: String,
    val items: List<HangerItem>
) {
    data class Tag(
        val name: String,
        val color: String
    )
}

fun List<BuybackItem>.toItemProperty(): List<HangerItemProperty> {
    val map = mutableMapOf<String, HangerItemProperty>()
    for (item in this) {
        if(map[item.title] == null){
            map[item.title] = HangerItemProperty(
                id = item.id,
                name = item.title,
                image = item.image,
                number = 1,
                status = "回购中",
                tags = listOf(),
                date = item.date,
                contains = item.contains,
                price = -1,
                insurance = "",
                alsoContains = item.contains,
                items = listOf()
            )
        } else {
            map[item.title]!!.number++
        }
    }
    return map.values.toList()
}

fun List<HangerPackageWithItems>.toItemPropertyList(): List<HangerItemProperty> {
    val map = mutableMapOf<String, HangerItemProperty>()
    for(packageWithItems in this) {
        if(map[packageWithItems.hangerPackage.title + packageWithItems.hangerPackage.status] == null){
            var insuranceTime = 0
            var insuranceString = ""
            packageWithItems.hangerPackage.also_contains.split("#").forEach {
                var eachInsuranceTime = 0
                if(it.contains("Insurance")){
                    if(it.contains("Lifetime")){
                        insuranceString = "LTI"
                    } else {
                        eachInsuranceTime = it.split(" ")[0].toInt()
                    }
                    if(it.split(" ")[1] == "Year"){
                        eachInsuranceTime *= 10
                    }
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
            map[packageWithItems.hangerPackage.title + packageWithItems.hangerPackage.status] = HangerItemProperty(
                packageWithItems.hangerPackage.id,
                packageWithItems.hangerPackage.title,
                packageWithItems.hangerPackage.image,
                1,
                status,
                tagList,
                convertLongToDate(packageWithItems.hangerPackage.date),
                packageWithItems.hangerPackage.contains,
                packageWithItems.hangerPackage.value,
                insuranceString,
                packageWithItems.hangerPackage.also_contains,
                packageWithItems.hangerItems
            )
        } else {
            map[packageWithItems.hangerPackage.title + packageWithItems.hangerPackage.status]!!.number++
        }
    }
    return map.values.toList()
}