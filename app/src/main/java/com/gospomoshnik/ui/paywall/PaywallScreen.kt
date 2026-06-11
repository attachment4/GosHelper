package com.gospomoshnik.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gospomoshnik.domain.model.PlanType
import com.gospomoshnik.ui.theme.GosColors

private val DarkBrand  = GosColors.BlueDark
private val BrandColor = GosColors.Blue
private val BrandLight = GosColors.BlueLight
private val GreenColor = GosColors.Green
private val GreenLight = GosColors.GreenLight
private val GoldColor  = GosColors.Amber
private val GoldLight  = GosColors.AmberLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val processing = uiState.payment is PaymentState.Processing

    // Успешная оплата — короткая пауза и закрытие (Pro уже активирован)
    LaunchedEffect(uiState.payment) {
        if (uiState.payment is PaymentState.Success) {
            kotlinx.coroutines.delay(1400)
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        HeroSection(onClose = onClose)

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeaturesList()

            PlansRow(
                selected = uiState.selectedPlan,
                onSelect = viewModel::selectPlan
            )

            Button(
                onClick  = viewModel::pay,
                enabled  = !processing,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = BrandColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                when (uiState.payment) {
                    is PaymentState.Processing -> CircularProgressIndicator(
                        color = Color.White, strokeWidth = 2.dp, modifier = Modifier.height(22.dp)
                    )
                    is PaymentState.Success -> Text("Подписка оформлена ✓", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    else -> Text(uiState.selectedPlan.ctaText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            (uiState.payment as? PaymentState.Error)?.let { err ->
                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = GosColors.RedLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(err.message, color = GosColors.Red, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::dismissError) { Text("OK", fontSize = 12.sp) }
                    }
                }
            }

            PaymentBadges()

            FreeNote()

            Disclaimer()
        }
    }
}

@Composable
private fun HeroSection(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF062B6E), GosColors.BlueDark, GosColors.Blue)
                )
            )
            .padding(24.dp)
    ) {
        // Кнопка закрыть
        IconButton(
            onClick  = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            // Иконка короны
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("👑", fontSize = 30.sp)
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text       = "ГосПомощник Pro",
                color      = Color.White,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text      = "Полный доступ к ИИ-юристу\nбез ограничений",
                color     = Color.White.copy(alpha = 0.65f),
                fontSize  = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier  = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun FeaturesList() {
    val features = listOf(
        Pair("Безлимитные консультации ИИ",    "Сколько угодно вопросов каждый день"),
        Pair("Генерация документов PDF",        "Жалобы, заявления, претензии"),
        Pair("История всех чатов",              "Сохраняется на устройстве бессрочно"),
        Pair("Голосовой ввод вопросов",         "Говорите — ИИ слушает"),
        Pair("Приоритетная скорость ответа",    "Быстрее в 3× в часы пик")
    )

    Column {
        features.forEachIndexed { i, (title, sub) ->
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier         = Modifier.size(22.dp).clip(CircleShape).background(GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = GreenColor, modifier = Modifier.size(13.dp))
                }
                Column {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text(sub, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (i < features.size - 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun PlansRow(selected: PlanType, onSelect: (PlanType) -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PlanType.entries.forEach { plan ->
            PlanCard(
                plan     = plan,
                badge    = if (plan == PlanType.YEARLY) "Выгодно" else null,
                selected = selected == plan,
                modifier = Modifier.weight(1f),
                onClick  = { onSelect(plan) }
            )
        }
    }
}

@Composable
private fun PlanCard(
    plan: PlanType,
    badge: String?,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (selected) BrandColor else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 2.dp else 1.5.dp

    Box(modifier = modifier) {
        Surface(
            shape    = RoundedCornerShape(14.dp),
            color    = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier            = Modifier
                    .padding(horizontal = 6.dp, vertical = 14.dp)
                    .padding(top = if (badge != null) 6.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(plan.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "${plan.priceRub} ₽",
                    fontSize   = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (selected) BrandColor else MaterialTheme.colorScheme.onSurface
                )
                Text(plan.perMonthHint, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        badge?.let {
            Surface(
                shape    = RoundedCornerShape(20.dp),
                color    = BrandColor,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-9).dp)
            ) {
                Text(
                    text     = it,
                    color    = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentBadges() {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        listOf(
            Triple("СБП",   GosColors.Green, GosColors.GreenLight),
            Triple("ЮKassa", GosColors.BlueDark, BrandLight),
            Triple("Карта",  MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
        ).forEach { (label, textColor, bgColor) ->
            Surface(
                shape    = RoundedCornerShape(8.dp),
                color    = bgColor,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text       = label,
                    color      = textColor,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FreeNote() {
    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = GoldLight,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GosColors.AmberLight, RoundedCornerShape(10.dp))
    ) {
        Text(
            text      = "🎁 Бесплатно: 3 вопроса каждый день\nБез подписки — без скрытых списаний",
            fontSize  = 12.sp,
            color     = GoldColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier  = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun Disclaimer() {
    Text(
        text      = "Подписка продлевается автоматически.\nОтменить можно в любой момент в профиле.",
        fontSize  = 11.sp,
        color     = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 17.sp,
        modifier  = Modifier.fillMaxWidth()
    )
}
