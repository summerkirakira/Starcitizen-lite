package vip.kirakira.starcitizenlite.network.account

data class ResetCharacterBody(
    val password: String,
    val reason: String,
    val issue_council: String
)

data class PtuAccountBody (val destination: String="ptu")