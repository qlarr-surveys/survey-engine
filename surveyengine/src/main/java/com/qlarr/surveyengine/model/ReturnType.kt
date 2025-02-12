package com.qlarr.surveyengine.model


enum class ReturnType {
    BOOLEAN, STRING, INT, DOUBLE, LIST, MAP, DATE, FILE;


    fun defaultTextValue(): String {
        return when (this) {
            LIST -> "[]"
            STRING -> ""
            BOOLEAN -> "false"
            DATE -> "1970-01-01 00:00:00"
            INT, DOUBLE -> "0"
            MAP -> "{}"
            FILE -> "{\"filename\":\"\",\"stored_filename\":\"\",\"size\":0,\"type\":\"\"}"
        }
    }

    fun toDbType(): DataType = when (this) {
        LIST -> DataType.LIST
        BOOLEAN -> DataType.BOOLEAN
        DATE -> DataType.DATE
        DOUBLE -> DataType.DOUBLE
        FILE -> DataType.FILE
        INT -> DataType.INT
        MAP -> DataType.MAP
        STRING -> DataType.STRING
    }

    companion object{
        fun fromString(text:String) = entries.first { it.name.lowercase() == text }
    }
}

data class TypedValue(val returnType: ReturnType, val value: Any)

