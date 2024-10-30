package com.qlarr.surveyengine.usecase

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.qlarr.surveyengine.ext.copyErrorsToJSON
import com.qlarr.surveyengine.model.Survey
import com.qlarr.surveyengine.model.jacksonKtMapper

interface ValidationUseCaseWrapper {
    fun validate(): ValidationJsonOutput
}

class ValidationUseCaseWrapperImpl(scriptEngine: ScriptEngineValidate, private val surveyJson: String) :
    ValidationUseCaseWrapper {
    private val useCase: ValidationUseCase

    init {
        val survey = jacksonKtMapper.readValue(surveyJson, jacksonTypeRef<Survey>())!!
        useCase = ValidationUseCaseImpl(scriptEngine, survey)
    }


    override fun validate(): ValidationJsonOutput {
        val output = useCase.validate()
        val survey = jacksonKtMapper.readTree(surveyJson)
        return ValidationJsonOutput(
            survey = output.survey.copyErrorsToJSON(survey as ObjectNode),
            schema = output.schema,
            impactMap = output.impactMap,
            componentIndexList = output.componentIndexList,
            skipMap = output.skipMap,
            script = output.script
        )
    }
}
