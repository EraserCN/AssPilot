package wilddad.oppo.asspilot.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wilddad.oppo.asspilot.AssPilotApp
import wilddad.oppo.asspilot.api.MindPilotApi
import wilddad.oppo.asspilot.db.MessageEntity
import wilddad.oppo.asspilot.db.SessionEntity
import wilddad.oppo.asspilot.utils.Prefs
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AssPilotApp.instance.database
    private val sessionDao = db.sessionDao()
    private val messageDao = db.messageDao()
    private val prefs = Prefs(application)

    private val _sessionId = MutableLiveData<String>()

    // ✅ Fix: Use getSessionByIdLiveData to reactively observe DB changes
    val session: LiveData<SessionEntity?> = _sessionId.switchMap { id ->
        sessionDao.getSessionByIdLiveData(id)
    }

    val messages: LiveData<List<MessageEntity>> = _sessionId.switchMap { id ->
        messageDao.getMessagesForSession(id)
    }

    private val _isStreaming = MutableLiveData(false)
    val isStreaming: LiveData<Boolean> = _isStreaming

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _streamingContent = MutableLiveData<String>()
    val streamingContent: LiveData<String> = _streamingContent

    fun createNewSession(model: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sessionId = UUID.randomUUID().toString()
            val sessionCookie = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val entity = SessionEntity(
                id = sessionId,
                title = "",
                model = model,
                cookieId = sessionCookie,
                createdAt = now,
                updatedAt = now
            )
            sessionDao.insertSession(entity)
            _sessionId.postValue(sessionId)
        }
    }

    fun loadSession(sessionId: String) {
        _sessionId.value = sessionId
    }

    fun updateModel(newModel: String) {
        val id = _sessionId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            sessionDao.updateModel(id, newModel, System.currentTimeMillis())
        }
    }

    fun sendMessage(userInput: String) {
        val sessionId = _sessionId.value ?: return
        if (_isStreaming.value == true) return

        viewModelScope.launch(Dispatchers.IO) {
            // ✅ Re-fetch current session to get the latest model selected by user
            val sessionEntity = sessionDao.getSessionById(sessionId) ?: return@launch
            val model = sessionEntity.model
            val cookie = sessionEntity.cookieId
            val baseUrl = prefs.baseUrl
            val language = prefs.language

            val userMsg = MessageEntity(
                sessionId = sessionId,
                role = "user",
                content = userInput,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(userMsg)

            if (sessionEntity.title.isEmpty()) {
                val title = if (userInput.length > 40) userInput.take(40) + "…" else userInput
                sessionDao.updateTitle(sessionId, title, System.currentTimeMillis())
            }

            val assistantMsg = MessageEntity(
                sessionId = sessionId,
                role = "assistant",
                content = "",
                timestamp = System.currentTimeMillis()
            )
            val assistantMsgId = messageDao.insertMessage(assistantMsg)

            _isStreaming.postValue(true)
            _streamingContent.postValue("")

            val api = MindPilotApi(baseUrl, cookie)
            val buffer = StringBuilder()

            api.chat(
                userInput = userInput,
                sessionId = sessionId,
                model = model,
                language = language,
                onToken = { token ->
                    buffer.append(token)
                    _streamingContent.postValue(buffer.toString())
                },
                onComplete = {
                    viewModelScope.launch(Dispatchers.IO) {
                        val finalMsg = assistantMsg.copy(
                            id = assistantMsgId,
                            content = buffer.toString(),
                            timestamp = System.currentTimeMillis()
                        )
                        messageDao.updateMessage(finalMsg)
                        _isStreaming.postValue(false)
                    }
                },
                onError = { error ->
                    viewModelScope.launch(Dispatchers.IO) {
                        val errMsg = assistantMsg.copy(
                            id = assistantMsgId,
                            content = "⚠️ $error",
                            timestamp = System.currentTimeMillis()
                        )
                        messageDao.updateMessage(errMsg)
                    }
                    _errorMessage.postValue(error)
                    _isStreaming.postValue(false)
                }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class ChatViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChatViewModel(application) as T
    }
}
