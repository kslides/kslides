package com.github.pambrose

import kotlinx.html.*
import java.io.*
import java.net.*
import java.util.*

fun slideBackground(color: String) = "<!-- .slide: data-background=\"$color\" -->"

fun fragmentIndex(index: Int) =
    "<!-- .element: class=\"fragment\" data-fragment-index=\"$index\" -->"

fun includeFile(
    path: String,
    beginToken: String = "",
    endToken: String = "",
    commentPrefix: String = "//"
): String {
    val text = File("${System.getProperty("user.dir")}/$path").readLines()
    return processCode(
        text,
        beginToken,
        endToken,
        commentPrefix
    )
}

fun includeUrl(
    source: String,
    beginToken: String = "",
    endToken: String = "",
    commentPrefix: String = "//"
): String {
    val text = URL(source).readText().split("\n")
    return processCode(
        text,
        beginToken,
        endToken,
        commentPrefix
    )
}

private fun processCode(
    lines: List<String>,
    beginToken: String,
    endToken: String,
    commentPrefix: String
): String {
    val startIndex =
        if (beginToken.isEmpty())
            0
        else
            (lines
                .asSequence()
                .mapIndexed { i, s -> i to s }
                .firstOrNull { it.second.contains(Regex("$commentPrefix\\s*$beginToken")) }?.first
                ?: throw IllegalArgumentException("beginToken not found: $beginToken")) + 1


    val endIndex =
        if (endToken.isEmpty())
            lines.size
        else
            (lines.reversed()
                .asSequence()
                .mapIndexed { i, s -> (lines.size - i - 1) to s }
                .firstOrNull { it.second.contains(Regex("$commentPrefix\\s*$endToken")) }?.first
                ?: throw IllegalArgumentException("endToken not found: $endToken"))

    return lines.subList(startIndex, endIndex).joinToString("\n")

}

// Keep this global to make it easier for users to be prompted for completion in it
@HtmlTagMarker
fun presentation(path: String = "/", title: String = "", theme: Theme = Theme.Black, block: Presentation.() -> Unit) =
    Presentation(path, title, "dist/theme/${theme.name.toLower()}.css").apply { block(this) }

fun String.toLower(locale: Locale = Locale.getDefault()) = lowercase(locale)