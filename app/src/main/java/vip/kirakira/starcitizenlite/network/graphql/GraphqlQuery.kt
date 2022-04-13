package vip.kirakira.viewpagertest.network.graphql

import android.telecom.Call.Details
import com.squareup.moshi.Moshi


class UpdateCatalogMutation {
    val query = """mutation UpdateCatalogQueryMutation(${"$"}storeFront: String, ${"$"}query: SearchQuery!) {
  store(name: ${"$"}storeFront, browse: true) {
    listing: search(query: ${"$"}query) {
      resources {
        ...TyItemFragment
        __typename
      }
      __typename
    }
    __typename
  }
}

fragment TyItemFragment on TyItem {
  id
  name
  title
  subtitle
  url
  body
  excerpt
  type
  media {
    thumbnail {
      slideshow
      storeSmall
      __typename
    }
    list {
      slideshow
      __typename
    }
    __typename
  }
  nativePrice {
    amount
    discounted
    discountDescription
    __typename
  }
  price {
    amount
    discounted
    taxDescription
    discountDescription
    __typename
  }
  stock {
    ...TyStockFragment
    __typename
  }
  tags {
    name
    __typename
  }
  ... on TySku {
    label
    customizable
    isWarbond
    isPackage
    isVip
    isDirectCheckout
    __typename
  }
  ... on TyProduct {
    skus {
      id
      isDirectCheckout
      __typename
    }
    isVip
    __typename
  }
  ... on TyBundle {
    isVip
    media {
      thumbnail {
        slideshow
        __typename
      }
      __typename
    }
    discount {
      ...TyDiscountFragment
      __typename
    }
    __typename
  }
  __typename
}

fragment TyDiscountFragment on TyDiscount {
  id
  title
  skus {
    ...TyBundleSkuFragment
    __typename
  }
  products {
    ...TyBundleProductFragment
    __typename
  }
  __typename
}

fragment TyBundleSkuFragment on TySku {
  id
  title
  label
  excerpt
  subtitle
  url
  type
  isWarbond
  isDirectCheckout
  media {
    thumbnail {
      storeSmall
      slideshow
      __typename
    }
    __typename
  }
  gameItems {
    __typename
  }
  stock {
    ...TyStockFragment
    __typename
  }
  price {
    amount
    taxDescription
    __typename
  }
  __typename
}

fragment TyStockFragment on TyStock {
  unlimited
  show
  available
  backOrder
  qty
  backOrderQty
  level
  __typename
}

fragment TyBundleProductFragment on TyProduct {
  id
  name
  title
  subtitle
  url
  type
  excerpt
  stock {
    ...TyStockFragment
    __typename
  }
  media {
    thumbnail {
      storeSmall
      slideshow
      __typename
    }
    __typename
  }
  nativePrice {
    amount
    discounted
    __typename
  }
  price {
    amount
    discounted
    taxDescription
    __typename
  }
  skus {
    ...TyBundleSkuFragment
    __typename
  }
  __typename
}"""
    data class CatalogVariables(var query: Query) {
        data class Query(var page: Int, var sort: Sort, var skus: Skus){
            data class Sort(var field: String, var direction: String)
            data class Skus(var products: List<String>)
        }
    }

    private fun getCatalogVariables(page: Int, sort: String, products: List<String>, direction: String): CatalogVariables {
        val query = CatalogVariables.Query(page, CatalogVariables.Query.Sort(sort, direction), CatalogVariables.Query.Skus(products))
        return CatalogVariables(query)
    }
    fun getRequestBody(page: Int): BaseGraphQLBody {
        val variables = getCatalogVariables(page, "price", listOf("72", "268", "289", "270", "3", "41", "60", "9", "45", "46"), "asc")
        return BaseGraphQLBody(query, variables)
    }
}

class SignInMutation {
    private val moshi: Moshi = Moshi.Builder().build()


    data class SignInVariables(var operationName: String, var variables: Variables) {
        data class Variables(
            var email: String,
            var password: String,
            val remember: Boolean,
            val captcha: String
        )
    }
    data class NeedMultiStep(val errors: List<SignInError>) {
        data class SignInError(val extensions: Extensions, val message: String) {
            data class Extensions(val details: Detail) {
                data class Detail(val session_id: String, val device_id: String, val device_name: String)
            }
        }
    }

