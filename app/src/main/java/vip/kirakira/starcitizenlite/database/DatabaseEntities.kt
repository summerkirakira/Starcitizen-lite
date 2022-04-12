package vip.kirakira.starcitizenlite.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import vip.kirakira.starcitizenlite.network.shop.CatalogProperty
import java.sql.Date

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
    val subtitle: String,
    val url: String,
    val slideshow: String,
    val storeSmall: String,
    val NativePrice: Int,
    val excerpt: String,
    val price: Int,
    val type: String,
    val insert_time: Long
)


@Entity(tableName = "hanger_items")
data class HangerItem constructor(
    @PrimaryKey val id: String,
    val image: String,
    val package_id: Int,
    val title: String,
    val kind: String,
    val subtitle: String,
    val insert_time: Long
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
    val also_contains: String,
    val can_gift: Boolean,
    val exchangeable: Boolean,
    val insert_time: Long
)

@Entity(tableName = "buyback")
data class BuybackItem constructor(
    @PrimaryKey val id: Int,
    val title: String,
    val image: String,
    val date: String,
    val contains: String,
    val also_contains: String,
    val insert_time: Long
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
   val image: String
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
            excerpt = it.excerpt,
            price = it.price.amount,
            type = it.type,
            insert_time = System.currentTimeMillis()
        )
    }
}