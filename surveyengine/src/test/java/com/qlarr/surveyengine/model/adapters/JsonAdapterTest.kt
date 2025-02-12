package com.qlarr.surveyengine.model.adapters

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.qlarr.surveyengine.model.*
import com.qlarr.surveyengine.model.InstructionError.ScriptError
import com.qlarr.surveyengine.model.Instruction.*
import com.qlarr.surveyengine.model.ReservedCode.*
import com.qlarr.surveyengine.usecase.ValidationJsonOutput
import org.junit.Assert.assertEquals
import org.junit.Test


@Suppress("PrivatePropertyName")
class JsonAdapterTest {

    private val SKIP =
        SkipInstruction(skipToComponent = "Q2", code = "skip_to_q4", condition = "true", isActive = false)
    private val SKIP_TEXT =
        "{\"code\":\"skip_to_q4\",\"text\":\"true\",\"returnType\":\"boolean\",\"isActive\":false,\"skipToComponent\":\"Q2\",\"condition\":\"true\",\"toEnd\":false}"
    private val PRIORITY_GROUPS = PriorityGroups(
        priorities = listOf(
            PriorityGroup(listOf(ChildPriority("Q1"), ChildPriority("Q2"))),
            PriorityGroup(listOf(ChildPriority("G1"), ChildPriority("G2")))
        )
    )
    private val PRIORITY_GROUPS_TEXT =
        "{\"code\":\"priority_groups\",\"priorities\":[{\"weights\":[{\"code\":\"Q1\",\"weight\":1.0},{\"code\":\"Q2\",\"weight\":1.0}],\"limit\":1},{\"weights\":[{\"code\":\"G1\",\"weight\":1.0},{\"code\":\"G2\",\"weight\":1.0}],\"limit\":1}]}"
    private val RANDOM_GROUP = RandomGroups(groups = listOf(listOf("1", "2", "3"), listOf("4", "5", "6")))
    private val RANDOM_GROUP_TEXT =
        "{\"code\":\"random_group\",\"groups\":[{\"codes\":[\"1\",\"2\",\"3\"],\"randomOption\":\"RANDOM\"},{\"codes\":[\"4\",\"5\",\"6\"],\"randomOption\":\"RANDOM\"}]}"
    private val REF_EQ_TEXT =
        "{\"code\":\"reference_label\",\"references\":[\"Q1.label\"],\"lang\":\"en\"}"
    private val DYNAMIC_EQ_TEXT =
        "{\"code\":\"conditional_relevance\",\"text\":\"true\",\"returnType\":\"boolean\",\"isActive\":true}"
    private val PARENT_REL_TEXT = "{\"code\":\"parent_relevance\",\"children\":[[\"A1\",\"A2\",\"A3\",\"A4\"]]}"
    private val VALUE_EQ_TEXT_INPUT =
        "{\"code\":\"value\",\"text\":\"\",\"returnType\":\"string\",\"isActive\":false}"
    private val VALUE_EQ_TEXT =
        "{\"code\":\"value\",\"text\":\"\",\"returnType\":\"string\",\"isActive\":false}"
    private val EQ_List_TEXT = "[$DYNAMIC_EQ_TEXT,$VALUE_EQ_TEXT]"
    private val DYNAMIC_EQ = SimpleState(
        text = "true",
        reservedCode = ConditionalRelevance
    )
    private val PARENT_REL = ParentRelevance(
        children = listOf(listOf("A1", "A2", "A3", "A4"))
    )
    private val VALUE_EQ = SimpleState(
        text = "",
        reservedCode = Value
    )

    private val REF_EQ = Reference(
        "reference_label",
        listOf("Q1.label"),
        lang = SurveyLang.EN.code
    )

    private val QUESTION = Question("Q2", listOf(SimpleState("true", ConditionalRelevance)))
    private val QUESTION_TEXT =
        "{\"code\":\"Q2\",\"instructionList\":[{\"code\":\"conditional_relevance\",\"text\":\"true\",\"returnType\":\"boolean\",\"isActive\":true}],\"answers\":[],\"errors\":[]}"


