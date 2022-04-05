package vip.kirakira.starcitizenlite.network.shop

data class CatalogProperty(
    val id: Int,
    val name: String,
    val title: String,
    val subtitle: String,
    var url: String,
    var excerpt: String,
    var type: String,
    var media: Media,
    var price: Price,
    val stock: Stock,
    val nativePrice: NativePrice
) {
    data class Media(val thumbnail: ImageUrl) {
        data class ImageUrl(val slideshow: String, val storeSmall: String)
    }
    data class Price(val amount: Int, val taxDescription: List<String>)
    data class Stock(val unlimited: Boolean, val show: Boolean)
    data class NativePrice(val amount: Int,)
}

data class CatalogResponse(val data: Data) {
    data class Data(val store: Store) {
        class Store(val listing: Listing) {
            data class Listing(val resources: List<CatalogProperty>)
        }
    }
}

data class CatalogVariables(var query: Query) {
    data class Query(var page: Int, var sort: Sort, var skus: Skus){
        data class Sort(var field: String, var direction: String)
        data class Skus(var products: List<String>)
    }
}

fun getCatalogVariables(page: Int, sort: String, products: List<String>, direction: String): CatalogVariables {
    val query = CatalogVariables.Query(page, CatalogVariables.Query.Sort(sort, direction), CatalogVariables.Query.Skus(products))
    return CatalogVariables(query)
}