    data class LoginSuccess(val data: Data) {
        data class Data(val account_multistep: AccountMultistep) {
            data class AccountMultistep(val displayname: String, val id: Int, val __typename: String)
        }
    }

    fun parseRequest(json: String): SignInVariables {
        val jsonAdapter = moshi.adapter(SignInVariables::class.java)
        return jsonAdapter.fromJson(json)!!
    }

    fun parseFailure(json: String): NeedMultiStep {
        val jsonAdapter = moshi.adapter(NeedMultiStep::class.java)
        return jsonAdapter.fromJson(json)!!
    }

    fun parseLoginSuccess(json: String): LoginSuccess {
        val jsonAdapter = moshi.adapter(LoginSuccess::class.java)
        return jsonAdapter.fromJson(json)!!
    }


}

class CartSummaryViewMutation {
    val query = """query CartSummaryViewQuery(${"$"}storeFront: String) {
  account {
    isAnonymous
    __typename
  }
  store(name: ${"$"}storeFront) {
    cart {
      totals {
        ...TyCartTotalFragment
        __typename
      }
      __typename
    }
    ...CartFlowFragment
    __typename
  }
}

fragment TyCartTotalFragment on TyCartTotal {
  discount
  shipping
  total
  subTotal
  tax1 {
    name
    amount
    __typename
  }
  tax2 {
    name
    amount
    __typename
  }
  coupon {
    amount
    allowed
    code
    __typename
  }
  credits {
    amount
    nativeAmount {
      value
      __typename
    }
    applicable
    maxApplicable
    __typename
  }
  __typename
}

fragment CartFlowFragment on TyStore {
  cart {
    flow {
      steps {
        step
        action
        finalStep
        active
        __typename
      }
      current {
        orderCreated
        __typename
      }
      __typename
    }
    __typename
  }
  __typename
}
"""
    class Variables {
        var storeFront: String = "pledge"
    }

    fun getRequestBody(): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables())
    }
}

class AddToCartMutation {
    val query = """mutation tn(${"$"}skuId: ID!, ${"$"}qty: Int!, ${"$"}identifier: String) {
  store(name: "pledge") {
    cart {
      mutations {
        add(skuId: ${"$"}skuId, qty: ${"$"}qty, identifier: ${"$"}identifier)
        __typename
      }
      __typename
    }
    __typename
  }
}"""

    data class Variables(var skuId: Int, var qty: Int, var identifier: String?)

    private fun getVariables(skuId: Int, qty: Int, identifier: String?): Variables {
        return Variables(skuId, qty, identifier)
    }

    fun getRequestBody(skuId: Int, qty: Int, identifier: String?=null): BaseGraphQLBody {
        return BaseGraphQLBody(query, getVariables(skuId, qty, identifier))
    }
  }

class Step1Mutation {
    val query = """query Step1Query(${"$"}storeFront: String) {
  store(name: ${"$"}storeFront) {
    context {
      pricing {
        currencyCode
        __typename
      }
      __typename
    }
    cart {
      hasDigital
      lineItems {
        ...TyCartLineItemFragment
        __typename
      }
      __typename
    }
    __typename
  }
}

fragment TyCartLineItemFragment on TyCartLineItem {
  id
  skuId
  identifier
  taxDescription
  ...TySkuFragment
  unitPriceWithTax {
    amount
    discounted
    __typename
  }
  qty
  ... on ShipCustomizationLineItem {
    customizationId
    __typename
  }
  ... on ShipUpgradeLineItem {
    upgrade {
      name
      fromShipId
      toShipId
      toSkuId
      thumbnail
      __typename
    }
    __typename
  }
  discounts {
    ...TyCartLineItemDiscountFragment
    __typename
  }
  __typename
}

fragment TySkuFragment on TyCartLineItem {
  sku {
    id
    productId
    title
    label
    subtitle
    url
    type
    isWarbond
    isPackage
    stock {
      ...TyStockFragment
      __typename
    }
    media {
      thumbnail {
        storeSmall
        __typename
      }
      __typename
    }
    maxQty
    minQty
    publicType {
      code
      label
      __typename
    }
    nativePrice {
      amount
      discounted
      __typename
    }
    price {
      amount
      discounted
      taxDescription
      __typename
    }
    __typename
  }
  __typename
}

fragment TyStockFragment on TyStock {
  unlimited
  show
  available
  backOrder
  qty
  backOrderQty
  level
  __typename
}

fragment TyCartLineItemDiscountFragment on TyCartLineItemDiscount {
  id
  title
  type
  reduction
  details
  bundle {
    id
    title
    __typename
  }
  __typename
}
"""

