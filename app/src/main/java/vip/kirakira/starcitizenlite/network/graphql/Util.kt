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

@JsonClass(generateAdapter = true)
class ChooseUpgradeTargetBody(private val upgrade_id: String)

@JsonClass(generateAdapter = true)
class ApplyUpgradeBody(private val current_password: String,
                       private val pledge_id: String,
                       private val upgrade_id: String)