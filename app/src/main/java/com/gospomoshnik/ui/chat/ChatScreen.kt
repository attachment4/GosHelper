package com.gospomoshnik.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gospomoshnik.domain.model.ChatMessage
import com.gospomoshnik.domain.model.FREE_DAILY_LIMIT
import com.gospomoshnik.ui.theme.GosColors
import kotlinx.coroutines.launch

private val BrandColor = GosColors.Blue
private val BrandLight = GosColors.BlueLight
private val BrandMid   = GosColors.BlueMid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    category: String,
    onGenerateDocument: (Long) -> Unit,
    onPaywallRequired: () -> Unit,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(uiState.messages.size - 1) }
        }
    }

    LaunchedEffect(uiState.showPaywall) {
        if (uiState.showPaywall) {
            onPaywallRequired()
            viewModel.dismissPaywall()
        }
    }

    // После удаления диалога — закрыть экран
    LaunchedEffect(uiState.closed) {
        if (uiState.closed) onBack()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Удалить диалог?") },
            text    = { Text("Переписка по этой теме будет удалена с устройства без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.deleteCurrentChat() }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") } }
        )
    }

    // Голосовой ввод через системное распознавание (без разрешения RECORD_AUDIO)
    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data
            ?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.let { spoken -> if (spoken.isNotBlank()) viewModel.appendInput(spoken) }
    }
    val startVoice: () -> Unit = {
        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Говорите…")
        }
        runCatching { voiceLauncher.launch(intent) }
    }

    // Документ можно создать, когда ИИ уже что-то ответил в сохранённой сессии
    val canGenerateDoc = uiState.sessionId > 0L &&
        uiState.messages.any { it.role == "assistant" }

    Scaffold(
        modifier       = Modifier.imePadding(),   // поднимаем ввод над клавиатурой
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChatTopBar(
                category        = category,
                requestsLeft    = uiState.requestsLeft,
                isPro           = uiState.isPro,
                canGenerateDoc  = canGenerateDoc,
                canDelete       = uiState.sessionId > 0L && uiState.messages.isNotEmpty(),
                onGenerateDoc   = { onGenerateDocument(uiState.sessionId) },
                onDelete        = { showDeleteDialog = true },
                onBack          = onBack
            )
        },
        bottomBar = {
            ChatInputBar(
                text      = uiState.inputText,
                onChange  = viewModel::onInputChange,
                onSend    = viewModel::send,
                onMic     = startVoice,
                isLoading = uiState.isLoading
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                WelcomePlaceholder(category = category, onExample = viewModel::onInputChange)
            } else {
                LazyColumn(
                    state          = listState,
                    modifier       = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { msg ->
                        MessageBubble(msg = msg)
                    }
                    if (uiState.isLoading) {
                        item { TypingIndicator() }
                    }
                }
            }

            uiState.error?.let { err ->
                ErrorBanner(message = err, onRetry = viewModel::retry, onDismiss = viewModel::clearError)
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    category: String,
    requestsLeft: Int,
    isPro: Boolean,
    canGenerateDoc: Boolean,
    canDelete: Boolean,
    onGenerateDoc: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(GosColors.Blue, GosColors.BlueDark)))
            .statusBarsPadding()
            .padding(start = 4.dp, end = 16.dp, top = 14.dp, bottom = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = categoryTitle(category),
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 17.sp,
                    maxLines   = 1
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text     = "ИИ-консультант · онлайн",
                    color    = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }
            if (canGenerateDoc) {
                IconButton(onClick = onGenerateDoc) {
                    Icon(Icons.Default.Description, contentDescription = "Создать документ", tint = Color.White)
                }
            }
            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить диалог", tint = Color.White)
                }
            }
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.18f)) {
                Text(
                    text       = if (isPro) "PRO" else "$requestsLeft / $FREE_DAILY_LIMIT",
                    color      = Color.White,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment     = Alignment.Bottom
    ) {
        if (!isUser) {
            AiAvatar()
            Spacer(Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Surface(
                shape = RoundedCornerShape(
                    topStart    = 16.dp,
                    topEnd      = 16.dp,
                    bottomEnd   = if (isUser) 4.dp else 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp
                ),
                color           = if (isUser) BrandColor else MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier        = Modifier.widthIn(max = 290.dp)
            ) {
                if (isUser) {
                    Text(
                        text       = msg.content,
                        color      = Color.White,
                        fontSize   = 13.sp,
                        lineHeight = 20.sp,
                        modifier   = Modifier.padding(horizontal = 13.dp, vertical = 10.dp)
                    )
                } else {
                    // Ответ ИИ приходит в Markdown — рендерим со списками и ссылками
                    MarkdownText(
                        markdown = msg.content,
                        color    = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp)
                    )
                }
            }
            // Под ответом ИИ — копировать и поделиться
            if (!isUser) MessageActions(text = msg.content)
        }
        if (isUser) Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun MessageActions(text: String) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        TextButton(
            onClick = {
                clipboard.setText(AnnotatedString(text))
                android.widget.Toast.makeText(context, "Скопировано", android.widget.Toast.LENGTH_SHORT).show()
            },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp), tint = GosColors.Blue)
            Spacer(Modifier.width(4.dp))
            Text("Копировать", fontSize = 12.sp, color = GosColors.Blue)
        }
        TextButton(
            onClick = {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, text)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Поделиться ответом"))
            },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = GosColors.Blue)
            Spacer(Modifier.width(4.dp))
            Text("Поделиться", fontSize = 12.sp, color = GosColors.Blue)
        }
    }
}

