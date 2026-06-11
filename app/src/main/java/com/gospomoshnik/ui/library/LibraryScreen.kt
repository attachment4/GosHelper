package com.gospomoshnik.ui.library

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gospomoshnik.ui.theme.GosColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenDoc: (String) -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(docsCatalog.first().key) }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(title = { Text("Документы и инструкции") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(docsCatalog, key = { it.key }) { category ->
                CategoryCard(
                    category   = category,
                    expanded   = expanded == category.key,
                    onToggle   = { expanded = if (expanded == category.key) "" else category.key },
                    onOpenDoc  = onOpenDoc
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: LibraryCategory,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenDoc: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape           = RoundedCornerShape(18.dp),
        color           = cs.surface,
        shadowElevation = 2.dp,
        modifier        = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(GosColors.BlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(category.icon, contentDescription = null, tint = GosColors.Blue)
                }
                Text(
                    category.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = cs.onSurfaceVariant
                )
            }

            if (expanded) {
                category.bySubcategory.forEach { (sub, docs) ->
                    Text(
                        sub.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 2.dp)
                    )
                    docs.forEach { doc ->
                        DocRow(doc = doc, onClick = { onOpenDoc(doc.id) })
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DocRow(doc: LibraryDoc, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GosColors.Blue))
        Column(modifier = Modifier.weight(1f)) {
            Text(doc.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = cs.onSurface)
            Text(doc.subtitle, fontSize = 12.sp, color = cs.onSurfaceVariant)
        }
    }
}
