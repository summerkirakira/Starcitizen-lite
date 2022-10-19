package vip.kirakira.starcitizenlite.network.CirnoProperty

data class Announcement(
    val id: Int,
    val title: String,
    val content: String,
    val date: String,
)

data class Version (
    val id: Int,
    val version: String,
    val url: String,
    val date: String
)

data class StarUp(
    val id: Int,
    val title: String,
    val content: String,
)

data class RecaptchaList(
    val captcha_list: List<ReCaptcha>? = null,
    val error: String? = null
) {
    data class ReCaptcha(
        val token: String
    )
}