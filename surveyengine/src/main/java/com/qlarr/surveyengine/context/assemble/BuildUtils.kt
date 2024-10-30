package com.qlarr.surveyengine.context.assemble

import com.qlarr.surveyengine.ext.VALID_ANSWER_CODE
import com.qlarr.surveyengine.ext.VALID_QUESTION_CODE
import com.qlarr.surveyengine.ext.flatten
import com.qlarr.surveyengine.model.*
import com.qlarr.surveyengine.model.Instruction.RandomOption.FLIP
import com.qlarr.surveyengine.model.Instruction.RandomOption.RANDOM

internal fun List<SurveyComponent>.getSchema(
    parentCode: String = "",
    randomizedChildrenCodes: List<String> = listOf(),
    prioritisedChildrenCodes: List<String> = listOf(),
): List<ResponseField> {
    val returnList = mutableListOf<ResponseField>()
    forEach { component ->
        val code = component.uniqueCode(parentCode)
        if (randomizedChildrenCodes.contains(component.code))
            returnList.add(ResponseField(code, ColumnName.ORDER, DataType.INT))
        if (prioritisedChildrenCodes.contains(component.code))
            returnList.add(ResponseField(code, ColumnName.PRIORITY, DataType.INT))
        if (component.code.matches(Regex(VALID_QUESTION_CODE)) || component.code.matches(Regex(VALID_ANSWER_CODE))) {
            component.instructionList.firstOrNull { it is Instruction.State && it.reservedCode == ReservedCode.Value }
                ?.let {
                    returnList.add(
                        ResponseField(
                            code,
                            ColumnName.VALUE,
                            (it as Instruction.State).returnType.toDbType()
                        )
                    )
                }
        }
        returnList.addAll(
            component.children.getSchema(
                code,
                component.randomGroups(listOf(RANDOM, FLIP)).map { it.codes }.flatten(),
                component.priorityGroupCodes()
            )
        )
    }
    return returnList
}