package com.qlarr.expressionmanager.context.assemble

import com.qlarr.expressionmanager.common.buildScriptEngine
import com.qlarr.expressionmanager.model.ReservedCode
import com.qlarr.expressionmanager.model.SurveyLang
import com.qlarr.expressionmanager.context.nestedComponents
import com.qlarr.expressionmanager.model.*
import com.qlarr.expressionmanager.model.Instruction.*
import com.qlarr.expressionmanager.usecase.wrapToSurvey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("LocalVariableName")
class ContextBuilderTest {

    @Test
    fun `bindComponentUnit will attach scrip failure errors to instructions with messed scripts`() {

        val SURVEY_TITLE_INVALID_SCRIPT = Survey(
            listOf(
                SimpleState("toksdfen.firstName++ > 'Wawa'", ReservedCode.Value, isActive = true),
            ),
            groups = listOf(
                Group("G1", questions = listOf(Question("Q1"))),
                Group(
                    "G2", questions = listOf(Question("Q2")), instructionList = listOf(
                        Reference("reference_en_desc", references = listOf("G1.kabaka"), lang = SurveyLang.EN.code)
                    )
                )
            )
        )
        val contextManager = ContextBuilder(mutableListOf(SURVEY_TITLE_INVALID_SCRIPT), buildScriptEngine())
        contextManager.validate()
        assertTrue(contextManager.components[0].instructionList[0].errors[0] is BindingErrors.ScriptError)
        assertEquals(
            BindingErrors.InvalidReference("G1.kabaka", false),
            contextManager.components[0].children[1].instructionList[0].errors[0]
        )

    }


    @Test
    fun `a component with fatal error cannot be referenced`() {

        val SURVEY_TITLE_INVALID_SCRIPT = Survey(
            listOf(
                SimpleState("toksdfen.firstName++ > 'Wawa'", ReservedCode.Value, isActive = true),
            ),
            groups = listOf(
                Group("G1"),
                Group(
                    "G2", questions = listOf(Question("Q1")), instructionList = listOf(
                        Reference("reference_en_desc", references = listOf("G1.kabaka"), lang = SurveyLang.EN.code)
                    )
                )
            )
        )
        val contextManager = ContextBuilder(mutableListOf(SURVEY_TITLE_INVALID_SCRIPT), buildScriptEngine())
        contextManager.validate()
        assertTrue(contextManager.components[0].instructionList[0].errors[0] is BindingErrors.ScriptError)
        assertEquals(
            BindingErrors.InvalidReference("G1.kabaka", true),
            contextManager.components[0].children[1].instructionList[0].errors[0]
        )

    }

    @Test
    fun `bindComponentUnit will attach scrip failure errors to instructions with messed scripts2`() {

        val SURVEY_TITLE_INVALID_SCRIPT = Survey(
            listOf(
                SimpleState(";;toksdfen.firstName++ > 'Wawa'", ReservedCode.Value, isActive = true),
                Reference(
                    "reference_1",
                    listOf(
                        "Q1Afirst_name.validity",
                        "Q1Alast_name.validity",
                        "Q1Alast_name.value",
                        "Q1Afirst_name.value"
                    ),
                    SurveyLang.EN.code
                )
            ), groups = listOf(Group("G1"))
        )
        val contextManager = ContextBuilder(mutableListOf(SURVEY_TITLE_INVALID_SCRIPT), buildScriptEngine())
        contextManager.validate()
        assertTrue(contextManager.components[0].instructionList[0].errors[0] is BindingErrors.ScriptError)
        assertEquals(
            BindingErrors.InvalidReference("Q1Afirst_name.validity", true),
            contextManager.components[0].instructionList[1].errors[0]
        )
        assertEquals(
            BindingErrors.InvalidReference("Q1Alast_name.validity", true),
            contextManager.components[0].instructionList[1].errors[1]
        )
        assertEquals(
            BindingErrors.InvalidReference("Q1Alast_name.value", true),
            contextManager.components[0].instructionList[1].errors[2]
        )
        assertEquals(
            BindingErrors.InvalidReference("Q1Afirst_name.value", true),
            contextManager.components[0].instructionList[1].errors[3]
        )

    }

    @Test
    fun `Skip Equations are validated`() {
        val QUESTION_ONE = Question("Q1")
        val QUESTION_TWO = Question(
            "Q2", listOf(
                SkipInstruction(
                    skipToComponent = "Q4",
                    code = "skip_to_question_4",
                    isActive = false,
                    condition = "toksdfen.firstName++ > 'Wawa'"
                )
            )
        )
        val QUESTION_THREE = Question(
            "Q3", listOf(
                SkipInstruction(
                    skipToComponent = "Q7",
                    code = "skip_to_question_7",
                    isActive = false,
                    condition = "true"
                )
            )
        )
        val QUESTION_FOUR = Question("Q4", listOf(
            SkipInstruction(
                skipToComponent = "G2",
                code = "skip_to_G2",
                toEnd = true,
                isActive = false,
                condition = "true"
            )
        ))
        val Survey = Survey(
            groups = listOf(
                Group("G1", questions = listOf(QUESTION_ONE, QUESTION_TWO, QUESTION_THREE, QUESTION_FOUR)),
                Group("G2", groupType = GroupType.END, questions = listOf(Question("Q10")))
            )
        )
        val contextManager = ContextBuilder(mutableListOf(Survey), buildScriptEngine())
        contextManager.validate()

        assertEquals(
            BindingErrors.InvalidSkipReference("Q7"),
            contextManager.components[0].children[0].children[2].instructionList[0].errors[0]
        )
        assertEquals(
            BindingErrors.SkipToEndOfEndGroup,
            contextManager.components[0].children[0].children[3].instructionList[0].errors[0]
        )
        contextManager.components
    }

