package wilddad.oppo.asspilot.utils

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("asspilot_prefs", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = prefs.edit().putString(KEY_BASE_URL, value).apply()

    var defaultModel: String
        get() = prefs.getString(KEY_DEFAULT_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_DEFAULT_MODEL, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "zh") ?: "zh"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    /** True once the user has accepted the first-launch disclaimer */
    var disclaimerAccepted: Boolean
        get() = prefs.getBoolean(KEY_DISCLAIMER, false)
        set(value) = prefs.edit().putBoolean(KEY_DISCLAIMER, value).apply()

    companion object {
        const val DEFAULT_BASE_URL = "https://mindpilot-server-sg.allawnos.com"

        // ✅ Requirement 4: Default model is Perplexity
        const val DEFAULT_MODEL = "Perplexity"

        val AVAILABLE_MODELS = listOf(
            "Mind Pilot",
            "Gemini",
            "Perplexity",
            "ChatGPT",
            "Claude"
        )

        private const val KEY_BASE_URL = "base_url"
        private const val KEY_DEFAULT_MODEL = "default_model"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_DISCLAIMER = "disclaimer_accepted"
    }
}
