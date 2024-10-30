package com.qlarr.surveyengine.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "name",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(ReturnType.QlarrBoolean::class, name = "Boolean"),
    JsonSubTypes.Type(ReturnType.QlarrString::class, name = "String"),
    JsonSubTypes.Type(ReturnType.QlarrInt::class, name = "Int"),
    JsonSubTypes.Type(ReturnType.QlarrList::class, name = "List"),
    JsonSubTypes.Type(ReturnType.QlarrFile::class, name = "File"),
    JsonSubTypes.Type(ReturnType.QlarrMap::class, name = "Map"),
    JsonSubTypes.Type(ReturnType.QlarrDate::class, name = "Date"),
    JsonSubTypes.Type(ReturnType.QlarrDouble::class, name = "Double"),
)
sealed class ReturnType(val name: String) {
    object QlarrBoolean : ReturnType("Boolean")

    object QlarrString : ReturnType("String")
    object QlarrInt : ReturnType("Int")
    object QlarrDouble : ReturnType("Double")
    object QlarrList : ReturnType("List")
    object QlarrMap : ReturnType("Map")
    object QlarrDate : ReturnType("Date")
    object QlarrFile : ReturnType("File")

    override fun equals(other: Any?): Boolean {
        return when (this) {
            is QlarrBoolean -> other is QlarrBoolean
            is QlarrString -> other is QlarrString
            is QlarrDate -> other is QlarrDate
            is QlarrInt -> other is QlarrInt
            is QlarrDouble -> other is QlarrDouble
            is QlarrList -> other is QlarrList
            is QlarrFile -> other is QlarrFile
            is QlarrMap -> other is QlarrMap
        }
    }

    fun defaultTextValue(): String {
        return when (this) {
            QlarrList -> "[]"
            QlarrString -> ""
            QlarrBoolean -> "false"
            QlarrDate -> "1970-01-01 00:00:00"
            QlarrInt, QlarrDouble -> "0"
            QlarrMap -> "{}"
            is QlarrFile -> "{\"filename\":\"\",\"stored_filename\":\"\",\"size\":0,\"type\":\"\"}"
        }
    }

    fun toDbType(): DataType = when (this) {
        QlarrList -> DataType.LIST
        QlarrBoolean -> DataType.BOOLEAN
        QlarrDate -> DataType.DATE
        QlarrDouble -> DataType.DOUBLE
        is QlarrFile -> DataType.FILE
        QlarrInt -> DataType.INT
        QlarrMap -> DataType.MAP
        QlarrString -> DataType.STRING
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

data class TypedValue(val returnType: ReturnType, val value: Any)

