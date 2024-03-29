package vip.kirakira.viewpagertest.network.graphql

import android.telecom.Call.Details
import com.google.gson.Gson
import com.squareup.moshi.Json
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
      title
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
        val variables = getCatalogVariables(page, "price", listOf("72", "268", "289", "270", "3", "41", "60", "9", "45", "46"), "desc")
        return BaseGraphQLBody(query, variables)
    }
}

class SignInMutation {
    private val moshi: Moshi = Moshi.Builder().build()


    data class SignInVariables(var variables: Variables) {
        data class Variables(
            var email: String,
            var password: String,
            val remember: Boolean,
            val captcha: String?
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
        return Gson().fromJson(json, SignInVariables::class.java)
    }

    fun parseFailure(json: String): NeedMultiStep {
        return Gson().fromJson(json, NeedMultiStep::class.java)
    }

    fun parseLoginSuccess(json: String): LoginSuccess {
        return Gson().fromJson(json, LoginSuccess::class.java)
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
    frequency
    isWarbond
    isPackage
    gameItems {
      ... on ShipGameItem {
        specs {
          productionStatus
          __typename
        }
        __typename
      }
      __typename
    }
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

    fun getRequestBody(storeFront: String="pledge", token: String, mark: String="rLK3FmRE+uURZ4x40anLgg"): BaseGraphQLBody {
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

class initShipUpgradeQuery {
    val query = """query initShipUpgrade {
  ships {
    id
    name
    medias {
      productThumbMediumAndSmall
      slideShow
    }
    manufacturer {
      id
      name
    }
    focus
    type
    flyableStatus
    owned
    msrp
    link
    skus {
      id
      title
      available
      price
      body
      unlimitedStock
      availableStock
    }
  }
  manufacturers {
    id
    name
  }
  app {
    version
    env
    cookieName
    sentryDSN
    pricing {
      currencyCode
      currencySymbol
      exchangeRate
      taxRate
      isTaxInclusive
    }
    mode
    isAnonymous
    buyback {
      credit
    }
  }
}
"""
    class Variables()

    fun getRequestBody(): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables())
    }
}

class FilterShipsQuery {
    val query = """query filterShips(${"$"}fromId: Int, ${"$"}toId: Int, ${"$"}fromFilters: [FilterConstraintValues], ${"$"}toFilters: [FilterConstraintValues]) {
  from(to: ${"$"}toId, filters: ${"$"}fromFilters) {
    ships {
      id
    }
  }
  to(from: ${"$"}fromId, filters: ${"$"}toFilters) {
    featured {
      reason
      style
      tagLabel
      tagStyle
      footNotes
      shipId
    }
    ships {
      id
      skus {
        id
        price
        upgradePrice
        unlimitedStock
        showStock
        available
        availableStock
      }
    }
  }
}
"""
    class Variables(
        val fromFilters: List<String> = listOf(),
        val toFilters: List<String> = listOf()
    )

    fun getRequestBody(fromFilters: List<String> = listOf(), toFilters: List<String> = listOf()): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(fromFilters = fromFilters, toFilters = toFilters))
    }
}


class SetPaymentMethodMutation {
    val query = """mutation SetPaymentMethodMutation(${"$"}orderSlug: String!, ${"$"}method: String!) {
  tycoon_order_setPaymentMethod(orderSlug: ${"$"}orderSlug, method: ${"$"}method) {
    order {
      slug
      __typename
    }
    __typename
  }
}

    """.trimIndent()
    class Variables(
        val orderSlug: String,
        val method: String = "alipay"
    )

    fun getRequestBody(orderSlug: String): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(orderSlug = orderSlug))
    }
}

class GetStripePaymentMethodQuery {
    val query="""query GetStripePaymentMethodQuery(${"$"}orderSlug: String!) {
  order(slug: ${"$"}orderSlug) {
    payment {
      apiKey(name: "STRIPE") {
        value
        __typename
      }
      __typename
    }
    order {
      id
      recurring
      slug
      paymentMethod {
        ... on StripeIntentMethod {
          clientSecret
          __typename
        }
        __typename
      }
      __typename
    }
    billingAddress {
      ...PostalAddressFragment
      __typename
    }
    shippingAddress {
      ...PostalAddressFragment
      __typename
    }
    context {
      pricing {
        currencyCode
        __typename
      }
      __typename
    }
    totals {
      total
      __typename
    }
    savedCards {
      id
      brand
      last4
      expMonth
      expYear
      __typename
    }
    __typename
  }
  customer {
    id
    email
    __typename
  }
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
""".trimIndent()
    class Variables(
        val orderSlug: String
    )

    fun getRequestBody(orderSlug: String): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(orderSlug = orderSlug))
    }
}


