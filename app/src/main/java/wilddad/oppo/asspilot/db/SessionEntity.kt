package wilddad.oppo.asspilot.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: String,              // UUID
    val title: String,           // First user message (truncated to 40 chars)
    val model: String,           // Selected AI model
    val cookieId: String,        // Independent UUID per session for cookie tracking
    val createdAt: Long,
    val updatedAt: Long
)
