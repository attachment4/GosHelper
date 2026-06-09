package com.gospomoshnik.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gospomoshnik.domain.model.ChatSession

@Composable
fun MainScreen(
    onCategoryClick: (String) -> Unit,
    onHistoryItemClick: (Long) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selected = 0, onProfileClick = onProfileClick)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { HeaderSection(requestsLeft = uiState.requestsLeft) }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                SectionTitle("Категории", modifier = Modifier.padding(horizontal = 16.dp))
            }
            item { CategoryGrid(onCategoryClick = onCategoryClick) }
            if (uiState.recentSessions.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    SectionTitle("Недавние вопросы", modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                }
                items(uiState.recentSessions.take(5)) { session ->
                    RecentSessionItem(
                        session = session,
                        onClick = { onHistoryItemClick(session.id) }
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Вспомогательные composable ────────────────────────────────────────────

@Composable
private fun HeaderSection(requestsLeft: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Text(
                    text       = "ГосПомощник",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text     = "$requestsLeft / 10 запросов",
                        color    = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text       = "Чем могу помочь?",
                color      = Color.White,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text     = "ИИ-консультант по законодательству РФ",
                color    = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text       = text.uppercase(),
        fontSize   = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier   = modifier
    )
}

@Composable
private fun CategoryGrid(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        Triple("gibdd",     "ГИБДД",     "Штрафы, лишение прав"),
        Triple("zhkh",      "ЖКХ",       "УК, квитанции, нормы"),
        Triple("labor",     "Трудовые",  "Права, увольнение"),
        Triple("benefits",  "Льготы",    "Субсидии, пособия"),
        Triple("court",     "Суд",       "Иски, обжалование"),
        Triple("documents", "Документы", "Шаблоны заявлений")
    )
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        categories.chunked(2).forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (key, label, sub) ->
                    CategoryCard(
                        label    = label,
                        subtitle = sub,
                        modifier = Modifier.weight(1f),
                        onClick  = { onCategoryClick(key) }
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
    label: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier  = modifier.clickable(onClick = onClick),
        shape     = RoundedCornerShape(14.dp),
        color     = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecentSessionItem(session: ChatSession, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(session.title, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1)
        },
        supportingContent = {
            Text(session.category, fontSize = 11.sp)
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun BottomNavigationBar(selected: Int, onProfileClick: () -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == 0,
            onClick  = {},
            icon     = { Icon(Icons.Default.Home, contentDescription = "Главная") },
            label    = { Text("Главная") }
        )
        NavigationBarItem(
            selected = selected == 1,
            onClick  = {},
            icon     = { Icon(Icons.Default.Chat, contentDescription = "Чаты") },
            label    = { Text("Чаты") }
        )
        NavigationBarItem(
            selected = selected == 2,
            onClick  = {},
            icon     = { Icon(Icons.Default.Description, contentDescription = "Документы") },
            label    = { Text("Документы") }
        )
        NavigationBarItem(
            selected = selected == 3,
            onClick  = onProfileClick,
            icon     = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
            label    = { Text("Профиль") }
        )
    }
}
