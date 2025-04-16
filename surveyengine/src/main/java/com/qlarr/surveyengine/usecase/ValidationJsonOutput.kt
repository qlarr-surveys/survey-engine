package com.qlarr.surveyengine.usecase

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.qlarr.surveyengine.context.assemble.NotSkippedInstructionManifesto
import com.qlarr.surveyengine.ext.flatten
import com.qlarr.surveyengine.model.*

data class ValidationJsonOutput(
    val survey: ObjectNode = JsonNodeFactory.instance.objectNode(),
    val schema: List<ResponseField> = listOf(),
    val impactMap: StringImpactMap = mapOf(),
    val componentIndexList: List<ComponentIndex> = listOf(),
    val skipMap: Map<String, List<NotSkippedInstructionManifesto>> = mapOf(),
    val script: String = ""
) {
    fun toValidationOutput() = ValidationOutput(
        impactMap = impactMap,
        schema = schema,
        survey = jacksonKtMapper.readValue(survey.toString(), jacksonTypeRef<Survey>()),
        script = script,
        componentIndexList = componentIndexList,
        skipMap = skipMap
    )

    fun toDesignerInput(): DesignerInput = DesignerInput(
        survey.flatten(),
        componentIndexList
    )

    fun surveyNavigationData(): SurveyNavigationData {
        return SurveyNavigationData(
            allowJump = survey.get("allowJump")?.booleanValue() ?: true,
            allowPrevious = survey.get("allowPrevious")?.booleanValue() ?: true,
            skipInvalid = survey.get("skipInvalid")?.booleanValue() ?: true,
            allowIncomplete = survey.get("allowIncomplete")?.booleanValue() ?: true,
            navigationMode = NavigationMode.fromString(survey.get("navigationMode")?.textValue())
        )
    }

    companion object {
        private fun groups(surveyName: String) = JsonNodeFactory.instance.arrayNode().apply {
            add(JsonNodeFactory.instance.objectNode().apply {
                put("code", "G1")
                set<JsonNode>("content", JsonNodeFactory.instance.objectNode().apply {
                    set<JsonNode>("en", JsonNodeFactory.instance.objectNode().apply {
                        put("label", surveyName)
                    })
                })
                put("groupType", "GROUP")
                set<JsonNode>("questions", JsonNodeFactory.instance.arrayNode().apply {
                    add(JsonNodeFactory.instance.objectNode().apply {
                        set<JsonNode>("content", JsonNodeFactory.instance.objectNode().apply {
                            set<JsonNode>("en", JsonNodeFactory.instance.objectNode().apply {
                                put("label", "Sample Text Question")
                            })
                        })
                        put("code", "Q1")
                        put("type", "text_display")
                    })
                })
            })
            add(JsonNodeFactory.instance.objectNode().apply {
                set<JsonNode>("content", JsonNodeFactory.instance.objectNode().apply {
                    set<JsonNode>("en", JsonNodeFactory.instance.objectNode().apply {
                        put("label", "End Page")
                    })
                })
                put("code", "G2")
                put("groupType", "END")
                set<JsonNode>("questions", JsonNodeFactory.instance.arrayNode().apply {
                    add(JsonNodeFactory.instance.objectNode().apply {
                        set<JsonNode>("content", JsonNodeFactory.instance.objectNode().apply {
                            set<JsonNode>("en", JsonNodeFactory.instance.objectNode().apply {
                                put("label", "Bye Question")
                            })
                        })
                        put("code", "Q2")
                        put("type", "text_display")
                    })
                })
            })
        }

        fun new(surveyName: String) = ValidationJsonOutput(
            survey = JsonNodeFactory.instance.objectNode().apply {
                set<JsonNode>("defaultLang", jacksonKtMapper.valueToTree(SurveyLang.EN))
                set<TextNode>("code", TextNode("Survey"))
                set<TextNode>("navigationMode", TextNode(NavigationMode.GROUP_BY_GROUP.name.lowercase()))
                set<BooleanNode>("allowPrevious", BooleanNode.TRUE)
                set<BooleanNode>("skipInvalid", BooleanNode.TRUE)
                set<BooleanNode>("allowIncomplete", BooleanNode.TRUE)
                set<BooleanNode>("allowJump", BooleanNode.TRUE)
            }
        )
    }
}

fun ObjectNode.defaultLang(): String =
    (get("defaultLang") as? ObjectNode)?.get("code")?.textValue() ?: SurveyLang.EN.code

fun ObjectNode.defaultSurveyLang(): SurveyLang =
    try {
        jacksonKtMapper.treeToValue(get("defaultLang") as? ObjectNode, SurveyLang::class.java)
    } catch (e: Exception) {
        SurveyLang.EN
    }


fun ObjectNode.additionalLang(): List<SurveyLang> =
    try {
        jacksonKtMapper.readValue(get("additionalLang").toString(), jacksonTypeRef<List<SurveyLang>>())
    } catch (e: Exception) {
        listOf()
    }

fun ObjectNode.availableLangByCode(code: String?): SurveyLang {
    val defaultLang = defaultSurveyLang()
    return if (code == null || defaultLang.code == code) {
        defaultLang
    } else {
        additionalLang().firstOrNull { it.code == code } ?: defaultLang
    }
}

data class SurveyNavigationData(
    val navigationMode: NavigationMode = NavigationMode.GROUP_BY_GROUP,
    val allowPrevious: Boolean = true,
    val skipInvalid: Boolean = true,
    val allowIncomplete: Boolean = true,
    val allowJump: Boolean = true
)