    class Variables {
        var storeFront: String = "pledge"
    }

    fun getRequestBody(): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables())
    }
}

class AddCreditMutation {
    val query = """mutation AddCreditMutation(${"$"}amount: Float!, ${"$"}storeFront: String) {
  store(name: ${"$"}storeFront) {
    cart {
      mutations {
        credit_update(amount: ${"$"}amount)
        __typename
      }
      __typename
    }
    ...EntityAfterUpdateFragment
    __typename
  }
}

fragment EntityAfterUpdateFragment on TyStore {
  ...CartFlowFragment
  ...OrderSlugFragment
  cart {
    lineItemsQties
    billingRequired
    shippingRequired
    totals {
      ...TyCartTotalFragment
      __typename
    }
    lineItems {
      ...TySkuFragment
      id
      skuId
      identifier
      taxDescription
      discounts {
        ...TyCartLineItemDiscountFragment
        __typename
      }
      unitPriceWithTax {
        amount
        discounted
        __typename
      }
      qty
      ... on ShipCustomizationLineItem {
        customizationId
        __typename
      }
      ... on ShipUpgradeLineItem {
        upgrade {
          name
          fromShipId
          toShipId
          toSkuId
          thumbnail
          __typename
        }
        __typename
      }
      __typename
    }
    __typename
  }
  __typename
}

fragment TyCartTotalFragment on TyCartTotal {
  discount
  shipping
  total
  subTotal
  tax1 {
    name
    amount
    __typename
  }
  tax2 {
    name
    amount
    __typename
  }
  coupon {
    amount
    allowed
    code
    __typename
  }
  credits {
    amount
    nativeAmount {
      value
      __typename
    }
    applicable
    maxApplicable
    __typename
  }
  __typename
}

fragment CartFlowFragment on TyStore {
  cart {
    flow {
      steps {
        step
        action
        finalStep
        active
        __typename
      }
      current {
        orderCreated
        __typename
      }
      __typename
    }
    __typename
  }
  __typename
}

fragment OrderSlugFragment on TyStore {
  order {
    slug
    __typename
  }
  __typename
}

fragment TySkuFragment on TyCartLineItem {
  sku {
    id
    productId
    title
    label
    subtitle
    url
    type
    isWarbond
    isPackage
    stock {
      ...TyStockFragment
      __typename
    }
    media {
      thumbnail {
        storeSmall
        __typename
      }
      __typename
    }
    maxQty
    minQty
    publicType {
      code
      label
      __typename
    }
    nativePrice {
      amount
      discounted
      __typename
    }
    price {
      amount
      discounted
      taxDescription
      __typename
    }
    __typename
  }
  __typename
}

fragment TyStockFragment on TyStock {
  unlimited
  show
  available
  backOrder
  qty
  backOrderQty
  level
  __typename
}

fragment TyCartLineItemDiscountFragment on TyCartLineItemDiscount {
  id
  title
  type
  reduction
  details
  bundle {
    id
    title
    __typename
  }
  __typename
}
"""
    data class Variables(
        val amount: Float,
        val storeFront: String
    )
    fun getRequestBody(amount: Float, storeFront: String="pledge"): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(amount = amount, storeFront = storeFront))
    }
}

class NextStepMutation {
    val query = """mutation NextStepMutation(${"$"}storeFront: String) {
  store(name: ${"$"}storeFront) {
    cart {
      hasDigital
      mutations {
        flow {
          moveNext
          __typename
        }
        __typename
      }
      totals {
        ...TyCartTotalFragment
        __typename
      }
      __typename
    }
    ...CartFlowFragment
    ...OrderSlugFragment
    __typename
  }
}

fragment CartFlowFragment on TyStore {
  cart {
    flow {
      steps {
        step
        action
        finalStep
        active
        __typename
      }
      current {
        orderCreated
        __typename
      }
      __typename
    }
    __typename
  }
  __typename
}

fragment OrderSlugFragment on TyStore {
  order {
    slug
    __typename
  }
  __typename
}

fragment TyCartTotalFragment on TyCartTotal {
  discount
  shipping
  total
  subTotal
  tax1 {
    name
    amount
    __typename
  }
  tax2 {
    name
    amount
    __typename
  }
  coupon {
    amount
    allowed
    code
    __typename
  }
  credits {
    amount
    nativeAmount {
      value
      __typename
    }
    applicable
    maxApplicable
    __typename
  }
  __typename
}
"""
    data class Variables(
        val storeFront: String
    )
    fun getRequestBody(storeFront: String="pledge"): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(storeFront = storeFront))
    }
}

