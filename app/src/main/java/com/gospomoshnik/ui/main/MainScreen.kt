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
import androidx.compose.ui.graphics.Brush
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
import java.util.Calendar
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
    Category("gibdd",     "ГИБДД",     "Штрафы, права",      Icons.Default.DirectionsCar, GosColors.Blue,  GosColors.BlueLight),
    Category("zhkh",      "ЖКХ",       "УК, квитанции",      Icons.Default.Home,          GosColors.Green, GosColors.GreenLight),
    Category("labor",     "Трудовые",  "Права, увольнение",  Icons.Default.Work,          GosColors.Amber, GosColors.AmberLight),
    Category("benefits",  "Льготы",    "Субсидии, пособия",  Icons.Default.Favorite,      GosColors.Red,   GosColors.RedLight),
    Category("court",     "Суд",       "Иски, обжалование",  Icons.Default.Balance,       GosColors.Blue,  GosColors.BlueLight),
    Category("documents", "Документы", "Шаблоны заявлений",  Icons.Default.Description,   GosColors.Green, GosColors.GreenLight)
)

@Composable
fun MainScreen(
    onCategoryClick: (String) -> Unit,
    onHistoryItemClick: (String, Long) -> Unit,
    onProfileClick: () -> Unit,
    onLibraryClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showClear by remember { mutableStateOf(false) }
    if (showClear) {
        AlertDialog(
            onDismissRequest = { showClear = false },
            title   = { Text("Очистить все чаты?") },
            text    = { Text("Вся история диалогов будет удалена с устройства без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { showClear = false; viewModel.clearAllHistory() }) {
                    Text("Очистить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showClear = false }) { Text("Отмена") } }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(onProfileClick = onProfileClick, onLibraryClick = onLibraryClick)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item { GreetingBar(onProfileClick = onProfileClick) }
            item { Spacer(Modifier.height(12.dp)) }
            item { SearchPill(onClick = { onCategoryClick("general") }) }
            item { Spacer(Modifier.height(14.dp)) }
            item { StatusHeroCard(requestsLeft = uiState.requestsLeft, isPro = uiState.isPro, onClick = onProfileClick) }
            item { Spacer(Modifier.height(22.dp)) }
            item { SectionHeader("Категории") }
            item { Spacer(Modifier.height(12.dp)) }
            item { CategoryGrid(onCategoryClick = onCategoryClick) }
            if (uiState.recentSessions.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(22.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Недавние вопросы",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showClear = true }) {
                            Text("Очистить", fontSize = 13.sp, color = GosColors.Blue)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                items(uiState.recentSessions.take(5), key = { it.id }) { session ->
                    RecentSessionItem(
                        session = session,
                        onClick = { onHistoryItemClick(session.category, session.id) }
                    )
                }
            }
        }
    }
}

// ── Приветствие + аватар (как верх Сбера) ────────────────────────────────────

@Composable
private fun GreetingBar(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 16.dp, top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = greetingByTime(),
                fontSize = 14.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text       = "ГосПомощник",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(GosColors.BlueLight)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Профиль", tint = GosColors.Blue)
        }
    }
}

// ── Поисковая «пилюля» (открывает чат) ───────────────────────────────────────

@Composable
private fun SearchPill(onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(28.dp),
        color    = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = GosColors.Blue)
            Text(
                "Задайте вопрос юристу-ИИ…",
                fontSize = 15.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Карта-герой со статусом (как баланс-карта Сбера) ─────────────────────────

@Composable
private fun StatusHeroCard(requestsLeft: Int, isPro: Boolean, onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(24.dp),
        color    = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(GosColors.Blue, GosColors.BlueDark)),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text  = if (isPro) "Подписка Pro" else "Бесплатный доступ",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text       = if (isPro) "Безлимитные вопросы" else "Осталось $requestsLeft из $FREE_DAILY_LIMIT на сегодня",
                    color      = Color.White,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.18f)) {
                        Text(
                            text     = if (isPro) "Управлять подпиской" else "Подключить Pro — от 67 ₽/мес",
                            color    = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text       = text,
        fontSize   = 18.sp,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.onSurface,
        modifier   = Modifier.padding(horizontal = 18.dp)
    )
}

// ── Плитки категорий (круглые подложки иконок) ───────────────────────────────

@Composable
private fun CategoryGrid(onCategoryClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        categories.chunked(2).forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
            Spacer(Modifier.height(12.dp))
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
        shape           = RoundedCornerShape(20.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(category.tintBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = category.icon,
                    contentDescription = null,
                    tint               = category.tint,
                    modifier           = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text       = category.label,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text     = category.subtitle,
                fontSize = 12.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Недавние вопросы ─────────────────────────────────────────────────────────

@Composable
private fun RecentSessionItem(session: ChatSession, onClick: () -> Unit) {
    Surface(
        shape           = RoundedCornerShape(18.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GosColors.BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint     = GosColors.Blue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = session.title,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 1
                )
                Text(
                    text     = formatDate(session.updatedAt),
                    fontSize = 12.sp,
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

private fun greetingByTime(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (h) {
        in 5..11  -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else      -> "Доброй ночи"
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

// ── Нижняя навигация ─────────────────────────────────────────────────────────

@Composable
private fun BottomNavigationBar(onProfileClick: () -> Unit, onLibraryClick: () -> Unit) {
    val unselected = NavigationBarItemDefaults.colors(
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
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
            onClick  = onLibraryClick,
            icon     = { Icon(Icons.Default.Description, contentDescription = "Документы") },
            label    = { Text("Документы", fontSize = 11.sp) },
            colors   = unselected
        )
        NavigationBarItem(
            selected = false,
            onClick  = onProfileClick,
            icon     = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
            label    = { Text("Профиль", fontSize = 11.sp) },
            colors   = unselected
        )
    }
}
