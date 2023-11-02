package vip.kirakira.starcitizenlite.network.shop

import vip.kirakira.starcitizenlite.database.ShipUpgrade

data class Error(val message: String, val extensions: Extensions) {
    data class Extensions(val code: Int?, val details: Details?){
        data class Details(val amount: String?)
    }
}

data class CartSummaryProperty(val errors: List<Error>?, val data: CartData){
    data class CartData(val account: Account, val store: Store) {
        data class Account(val isAnonymous: Boolean)
        data class Store(val cart: Cart) {
            data class Cart(val totals: Totals){
                data class Totals(val discount: Int,
                                  val shipping: Int,
                                  val total: Int,
                                  val subTotal: Int,
                                  val tax1: Tax,
                                  val tax2: Tax,
                                  val credits: Credits) {
                    data class Tax(val amount: Int, val name: String?)
                    data class Credits(val amount: Int,
                                       val nativeAmount: NativeAmount?,
                                       val applicable: Boolean,
                                       val maxApplicable: Int) {
                        data class NativeAmount(val value: Int)
                    }
                }
            }
        }
    }
}

data class AddCartItemProperty(val errors: List<Error>?, val data: Data?) {
    data class Data(val store: Store) {
        data class Store(val cart: Cart) {
            data class Cart(val mutations: Mutations) {
                data class Mutations(val add: Boolean?)
            }
        }
    }
}

data class Step1QueryProperty(val errors: List<Error>?,val data: Data) {
    data class Data(val store: Store) {
        data class Store(val cart: Cart) {
        }
        data class Cart(val hasDigital: Boolean, val lineItems: List<LineItem>) {
            data class LineItem(val id: String, val skuId: String,
                                val taxDescription: List<String>,
                                val sku: Sku, val qty: Int) {
                data class Sku(val id: String, val productId: String,
                               val title: String, val label: String,
                               val subtitle: String, val url: String,
                               val type: String, val isWarbond: Boolean?,
                               val isPackage: Boolean, val stock: Stock,
                               val media: Media, val nativePrice: NativePrice,
                               val price: NativePrice) {
                    data class Stock(val unlimited: Boolean, val show: Boolean,
                                     val available: Boolean, val backOrder: Boolean)
                    data class Media(val thumbnail: Thumbnail) {
                        data class Thumbnail(val storeSmall: String)
                    }
                    data class NativePrice(val amount: Int, val discounted: String?)

                }
            }
        }
    }
}

data class AddCreditProperty(val errors: List<Error>?)

data class NextStepProperty(val errors: List<Error>?)

data class ClearCartProperty(val errors: List<Error>?)

data class CartValidationProperty(val errors: List<Error>?)

data class CartAddressAssignProperty(val errors: List<Error>?)

data class CartAddressProperty(val errors: List<Error>?, val data: Data) {
    data class Data(val store: Store) {
        data class Store(val addressBook: List<AddressBook>) {
            data class AddressBook(val id: String)
            }
        }
}

data class ChooseUpgradeTargetProperty(val code: String, val msg: String, val data: Data) {
    data class Data(val rendered: String?, val upgrade_id: String?)
}

data class ApplyUpgradeProperty(val code: String, val msg: String)

