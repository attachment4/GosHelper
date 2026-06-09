package com.gospomoshnik.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Экран чата с ИИ.
 * TODO Фаза 2: подключить ChatViewModel, GigaChat стриминг, сохранение истории.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    category: String,
    onGenerateDocument: (Long) -> Unit,
    onPaywallRequired: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryTitle(category)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier          = Modifier.fillMaxSize().padding(padding),
            contentAlignment  = Alignment.Center
        ) {
            Text("Чат — реализация в Фазе 2")
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