class CartValidationMutation {
    val query = """mutation CartValidateCartMutation(${"$"}storeFront: String, ${"$"}token: String, ${"$"}mark: String) {
  store(name: ${"$"}storeFront) {
    cart {
      mutations {
        validate(mark: ${"$"}mark, token: ${"$"}token)
        __typename
      }
      __typename
    }
    ...CartFlowFragment
    ...OrderSlugFragment
    __typename
  }
}

fragment CartFlowFragment on TyStore {
  cart {
    flow {
      steps {
        step
        action
        finalStep
        active
        __typename
      }
      current {
        orderCreated
        __typename
      }
      __typename
    }
    __typename
  }
  __typename
}

fragment OrderSlugFragment on TyStore {
  order {
    slug
    __typename
  }
  __typename
}"""

    data class Variables(
        val storeFront: String,
        val token: String,
        val mark: String
    )

    fun getRequestBody(storeFront: String="pledge", token: String, mark: String="mKGspgQ+jhzcWFWVLJiFfw"): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(storeFront = storeFront, token = token, mark = mark))
    }
}


class ClearCartMutation {
    val query = """mutation ClearCartMutation(${"$"}storeFront: String) {
  store(name: ${"$"}storeFront) {
    cart {
      mutations {
        clear
        __typename
      }
      __typename
    }
    ...EntityAfterUpdateFragment
    __typename
  }
}

fragment EntityAfterUpdateFragment on TyStore {
  ...CartFlowFragment
  ...OrderSlugFragment
  cart {
    lineItemsQties
    billingRequired
    shippingRequired
    totals {
      ...TyCartTotalFragment
      __typename
    }
    lineItems {
      ...TySkuFragment
      id
      skuId
      identifier
      taxDescription
      discounts {
        ...TyCartLineItemDiscountFragment
        __typename
      }
      unitPriceWithTax {
        amount
        discounted
        __typename
      }
      qty
      ... on ShipCustomizationLineItem {
        customizationId
        __typename
      }
      ... on ShipUpgradeLineItem {
        upgrade {
          name
          fromShipId
          toShipId
          toSkuId
          thumbnail
          __typename
        }
        __typename
      }
      __typename
    }
    __typename
  }
  __typename
}

fragment TyCartTotalFragment on TyCartTotal {
  discount
  shipping
  total
  subTotal
  tax1 {
    name
    amount
    __typename
  }
  tax2 {
    name
    amount
    __typename
  }
  coupon {
    amount
    allowed
    code
    __typename
  }
  credits {
    amount
    nativeAmount {
      value
      __typename
    }
    applicable
    maxApplicable
    __typename
  }
  __typename
}

fragment CartFlowFragment on TyStore {
  cart {
    flow {
      steps {
        step
        action
        finalStep
        active
        __typename
      }
      current {
        orderCreated
        __typename
      }
      __typename
    }
    __typename
  }
  __typename
}

fragment OrderSlugFragment on TyStore {
  order {
    slug
    __typename
  }
  __typename
}

fragment TySkuFragment on TyCartLineItem {
  sku {
    id
    productId
    title
    label
    subtitle
    url
    type
    isWarbond
    isPackage
    stock {
      ...TyStockFragment
      __typename
    }
    media {
      thumbnail {
        storeSmall
        __typename
      }
      __typename
    }
    maxQty
    minQty
    publicType {
      code
      label
      __typename
    }
    nativePrice {
      amount
      discounted
      __typename
    }
    price {
      amount
      discounted
      taxDescription
      __typename
    }
    __typename
  }
  __typename
}

fragment TyStockFragment on TyStock {
  unlimited
  show
  available
  backOrder
  qty
  backOrderQty
  level
  __typename
}

fragment TyCartLineItemDiscountFragment on TyCartLineItemDiscount {
  id
  title
  type
  reduction
  details
  bundle {
    id
    title
    __typename
  }
  __typename
}
"""

