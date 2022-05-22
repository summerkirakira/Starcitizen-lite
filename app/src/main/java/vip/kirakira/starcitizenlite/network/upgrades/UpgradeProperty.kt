package vip.kirakira.starcitizenlite.network.upgrades

data class ShipUpgradeInfo(
    val id: Int,
    val name: String,
    val medias: Media,
    val manufacturer: Manufacturer,
    val focus: String,
    val type: String,
    val flyableStatus: String,
    val owned: Boolean,
    val msrp: Int,
    val link: String,
    val skus: List<Sku>
) {
    data class Media(
        val productThumbMediumAndSmall: String,
        val slideShow: String
    )
    data class Manufacturer(
        val id: Int,
        val name: String
    )
    data class Sku(
        val id: Int,
        val title: String,
        val price: Int
    )
}

data class filterShipUpgradeInfo(val filterList: List<FilterList>) {
    data class FilterList(val data: Data) {
        data class Data(val from: From, val to: To) {
            data class From(val ships: List<Ship>) {
                data class Ship(val id: Int)
            }
            data class To(val ships: List<Ship>) {
                data class Ship(val id: Int, val skus: List<Sku>) {
                    data class Sku(
                        val id: Int,
                        val price: Int
                    )
                }
            }

        }
    }
}

data class InitUpgradeProperty(val dataList: List<DataSet>) {
    data class DataSet(val data: Data) {
        data class Data(
            val manufacturers: List<Manufacturer>,
            val ships: List<ShipUpgradeInfo>
        ) {
            data class Manufacturer(
                val id: Int,
                val name: String
            )
        }

    }
}