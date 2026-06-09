package com.gospomoshnik.ui.document

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gospomoshnik.domain.model.ChatMessage
import com.gospomoshnik.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DocumentField(val label: String, val value: String, val editable: Boolean = false)

data class DocumentUiState(
    val title: String = "Документ",
    val subtitle: String = "",
    val fields: List<DocumentField> = emptyList(),
    val bodyText: String = "",
    val isLoading: Boolean = true,
    val pdfPath: String? = null,
    val error: String? = null
)

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Long = savedStateHandle["sessionId"] ?: 0L

    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState: StateFlow<DocumentUiState> = _uiState.asStateFlow()

    init {
        loadDocument()
    }

    private fun loadDocument() {
        viewModelScope.launch {
            val messages = chatRepository.getMessages(sessionId).first()
            val today = SimpleDateFormat("d MMMM yyyy 'г.'", Locale("ru")).format(Date())

            val category = detectCategory(messages)
            val template = buildTemplate(category, messages, today)
            _uiState.update { template.copy(isLoading = false) }
        }
    }

    fun updateField(index: Int, newValue: String) {
        _uiState.update { state ->
            val updated = state.fields.toMutableList()
            updated[index] = updated[index].copy(value = newValue)
            state.copy(fields = updated)
        }
    }

    fun updateBody(text: String) {
        _uiState.update { it.copy(bodyText = text) }
    }

    fun exportPdf() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val dir  = File(context.filesDir, "documents").also { it.mkdirs() }
                val file = File(dir, "document_${System.currentTimeMillis()}.txt")

                // Собираем текст документа (PDF через iTextG — Фаза 3+)
                val content = buildString {
                    appendLine(state.title.uppercase())
                    appendLine(state.subtitle)
                    appendLine()
                    state.fields.forEach { f -> appendLine("${f.label}: ${f.value}") }
                    appendLine()
                    appendLine(state.bodyText)
                }
                file.writeText(content)
                _uiState.update { it.copy(pdfPath = file.absolutePath) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка сохранения: ${e.message}") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun detectCategory(messages: List<ChatMessage>): String {
        val text = messages.joinToString(" ") { it.content }.lowercase()
        return when {
            "гибдд" in text || "штраф" in text || "постановл" in text -> "gibdd"
            "жкх" in text || "управляющ" in text || "квитанц" in text -> "zhkh"
            "уволь" in text || "трудов" in text || "зарплат" in text  -> "labor"
            "суд" in text || "иск" in text                            -> "court"
            else -> "general"
        }
    }

    private fun buildTemplate(
        category: String,
        messages: List<ChatMessage>,
        today: String
    ): DocumentUiState = when (category) {
        "gibdd" -> DocumentUiState(
            title    = "Жалоба в ГИБДД",
            subtitle = "Шаблон заявления об обжаловании",
            fields   = listOf(
                DocumentField("Кому", "Начальнику УГИБДД по г. Москве"),
                DocumentField("От кого", extractName(messages)),
                DocumentField("Номер постановления", "", editable = true),
                DocumentField("Дата постановления", "", editable = true),
                DocumentField("Дата", today)
            ),
            bodyText = "В указанное в постановлении время я не управлял транспортным средством, " +
                "указанным в постановлении. Транспортное средство не находилось на месте " +
                "предполагаемого нарушения. Прошу отменить постановление в связи с отсутствием " +
                "состава административного правонарушения и недоказанностью моей вины."
        )
        "zhkh" -> DocumentUiState(
            title    = "Жалоба в ГЖИ",
            subtitle = "Претензия к управляющей компании",
            fields   = listOf(
                DocumentField("Кому", "В Государственную жилищную инспекцию"),
                DocumentField("От кого", extractName(messages)),
                DocumentField("Адрес", "", editable = true),
                DocumentField("Дата", today)
            ),
            bodyText = "Прошу провести проверку деятельности управляющей компании " +
                "в связи с ненадлежащим исполнением обязанностей по содержанию " +
                "общего имущества многоквартирного дома."
        )
        else -> DocumentUiState(
            title    = "Заявление",
            subtitle = "Юридический документ",
            fields   = listOf(
                DocumentField("Кому", "", editable = true),
                DocumentField("От кого", extractName(messages)),
                DocumentField("Дата", today)
            ),
            bodyText = messages.lastOrNull { it.role == "assistant" }?.content
                ?: "Прошу рассмотреть мою жалобу и принять меры в соответствии с действующим законодательством РФ."
        )
    }

    private fun extractName(messages: List<ChatMessage>): String {
        val userMessages = messages.filter { it.role == "user" }.joinToString(" ") { it.content }
        val nameRegex = Regex("меня зовут ([А-ЯЁ][а-яё]+ [А-ЯЁ][а-яё]+ [А-ЯЁ][а-яё]+)", RegexOption.IGNORE_CASE)
        return nameRegex.find(userMessages)?.groupValues?.get(1) ?: ""
    }
}
