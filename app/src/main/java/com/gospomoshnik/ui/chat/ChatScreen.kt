package com.gospomoshnik.ui.chat

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
                onGenerateDoc   = { onGenerateDocument(uiState.sessionId) },
                onBack          = onBack
            )
        },
        bottomBar = {
            ChatInputBar(
                text      = uiState.inputText,
                onChange  = viewModel::onInputChange,
                onSend    = viewModel::send,
                isLoading = uiState.isLoading
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                WelcomePlaceholder(category = category)
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
                ErrorBanner(message = err, onDismiss = viewModel::clearError)
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
    onGenerateDoc: () -> Unit,
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
        if (isUser) Spacer(Modifier.width(8.dp))
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
private fun WelcomePlaceholder(category: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            Text("👋", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text       = "Задайте вопрос по теме\n«${categoryTitle(category)}»",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = "ИИ ответит и при необходимости\nпоможет составить документ",
                fontSize   = 12.sp,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = message,
                color    = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) { Text("OK", fontSize = 12.sp) }
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
