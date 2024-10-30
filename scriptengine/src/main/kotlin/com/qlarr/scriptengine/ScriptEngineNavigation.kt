package com.qlarr.scriptengine

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.ResourceLimits
import javax.script.Bindings
import javax.script.Compilable
import javax.script.CompiledScript
import javax.script.ScriptEngine


class ScriptEngineNavigation(script: String) {

    private val compiledScript: CompiledScript

    init {
        compiledScript = (engine as Compilable).compile(
            script +
                    "\nnavigate(JSON.parse(params)) "
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

    fun navigate(script: String): String {
        val scriptParams: Bindings = engine.createBindings()
        scriptParams["params"] = script
        return compiledScript.eval(scriptParams).toString()
    }

}