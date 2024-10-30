package com.qlarr.surveyengine.model

import com.qlarr.surveyengine.ext.VALID_ANSWER_CODE
import com.qlarr.surveyengine.ext.VALID_GROUP_CODE
import com.qlarr.surveyengine.ext.VALID_QUESTION_CODE
import com.qlarr.surveyengine.ext.VALID_SURVEY_CODE
import com.qlarr.surveyengine.model.SurveyElementType.*


enum class SurveyElementType(val codeRegex: String) {
    ANSWER(VALID_ANSWER_CODE),
    QUESTION(VALID_QUESTION_CODE),
    GROUP(VALID_GROUP_CODE),
    SURVEY(VALID_SURVEY_CODE);
}

fun SurveyElementType.childType(): SurveyElementType = when (this) {
    ANSWER -> ANSWER
    QUESTION -> ANSWER
    GROUP -> QUESTION
    SURVEY -> GROUP
}

fun SurveyElementType.nameAsChildList(): String = when (this) {
    ANSWER -> "answers"
    QUESTION -> "questions"
    GROUP -> "groups"
    SURVEY -> throw IllegalStateException("cannot be child")
}

fun SurveyElementType.hasUniqueCode(): Boolean = when (this) {
    ANSWER -> false
    else -> true
}
