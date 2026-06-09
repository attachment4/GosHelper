package com.gospomoshnik.ui.paywall

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Экран подписки Pro.
 * TODO Фаза 5: планы 199₽/мес и 990₽/год, интеграция ЮKassa SDK, СБП.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(onClose: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ГосПомощник Pro") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier         = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Подписка — реализация в Фазе 5")
        }
    }
}