    private val G3Q5 = Question(
        code = "Q5",
        instructionList = listOf(SimpleState("", Value))
    )

    private val G3Q6 = Question(
        code = "Q6",
        instructionList = listOf(SimpleState("", Value))
    )

    private val G3_TEXT =
        "{\"code\":\"G3\",\"instructionList\":[{\"code\":\"value\",\"text\":\"\",\"returnType\":\"string\"," +
                "\"isActive\":false}],\"questions\":[{\"code\":\"Q5\",\"instructionList\":[{\"code\":\"value\"," +
                "\"text\":\"\",\"returnType\":\"string\",\"isActive\":false}],\"answers\":[],\"errors\":[]}," +
                "{\"code\":\"Q6\",\"instructionList\":[{\"code\":\"value\",\"text\":\"\",\"returnType\":\"string\"," +
                "\"isActive\":false}],\"answers\":[],\"errors\":[]}],\"groupType\":\"GROUP\",\"errors\":[]}"
    private val G3 = Group(
        code = "G3",
        instructionList = listOf(SimpleState("", Value)),
        questions = listOf(G3Q5, G3Q6)
    )
    private val COMPONENT_List = listOf(G3)

    private val COMPONENT_List_TEXT = "[$G3_TEXT]"

    private val RG_DUPLICATE = InstructionError.DuplicateRandomGroupItems(listOf())
    private val RG_DUPLICATE_TXT = "{\"items\":[],\"name\":\"DuplicateRandomGroupItems\"}"
    private val RG_NOT_CHILD = InstructionError.RandomGroupItemNotChild(listOf("A1", "A2"))
    private val RG_NOT_CHILD_TXT = "{\"items\":[\"A1\",\"A2\"],\"name\":\"RandomGroupItemNotChild\"}"

    private val SCRIPT_FAILURE_ERR = ScriptError(message = "error message", start = 5, end = 120)
    private val SCRIPT_FAILURE_ERR_TEXT =
        "{\"message\":\"error message\",\"start\":5,\"end\":120,\"name\":\"ScriptError\"}"
    private val FWD_DEPENDENCY_ERR = InstructionError.ForwardDependency(Dependency("G1Q1", Value))
    private val FWD_DEPENDENCY_ERR_TEXT =
        "{\"dependency\":{\"componentCode\":\"G1Q1\",\"reservedCode\":\"value\"},\"name\":\"ForwardDependency\"}"
    private val List_ERR = listOf(SCRIPT_FAILURE_ERR, FWD_DEPENDENCY_ERR)
    private val List_ERR_TEXT = "[$SCRIPT_FAILURE_ERR_TEXT,$FWD_DEPENDENCY_ERR_TEXT]"
    private val List_ERR_1 = listOf(RG_DUPLICATE, RG_NOT_CHILD)
    private val List_ERR_1_TEXT = "[$RG_DUPLICATE_TXT,$RG_NOT_CHILD_TXT]"

    private val NAV_INDEX_G1 = NavigationIndex.Group("G1")
    private val NAV_INDEX_Q1 = NavigationIndex.Question("Q1")
    private val NAV_INDEX_G1_2_3 = NavigationIndex.Groups(listOf("G1", "G2", "G3"))
    private val NAV_INDEX_G1_TEXT = "{\"groupId\":\"G1\",\"name\":\"group\"}"
    private val NAV_INDEX_G1_2_3_TEXT = "{\"groupIds\":[\"G1\",\"G2\",\"G3\"],\"name\":\"groups\"}"
    private val NAV_INDEX_Q1_TEXT = "{\"questionId\":\"Q1\",\"name\":\"question\"}"


