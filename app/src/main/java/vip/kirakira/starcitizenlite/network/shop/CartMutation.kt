package vip.kirakira.starcitizenlite.network.shop

import vip.kirakira.starcitizenlite.network.RSIApi
import vip.kirakira.viewpagertest.network.graphql.*

suspend fun getCartSummary(): CartSummaryProperty {
    return RSIApi.retrofitService.getCart(CartSummaryViewMutation().getRequestBody())
}

suspend fun addToCart(itemId: Int, quantity: Int): AddCartItemProperty {
    val addCartItemMutation = AddToCartMutation().getRequestBody(itemId, quantity)
    return RSIApi.retrofitService.addToCart(addCartItemMutation)
}

suspend fun step1Query(): Step1QueryProperty {
    return RSIApi.retrofitService.step1Query(Step1Mutation().getRequestBody())
}

suspend fun addCredit(credit: Float): AddCreditProperty {
    return RSIApi.retrofitService.addCredit(AddCreditMutation().getRequestBody(credit))
}

suspend fun nextStep(): NextStepProperty {
    return RSIApi.retrofitService.nextStep(NextStepMutation().getRequestBody())
}

suspend fun clearCart(): ClearCartProperty {
    return RSIApi.retrofitService.clearCart(ClearCartMutation().getRequestBody())
}

suspend fun cartValidation(token: String): CartValidationProperty {
    return RSIApi.retrofitService.cartValidation(CartValidationMutation().getRequestBody(token=token))
}

suspend fun cartAddressAssign(billing: String): CartAddressAssignProperty {
    return RSIApi.retrofitService.cartAddressAssign(CartAddressAssignMutation().getRequestBody(billing))
}

suspend fun cartAddressQuery(): CartAddressProperty {
    return RSIApi.retrofitService.cartAddressQuery(AddressBookQuery().getRequestBody())
}

suspend fun setPaymentMethodMutation(slug: String): SetPaymentMethodProperty {
    return RSIApi.retrofitService.setPaymentMethod(SetPaymentMethodMutation().getRequestBody(slug))
}

suspend fun getStripePaymentMethod(slug: String): GetStripePaymentMethodProperty {
    return RSIApi.retrofitService.getStripePaymentMethod(GetStripePaymentMethodQuery().getRequestBody(slug))
}