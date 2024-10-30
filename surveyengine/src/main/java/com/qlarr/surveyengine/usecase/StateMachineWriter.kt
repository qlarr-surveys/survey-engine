package com.qlarr.surveyengine.usecase

import com.fasterxml.jackson.databind.node.ObjectNode
import com.qlarr.surveyengine.ext.mapToJsonObject
import com.qlarr.surveyengine.model.*
import com.qlarr.surveyengine.model.Instruction.State
import org.json.JSONArray

internal class StateMachineWriter(scriptInput: ScriptInput) {
    private val bindings = scriptInput.bindings
    private val formatBindings = scriptInput.formatBindings
    private val jsComponents = scriptInput.contextComponents
    private val impactMap = scriptInput.dependencyMapBundle.first
    private val state = mutableMapOf<String,Any>()
    private val qlarrVariables = mutableMapOf<String,Any>()
    private val qlarrDependents = mutableMapOf<String,Any>()

    fun state(): ObjectNode {

        jsComponents.forEach {
            it.writeSystemInstruction()
        }
        state["qlarrVariables"] = qlarrVariables
        state["qlarrDependents"] = qlarrDependents
        return mapToJsonObject(state).toObjectNode()
    }


    private fun ChildlessComponent.writeSystemInstruction() {
        val componentVariables = mutableMapOf<String,Any>()
        val componentDependents = mutableMapOf<String,Any>()

        instructionList.filterIsInstance<Instruction.Reference>().forEach { instruction ->
            componentVariables[instruction.code] = formatBindings[Dependent(code, instruction.code)]!!
        }
        bindings.filter { it.key.componentCode == code }.forEach {
            componentVariables[it.key.reservedCode.code] = it.value
        }
        instructionList.filterIsInstance<State>().forEach { reservedInstruction ->
            if (isDependency(code, reservedInstruction.reservedCode)) {
                val returnDependents = JSONArray()
                writeDependentFunctions(code, reservedInstruction.reservedCode).forEach {
                    returnDependents.put(it.toJsonArray())
                }
                if (returnDependents.length() > 0) {
                    componentDependents[reservedInstruction.code] = returnDependents
                }
            }
        }
        if (componentDependents.keys.isNotEmpty()) {
            qlarrDependents[code] = componentDependents
        }
        if (componentVariables.keys.isNotEmpty() || shouldHaveEmptyObject()) {
            qlarrVariables[code] = componentVariables
        }
    }

    private fun writeDependentFunctions(componentCode: String, reservedCode: ReservedCode): List<List<String>> {
        return getDependents(componentCode, reservedCode).map {
            listOf(it.componentCode, it.instructionCode)
        }
    }

    private fun List<String>.toJsonArray(): JSONArray {
        val returnArray = JSONArray()
        forEach {
            returnArray.put(it)
        }
        return returnArray
    }

    private fun isDependency(qualifiedCode: String, reservedCode: ReservedCode): Boolean {
        return impactMap.keys.any { it.componentCode == qualifiedCode && it.reservedCode == reservedCode }
    }

    private fun getDependents(qualifiedCode: String, reservedCode: ReservedCode): List<Dependent> {
        val key = impactMap.keys.firstOrNull { it.componentCode == qualifiedCode && it.reservedCode == reservedCode }
        return impactMap[key] ?: listOf()
    }

    private fun ChildlessComponent.shouldHaveEmptyObject(): Boolean =
        instructionList.any { it.code == ReservedCode.Value.code }
}