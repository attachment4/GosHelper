package com.gospomoshnik.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gospomoshnik.ui.theme.GosColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BrandColor = GosColors.Blue
private val GoldColor  = GosColors.Amber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onUpgradeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val subscription by viewModel.subscription.collectAsState()
    val cs = MaterialTheme.colorScheme

    var showClearDialog by remember { mutableStateOf(false) }
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title   = { Text("Очистить историю чатов?") },
            text    = { Text("Все диалоги будут удалены с устройства без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { showClearDialog = false; viewModel.clearAllHistory() }) {
                    Text("Очистить", color = cs.error)
                }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Отмена") } }
        )
    }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Карточка тарифа
            Surface(
                shape    = RoundedCornerShape(16.dp),
                color    = if (subscription.isPro) GosColors.AmberLight else GosColors.BlueLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text       = if (subscription.isPro) "👑 ГосПомощник Pro" else "Бесплатный план",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (subscription.isPro) GoldColor else BrandColor
                    )
                    Spacer(Modifier.height(8.dp))
                    if (subscription.isPro) {
                        Text(
                            text     = "Безлимитные запросы",
                            fontSize = 13.sp,
                            color    = GoldColor.copy(alpha = 0.85f)
                        )
                        subscription.expiresAt?.let { ts ->
                            val date = SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(Date(ts))
                            Text(
                                text     = "Действует до $date",
                                fontSize = 12.sp,
                                color    = GoldColor.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Text(
                            text     = "Осталось ${subscription.requestsLeft} из ${subscription.requestsLimit} вопросов на сегодня",
                            fontSize = 13.sp,
                            color    = BrandColor.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = {
                                subscription.requestsLeft.toFloat() / subscription.requestsLimit
                            },
                            modifier   = Modifier.fillMaxWidth().height(8.dp),
                            color      = BrandColor,
                            trackColor = BrandColor.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            if (!subscription.isPro) {
                Button(
                    onClick  = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(18.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = BrandColor)
                ) {
                    Text("Перейти на Pro — от 67 ₽/мес", fontWeight = FontWeight.SemiBold)
                }
            }

            // Быстрый вход в настройки
            Surface(
                shape    = RoundedCornerShape(18.dp),
                color    = cs.surface,
                modifier = Modifier.fillMaxWidth(),
                onClick  = onSettingsClick
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = cs.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text("Тема и размер текста", fontSize = 16.sp, color = cs.onSurface)
                }
            }

            // Очистка всей истории чатов
            Surface(
                shape    = RoundedCornerShape(18.dp),
                color    = cs.surface,
                modifier = Modifier.fillMaxWidth(),
                onClick  = { showClearDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = cs.error)
                    Spacer(Modifier.width(12.dp))
                    Text("Очистить историю чатов", fontSize = 16.sp, color = cs.error)
                }
            }

            Text(
                text       = "Лимит обновляется каждый день.\nИстория чатов хранится только на устройстве.",
                fontSize   = 12.sp,
                color      = cs.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
