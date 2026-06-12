package com.gospomoshnik.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gospomoshnik.ui.theme.GosColors

private val BrandColor = GosColors.Blue
private val BrandLight = GosColors.BlueLight
private val BrandMid   = GosColors.BlueMid
private val GreenColor = GosColors.Green
private val GreenLight = GosColors.GreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    onBack: () -> Unit,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(GosColors.Blue, GosColors.BlueDark)))
                    .statusBarsPadding()
                    .padding(start = 4.dp, end = 16.dp, top = 14.dp, bottom = 18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = uiState.title,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 17.sp,
                            maxLines   = 1
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text     = uiState.subtitle,
                            color    = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandColor)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ИИ-баннер
            AiBanner()

            // Превью карточка
            DocPreviewCard(title = uiState.title)

            // Поля
            uiState.fields.forEachIndexed { i, field ->
                DocumentFieldRow(
                    field    = field,
                    onChange = { viewModel.updateField(i, it) }
                )
            }

            // Текст жалобы
            BodyTextEditor(
                text     = uiState.bodyText,
                onChange = viewModel::updateBody
            )

            HorizontalDivider()

            // Кнопки — собираем текст документа и делимся / копируем
            val context = LocalContext.current
            val clipboard = LocalClipboardManager.current
            val docText = remember(uiState) { buildDocText(uiState) }

            Button(
                onClick  = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, uiState.title)
                        putExtra(android.content.Intent.EXTRA_TEXT, docText)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Поделиться документом"))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = BrandColor)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Поделиться документом", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick  = {
                    clipboard.setText(AnnotatedString(docText))
                    android.widget.Toast.makeText(context, "Скопировано", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Копировать текст")
            }

            PrivacyNote()

            // Статус сохранения
            uiState.pdfPath?.let {
                SavedBanner()
            }

            uiState.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        }
    }
}

@Composable
private fun AiBanner() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BrandLight,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BrandMid, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("✨", fontSize = 18.sp)
            Text(
                text       = "ИИ заполнил шаблон на основе вашего диалога. Проверьте данные и при необходимости скорректируйте.",
                fontSize   = 12.sp,
                color      = BrandColor,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun DocPreviewCard(title: String) {
    Surface(
        shape           = RoundedCornerShape(14.dp),
        shadowElevation = 1.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = BrandColor, modifier = Modifier.size(20.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))
            Text("📌 Тип: Юридическая жалоба", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("⚖️ Основание: КоАП РФ / ГК РФ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DocumentFieldRow(field: DocumentField, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text          = field.label.uppercase(),
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        if (field.editable) {
            OutlinedTextField(
                value         = field.value,
                onValueChange = onChange,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(8.dp),
                singleLine    = true,
                textStyle     = LocalTextStyle.current.copy(fontSize = 13.sp, color = BrandColor),
                trailingIcon  = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = BrandColor,
                    unfocusedBorderColor = BrandMid,
                    focusedContainerColor   = BrandLight,
                    unfocusedContainerColor = BrandLight
                )
            )
        } else {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text     = field.value.ifBlank { "—" },
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun BodyTextEditor(text: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text          = "СУТЬ ЖАЛОБЫ",
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        OutlinedTextField(
            value         = text,
            onValueChange = onChange,
            modifier      = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            shape         = RoundedCornerShape(8.dp),
            textStyle     = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = 18.sp)
        )
    }
}

@Composable
private fun PrivacyNote() {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text("Данные хранятся только на вашем устройстве", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SavedBanner() {
    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = GreenLight,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GreenColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("✅", fontSize = 16.sp)
            Text("Документ сохранён на устройстве", fontSize = 12.sp, color = GreenColor, fontWeight = FontWeight.Medium)
        }
    }
}

private fun buildDocText(state: DocumentUiState): String = buildString {
    appendLine(state.title.uppercase())
    if (state.subtitle.isNotBlank()) appendLine(state.subtitle)
    appendLine()
    state.fields.forEach { f -> if (f.value.isNotBlank()) appendLine("${f.label}: ${f.value}") }
    appendLine()
    appendLine(state.bodyText)
}
