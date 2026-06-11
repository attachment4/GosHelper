package com.gospomoshnik.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.gospomoshnik.ui.theme.GosColors
import kotlinx.coroutines.launch

private data class Page(val emoji: String, val title: String, val text: String)

private val pages = listOf(
    Page("⚖️", "Юрист в кармане",
        "ИИ-консультант по законам РФ. Объяснит простыми словами, со ссылками на статьи и официальные источники."),
    Page("📄", "Документы и инструкции",
        "48 готовых разборов и шаблонов: ГИБДД, ЖКХ, трудовые, льготы, суд. Сразу можно задать вопрос ИИ по теме."),
    Page("🎁", "3 вопроса в день — бесплатно",
        "Безлимит — в подписке Pro. Данные хранятся только на вашем устройстве, без сбора персональной информации.")
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GosColors.Blue, GosColors.BlueDark)))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Пропустить
        TextButton(
            onClick = onFinish,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Text("Пропустить", color = Color.White.copy(alpha = 0.85f))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { i ->
                val page = pages[i]
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(page.emoji, fontSize = 56.sp)
                    }
                    Spacer(Modifier.height(36.dp))
                    Text(
                        text       = page.title,
                        color      = Color.White,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text       = page.text,
                        color      = Color.White.copy(alpha = 0.85f),
                        fontSize   = 16.sp,
                        lineHeight = 24.sp,
                        textAlign  = TextAlign.Center
                    )
                }
            }

            // Индикаторы страниц
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { i ->
                    val active = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = if (active) 22.dp else 8.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (active) 1f else 0.4f))
                    )
                }
            }

            Button(
                onClick = {
                    if (isLast) onFinish()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp)
                    .height(54.dp),
                shape  = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = GosColors.Blue
                )
            ) {
                Text(
                    text = if (isLast) "Начать" else "Далее",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