data class InitShipUpgradeProperty(val data: Data) {
    data class Data(val app: App, val manufacturers: List<Manufacturer>, val ships: List<Ship>) {
        data class App(val buyback: Buyback?, val isAnonymous: Boolean, val mode: String, val version: String) {
            data class Buyback(val credit: Int)
        }
        data class Manufacturer(val id: Int, val name: String)
        data class Ship(
            val flyableStatus: String,
            val focus: String?,
            val id: Int,
            val manufacturer: Manufacturer,
            val medias: Medias,
            val msrp: Int,
            val name: String,
            val owned: Boolean,
            val type: String,
            val skus: List<Sku>?,
            val link: String
        ) {
            data class Medias(val productThumbMediumAndSmall: String, val slideShow: String)
            data class Sku(
                val available: Boolean,
                val availableStock: Int?,
                val id: Int,
                val price: Int,
                val title: String,
                val unlimitedStock: Boolean
            ) {
                fun extract(): String {
                    return "$title#&$price#&$id"
                }
            }

            companion object {
                fun toShipUpgradeRepoItem(ship: Ship): ShipUpgrade {
                    return ShipUpgrade(
                        skuId = ship.id,
                        shipId = ship.id,
                        name = ship.name,
                        isFlyable = ship.flyableStatus == "Flyable",
                        focus = ship.focus?: "",
                        link = ship.link,
                        manufacturer = ship.manufacturer.name,
                        productThumbMediumAndSmall = ship.medias.productThumbMediumAndSmall,
                        slideShow = ship.medias.slideShow,
                        price = ship.msrp,
                        edition = "Standard Edition",
                        isAvailable = false
                    )
                }

                fun toShipUpgradeRepoItems(ship: Ship): List<ShipUpgrade> {
                    return ship.skus!!.map { sku ->
                        ShipUpgrade(
                            skuId = sku.id,
                            shipId = ship.id,
                            name = ship.name,
                            isFlyable = ship.flyableStatus == "Flyable",
                            focus = ship.focus?: "",
                            link = ship.link,
                            manufacturer = ship.manufacturer.name,
                            productThumbMediumAndSmall = ship.medias.productThumbMediumAndSmall,
                            slideShow = ship.medias.slideShow,
                            price = sku.price,
                            edition = sku.title,
                            isAvailable = false
                        )
                    }
                }
            }
        }
    }
}

data class FilterShipsProperty(val data: Data, val errors: List<Error>?) {
    data class Data(val from: From?, val to: To) {
        data class From(val ships: List<Ship>) {
            data class Ship(val id: Int)
        }

        data class To(val ships: List<Ship>) {
            data class Ship(val id: Int, val skus: List<Sku>) {
                data class Sku(val id: Int, val price: Int, val upgradePrice: Int?)
            }
        }
    }
    data class Error(val message: String)
}

data class AddUpgradeToCartProperty(val data: Data) {
    data class Data(val addToCart: AddToCart?) {
        data class AddToCart(val jwt: String)
    }
}

data class ApplyTokenProperty(val success: Int, val msg: String)

data class BuyBackPledgeProperty(val success: Int, val msg: String)

data class LoginProperty(val errors: List<Error>?, val data: Data) {
    data class Data(val account_signin: Login?) {
        data class Login(val displayname: String, val id: Int)
    }

    data class Error(val message: String, val extensions: Extensions, val code: String) {
        data class Extensions(val category: String, val details: Details) {
            data class Details(val session_id: String?, val device_id: String?)
        }
    }
}

data class LoginAgainProperty(val errors: List<Error>?, val data: Data) {
    data class Data(val account_signin: Login?) {
        data class Login(val displayname: String, val id: Int)
    }

    data class Error(val message: String, val extensions: Extensions, val code: String) {
        data class Extensions(val category: String, val details: String)
    }
}

data class MultiStepLoginProperty(val errors: List<Error>?, val data: Data) {
    data class Data(val account_multistep: Login?) {
        data class Login(val displayname: String, val id: Int)
    }
    data class Error(val message: String, val code: String)
}

data class SignUpProperty(val errors: List<Error>?, val data: Data) {
    data class Data(val account_signup: SignUp?) {
        data class SignUp(val displayname: String, val username: String)
    }
    data class Error(val message: String, val code: String, val extensions: Extensions) {
        data class Extensions(val category: String, val details: Details) {
            data class Details(val handle: String?, val email: String?, val password: String?)
        }
    }
}

data class ApplyPromoProperty(
    val success: Int,
    val code: String,
    val msg: String)

data class HangarLogProperty(
    val code: String,
    val msg: String,
    val success: Int,
    val data: Data?
) {
    data class Data(val page: Int, val pagecount: Int, val rendered: String)
}

data class RsiLauncherSignInCheckResponse(
    val code: String,
    val data: Data) {
    data class Data(val auth: Boolean)
}

data class RsiLauncherSignInResponse(
    val code: String,
    val data: Data?
) {
    data class Data(
        val account_id: Int,
        val session_id: String,
        val citizen_id: String,
        val nickname: String,
        val displayname: String,
    )
}