    data class Variables(
        val storeFront: String
    )

    fun getRequestBody(storeFront: String="pledge"): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(storeFront = storeFront))
    }
}


class CartAddressAssignMutation {
    val query = """mutation CartAddressAssignMutation(${"$"}billing: ID, ${"$"}shipping: ID, ${"$"}storeFront: String) {
  store(name: ${"$"}storeFront) {
    cart {
      mutations {
        assignAddresses(assign: {billing: ${"$"}billing, shipping: ${"$"}shipping})
        __typename
      }
      shippingAddress {
        ...PostalAddressFragment
        __typename
      }
      billingAddress {
        ...PostalAddressFragment
        __typename
      }
      totals {
        ...TyCartTotalFragment
        __typename
      }
      lineItems {
        id
        sku {
          id
          price {
            amount
            discounted
            taxDescription
            __typename
          }
          nativePrice {
            amount
            discounted
            __typename
          }
          __typename
        }
        taxDescription
        unitPriceWithTax {
          amount
          discounted
          __typename
        }
        __typename
      }
      __typename
    }
    context {
      currencies {
        code
        symbol
        __typename
      }
      pricing {
        ...PricingContextFragment
        __typename
      }
      __typename
    }
    ...CartFlowFragment
    __typename
  }
}

fragment PricingContextFragment on PricingContext {
  currencyCode
  currencySymbol
  exchangeRate
  taxInclusive
  __typename
}

fragment PostalAddressFragment on PostalAddress {
  id
  firstname
  lastname
  addressLine
  city
  company
  phone
  postalCode
  regionName
  countryName
  countryCode
  __typename
}

fragment TyCartTotalFragment on TyCartTotal {
  discount
  shipping
  total
  subTotal
  tax1 {
    name
    amount
    __typename
  }
  tax2 {
    name
    amount
    __typename
  }
  coupon {
    amount
    allowed
    code
    __typename
  }
  credits {
    amount
    nativeAmount {
      value
      __typename
    }
    applicable
    maxApplicable
    __typename
  }
  __typename
}

fragment CartFlowFragment on TyStore {
  cart {
    flow {
      steps {
        step
        action
        finalStep
        active
        __typename
      }
      current {
        orderCreated
        __typename
      }
      __typename
    }
    __typename
  }
  __typename
}
"""

    data class Variables(
        val billing: String,
        val storeFront: String
    )

    fun getRequestBody(billing: String, storeFront: String="pledge"): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(billing = billing, storeFront = storeFront))
    }
}

class AddressBookQuery {
    val query = """query AddressBookQuery(${"$"}storeFront: String) {
  store(name: ${"$"}storeFront) {
    addressBook {
      ...TyAddressFragment
      __typename
    }
    cart {
      lineItems {
        id
        sku {
          id
          stock {
            unlimited
            backOrder
            __typename
          }
          __typename
        }
        __typename
      }
      totals {
        ...TyCartTotalFragment
        __typename
      }
      shippingAddress {
        ...PostalAddressFragment
        __typename
      }
      billingAddress {
        ...PostalAddressFragment
        __typename
      }
      shippingRequired
      billingRequired
      __typename
    }
    __typename
  }
}

fragment TyAddressFragment on TyAddress {
  id
  defaultBilling
  defaultShipping
  company
  firstname
  lastname
  addressLine
  postalCode
  phone
  city
  country {
    id
    name
    requireRegion
    hasRegion
    code
    __typename
  }
  region {
    id
    code
    name
    __typename
  }
  __typename
}

fragment PostalAddressFragment on PostalAddress {
  id
  firstname
  lastname
  addressLine
  city
  company
  phone
  postalCode
  regionName
  countryName
  countryCode
  __typename
}

fragment TyCartTotalFragment on TyCartTotal {
  discount
  shipping
  total
  subTotal
  tax1 {
    name
    amount
    __typename
  }
  tax2 {
    name
    amount
    __typename
  }
  coupon {
    amount
    allowed
    code
    __typename
  }
  credits {
    amount
    nativeAmount {
      value
      __typename
    }
    applicable
    maxApplicable
    __typename
  }
  __typename
}
"""

    data class Variables(
        val storeFront: String
    )

    fun getRequestBody(storeFront: String="pledge"): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(storeFront = storeFront))
    }
}

