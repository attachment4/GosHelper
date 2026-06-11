package com.gospomoshnik.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gospomoshnik.domain.model.ChatSession
import com.gospomoshnik.domain.model.FREE_DAILY_LIMIT
import com.gospomoshnik.ui.theme.GosColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class Category(
    val key: String,
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val tintBg: Color
)

private val categories = listOf(
    Category("gibdd",     "ГИБДД",     "Штрафы, лишение прав",  Icons.Default.DirectionsCar, GosColors.Blue,  GosColors.BlueLight),
    Category("zhkh",      "ЖКХ",       "УК, квитанции, нормы",  Icons.Default.Home,          GosColors.Green, GosColors.GreenLight),
    Category("labor",     "Трудовые",  "Права, увольнение",     Icons.Default.Work,          GosColors.Amber, GosColors.AmberLight),
    Category("benefits",  "Льготы",    "Субсидии, пособия",     Icons.Default.Favorite,      GosColors.Red,   GosColors.RedLight),
    Category("court",     "Суд",       "Иски, обжалование",     Icons.Default.Balance,       GosColors.Blue,  GosColors.BlueLight),
    Category("documents", "Документы", "Шаблоны заявлений",     Icons.Default.Description,   GosColors.Green, GosColors.GreenLight)
)

@Composable
fun MainScreen(
    onCategoryClick: (String) -> Unit,
    onHistoryItemClick: (String, Long) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(onProfileClick = onProfileClick)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { HeaderSection(requestsLeft = uiState.requestsLeft, isPro = uiState.isPro) }
            item { Spacer(Modifier.height(20.dp)) }
            item {
                SectionTitle("Категории", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(10.dp))
            }
            item { CategoryGrid(onCategoryClick = onCategoryClick) }
            if (uiState.recentSessions.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    SectionTitle("Недавние вопросы", modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(10.dp))
                }
                items(uiState.recentSessions.take(5), key = { it.id }) { session ->
                    RecentSessionItem(
                        session = session,
                        onClick = { onHistoryItemClick(session.category, session.id) }
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Шапка в стилистике Госуслуг: белая, синий логотип ────────────────────────

@Composable
private fun HeaderSection(requestsLeft: Int, isPro: Boolean) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text          = "госпомощник",
                    color         = GosColors.Blue,
                    fontWeight    = FontWeight.Bold,
                    fontSize      = 18.sp,
                    letterSpacing = (-0.3).sp
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GosColors.BlueLight
                ) {
                    Text(
                        text       = if (isPro) "PRO · безлимит" else "$requestsLeft / $FREE_DAILY_LIMIT сегодня",
                        color      = GosColors.Blue,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text       = "Чем могу помочь?",
                color      = MaterialTheme.colorScheme.onSurface,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text     = "ИИ-консультант по законодательству РФ",
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text       = text,
        fontSize   = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.onSurface,
        modifier   = modifier
    )
}

// ── Категории: белые карточки с цветными иконками (как сервисы Госуслуг) ────

@Composable
private fun CategoryGrid(onCategoryClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        categories.chunked(2).forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { cat ->
                    CategoryCard(
                        category = cat,
                        modifier = Modifier.weight(1f),
                        onClick  = { onCategoryClick(cat.key) }
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier        = modifier.clickable(onClick = onClick),
        shape           = RoundedCornerShape(14.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(category.tintBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = category.icon,
                    contentDescription = null,
                    tint               = category.tint,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text       = category.label,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text     = category.subtitle,
                fontSize = 11.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Недавние вопросы ─────────────────────────────────────────────────────────

@Composable
private fun RecentSessionItem(session: ChatSession, onClick: () -> Unit) {
    Surface(
        shape           = RoundedCornerShape(12.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GosColors.BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint     = GosColors.Blue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = session.title,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 13.sp,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 1
                )
                Text(
                    text     = formatDate(session.updatedAt),
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val time  = SimpleDateFormat("HH:mm", Locale("ru")).format(Date(timestamp))
    val dayMs = 24 * 60 * 60 * 1000L
    val now   = System.currentTimeMillis()
    return when {
        now - timestamp < dayMs     -> "Сегодня, $time"
        now - timestamp < 2 * dayMs -> "Вчера, $time"
        else -> SimpleDateFormat("d MMMM", Locale("ru")).format(Date(timestamp))
    }
}

// ── Нижняя навигация: только рабочие разделы ─────────────────────────────────

@Composable
private fun BottomNavigationBar(onProfileClick: () -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = true,
            onClick  = {},
            icon     = { Icon(Icons.Default.Home, contentDescription = "Главная") },
            label    = { Text("Главная", fontSize = 11.sp) },
            colors   = NavigationBarItemDefaults.colors(
                selectedIconColor = GosColors.Blue,
                selectedTextColor = GosColors.Blue,
                indicatorColor    = GosColors.BlueLight
            )
        )
        NavigationBarItem(
            selected = false,
            onClick  = onProfileClick,
            icon     = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
            label    = { Text("Профиль", fontSize = 11.sp) },
            colors   = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
