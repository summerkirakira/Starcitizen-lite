package vip.kirakira.starcitizenlite.network.shop

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