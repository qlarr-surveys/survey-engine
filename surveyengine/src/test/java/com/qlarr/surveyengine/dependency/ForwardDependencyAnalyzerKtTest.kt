package com.qlarr.surveyengine.dependency

import com.qlarr.surveyengine.common.buildScriptEngine
import com.qlarr.surveyengine.model.ReservedCode
import com.qlarr.surveyengine.context.assemble.ContextBuilder
import com.qlarr.surveyengine.model.Group
import com.qlarr.surveyengine.model.Instruction
import com.qlarr.surveyengine.model.Question
import org.junit.Test

class ForwardDependencyAnalyzerKtTest {

    @Test
    fun `bindComponent loads survey elements and evaluates its instructions`() {
        val QUESTION_ONE = Question(
            "Q1", listOf(
                Instruction.SimpleState("Q2.relevance", ReservedCode.ConditionalRelevance)
            )
        )

        val QUESTION_TWO = Question(
            "Q2", listOf(
                Instruction.SimpleState("Q1.conditional_relevance", ReservedCode.ConditionalRelevance)
            )
        )

        val QUESTION_THREE = Question(
            "Q3", listOf(
                Instruction.SimpleState("Q2.relevance", ReservedCode.ConditionalRelevance)
            )
        )

        val group = Group("G1", listOf(), listOf(QUESTION_ONE, QUESTION_TWO, QUESTION_THREE))
        val contextManager = ContextBuilder(mutableListOf(group), buildScriptEngine())
        contextManager.validate()
        contextManager.components[0]
    }

}