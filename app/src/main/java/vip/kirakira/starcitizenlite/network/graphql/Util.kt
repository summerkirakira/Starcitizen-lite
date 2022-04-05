package vip.kirakira.viewpagertest.network.graphql

class BaseGraphQLBody(private var query: String, private var variables: Any) {
    override fun toString(): String {
        return "BaseGraphQLBody(query='$query', variables=$variables)"
    }

    fun setQuery(query: String) {
        this.query = query
    }

    fun setVariables(variables: Any) {
        this.variables = variables
    }
}