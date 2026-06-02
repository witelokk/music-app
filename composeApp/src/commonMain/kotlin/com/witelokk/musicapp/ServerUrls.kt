package com.witelokk.musicapp

private const val HEALTH_PATH_SEGMENT = "/health"

internal fun normalizeServerUrl(value: String): String {
    if (value.isBlank()) return DEFAULT_BASE_URL

    val withoutQuery = value.trim()
        .substringBefore('?')
        .substringBefore('#')
    val collapsed = withoutQuery.collapsePathSlashes().trimEnd('/')
    val healthPathIndex = collapsed.indexOfHealthPathSegment()

    return if (healthPathIndex >= 0) {
        collapsed.substring(0, healthPathIndex).trimEnd('/')
    } else {
        collapsed
    }.ifBlank { DEFAULT_BASE_URL }
}

internal fun serverHealthUrl(serverUrl: String): String =
    "${normalizeServerUrl(serverUrl).trimEnd('/')}$HEALTH_PATH_SEGMENT"

private fun String.collapsePathSlashes(): String {
    val schemeSeparator = "://"
    val schemeIndex = indexOf(schemeSeparator)
    if (schemeIndex < 0) return replace(Regex("/{2,}"), "/")

    val authorityStart = schemeIndex + schemeSeparator.length
    val pathStart = indexOf('/', startIndex = authorityStart)
    if (pathStart < 0) return this

    return substring(0, pathStart) + substring(pathStart).replace(Regex("/{2,}"), "/")
}

private fun String.indexOfHealthPathSegment(): Int {
    val schemeSeparator = "://"
    val schemeIndex = indexOf(schemeSeparator)
    val pathStart = if (schemeIndex >= 0) {
        val authorityStart = schemeIndex + schemeSeparator.length
        indexOf('/', startIndex = authorityStart)
    } else {
        indexOf('/')
    }

    if (pathStart < 0) return -1
    return indexOf(HEALTH_PATH_SEGMENT, startIndex = pathStart)
}