class SearchFromShipQuery {
    val query = """query filterShips(${"$"}fromId: Int, ${"$"}toId: Int, ${"$"}fromFilters: [FilterConstraintValues], ${"$"}toFilters: [FilterConstraintValues]) {
  from(to: ${"$"}toId, filters: ${"$"}fromFilters) {
    ships {
      id
    }
  }
  to(from: ${"$"}fromId, filters: ${"$"}toFilters) {
    featured {
      reason
      style
      tagLabel
      tagStyle
      footNotes
      shipId
    }
    ships {
      id
      skus {
        id
        price
        upgradePrice
        unlimitedStock
        showStock
        available
        availableStock
      }
    }
  }
}
"""
    class Variables(
        val fromFilters: List<String> = listOf(),
        val toFilters: List<String> = listOf(),
        val toId: Int
    )

    fun getRequestBody(fromFilters: List<String> = listOf(), toFilters: List<String> = listOf(), toId: Int): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(fromFilters = fromFilters, toFilters = toFilters, toId = toId))
    }
}

class UpgradeAddToCartQuery {
    val query = """mutation addToCart(${"$"}from: Int!, ${"$"}to: Int!) {
  addToCart(from: ${"$"}from, to: ${"$"}to) {
    jwt
  }
}
"""
    class Variables(
        val from: Int,
        val to: Int
    )

    fun getRequestBody(from: Int, to: Int): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(from = from, to = to))
    }
}

class LoginQuery {
    val query = """mutation signin(${"$"}email: String!, ${"$"}password: String!, ${"$"}captcha: String, ${"$"}remember: Boolean) {
  account_signin(email: ${"$"}email, password: ${"$"}password, captcha: ${"$"}captcha, remember: ${"$"}remember) {
    displayname
    id
    __typename
  }
}
"""
    class Variables(
        val captcha: String,
        val email: String,
        val password: String,
        val remember: Boolean = false
    )

    fun getRequestBody(email: String, password: String, captcha: String, remember: Boolean = false): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(captcha = captcha, email = email, password = password, remember = remember))
    }
}

class MultiStepLoginQuery {
    val query = """mutation multistep(${"$"}code: String!, ${"$"}deviceType: String!, ${"$"}deviceName: String!, ${"$"}duration: String!) {
  account_multistep(code: ${"$"}code, device_type: ${"$"}deviceType, device_name: ${"$"}deviceName, duration: ${"$"}duration) {
    displayname
    id
    __typename
  }
}"""
    class Variables(
        val code: String,
        val deviceName: String = "StarRefuge",
        val deviceType: String = "computer",
        val duration: String = "year"
    )

    fun getRequestBody(code: String, deviceName: String = "StarRefuge", deviceType: String = "computer", duration: String = "year"): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(code = code, deviceName = deviceName, deviceType = deviceType, duration = duration))
    }
}

class PromotionCodeQuery {
    val query = """query PromotionCodeQuery(${"$"}code: String!, ${"$"}id: ID!) {
  promotionalCode {
    validate(query: {code: ${"$"}code, id: ${"$"}id}) {
      isValid
      error
      __typename
    }
    __typename
  }
  heap {
    launcherDownloadLink: variables(key: "rsi.game.download_url") {
      value
      __typename
    }
    __typename
  }
}
"""
    class Variables(
        val code: String,
        val id: String
    )

    fun getRequestBody(code: String, id: String): BaseGraphQLBody {
        return BaseGraphQLBody(query, Variables(code = code, id = id))
    }
}


data class RegisterBody(
    @Json(name = "query")
    val query: String = """mutation signup(${"$"}handle: String!, ${"$"}email: String!, ${"$"}password: String!, ${"$"}dob: String!, ${"$"}agreementChecked: Boolean!, ${"$"}messageMe: Boolean, ${"$"}captcha: String!, ${"$"}referralCode: String, ${"$"}mark: String) {
  account_signup(handle: ${"$"}handle, email: ${"$"}email, password: ${"$"}password, dob: ${"$"}dob, agreementChecked: ${"$"}agreementChecked, messageMe: ${"$"}messageMe, captcha: ${"$"}captcha, referralCode: ${"$"}referralCode, mark: ${"$"}mark) {
    displayname
    username
    __typename
  }
}
""",
    @Json(name = "variables")
    val variables: Variables
) {
    data class Variables(
        @Json(name = "agreementChecked")
        val agreementChecked: Boolean = true,
        @Json(name = "captcha")
        val captcha: String,
        @Json(name = "dob")
        val dob: String = "1990-01-01",
        @Json(name = "email")
        val email: String,
        @Json(name = "handle")
        val handle: String,
        @Json(name = "messageMe")
        val messageMe: Boolean = false,
        @Json(name = "password")
        val password: String,
        @Json(name = "referralCode")
        val referralCode: String
    )
}

data class LoginBody(
    @Json(name = "query")
    val query: String = """mutation signin(${"$"}email: String!, ${"$"}password: String!, ${"$"}captcha: String, ${"$"}remember: Boolean) {
  account_signin(email: ${"$"}email, password: ${"$"}password, captcha: ${"$"}captcha, remember: ${"$"}remember) {
    displayname
    id
    __typename
  }
}
""",
    @Json(name = "variables")
    val variables: Variables
) {
    data class Variables(
        @Json(name = "captcha")
        val captcha: String,
        @Json(name = "email")
        val email: String,
        @Json(name = "password")
        val password: String,
        @Json(name = "remember")
        val remember: Boolean = true
    )
}