    @Test
    fun `Duplicate Instructions are skipped`() {
        val QUESTION = Question(
            "Q1", listOf(
                SimpleState("false", ReservedCode.ConditionalRelevance),
                SimpleState("2", ReservedCode.ConditionalRelevance)
            )
        )
        val contextManager = ContextBuilder(mutableListOf(QUESTION), buildScriptEngine())
        contextManager.validate()
    }


    @Test
    fun `FWD Dependency errors are flagged`() {
        val G1Q1 = Question("Q1", listOf(SimpleState("Q2.value", ReservedCode.Value, isActive = true)))
        val G1Q2 = Question("Q2", listOf(SimpleState("Q1.value", ReservedCode.Value, isActive = true)))
        val G1 = Group("G1", listOf(SimpleState("", ReservedCode.Value)), listOf(G1Q1, G1Q2))
        val contextManager = ContextBuilder(mutableListOf(G1.wrapToSurvey()), buildScriptEngine())
        contextManager.validate()

        assertTrue(contextManager.components[0].children[0].children[0].instructionList[0].errors[0] is BindingErrors.ForwardDependency)
        assertTrue(contextManager.components[0].children[0].children[1].instructionList[0].errors.isEmpty())
    }

    @Test
    fun `DependencyErrorAnalyzer analyzes circular RelevanceToOwnParentDependency`() {

        val A1 = Answer("A1", listOf(SimpleState("G1.relevance", ReservedCode.ConditionalRelevance)))
        val Q1 = Question(
            "Q1",
            answers = listOf(A1),
            instructionList = listOf(ParentRelevance(listOf(listOf("A1"))))
        )
        val G1 = Group("G1", questions = listOf(Q1))
        val contextManager = ContextBuilder(mutableListOf(G1.wrapToSurvey()), buildScriptEngine())
        contextManager.validate()
        assertTrue(contextManager.components[0].children[0].children[0].children[0].instructionList[0].errors[0] is BindingErrors.ForwardDependency)

    }

    @Test
    fun `DependsencyErrorAnalyzer analyzes circular RelevanceToOwnParentDependency`() {

        val Survey = Survey(
            instructionList = listOf(RandomGroups(listOf(listOf("G2", "G3")))),
            groups = listOf(
                Group(
                    "G1",
                    instructionList = listOf(
                        RandomGroups(listOf(listOf("Q1", "Q2"), listOf("Q3", "Q4"))),
                        SkipInstruction(code = "skip_to_G3_from_G1", skipToComponent = "G3")

                    ),
                    questions = listOf(
                        Question(
                            "Q1",
                            instructionList = listOf(
                                SkipInstruction(
                                    code = "skip_to_Q8_from_Q1",
                                    skipToComponent = "Q8"
                                )
                            )
                        ),
                        Question("Q2"),
                        Question("Q3"),
                        Question("Q4")
                    ),
                ),
                Group(
                    "G2",
                    instructionList = listOf(
                        SkipInstruction(code = "skip_to_G3_from_G2", skipToComponent = "G3"),
                        SkipInstruction(code = "skip_to_Q11_from_G2", skipToComponent = "Q11")

                    ),
                    questions = listOf(
                        Question("Q5"),
                        Question("Q6"),
                        Question(
                            "Q7",
                            instructionList = listOf(
                                SkipInstruction(code = "skip_to_G3_from_Q7", skipToComponent = "G3"),
                                SkipInstruction(code = "skip_to_G4_from_Q7", skipToComponent = "G4"),
                                SkipInstruction(code = "skip_to_Q11_from_Q7", skipToComponent = "Q11")

                            )
                        )
                    )
                ),
                Group(
                    "G3",
                    questions = listOf(
                        Question("Q8"),
                        Question("Q9"),
                        Question("Q10")
                    )
                ),
                Group(
                    "G4",
                    questions = listOf(
                        Question("Q11"),
                        Question("Q12"),
                        Question(
                            "Q13",
                            instructionList = listOf(
                                SkipInstruction(code = "skip_to_G1_from_Q13", skipToComponent = "G1"),

                                )
                        )
                    )
                )
            )
        )

        val contextManager = ContextBuilder(mutableListOf(Survey), buildScriptEngine())
        contextManager.validate()
        val allSkip = contextManager.components.nestedComponents()
            .map { it.instructionList.filterIsInstance<SkipInstruction>() }
            .flatten()
            .map { skipInstruction ->
                Pair(
                    skipInstruction.code,
                    skipInstruction.errors.any { it is BindingErrors.InvalidSkipReference })
            }
        assertEquals(
            listOf(
                Pair("skip_to_G3_from_G1", false),
                Pair("skip_to_Q8_from_Q1", false),
                Pair("skip_to_G3_from_G2", true),
                Pair("skip_to_Q11_from_G2", false),
                Pair("skip_to_G3_from_Q7", true),
                Pair("skip_to_G4_from_Q7", false),
                Pair("skip_to_Q11_from_Q7", false),
                Pair("skip_to_G1_from_Q13", true)
            ), allSkip
        )

    }
}