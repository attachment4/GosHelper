package com.gospomoshnik.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gospomoshnik.ui.chat.MarkdownText
import com.gospomoshnik.ui.theme.GosColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocViewerScreen(
    docId: String,
    onAskInChat: (String) -> Unit,   // category
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val doc = remember(docId) { findDoc(docId) }
    var text by remember(docId) { mutableStateOf("") }

    LaunchedEffect(docId) {
        text = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("docs/$docId.md").bufferedReader().use { it.readText() }
            }.getOrDefault("Документ недоступен.")
        }
    }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(
                title = { Text(doc?.title ?: "Документ", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            MarkdownText(markdown = text, color = cs.onSurface, baseSize = 14)

            Spacer(Modifier.height(20.dp))

            // Перейти в чат с готовым вопросом по теме документа
            val category = categoryForDoc(docId)
            Button(
                onClick  = { onAskInChat(category) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = GosColors.Blue)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Задать вопрос ИИ по теме", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun categoryForDoc(docId: String): String =
    docsCatalog.firstOrNull { cat -> cat.docs.any { it.id == docId } }?.key ?: "general"
