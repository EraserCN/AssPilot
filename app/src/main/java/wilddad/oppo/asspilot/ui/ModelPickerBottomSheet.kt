package wilddad.oppo.asspilot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import wilddad.oppo.asspilot.databinding.BottomSheetModelPickerBinding
import wilddad.oppo.asspilot.databinding.ItemModelOptionBinding
import wilddad.oppo.asspilot.utils.Prefs

class ModelPickerBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetModelPickerBinding? = null
    private val binding get() = _binding!!

    private var onModelSelected: ((String) -> Unit)? = null
    private var currentModel: String = ""

    // Model metadata: name, emoji icon, short description
    private val modelMeta = listOf(
        Triple("Mind Pilot",   "🧠", "OPPO AI 旗舰模型"),
        Triple("Gemini",       "✨", "Google Gemini"),
        Triple("Perplexity",   "🔍", "实时搜索增强回答"),
        Triple("ChatGPT",      "💬", "OpenAI GPT 系列"),
        Triple("Claude",       "🌟", "Anthropic Claude")
    )

    companion object {
        fun newInstance(
            currentModel: String,
            onModelSelected: (String) -> Unit
        ): ModelPickerBottomSheet {
            return ModelPickerBottomSheet().apply {
                this.currentModel = currentModel
                this.onModelSelected = onModelSelected
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetModelPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemBindings = listOf(
            ItemModelOptionBinding.bind(binding.modelMindPilot.root),
            ItemModelOptionBinding.bind(binding.modelGemini.root),
            ItemModelOptionBinding.bind(binding.modelPerplexity.root),
            ItemModelOptionBinding.bind(binding.modelChatgpt.root),
            ItemModelOptionBinding.bind(binding.modelClaude.root)
        )

        modelMeta.forEachIndexed { index, (name, icon, desc) ->
            if (index < itemBindings.size) {
                val itemBinding = itemBindings[index]
                itemBinding.tvModelIcon.text = icon
                itemBinding.tvModelName.text = name
                itemBinding.tvModelDesc.text = desc
                itemBinding.ivCheck.visibility =
                    if (name == currentModel) View.VISIBLE else View.GONE

                itemBinding.root.setOnClickListener {
                    onModelSelected?.invoke(name)
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
