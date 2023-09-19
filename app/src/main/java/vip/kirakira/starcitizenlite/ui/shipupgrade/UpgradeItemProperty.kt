package vip.kirakira.starcitizenlite.ui.shipupgrade

import vip.kirakira.starcitizenlite.network.CirnoProperty.ShipAlias

data class UpgradeItemProperty(
    val id: Int,
    val name: String,
    val fromShipName: String,
    val toShipName: String,
    val image: String,
    val isAvailable: Boolean,
    val isWarbond: Boolean,
    val origin: OriginType,
    val price: Int,
    val originalPrice: Int,
    val saving: Int,
    val isLimitedTime: Boolean,
    val type: String,
    val currentPrice: Int,
    val shipPrice: Int
) {
    enum class OriginType {
        BUYBACK, NORMAL, HISTORY, HANGAR, NOT_AVAILABLE
    }
}

data class OwnedUpgrade(
    val id: Int,
    val fromShip: ShipAlias,
    val toShip: ShipAlias,
    val type: UpgradeItemProperty.OriginType,
    val name: String,
    val fromShipName: String,
    val toShipName: String,
    val price: Int
)

data class UpgradeOptions(
    val useHistoryCcu: Boolean,
    val onlyCanBuyShips: Boolean,
    val upgradeMultiplier: Float,
    val bannedList: List<Int>,
    val useBuyBack: Boolean
)

data class WarningMessage(
    val message: String,
    val type: Int
)