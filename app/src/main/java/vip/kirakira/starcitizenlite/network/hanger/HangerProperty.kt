package vip.kirakira.starcitizenlite.network.hanger

data class ReclaimRequestBody(val pledge_id: String, val current_password: String)

data class BasicResponseBody(val success: Int, val code: String, val msg: String)

data class GiftPledgeRequestBody(val pledge_id: String, val current_password: String, val email: String, val name: String)

data class CancelPledgeRequestBody(val pledge_id: String)
