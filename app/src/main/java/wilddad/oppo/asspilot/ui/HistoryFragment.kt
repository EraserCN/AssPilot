package wilddad.oppo.asspilot.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wilddad.oppo.asspilot.AssPilotApp
import wilddad.oppo.asspilot.ChatActivity
import wilddad.oppo.asspilot.R
import wilddad.oppo.asspilot.adapter.HistoryAdapter
import wilddad.oppo.asspilot.databinding.FragmentHistoryBinding
import wilddad.oppo.asspilot.db.SessionEntity

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistoryAdapter
    private val sessionDao by lazy { AssPilotApp.instance.database.sessionDao() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HistoryAdapter(
            onItemClick = { session -> openSession(session) },
            onItemLongClick = { session -> confirmDelete(session) }
        )

        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }

        observeSessions()
    }

    private fun observeSessions() {
        sessionDao.getAllSessions().observe(viewLifecycleOwner) { sessions ->
            adapter.submitList(sessions)
            binding.emptyState.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
            binding.rvHistory.visibility = if (sessions.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun openSession(session: SessionEntity) {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_SESSION_ID, session.id)
        }
        startActivity(intent)
    }

    private fun confirmDelete(session: SessionEntity) {
        AlertDialog.Builder(requireContext())
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

    fun refresh() {
        // LiveData auto-refreshes, but we can trigger a manual update if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
