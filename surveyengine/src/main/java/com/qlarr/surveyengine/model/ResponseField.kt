package com.qlarr.surveyengine.model


data class ResponseField(
    val componentCode: String,
    val columnName: ColumnName,
    val returnType: ReturnType
) {
    @Suppress("unused")
    fun toValueKey() = "$componentCode.${columnName.name.lowercase()}"
}

enum class ColumnName {
    VALUE,
    ORDER,
    PRIORITY;
}