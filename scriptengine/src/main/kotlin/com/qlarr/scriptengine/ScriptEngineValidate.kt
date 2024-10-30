package com.qlarr.scriptengine

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine
import com.qlarr.surveyengine.model.ReturnType
import com.qlarr.surveyengine.model.jacksonKtMapper
import com.qlarr.surveyengine.usecase.ScriptValidationInput
import com.qlarr.surveyengine.usecase.ScriptValidationOutput
import com.qlarr.surveyengine.usecase.ValidationScriptError
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.ResourceLimits
import org.json.JSONArray
import org.json.JSONObject
import javax.script.Bindings
import javax.script.Compilable
import javax.script.CompiledScript
import javax.script.ScriptEngine


class ScriptEngineValidation {
    private val compiledScript: CompiledScript

    init {
        val classLoader = javaClass.classLoader
        val script = classLoader.getResourceAsStream("survey-engine-script.min.js")!!.reader().readText()
        compiledScript = (engine as Compilable).compile(
            "$script;" +
                    "const EMScript = typeof globalThis !== 'undefined' ? globalThis.EMScript : this.EMScript;" +
                    "EMScript.validateCode(instructionList);"
        )
    }

    companion object {
        var engine: ScriptEngine = GraalJSScriptEngine.create(null,
            Context.newBuilder("js")
                .allowHostAccess(HostAccess.NONE)
                .allowHostClassLookup { false }
                .resourceLimits(
                    ResourceLimits.newBuilder()
                        .statementLimit(1000000, null)
                        .build()
                ) // Set resource limits
                .allowIO(false)
                .option("js.ecmascript-version", "2021"))

    }

    fun validate(input: List<ScriptValidationInput>): List<ScriptValidationOutput> {
        val scriptParams: Bindings = engine.createBindings()
        val items = JSONArray()
        input.forEach {
            val item = JSONObject()
            it.componentInstruction.instruction.run {
                item.put("script", if (returnType == ReturnType.QlarrString && !isActive) "\"$text\"" else text)
            }
            item.put("allowedVariables", JSONArray(it.dependencies))
            items.put(item)
        }
        scriptParams["instructionList"] = items.toString()
        val result = compiledScript.eval(scriptParams).toString()
        val processed: List<List<ValidationScriptError>> =
            jacksonKtMapper.readValue(result, jacksonTypeRef<List<List<ValidationScriptError>>>())
                ?: listOf()
        return input.mapIndexed { index, scriptValidationInput ->
            ScriptValidationOutput(scriptValidationInput.componentInstruction, processed[index])
        }

    }

}