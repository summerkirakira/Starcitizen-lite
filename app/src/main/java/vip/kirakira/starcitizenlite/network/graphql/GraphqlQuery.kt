package vip.kirakira.viewpagertest.network.graphql


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