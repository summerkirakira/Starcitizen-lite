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

@JsonClass(generateAdapter = true)
class SetAuthTokenBody()

@JsonClass(generateAdapter = true)
class SetContextTokenBody(
        private val fromShipId: Int? = null,
        private val pledgeId:Int? = null,
        private val toShipId:Int? = null,
        private val toSkuId: Int? = null
)

@JsonClass(generateAdapter = true)
class ApplyTokenBody(private val jwt: String)

@JsonClass(generateAdapter = true)
class BuyBackPledgeBody(private val id: Int)

@JsonClass(generateAdapter = true)
class ApplyPromoBody(
    private val promo: String,
    private val currency: String,
    private val code: String
    )

@JsonClass(generateAdapter = true)
class GetPledgeBody(private val page: Int)