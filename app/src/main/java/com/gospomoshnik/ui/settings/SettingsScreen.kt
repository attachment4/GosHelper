package com.gospomoshnik.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gospomoshnik.domain.model.FontSize
import com.gospomoshnik.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenLegal: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val cs = MaterialTheme.colorScheme

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Тема оформления ──────────────────────────────────────────
            SettingsGroup(title = "Тема оформления") {
                val themes = listOf(
                    ThemeMode.SYSTEM to "Как в системе",
                    ThemeMode.LIGHT  to "Светлая",
                    ThemeMode.DARK   to "Тёмная"
                )
                themes.forEach { (mode, label) ->
                    OptionRow(
                        label    = label,
                        selected = settings.themeMode == mode,
                        onClick  = { viewModel.setTheme(mode) }
                    )
                }
            }

            // ── Размер текста ────────────────────────────────────────────
            SettingsGroup(title = "Размер текста") {
                FontSize.entries.forEach { size ->
                    OptionRow(
                        label     = size.label,
                        selected  = settings.fontSize == size,
                        onClick   = { viewModel.setFontSize(size) },
                        trailing  = {
                            // Живой предпросмотр кегля
                            Text(
                                "Аа",
                                fontSize = (15 * size.scale).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = cs.onSurfaceVariant
                            )
                        }
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = "Так будет выглядеть текст в приложении.",
                    fontSize = 14.sp,
                    color    = cs.onSurfaceVariant
                )
            }

            // ── Документы ────────────────────────────────────────────────
            SettingsGroup(title = "Правовые документы") {
                OptionRow(label = "Условия использования", selected = false, onClick = { onOpenLegal("terms") })
                OptionRow(label = "Политика конфиденциальности", selected = false, onClick = { onOpenLegal("privacy") })
                OptionRow(label = "Отказ от ответственности", selected = false, onClick = { onOpenLegal("disclaimer") })
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text       = title,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = cs.onSurface
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = cs.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, color = cs.onSurface, modifier = Modifier.weight(1f))
        trailing?.let { it(); Spacer(Modifier.width(12.dp)) }
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = "Выбрано", tint = cs.primary)
        }
    }
}
