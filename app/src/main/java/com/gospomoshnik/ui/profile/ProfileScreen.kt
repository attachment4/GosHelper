package com.gospomoshnik.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gospomoshnik.ui.theme.GosColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BrandColor = GosColors.Blue
private val BrandLight = GosColors.BlueLight
private val GoldLight  = GosColors.AmberLight
private val GoldColor  = GosColors.Amber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onUpgradeClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val subscription by viewModel.subscription.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                shape = RoundedCornerShape(16.dp),
                color = if (subscription.isPro) GoldLight else BrandLight,
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
                            color    = GoldColor.copy(alpha = 0.8f)
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
                            color    = BrandColor.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(12.dp))
                        // Прогресс-бар лимита
                        LinearProgressIndicator(
                            progress = {
                                subscription.requestsLeft.toFloat() / subscription.requestsLimit
                            },
                            modifier   = Modifier.fillMaxWidth().height(8.dp),
                            color      = BrandColor,
                            trackColor = Color.White
                        )
                    }
                }
            }

            if (!subscription.isPro) {
                Button(
                    onClick  = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = BrandColor)
                ) {
                    Text("Перейти на Pro — от 67 ₽/мес", fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                text     = "Лимит обновляется каждый день.\nИстория чатов хранится только на устройстве.",
                fontSize = 12.sp,
                color    = GosColors.TextSecond,
                lineHeight = 18.sp
            )
        }
    }
}
