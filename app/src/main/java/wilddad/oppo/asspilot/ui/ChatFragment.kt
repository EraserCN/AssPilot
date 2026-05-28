package wilddad.oppo.asspilot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import wilddad.oppo.asspilot.R
import wilddad.oppo.asspilot.adapter.ChatAdapter
import wilddad.oppo.asspilot.databinding.FragmentChatBinding
import wilddad.oppo.asspilot.utils.Prefs
import wilddad.oppo.asspilot.viewmodel.ChatViewModel
import wilddad.oppo.asspilot.viewmodel.ChatViewModelFactory

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: ChatAdapter
    private lateinit var prefs: Prefs
    
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())
        
        setupRecyclerView()
        setupInputHandling()
        observeViewModel()
        setupClickListeners()

        // Initialize a new session if none exists
        if (viewModel.session.value == null) {
            viewModel.createNewSession(prefs.defaultModel)
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(requireContext())
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = this@ChatFragment.adapter
        }
    }

    private fun setupInputHandling() {
        // Handle Keyboard (IME) for the input bar in fragment
        ViewCompat.setOnApplyWindowInsetsListener(binding.inputBar) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // In MainActivity, we might already have bottom padding from BottomNav, 
            // but when keyboard is up, we want to be above it.
            val bottomPadding = if (imeInsets.bottom > 0) {
                imeInsets.bottom
            } else {
                // When keyboard is hidden, we let the BottomNav handle the bottom space 
                // or use a small default.
                0 
            }

            v.setPadding(
                v.paddingLeft, 
                v.paddingTop,
                v.paddingRight, 
                bottomPadding + resources.getDimensionPixelSize(R.dimen.input_padding_bottom)
            )
            insets
        }
    }

    private fun observeViewModel() {
        viewModel.session.observe(viewLifecycleOwner) { session ->
            session ?: return@observe
            binding.tvTitle.text = session.title.ifEmpty { getString(R.string.new_chat) }
            binding.tvCurrentModel.text = session.model
        }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages.toMutableList())
            val isEmpty = messages.isEmpty()
            binding.welcomeView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvMessages.visibility = if (isEmpty) View.GONE else View.VISIBLE
            
            if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        viewModel.isStreaming.observe(viewLifecycleOwner) { streaming ->
            binding.btnSend.isEnabled = !streaming
            binding.etInput.isEnabled = !streaming
            binding.progressBar.visibility = if (streaming) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.streamingContent.observe(viewLifecycleOwner) { content ->
            val current = adapter.currentList.toMutableList()
            val lastIdx = current.indexOfLast { it.role == "assistant" }
            if (lastIdx >= 0) {
                current[lastIdx] = current[lastIdx].copy(content = content)
                adapter.submitList(current)
                binding.rvMessages.scrollToPosition(current.size - 1)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            binding.etInput.setText("")
            viewModel.sendMessage(text)
        }

        binding.tvCurrentModel.setOnClickListener { showModelPicker() }
        binding.btnModelPicker.setOnClickListener { showModelPicker() }
    }

    private fun showModelPicker() {
        val session = viewModel.session.value ?: return
        ModelPickerBottomSheet.newInstance(session.model) { selectedModel ->
            viewModel.updateModel(selectedModel)
        }.show(parentFragmentManager, "model_picker")
    }
    
    fun startNewChat() {
        viewModel.createNewSession(prefs.defaultModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
