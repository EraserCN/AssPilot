package wilddad.oppo.asspilot.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import wilddad.oppo.asspilot.ChatActivity
import wilddad.oppo.asspilot.R
import wilddad.oppo.asspilot.databinding.FragmentChatListBinding

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Landing page - shows welcome state, new chat triggered from FAB in MainActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
