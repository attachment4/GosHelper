package com.gospomoshnik.ui.library

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** Один документ/инструкция. id = имя файла в assets/docs/<id>.md */
data class LibraryDoc(
    val id: String,
    val title: String,
    val subtitle: String,
    val subcategory: String
)

data class LibraryCategory(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val docs: List<LibraryDoc>
) {
    /** Документы, сгруппированные по подкатегории (для аккуратной структуры). */
    val bySubcategory: Map<String, List<LibraryDoc>> get() = docs.groupBy { it.subcategory }
}

/** Курируемый каталог. Расширяется добавлением md в assets/docs и строки сюда. */
val docsCatalog = listOf(
    LibraryCategory("gibdd", "ГИБДД и автоправо", Icons.Default.DirectionsCar, listOf(
        LibraryDoc("gibdd_obzhalovanie", "Обжалование штрафа", "Пошагово, сроки, основания", "Штрафы")
    )),
    LibraryCategory("zhkh", "ЖКХ", Icons.Default.Home, listOf(
        LibraryDoc("zhkh_pereraschet", "Перерасчёт за услуги", "Некачественные/отсутствующие услуги", "Начисления")
    )),
    LibraryCategory("labor", "Трудовые права", Icons.Default.Work, listOf(
        LibraryDoc("labor_zarplata", "Невыплата зарплаты", "Что делать, сроки, компенсация", "Зарплата")
    )),
    LibraryCategory("benefits", "Льготы и пособия", Icons.Default.Favorite, listOf(
        LibraryDoc("benefits_edinoe", "Единое пособие на детей", "Как оформить через Госуслуги", "Семья")
    )),
    LibraryCategory("court", "Суд", Icons.Default.Balance, listOf(
        LibraryDoc("court_isk", "Как подать иск", "Подсудность, госпошлина, подача", "Иски")
    )),
    LibraryCategory("documents", "Документы", Icons.Default.Description, listOf(
        LibraryDoc("docs_pretenziya", "Претензия потребителя", "Структура и сроки", "Претензии")
    ))
)

fun findDoc(id: String): LibraryDoc? =
    docsCatalog.flatMap { it.docs }.firstOrNull { it.id == id }
