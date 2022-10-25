package vip.kirakira.starcitizenlite.util

class Translation {
    fun translateShopItemSubtitle(subtitle: String): String {
        return when(subtitle) {
            "Standalone Ship" -> "单船"
            "Upgrade" -> "升级"
            "Paints" -> "涂装"
            "Gear" -> "装备"
            "Add-Ons" -> "附加包"
            "UE Credits" -> "UEC"
            "Gift Cards" -> "礼品卡"
            "Package" -> "资格包"
            "Packs" -> "组合包"
            else -> subtitle
        }
    }
}