    private val useCaseInput = NavigationUseCaseInput(
        lang = SurveyLang.DE.code,
        navigationInfo = NavigationInfo(
            navigationDirection = NavigationDirection.Jump(navigationIndex = NavigationIndex.Groups(listOf()))
        ),
        values = mapOf(
            "Q1.value" to "",
            "Q2.value" to 2.2,
            "Q3.value" to true,
            "Q4.value" to listOf("1", "2", "3"),
            "Q5.value" to mapOf("first" to "john", "last" to "smith")
        )
    )
    private val useCaseInputText =
        "{\"values\":{\"Q1.value\":\"\",\"Q2.value\":2.2,\"Q3.value\":true,\"Q4.value\":[\"1\",\"2\",\"3\"],\"Q5.value\":{\"first\":\"john\",\"last\":\"smith\"}},\"lang\":\"de\",\"navigationInfo\":{\"navigationIndex\":null,\"navigationDirection\":{\"name\":\"JUMP\",\"navigationIndex\":{\"groupIds\":[],\"name\":\"groups\"}}}}"

    private val useCaseInput1 = NavigationUseCaseInput(
        navigationInfo = NavigationInfo(
            navigationIndex = NavigationIndex.Question("Q1"),
            navigationDirection = NavigationDirection.Next
        )
    )
    private val useCaseInputText1 =
        "{\"values\":{},\"lang\":null,\"navigationInfo\":{\"navigationIndex\":{\"questionId\":\"Q1\",\"name\":\"question\"},\"navigationDirection\":{\"name\":\"NEXT\"}}}"

    private val useCaseInput2 = NavigationUseCaseInput()
    private val useCaseInputText2 =
        "{\"values\":{},\"lang\":null,\"navigationInfo\":{\"navigationIndex\":null,\"navigationDirection\":{\"name\":\"START\"}}}"


    @Test
    fun `serializes and de-serializes instructions`() {
        assertEquals(SKIP_TEXT, jacksonKtMapper.writeValueAsString(SKIP))
        assertEquals(SKIP, jacksonKtMapper.readValue(SKIP_TEXT, jacksonTypeRef<Instruction>()))

        assertEquals(RANDOM_GROUP_TEXT, jacksonKtMapper.writeValueAsString(RANDOM_GROUP))
        assertEquals(RANDOM_GROUP, jacksonKtMapper.readValue(RANDOM_GROUP_TEXT, jacksonTypeRef<Instruction>()))

        assertEquals(PRIORITY_GROUPS_TEXT, jacksonKtMapper.writeValueAsString(PRIORITY_GROUPS))
        assertEquals(RANDOM_GROUP, jacksonKtMapper.readValue(RANDOM_GROUP_TEXT, jacksonTypeRef<Instruction>()))

        assertEquals(DYNAMIC_EQ_TEXT, jacksonKtMapper.writeValueAsString(DYNAMIC_EQ))
        assertEquals(DYNAMIC_EQ, jacksonKtMapper.readValue(DYNAMIC_EQ_TEXT, jacksonTypeRef<Instruction>()))

        assertEquals(PARENT_REL, jacksonKtMapper.readValue(PARENT_REL_TEXT, jacksonTypeRef<Instruction>()))
        assertEquals(PARENT_REL_TEXT, jacksonKtMapper.writeValueAsString(PARENT_REL))

        assertEquals(REF_EQ_TEXT, jacksonKtMapper.writeValueAsString(REF_EQ))
        assertEquals(REF_EQ, jacksonKtMapper.readValue(REF_EQ_TEXT, jacksonTypeRef<Instruction>()))

        assertEquals(VALUE_EQ_TEXT_INPUT, jacksonKtMapper.writeValueAsString(VALUE_EQ))
        assertEquals(VALUE_EQ, jacksonKtMapper.readValue(VALUE_EQ_TEXT_INPUT, jacksonTypeRef<Instruction>()))


        val list = listOf(DYNAMIC_EQ, VALUE_EQ)
        assertEquals(EQ_List_TEXT, jacksonKtMapper.writeValueAsString(list))
        assertEquals(list, jacksonKtMapper.readValue(EQ_List_TEXT, jacksonTypeRef<List<Instruction>>()))


    }

