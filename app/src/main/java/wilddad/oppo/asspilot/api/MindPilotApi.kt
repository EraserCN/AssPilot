package wilddad.oppo.asspilot.api

import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * MindPilot API client.
 *
 * @param baseUrl  Server base URL
 * @param cookie   Per-session randomly generated UUID used as the Cookie value.
 *                 Each session generates its own UUID via SessionEntity.cookieId.
 */
class MindPilotApi(
    private val baseUrl: String,
    private val cookie: String          // random UUID per session
) {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    private fun buildHeaders(): Headers {
        return Headers.Builder()
            .add("app-version-id", "20260306")
            .add("Content-Type", "application/json")
            .add("Cookie", "session_token=$cookie")   // UUID used as cookie value
            .add("User-Agent", "AssPilot/1.0 Android")
            .add("Accept", "text/event-stream, application/json")
            .build()
    }

    private fun buildPayload(
        userInput: String,
        sessionId: String,
        model: String,
        language: String = "zh"
    ): ChatPayload {
        val messageId = UUID.randomUUID().toString()
        return ChatPayload(
            contents = listOf(ContentItem(data = userInput)),
            message = userInput,
            model = model,
            sessionId = sessionId,
            messageId = messageId,
            route = RouteInfo(
                model = model,
                sessionId = sessionId,
                recommendedModels = listOf(model)
            ),
            meta = MetaInfo(language = language)
        )
    }

    /**
     * Executes preprocess + chat (SSE stream).
     * All callbacks run on the background thread — caller must switch to main thread if needed.
     */
    fun chat(
        userInput: String,
        sessionId: String,
        model: String,
        language: String = "zh",
        onToken: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val payload = buildPayload(userInput, sessionId, model, language)
        val jsonBody = gson.toJson(payload)
        val headers = buildHeaders()

        // Step 1: Preprocess (fire-and-forget, best-effort)
        try {
            val preprocessReq = Request.Builder()
                .url("$baseUrl/assistant/api/v1/preprocess")
                .headers(headers)
                .post(jsonBody.toRequestBody(JSON))
                .build()
            client.newCall(preprocessReq).execute().close()
        } catch (_: Exception) { /* Intentionally ignored */ }

        // Step 2: Chat SSE stream
        val chatReq = Request.Builder()
            .url("$baseUrl/assistant/api/v1/chat")
            .headers(headers)
            .post(jsonBody.toRequestBody(JSON))
            .build()

        try {
            val response = client.newCall(chatReq).execute()
            if (!response.isSuccessful) {
                onError("请求失败 (${response.code}): ${response.body?.string()?.take(200)}")
                return
            }

            val body = response.body ?: run {
                onError("响应体为空")
                return
            }

            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val l = line ?: continue
                if (l.startsWith("data: ")) {
                    val raw = l.removePrefix("data: ").trim()
                    if (raw == "[DONE]") break
                    try {
                        val data = gson.fromJson(raw, SseData::class.java)
                        val token = data.choices?.firstOrNull()?.delta?.content ?: continue
                        if (token.isNotEmpty()) onToken(token)
                    } catch (_: Exception) { /* Malformed SSE line — skip */ }
                }
            }
            onComplete()
        } catch (e: Exception) {
            onError("网络异常: ${e.message}")
        }
    }
}
