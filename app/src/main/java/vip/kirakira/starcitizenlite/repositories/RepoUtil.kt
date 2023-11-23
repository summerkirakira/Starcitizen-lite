package vip.kirakira.starcitizenlite.repositories

import android.util.Log
import com.google.gson.Gson
import vip.kirakira.starcitizenlite.database.HangerPackage
import vip.kirakira.starcitizenlite.network.CirnoProperty.AddUpgradeRecord
import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipAlias
import vip.kirakira.starcitizenlite.shipAlias
import vip.kirakira.starcitizenlite.ui.home.UpgradeInfo

class RepoUtil {
    companion object {
        fun getShipAlias(name: String): ShipAlias? {
            Log.d("RepoUtil", "getShipAlias: $name")
            for(ship in shipAlias) {
                if(compareShipName(ship.name, name)) {
                    return ship
                }
                for(alias in ship.alias) {
                    if(compareShipName(alias, name)) {
                        return ship
                    }
                }
            }
            return null
        }

        fun getShipAlias(id: Int): ShipAlias? {
            for(ship in shipAlias) {
                if(ship.id == id) {
                    return ship
                }
            }
            return null
        }

        private fun compareShipName(name1: String, name2: String): Boolean {
            return name1.lowercase() == name2.lowercase()
        }

        fun getHighestAliasPrice(shipAlias: ShipAlias): Int {
            var highestPrice = 0
            for(sku in shipAlias.skus) {
                if(sku.price > highestPrice) {
                    highestPrice = sku.price
                }
            }
            return highestPrice
        }

        fun parseUpgradeInfo(text: String): UpgradeInfo {
            return Gson().fromJson(text, UpgradeInfo::class.java)
        }

        fun generateAddUpgradeRecordPostBody(hangarPackages: List<HangerPackage>): AddUpgradeRecord {
            val upgradeInfoList = mutableListOf<AddUpgradeRecord.UpgradeRecord>()
            for (hangarPackage in hangarPackages) {
                try {
                    val upgradeInfo = parseUpgradeInfo(hangarPackage.upgrade_info)
                } catch (e: Exception) {
                    Log.d("RepoUtil", "generateAddUpgradeRecordPostBody: ${hangarPackage.upgrade_info}")
                }
                val upgradeInfo = parseUpgradeInfo(hangarPackage.upgrade_info)
                for (upgrade in upgradeInfoList) {
                    if (upgrade.upgrade_id == upgradeInfo.id) {
                        upgradeInfoList.remove(upgrade)
                        break
                    }
                }
                upgradeInfoList.add(
                    AddUpgradeRecord.UpgradeRecord(
                        name = upgradeInfo.name,
                        upgrade_id = upgradeInfo.id,
                        price = hangarPackage.value,
                        from_ship_id = upgradeInfo.match_items[0].id,
                        from_ship_name = upgradeInfo.match_items[0].name,
                        target_ship_id = upgradeInfo.target_items[0].id,
                        target_ship_name = upgradeInfo.target_items[0].name
                    )
                )
            }
            return AddUpgradeRecord(upgradeInfoList)
        }
    }
}