    @Test
    fun `serializes and de-serializes BindingErrors`() {
        assertEquals(FWD_DEPENDENCY_ERR_TEXT, jacksonKtMapper.writeValueAsString(FWD_DEPENDENCY_ERR))
        assertEquals(
            FWD_DEPENDENCY_ERR,
            jacksonKtMapper.readValue(FWD_DEPENDENCY_ERR_TEXT, jacksonTypeRef<InstructionError>())
        )

        assertEquals(List_ERR_TEXT, jacksonKtMapper.writeValueAsString(List_ERR))
        assertEquals(List_ERR, jacksonKtMapper.readValue(List_ERR_TEXT, jacksonTypeRef<List<InstructionError>>()))

        assertEquals(List_ERR_1, jacksonKtMapper.readValue(List_ERR_1_TEXT, jacksonTypeRef<List<InstructionError>>()))
        assertEquals(List_ERR_1_TEXT, jacksonKtMapper.writeValueAsString(List_ERR_1))
    }

    @Test
    fun `serializes and de-serializes NavIndex`() {
        assertEquals(NAV_INDEX_G1, jacksonKtMapper.readValue(NAV_INDEX_G1_TEXT, jacksonTypeRef<NavigationIndex>()))
        assertEquals(NAV_INDEX_G1_TEXT, jacksonKtMapper.writeValueAsString(NAV_INDEX_G1))

        assertEquals(
            NAV_INDEX_G1_2_3,
            jacksonKtMapper.readValue(NAV_INDEX_G1_2_3_TEXT, jacksonTypeRef<NavigationIndex>())
        )
        assertEquals(NAV_INDEX_G1_2_3_TEXT, jacksonKtMapper.writeValueAsString(NAV_INDEX_G1_2_3))

        assertEquals(NAV_INDEX_Q1, jacksonKtMapper.readValue(NAV_INDEX_Q1_TEXT, jacksonTypeRef<NavigationIndex>()))
        assertEquals(NAV_INDEX_Q1_TEXT, jacksonKtMapper.writeValueAsString(NAV_INDEX_Q1))
    }


    @Test
    fun `serializes and de-serializes components`() {
        assertEquals(QUESTION_TEXT, jacksonKtMapper.writeValueAsString(QUESTION))
        assertEquals(QUESTION, jacksonKtMapper.readValue(QUESTION_TEXT, jacksonTypeRef<Question>()))


        assertEquals(COMPONENT_List_TEXT, jacksonKtMapper.writeValueAsString(COMPONENT_List))
        assertEquals(COMPONENT_List, jacksonKtMapper.readValue(COMPONENT_List_TEXT, jacksonTypeRef<List<Group>>()))

        assertEquals(G3_TEXT, jacksonKtMapper.writeValueAsString(G3))
        assertEquals(G3, jacksonKtMapper.readValue(G3_TEXT, jacksonTypeRef<Group>()))
    }

    @Test
    fun `serialises and deserialises Return Type`() {
        val file = ReturnType.FILE
        assertEquals("\"FILE\"", jacksonKtMapper.writeValueAsString(file))
        assertEquals(
            file,
            jacksonKtMapper.readValue("\"FILE\"", jacksonTypeRef<ReturnType>())
        )
    }

    @Test
    fun `serialises and deserialises UseCaseInput`() {
        assertEquals(useCaseInputText, jacksonKtMapper.writeValueAsString(useCaseInput))
//        assertEquals(useCaseInputText, gson.toJson(useCaseInput))
        assertEquals(
            useCaseInput,
            jacksonKtMapper.readValue(useCaseInputText, jacksonTypeRef<NavigationUseCaseInput>())
        )

        assertEquals(useCaseInputText1, jacksonKtMapper.writeValueAsString(useCaseInput1))
        assertEquals(
            useCaseInput1,
            jacksonKtMapper.readValue(useCaseInputText1, jacksonTypeRef<NavigationUseCaseInput>())
        )

        assertEquals(useCaseInputText2, jacksonKtMapper.writeValueAsString(useCaseInput2))
        assertEquals(
            useCaseInput2,
            jacksonKtMapper.readValue(useCaseInputText2, jacksonTypeRef<NavigationUseCaseInput>())
        )
    }


