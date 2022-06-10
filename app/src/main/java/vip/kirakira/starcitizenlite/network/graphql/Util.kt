package vip.kirakira.viewpagertest.network.graphql

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
class BaseGraphQLBody(private var query: String, private var variables: Any) {
    override fun toString(): String {
        return "{\"query\"=$query, \"variables\"=$variables}"
    }

    fun setQuery(query: String) {
        this.query = query
    }

    fun setVariables(variables: Any) {
        this.variables = variables
    }
}