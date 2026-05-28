package wilddad.oppo.asspilot.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): LiveData<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun getSessionByIdLiveData(sessionId: String): LiveData<SessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("UPDATE sessions SET title = :title, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateTitle(sessionId: String, title: String, updatedAt: Long)

    @Query("UPDATE sessions SET model = :model, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateModel(sessionId: String, model: String, updatedAt: Long)
}
