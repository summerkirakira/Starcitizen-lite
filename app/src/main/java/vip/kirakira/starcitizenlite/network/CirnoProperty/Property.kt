package vip.kirakira.starcitizenlite.network.CirnoProperty

import com.squareup.moshi.JsonClass

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

data class TranslationVersionProperty(
    val version: String
)

data class TranslationProperty(
    val id: Int,
    val product_id: Int,
    val type: String,
    val english_title: String,
    val title: String,
//    val content: String,
    val excerpt: String? = null,
//    val contains: String,
    val from_ship: Int,
    val to_ship: Int
)

@JsonClass(generateAdapter = true)
class AddNotTranslationBody(
    val product_id: Int,
    val type: String = "hanger",
    val create_at: Int = 0,
    val english_title: String,
    val content: List<String> = listOf(),
    val excerpt: String = "",
    val contains: List<String> = listOf(),
    val from_ship: Int = 0,
    val to_ship: Int = 0
)

data class AddTranslationResult(
    val message: String
)