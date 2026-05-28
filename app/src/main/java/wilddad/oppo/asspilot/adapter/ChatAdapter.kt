package wilddad.oppo.asspilot.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import wilddad.oppo.asspilot.R
import wilddad.oppo.asspilot.db.MessageEntity

class ChatAdapter(context: Context) :
    ListAdapter<MessageEntity, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private val markwon = Markwon.create(context)

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_ASSISTANT = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessageEntity>() {
            override fun areItemsTheSame(old: MessageEntity, new: MessageEntity) = old.id == new.id
            override fun areContentsTheSame(old: MessageEntity, new: MessageEntity) =
                old.content == new.content && old.role == new.role
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).role == "user") VIEW_TYPE_USER else VIEW_TYPE_ASSISTANT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_assistant, parent, false)
                AssistantMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message.content)
            is AssistantMessageViewHolder -> holder.bind(message.content, markwon)
        }
    }

    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        fun bind(content: String) {
            tvMessage.text = content
        }
    }

    class AssistantMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        private val tvTyping: View = itemView.findViewById(R.id.typing_indicator)

        fun bind(content: String, markwon: Markwon) {
            if (content.isEmpty()) {
                tvMessage.text = ""
                tvTyping.visibility = View.VISIBLE
            } else {
                tvTyping.visibility = View.GONE
                markwon.setMarkdown(tvMessage, content)
            }
        }
    }
}
