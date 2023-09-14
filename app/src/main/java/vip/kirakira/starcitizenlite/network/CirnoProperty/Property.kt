package vip.kirakira.starcitizenlite.network.CirnoProperty

import com.squareup.moshi.JsonClass
import java.util.Currency

data class Announcement(
    val id: Int,
    val title: String,
    val content: String,
    val date: String,
)

data class Version (
    val version: String,
    val url: String,
    val shipDetailVersion: String,
    val shipDetailUrl: String,
    val shipAliasUrl: String,
    val isVip: Boolean,
    val vipExpire: Int,
    val credit: Int,
    val totalVipTime: Int
)

data class StarUp(
    val id: Int,
    val title: String,
    val content: String,
)

data class RecaptchaList(
    val captcha_list: List<ReCaptcha>,
    val error: String? = null,
    val message: String
) {
    data class ReCaptcha(
        val token: String
    )
}

data class TranslationVersionProperty(
    val version: String
)

data class TranslationProperty(
    val id: Int,
    val product_id: Int,
    val type: String,
    val english_title: String,
    val title: String,
//    val content: String,
    val excerpt: String? = null,
//    val contains: String,
    val from_ship: Int,
    val to_ship: Int
)

@JsonClass(generateAdapter = true)
class AddNotTranslationBody(
    val product_id: Int,
    val type: String = "hanger",
    val create_at: Int = 0,
    val english_title: String,
    val content: List<String> = listOf(),
    val excerpt: String = "",
    val contains: List<String> = listOf(),
    val from_ship: Int = 0,
    val to_ship: Int = 0
)

data class AddTranslationResult(
    val message: String
)

data class PromotionCode(
    val chinese_title: String,
    val promo: String,
    val currency: String,
    val code: String
)

@JsonClass(generateAdapter = true)
class RefugeInfo(
    val version: String,
    val androidVersion: String,
    val systemModel: String
)

@JsonClass(generateAdapter = true)
data class ClientInfo(
    val primaryUser: String
)

data class ShipAlias(
    val id: Int,
    val name: String,
    val alias: List<String>,
    val skus: List<Sku>
) {
    data class Sku(
        val title: String,
        val price: Int,
    )
    fun getHighestSku(): Int {
        if (this.skus.isEmpty()) return 0
        var highestPrice = this.skus[0].price
        for (sku in this.skus) {
            if (sku.price > highestPrice) {
                highestPrice = sku.price
            }
        }
        return highestPrice
    }
}

data class AddShipAliasBody(
    val ship_alias: List<String>
)

data class AddUpgradeRecord(
    val shipUpgrades: List<UpgradeRecord>
) {
    data class UpgradeRecord(
        val name: String,
        val upgrade_id: Int,
        val price: Int,
        val from_ship_id: Int,
        val from_ship_name: String,
        val target_ship_id: Int,
        val target_ship_name: String
    )
}

data class ShipUpgradePathPostBody(
    val from_ship_id: Int,
    val to_ship_id: Int,
    val banned_list: List<Int>,
    val hangar_upgrade_list: List<HangarUpgrade>,
    val buyback_upgrade_list: List<HangarUpgrade>,
    val use_history_ccu: Boolean,
    val only_can_buy_ships: Boolean,
    val upgrade_multiplier: Float
) {
    data class HangarUpgrade(
        val id: Int,
        val from_ship: Int,
        val to_ship: Int,
        val price: Int
    )
}

data class ShipUpgradeResponse(
    val code: Int,
    val message: String,
    val path: ShipUpgradePath
) {
    data class ShipUpgradePath(
        val from_ship: Int,
        val to_ship: Int,
        val steps: List<Step>
    ) {
        data class Step(
            val id: Int,
            val type: Int,
            val from_ship: Int,
            val to_ship: Int,
            val price: Int,
            val name: String,
            val available: Boolean
        )
    }
}