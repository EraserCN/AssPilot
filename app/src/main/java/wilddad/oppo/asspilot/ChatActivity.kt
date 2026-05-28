package wilddad.oppo.asspilot

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import wilddad.oppo.asspilot.adapter.ChatAdapter
import wilddad.oppo.asspilot.databinding.ActivityChatBinding
import wilddad.oppo.asspilot.ui.ModelPickerBottomSheet
import wilddad.oppo.asspilot.utils.Prefs
import wilddad.oppo.asspilot.viewmodel.ChatViewModel
import wilddad.oppo.asspilot.viewmodel.ChatViewModelFactory

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var prefs: Prefs

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)

        // Apply status bar top inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // ✅ Requirement 3: Handle Keyboard (IME) and Navigation Bar insets for the input bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.inputBar) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // If keyboard is shown, use imeInsets.bottom. 
            // If not, use systemBars.bottom (navigation bar).
            val bottomPadding = if (imeInsets.bottom > 0) {
                imeInsets.bottom
            } else {
                systemBars.bottom
            }

            v.setPadding(
                v.paddingLeft, 
                v.paddingTop,
                v.paddingRight, 
                bottomPadding + resources.getDimensionPixelSize(R.dimen.input_padding_bottom)
            )
            insets
        }

        setSupportActionBar(binding.toolbar)

        if (!prefs.disclaimerAccepted) {
            showDisclaimerDialog()
        } else {
            initSession()
        }
    }

    private fun showDisclaimerDialog() {
        AlertDialog.Builder(this, R.style.DisclaimerDialogTheme)
            .setTitle(getString(R.string.disclaimer_title))
            .setMessage(getString(R.string.disclaimer_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.disclaimer_confirm)) { dialog, _ ->
                prefs.disclaimerAccepted = true
                dialog.dismiss()
                initSession()
            }
            .show()
    }

    private fun initSession() {
        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        if (sessionId != null) {
            viewModel.loadSession(sessionId)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            viewModel.createNewSession(prefs.defaultModel)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(this)
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = this@ChatActivity.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.session.observe(this) { session ->
            session ?: return@observe
            supportActionBar?.title = session.title.ifEmpty { getString(R.string.new_chat) }
            binding.tvCurrentModel.text = session.model
        }

        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages.toMutableList())
            if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        viewModel.isStreaming.observe(this) { streaming ->
            binding.btnSend.isEnabled = !streaming
            binding.etInput.isEnabled = !streaming
            binding.progressBar.visibility = if (streaming) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.streamingContent.observe(this) { content ->
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
        }.show(supportFragmentManager, "model_picker")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_new_chat -> {
                val intent = Intent(this, ChatActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                true
            }
            R.id.action_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_SESSION_ID = "extra_session_id"
    }
}