@Composable
private fun AiAvatar() {
    Box(
        modifier         = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(BrandLight),
        contentAlignment = Alignment.Center
    ) {
        Text("ИИ", fontSize = 9.sp, color = BrandColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TypingIndicator() {
    Row(verticalAlignment = Alignment.Bottom) {
        AiAvatar()
        Spacer(Modifier.width(8.dp))
        Surface(
            shape           = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            color           = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier.size(7.dp).clip(CircleShape).background(BrandMid)
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomePlaceholder(category: String, onExample: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(28.dp)
        ) {
            Text("👋", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text       = "Задайте вопрос по теме\n«${categoryTitle(category)}»",
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = "ИИ ответит и поможет составить документ",
                fontSize   = 12.sp,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Примеры:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            exampleQuestions(category).forEach { q ->
                Surface(
                    shape    = RoundedCornerShape(14.dp),
                    color    = GosColors.BlueLight,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clickable { onExample(q) }
                ) {
                    Text(
                        q,
                        fontSize = 13.sp,
                        color    = GosColors.Blue,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

private fun exampleQuestions(category: String): List<String> = when (category) {
    "gibdd"     -> listOf("Как обжаловать штраф ГИБДД?", "Как выгодно оплатить штраф?", "Что делать при ДТП?")
    "zhkh"      -> listOf("УК не делает ремонт — что делать?", "Как сделать перерасчёт за ЖКУ?", "Как оформить субсидию на ЖКУ?")
    "labor"     -> listOf("Не платят зарплату — что делать?", "Что выгоднее: соглашение или сокращение?", "Как оспорить увольнение?")
    "benefits"  -> listOf("Какие выплаты положены на ребёнка?", "Как оформить единое пособие?", "Какие льготы у пенсионеров?")
    "court"     -> listOf("Как подать иск в суд?", "Сколько стоит госпошлина?", "Как взыскать долг по расписке?")
    "documents" -> listOf("Как составить претензию?", "Как написать жалобу в госорган?", "Как составить расписку?")
    else        -> listOf("Как обжаловать штраф?", "Не платят зарплату — что делать?", "Как составить претензию?")
}

@Composable
private fun ChatInputBar(
    text: String,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
    onMic: () -> Unit,
    isLoading: Boolean
) {
    Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value         = text,
                onValueChange = onChange,
                placeholder   = { Text("Ваш вопрос...", fontSize = 13.sp) },
                shape         = RoundedCornerShape(22.dp),
                modifier      = Modifier.weight(1f),
                maxLines      = 4,
                textStyle     = LocalTextStyle.current.copy(fontSize = 13.sp)
            )
            // Голосовой ввод
            IconButton(onClick = onMic, enabled = !isLoading) {
                Icon(Icons.Default.Mic, contentDescription = "Голосовой ввод", tint = BrandColor)
            }
            FilledIconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isLoading,
                colors  = IconButtonDefaults.filledIconButtonColors(containerColor = BrandColor)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Отправить", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit, onDismiss: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = message,
                color    = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { onDismiss(); onRetry() }) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("Повторить", fontSize = 12.sp)
            }
            TextButton(onClick = onDismiss) { Text("✕", fontSize = 14.sp) }
        }
    }
}

private fun categoryTitle(category: String) = when (category) {
    "gibdd"     -> "ГИБДД — Штрафы"
    "zhkh"      -> "ЖКХ — Управляющая компания"
    "labor"     -> "Трудовые права"
    "benefits"  -> "Льготы и субсидии"
    "court"     -> "Суд и иски"
    "documents" -> "Документы"
    else        -> "Консультация"
}