    @Test
    fun `serializes and de-serializes toValidationJsonOutput`() {
        val validationJsonOutput = ValidationJsonOutput(
            schema = listOf(
                ResponseField("Q1", ColumnName.VALUE, DataType.STRING),
                ResponseField("Q2", ColumnName.ORDER, DataType.INT)
            ),
            impactMap = mapOf(
                Dependency("Q1", Value) to listOf(
                    Dependent("Q1", "conditional_relevance"),
                    Dependent("Q1", "validity")
                ),
                Dependency("Q3", Meta) to listOf(Dependent("Q3", "conditional_relevance"), Dependent("Q3", "validity"))
            ).toStringImpactMap(),
            survey = jacksonKtMapper.readTree(
                "{\"code\":\"G3\"," +
                        "\"instructionList\":[{\"code\":\"value\",\"text\":\"\",\"isActive\":false,\"returnType\":\"String\"}]," +
                        "\"children\":[{\"code\":\"Q5\",\"instructionList\":[{\"code\":\"value\",\"text\":\"\",\"isActive\":false,\"returnType\":\"String\"}]},{\"code\":\"Q6\",\"instructionList\":[{\"code\":\"value\",\"text\":\"\",\"isActive\":false,\"returnType\":\"String\"}]}]}"
            ) as ObjectNode,
            componentIndexList = listOf(
                ComponentIndex(
                    "Q1", null, listOf("Q1A1"), 0, 0,
                    setOf(), setOf(ChildrenRelevance, Value)
                ),
                ComponentIndex(
                    "Q3", null, listOf("Q3A1", "Q3A3"), 1, 1,
                    setOf(), setOf(ChildrenRelevance, Value)
                )
            ),
            script = "",
            skipMap = mapOf()
        )
        val text =
            "{\"survey\":{\"code\":\"G3\",\"instructionList\":[{\"code\":\"value\",\"text\":\"\",\"isActive\":false,\"returnType\":\"String\"}],\"children\":[{\"code\":\"Q5\",\"instructionList\":[{\"code\":\"value\",\"text\":\"\",\"isActive\":false,\"returnType\":\"String\"}]},{\"code\":\"Q6\",\"instructionList\":[{\"code\":\"value\",\"text\":\"\",\"isActive\":false,\"returnType\":\"String\"}]}]},\"schema\":[{\"componentCode\":\"Q1\",\"columnName\":\"VALUE\",\"dataType\":\"STRING\"},{\"componentCode\":\"Q2\",\"columnName\":\"ORDER\",\"dataType\":\"INT\"}],\"impactMap\":{\"Q1.value\":[\"Q1.conditional_relevance\",\"Q1.validity\"],\"Q3.meta\":[\"Q3.conditional_relevance\",\"Q3.validity\"]},\"componentIndexList\":[{\"code\":\"Q1\",\"parent\":null,\"children\":[\"Q1A1\"],\"minIndex\":0,\"maxIndex\":0,\"prioritisedSiblings\":[],\"dependencies\":[\"children_relevance\",\"value\"]},{\"code\":\"Q3\",\"parent\":null,\"children\":[\"Q3A1\",\"Q3A3\"],\"minIndex\":1,\"maxIndex\":1,\"prioritisedSiblings\":[],\"dependencies\":[\"children_relevance\",\"value\"]}],\"skipMap\":{},\"script\":\"\"}"
        assertEquals(text, jacksonKtMapper.writeValueAsString(validationJsonOutput))
        assertEquals(validationJsonOutput, jacksonKtMapper.readValue(text, jacksonTypeRef<ValidationJsonOutput>()))
    }
}