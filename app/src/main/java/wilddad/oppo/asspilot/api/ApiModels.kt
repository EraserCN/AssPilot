package wilddad.oppo.asspilot.api

import com.google.gson.annotations.SerializedName

data class ChatPayload(
    val contents: List<ContentItem>,
    val message: String,
    val model: String,
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("message_id") val messageId: String,
    val history: List<Any> = emptyList(),
    val stream: Boolean = true,
    val route: RouteInfo,
    val meta: MetaInfo
)

data class ContentItem(
    val type: String = "text",
    val data: String,
    @SerializedName("mime_type") val mimeType: String = "text/plain"
)

data class RouteInfo(
    val model: String,
    val strategy: String = "default",
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("recommended_models") val recommendedModels: List<String>,
    @SerializedName("user_specified") val userSpecified: Boolean = true
)

data class MetaInfo(
    val language: String = "zh",
    val timezone: String = "Asia/Shanghai"
)

data class SseChoice(
    val delta: SseDelta?
)

data class SseDelta(
    val content: String?
)

data class SseData(
    val choices: List<SseChoice>?
)
