package wilddad.oppo.asspilot.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import wilddad.oppo.asspilot.R
import wilddad.oppo.asspilot.db.SessionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (SessionEntity) -> Unit,
    private val onItemLongClick: (SessionEntity) -> Unit
) : ListAdapter<SessionEntity, HistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SessionEntity>() {
            override fun areItemsTheSame(old: SessionEntity, new: SessionEntity) = old.id == new.id
            override fun areContentsTheSame(old: SessionEntity, new: SessionEntity) = old == new
        }
        private val DATE_FORMAT = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_session_title)
        private val tvModel: TextView = itemView.findViewById(R.id.tv_session_model)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_session_date)

        fun bind(session: SessionEntity) {
            tvTitle.text = session.title.ifEmpty { itemView.context.getString(R.string.new_chat) }
            tvModel.text = session.model
            tvDate.text = DATE_FORMAT.format(Date(session.updatedAt))

            itemView.setOnClickListener { onItemClick(session) }
            itemView.setOnLongClickListener {
                onItemLongClick(session)
                true
            }
        }
    }
}
