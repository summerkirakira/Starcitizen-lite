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
        val variables = getCatalogVariables(page, "price", listOf("72", "268", "289", "270", "3", "41", "60"), "asc")
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

class MultiStepMutation


