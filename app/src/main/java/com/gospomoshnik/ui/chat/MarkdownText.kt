package com.gospomoshnik.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.gospomoshnik.ui.theme.GosColors

/**
 * Лёгкий рендер Markdown без внешних библиотек.
 * Поддерживает: заголовки (#/##/###), списки (-, •, *), нумерованные пункты,
 * **жирный**, ссылки [текст](url) и «голые» http-ссылки (кликабельные).
 */
@Composable
fun MarkdownText(
    markdown: String,
    color: Color,
    modifier: Modifier = Modifier,
    baseSize: Int = 13
) {
    val blocks = remember(markdown) { parseBlocks(markdown) }
    Column(modifier = modifier) {
        blocks.forEachIndexed { i, block ->
            when (block) {
                is Block.Heading -> {
                    if (i > 0) Spacer(Modifier.height(6.dp))
                    Text(
                        text       = inline(block.text, color),
                        color      = color,
                        fontSize   = (baseSize + (4 - block.level).coerceAtLeast(1)).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (baseSize + 8).sp
                    )
                }
                is Block.Bullet -> Row(modifier = Modifier.padding(vertical = 1.dp)) {
                    Text("•  ", color = color, fontSize = baseSize.sp)
                    Text(
                        text       = inline(block.text, color),
                        color      = color,
                        fontSize   = baseSize.sp,
                        lineHeight = (baseSize + 7).sp
                    )
                }
                is Block.Numbered -> Row(modifier = Modifier.padding(vertical = 1.dp)) {
                    Text("${block.number}.  ", color = color, fontSize = baseSize.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text       = inline(block.text, color),
                        color      = color,
                        fontSize   = baseSize.sp,
                        lineHeight = (baseSize + 7).sp
                    )
                }
                is Block.Paragraph -> {
                    if (i > 0) Spacer(Modifier.height(4.dp))
                    Text(
                        text       = inline(block.text, color),
                        color      = color,
                        fontSize   = baseSize.sp,
                        lineHeight = (baseSize + 7).sp
                    )
                }
            }
        }
    }
}

private sealed interface Block {
    data class Heading(val text: String, val level: Int) : Block
    data class Bullet(val text: String) : Block
    data class Numbered(val number: Int, val text: String) : Block
    data class Paragraph(val text: String) : Block
}

private val numberedRegex = Regex("""^(\d+)[.)]\s+(.*)""")

private fun parseBlocks(md: String): List<Block> {
    val result = mutableListOf<Block>()
    val paragraph = StringBuilder()

    fun flush() {
        if (paragraph.isNotBlank()) result += Block.Paragraph(paragraph.trim().toString())
        paragraph.clear()
    }

    md.lines().forEach { raw ->
        val line = raw.trimEnd()
        when {
            line.isBlank() -> flush()
            line.startsWith("### ") -> { flush(); result += Block.Heading(line.removePrefix("### "), 3) }
            line.startsWith("## ")  -> { flush(); result += Block.Heading(line.removePrefix("## "), 2) }
            line.startsWith("# ")   -> { flush(); result += Block.Heading(line.removePrefix("# "), 1) }
            line.startsWith("- ") || line.startsWith("• ") || line.startsWith("* ") -> {
                flush(); result += Block.Bullet(line.drop(2).trim())
            }
            numberedRegex.matches(line) -> {
                flush()
                val m = numberedRegex.find(line)!!
                result += Block.Numbered(m.groupValues[1].toInt(), m.groupValues[2].trim())
            }
            else -> { if (paragraph.isNotEmpty()) paragraph.append(' '); paragraph.append(line.trim()) }
        }
    }
    flush()
    return result
}

private val urlRegex  = Regex("""https?://[^\s)<>"]+""")
private val linkRegex = Regex("""\[([^\]]+)]\((https?://[^)\s]+)\)""")

/** Инлайн-разметка: **жирный**, [текст](url), голые ссылки. */
private fun inline(text: String, color: Color): AnnotatedString = buildAnnotatedString {
    val linkStyle = TextLinkStyles(
        style = SpanStyle(color = GosColors.Blue, fontWeight = FontWeight.Medium)
    )

    // Сначала разбиваем по markdown-ссылкам, остальное обрабатываем как жирный/голые ссылки
    var cursor = 0
    val links = linkRegex.findAll(text).toList()
    for (match in links) {
        if (match.range.first > cursor) {
            appendInline(text.substring(cursor, match.range.first), color)
        }
        withLink(LinkAnnotation.Url(match.groupValues[2], styles = linkStyle)) {
            append(match.groupValues[1])
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) appendInline(text.substring(cursor), color)
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendInline(text: String, color: Color) {
    val linkStyle = TextLinkStyles(
        style = SpanStyle(color = GosColors.Blue, fontWeight = FontWeight.Medium)
    )
    var i = 0
    while (i < text.length) {
        // Жирный **...**
        if (text.startsWith("**", i)) {
            val end = text.indexOf("**", i + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text.substring(i + 2, end))
                }
                i = end + 2
                continue
            }
        }
        // Голая ссылка
        val urlMatch = urlRegex.find(text, i)
        if (urlMatch != null && urlMatch.range.first == i) {
            withLink(LinkAnnotation.Url(urlMatch.value, styles = linkStyle)) {
                append(urlMatch.value)
            }
            i = urlMatch.range.last + 1
            continue
        }
        // Обычный символ — добавляем до следующего спец-маркера
        val nextStar = text.indexOf("**", i).let { if (it == -1) Int.MAX_VALUE else it }
        val nextUrl  = urlRegex.find(text, i)?.range?.first ?: Int.MAX_VALUE
        val next     = minOf(nextStar, nextUrl, text.length)
        append(text.substring(i, next))
        i = next
    }
}
