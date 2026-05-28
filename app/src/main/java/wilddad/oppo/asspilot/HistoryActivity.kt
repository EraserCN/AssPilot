package wilddad.oppo.asspilot

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wilddad.oppo.asspilot.adapter.HistoryAdapter
import wilddad.oppo.asspilot.databinding.ActivityHistoryBinding
import wilddad.oppo.asspilot.db.SessionEntity

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter
    private val sessionDao by lazy { AssPilotApp.instance.database.sessionDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ Enable Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Handle Status Bar Inset for AppBar
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.history)

        adapter = HistoryAdapter(
            onItemClick = { session -> openSession(session) },
            onItemLongClick = { session -> confirmDelete(session) }
        )

        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }

        observeSessions()
    }

    private fun observeSessions() {
        sessionDao.getAllSessions().observe(this) { sessions ->
            adapter.submitList(sessions)
            binding.emptyState.visibility =
                if (sessions.isEmpty()) View.VISIBLE else View.GONE
            binding.rvHistory.visibility =
                if (sessions.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun openSession(session: SessionEntity) {
        startActivity(
            Intent(this, ChatActivity::class.java).apply {
                putExtra(ChatActivity.EXTRA_SESSION_ID, session.id)
            }
        )
    }

    private fun confirmDelete(session: SessionEntity) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_session_title)
            .setMessage(R.string.delete_session_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    sessionDao.deleteSession(session)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
