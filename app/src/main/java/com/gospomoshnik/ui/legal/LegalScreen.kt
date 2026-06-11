package com.gospomoshnik.ui.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gospomoshnik.ui.chat.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Просмотр юридических документов из assets/legal/<doc>.md */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    doc: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    var text by remember(doc) { mutableStateOf("") }

    LaunchedEffect(doc) {
        text = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("legal/$doc.md").bufferedReader().use { it.readText() }
            }.getOrDefault("Документ недоступен.")
        }
    }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(
                title = { Text(legalTitle(doc)) },
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
            Spacer(Modifier.height(24.dp))
        }
    }
}

fun legalTitle(doc: String) = when (doc) {
    "disclaimer" -> "Отказ от ответственности"
    "privacy"    -> "Политика конфиденциальности"
    "terms"      -> "Условия использования"
    else         -> "Документ"
}
