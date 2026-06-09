package com.gospomoshnik.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val BrandColor = Color(0xFF4338CA)
private val BrandLight = Color(0xFFEEF2FF)
private val BrandMid   = Color(0xFFC7D2FE)
private val GreenColor = Color(0xFF059669)
private val GreenLight = Color(0xFFD1FAE5)

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
                    .background(Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF3730A3))))
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = uiState.title,
                                color      = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp
                            )
                            Text(
                                text     = uiState.subtitle,
                                color    = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
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

            // Кнопки
            Button(
                onClick  = viewModel::exportPdf,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = BrandColor)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Скачать PDF", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick  = { /* TODO: share */ },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Поделиться")
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
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(Modifier.height(8.dp))
            Text("📌 Тип: Юридическая жалоба", fontSize = 11.sp, color = Color(0xFF6B7280))
            Text("⚖️ Основание: КоАП РФ / ГК РФ", fontSize = 11.sp, color = Color(0xFF6B7280))
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
            color         = Color(0xFF6B7280),
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
                color = Color(0xFFF9FAFB),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text     = field.value.ifBlank { "—" },
                    fontSize = 13.sp,
                    color    = Color(0xFF111827),
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
            color         = Color(0xFF6B7280),
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
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF9CA3AF))
        Spacer(Modifier.width(4.dp))
        Text("Данные хранятся только на вашем устройстве", fontSize = 11.sp, color = Color(0xFF9CA3AF))
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
