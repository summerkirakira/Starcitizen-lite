package vip.kirakira.starcitizenlite.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import vip.kirakira.starcitizenlite.network.shop.CatalogProperty
import vip.kirakira.starcitizenlite.network.shop.InitShipUpgradeProperty

@Entity(tableName = "user")
data class User constructor(
    @PrimaryKey
    val id: Int,
    val name: String,

    val login_id: String,
    val email: String,
    val password: String,
    val rsi_token: String,
    val rsi_device: String,

    val handle: String,
    val profile_image: String,
    val language: String,
    val country: String,
    val region: String,
    val referral_code: String,
    val referral_count: Int,

    val store: Int,
    val uec: Int,
    val rec: Int,
    var hanger_value: Int,
    val total_spent: Int,

    val is_concierge: Boolean,
    val is_subscribed: Boolean,

    val organization: String,
    val organization_image: String,

    val registerTime: String,
    val orgName: String,
    val orgLogo: String,
    val orgRank: Int,
    val orgRankName: String,
    val location: String,
    val fluency: String,
)

@Entity(tableName = "shop_items")
data class ShopItem constructor(
    @PrimaryKey
    val id: Int,
    val name: String,
    val title: String,
    var subtitle: String,
    val url: String,
    val slideshow: String,
    val storeSmall: String,
    val NativePrice: Int,
    val excerpt: String,
    val price: Int,
    val type: String,
    val insert_time: Long,
    val isTranslated: Boolean = false,
    val isUpgrade: Boolean = true,
    val skus: String? = null,
    var isUpgradeAvailable: Boolean = false,
    var chineseName: String? = null,
    var chineseDescription: String? = null,
    var chineseSubtitle: String? = null,
)


@Entity(tableName = "hanger_items")
data class HangerItem constructor(
    @PrimaryKey val id: String,
    val image: String,
    val package_id: Int,
    val title: String,
    var kind: String,
    var subtitle: String,
    val insert_time: Long,
    var chineseSubtitle: String? = null,
    var chineseTitle: String? = null
)


@Entity(tableName = "hanger_packages")
data class HangerPackage constructor(
    @PrimaryKey val id: Int,
    val title: String,
    val image: String,
    val value: Int,
    val status: String,
    val is_upgrade: Boolean,
    val upgrade_info: String,
    val date: Long,
    val contains: String,
    var also_contains: String,
    val can_gift: Boolean,
    val exchangeable: Boolean,
    val insert_time: Long,
    var chineseTitle: String? = null,
    var chineseContains: String? = null,
    val chineseAlsoContains: String? = null,
)

@Entity(tableName = "buyback")
data class BuybackItem constructor(
    @PrimaryKey val id: Int,
    val title: String,
    val image: String,
    val date: Long,
    val contains: String,
    val also_contains: String,
    val insert_time: Long,
    val isUpgrade: Boolean = false,
    val formShipId: Int = 0,
    val toShipId: Int = 0,
    val toSkuId: Int = 0,
    val chinesName: String? = null,
    val chinese_contains: String? = null,
    val chineseAlsoContains: String? = null,
)

@Entity(tableName = "translation")
data class Translation constructor(
    @PrimaryKey val id: Int,
    val product_id: Int,
    val type: String,
    val create_at: Int = 0,
    val english_title: String,
    val excerpt: String? = null,
    val from_ship: Int,
    val to_ship: Int,
    val title: String,
    val insert_time: Long,
)

@Entity(tableName = "config")
data class Config constructor(
    @PrimaryKey val id: Int,
    val version: String,
    val translation_version: Long,
)

data class HangerPackageWithItems constructor(
    @Embedded var hangerPackage: HangerPackage,
    @Relation(
        parentColumn = "id",
        entityColumn = "package_id"
    )
    val hangerItems: List<HangerItem>
)

@Entity(tableName = "banner_image")
data class BannerImage constructor(
   @PrimaryKey
   val id: Int,
   val url: String
)

@Entity(tableName = "hangar_ship")
data class HangarShip constructor(
    @PrimaryKey
    val id: Int,
    val name: String,
    val shipId: Int? = null,
    val packageId: Int,
    val packageTitle: String,
    val imageUrl: String,
    val chinesePackageTitle: String? = null,
    val image: String,
    val price: Int,
    val current_price: Int,
    val is_upgrade: Boolean,
    val insurance: Int,
    val date: Long,
    val isGiftable: Boolean,
    val packagePrice: Int,
    val isReclaimable: Boolean,
    val receiveTime: Long,
    val insert_time: Long
)

@Entity(tableName = "hangar_log")
data class HangarLog constructor(
    @PrimaryKey
    val id: Int,
    val time: Long,
    val type: String,
    val name: String,
    val chineseName: String? = null,
    val price: Int? = null,
    val source: String? = null,
    val target: String? = null,
    val operator: String? = null,
    val reason: String? = null,
    val insert_time: Long
)

fun List<ShopItem>.toCatalogProperty(): List<CatalogProperty> {
    return map {
        CatalogProperty(
            id = it.id,
            name = it.name,
            title = it.title,
            subtitle = it.subtitle,
            url = it.url,
            media = CatalogProperty.Media(
                CatalogProperty.Media.ImageUrl(it.slideshow, it.storeSmall)
            ),
            nativePrice = CatalogProperty.NativePrice(it.NativePrice),
            excerpt = it.excerpt,
            price = CatalogProperty.Price(it.price, listOf("")),
            type = it.type,
            stock = CatalogProperty.Stock(false, true)
        )
    }
}

fun List<CatalogProperty>.toShopItem(): List<ShopItem> {
    return map {
        ShopItem(
            id = it.id,
            name = it.name,
            title = it.title,
            subtitle = it.subtitle,
            url = it.url,
            slideshow = it.media.thumbnail.slideshow,
            storeSmall = it.media.thumbnail.storeSmall,
            NativePrice = it.nativePrice.amount,
            excerpt = it.excerpt?: "",
            price = it.price.amount,
            type = it.type,
            insert_time = System.currentTimeMillis()
        )
    }
}

fun List<InitShipUpgradeProperty.Data.Ship>.toUpgradeShopItem(): List<ShopItem> {
    return map {
        ShopItem(
            id = it.id + 100000, // 100000 is the offset, avoid conflict with catalog
            name = it.name,
            isUpgrade = true,
            title = it.name,
            subtitle = "Upgrade",
            url = it.link,
            slideshow = it.medias.slideShow,
            storeSmall = it.medias.productThumbMediumAndSmall,
            NativePrice = it.msrp,
            excerpt = it.focus?: "",
            price = it.msrp,
            type = it.type,
            insert_time = System.currentTimeMillis(),
            skus = it.skus?.extractToString()
        )
    }
}
fun List<InitShipUpgradeProperty.Data.Ship.Sku>.extractToString(): String {
    return this.joinToString(separator = ",") { it.extract() }
}