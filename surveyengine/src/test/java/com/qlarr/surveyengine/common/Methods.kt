package com.qlarr.surveyengine.common

import com.qlarr.surveyengine.model.SurveyComponent
import com.qlarr.surveyengine.usecase.ScriptEngineValidate
import com.qlarr.surveyengine.usecase.ScriptValidationInput
import com.qlarr.surveyengine.usecase.ScriptValidationOutput
import com.qlarr.scriptengine.ScriptEngineValidation

fun SurveyComponent.getErrorsCount(): Int {
    var returnResult = errors.size
    instructionList.forEach { instruction ->
        returnResult += instruction.errors.size
    }
    children.forEach { component ->
        returnResult += component.getErrorsCount()
    }

    return returnResult
}

fun buildScriptEngine(): ScriptEngineValidate {
    val scriptEngineWrapper = ScriptEngineValidation()
    return object : ScriptEngineValidate {
        override fun validate(input: List<ScriptValidationInput>): List<ScriptValidationOutput> {
            return scriptEngineWrapper.validate(input)
        }


    }
}
