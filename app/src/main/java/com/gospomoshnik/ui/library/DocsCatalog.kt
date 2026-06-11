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
    val subcategory: String,
    /** Готовый вопрос для перехода в чат по этой теме. */
    val question: String
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
        LibraryDoc("gibdd_obzhalovanie", "Обжалование штрафа", "Сроки, основания, куда подавать", "Штрафы",
            "Как обжаловать штраф ГИБДД? Распишите сроки, основания и порядок подачи."),
        LibraryDoc("gibdd_camera", "Штраф с камеры", "Когда можно отменить", "Штрафы",
            "Как обжаловать штраф с камеры, если за рулём был не я?"),
        LibraryDoc("gibdd_dtp", "Действия при ДТП", "Европротокол, сроки, ОСАГО", "ДТП",
            "Что делать при ДТП и когда можно оформить европротокол без ГИБДД?"),
        LibraryDoc("gibdd_lishenie", "Лишение прав", "Порядок и возврат ВУ", "Права",
            "Как вернуть водительское удостоверение после лишения?")
    )),
    LibraryCategory("zhkh", "ЖКХ", Icons.Default.Home, listOf(
        LibraryDoc("zhkh_pereraschet", "Перерасчёт за услуги", "Некачественные/отсутствующие услуги", "Начисления",
            "Как сделать перерасчёт за некачественные коммунальные услуги?"),
        LibraryDoc("zhkh_odn", "Оспаривание начислений и ОДН", "Где переплата и как вернуть", "Начисления",
            "Как оспорить завышенные начисления за ЖКХ и ОДН?"),
        LibraryDoc("zhkh_subsidiya", "Субсидия на ЖКУ", "Если платёж больше доли дохода", "Льготы",
            "Как оформить субсидию на оплату ЖКУ и какой порог расходов?"),
        LibraryDoc("zhkh_uk_zhaloba", "Жалоба на УК", "Порядок: УК → ГЖИ → суд", "Жалобы",
            "Как пожаловаться на управляющую компанию и заставить её работать?")
    )),
    LibraryCategory("labor", "Трудовые права", Icons.Default.Work, listOf(
        LibraryDoc("labor_zarplata", "Невыплата зарплаты", "Что делать, сроки, компенсация", "Зарплата",
            "Работодатель не платит зарплату — что делать и какая компенсация положена?"),
        LibraryDoc("labor_uvolnenie", "Незаконное увольнение", "Как оспорить за 1 месяц", "Увольнение",
            "Меня незаконно уволили — как оспорить и что можно взыскать?"),
        LibraryDoc("labor_sokrashchenie", "Сокращение и выплаты", "Какие выплаты положены", "Увольнение",
            "Меня сокращают — какие выплаты и права у меня есть?"),
        LibraryDoc("labor_otpusk", "Отпуск и компенсация", "Сроки, замена деньгами", "Отпуск",
            "Как рассчитывается отпуск и компенсация за неиспользованный отпуск?")
    )),
    LibraryCategory("benefits", "Льготы и пособия", Icons.Default.Favorite, listOf(
        LibraryDoc("benefits_edinoe", "Единое пособие на детей", "Как оформить через Госуслуги", "Семья",
            "Как оформить единое пособие на ребёнка и какие условия нуждаемости?"),
        LibraryDoc("benefits_matkapital", "Материнский капитал", "На что и как потратить", "Семья",
            "На что можно потратить материнский капитал и как им распорядиться?"),
        LibraryDoc("benefits_invalidy", "Льготы инвалидам", "ЕДВ, НСУ, ЖКУ", "Льготы",
            "Какие льготы и выплаты положены инвалиду и как их оформить?"),
        LibraryDoc("benefits_zhku", "Льготы по ЖКУ", "Компенсации и субсидии", "Льготы",
            "Какие компенсации и льготы по оплате ЖКУ мне положены?")
    )),
    LibraryCategory("court", "Суд", Icons.Default.Balance, listOf(
        LibraryDoc("court_isk", "Как подать иск", "Подсудность, госпошлина, подача", "Иски",
            "Как правильно подать исковое заявление в суд?"),
        LibraryDoc("court_prikaz", "Судебный приказ", "Быстро и дешевле иска", "Иски",
            "Чем судебный приказ отличается от иска и когда он выгоднее?"),
        LibraryDoc("court_poshlina", "Госпошлина и льготы", "Когда можно не платить", "Госпошлина",
            "Сколько стоит госпошлина в суд и когда её можно не платить?"),
        LibraryDoc("court_apellyaciya", "Апелляция", "Обжалование решения суда", "Обжалование",
            "Как подать апелляционную жалобу на решение суда и в какой срок?")
    )),
    LibraryCategory("documents", "Документы", Icons.Default.Description, listOf(
        LibraryDoc("docs_pretenziya", "Претензия потребителя", "Структура и сроки", "Претензии",
            "Как составить претензию по защите прав потребителей?"),
        LibraryDoc("docs_dosudebnaya", "Досудебная претензия", "Когда обязательна", "Претензии",
            "Как написать досудебную претензию перед обращением в суд?"),
        LibraryDoc("docs_zhaloba", "Жалоба в госорган", "По ФЗ-59, сроки 30 дней", "Обращения",
            "Как написать жалобу в государственный орган?"),
        LibraryDoc("docs_zayavlenie", "Заявление в ведомство", "Структура и подача", "Обращения",
            "Как правильно составить и подать заявление в ведомство?")
    ))
)

fun findDoc(id: String): LibraryDoc? =
    docsCatalog.flatMap { it.docs }.firstOrNull { it.id == id }
