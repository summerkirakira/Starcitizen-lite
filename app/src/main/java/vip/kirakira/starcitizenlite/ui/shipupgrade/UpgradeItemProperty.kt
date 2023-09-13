package vip.kirakira.starcitizenlite.ui.shipupgrade

data class UpgradeItemProperty(
    val id: Int,
    val name: String,
    val fromShipName: String,
    val toShipName: String,
    val image: String,
    val isAvailable: Boolean,
    val isWarbond: Boolean,
    val origin: OriginType,
    val originTextColor: String,
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
    val id: Int
)