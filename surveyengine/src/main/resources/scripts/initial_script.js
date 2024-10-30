function navigate(navigationInput) {
    qlarrVariables = {};
    var valuesKeys = Object.keys(navigationInput.values)
    var codes = navigationInput.codes;

    codes.forEach(function(componentCode) {
        eval(componentCode + " = {};");
        qlarrVariables[componentCode] = eval(componentCode);
    })
    // first we will set all the variables from DB
    valuesKeys.forEach(function(key){
        var value = navigationInput.values[key]
        var names = key.split('.')
        qlarrVariables[names[0]][names[1]] = verifyValue(names[1], value.returnType.name, value.value)
    });

    navigationInput.sequence.forEach(function(systemInstruction, index) {
        var instruction = systemInstruction.instruction
        // Then we run active instructions
        // Or Defaults if they don't have value already
        if (instruction.isActive) {
            qlarrVariables[systemInstruction.componentCode][instruction.code] = runInstruction(instruction.code, instruction.text, instruction.returnType.name)
        } else if (instruction.text != null && typeof qlarrVariables[systemInstruction.componentCode][instruction.code] === 'undefined') {
            if (instruction.returnType.name == "String") {
                var text = "\"" + instruction.text + "\""
            } else {
                var text = instruction.text
            }
            try {
                qlarrVariables[systemInstruction.componentCode][instruction.code] = JSON.parse(text)
            } catch (e) {
                //print(e)
                qlarrVariables[systemInstruction.componentCode][instruction.code] = defaultValue(instruction.code, instruction.returnType.name);
            }
        }
    })
    navigationInput.formatInstructions.forEach(function(formatInstruction, index) {
        var instruction = formatInstruction.instruction
        qlarrVariables[formatInstruction.componentCode][instruction.code] = runInstruction(instruction.code, instruction.text, "Map")
    })
    return JSON.stringify(qlarrVariables);
}

function verifyValue(code, returnTypeName, value) {
    if (isCorrectReturnType(returnTypeName, value)) {
        return value;
    } else {
        return defaultValue(code, returnTypeName);
    }
}

function runInstruction(code, instructionText, returnTypeName) {
    try {
        if (returnTypeName != "Map" && returnTypeName != "File") {
            var value = eval(instructionText);
        } else {
            eval("var value = " + instructionText + ";");
        }
        if (isCorrectReturnType(returnTypeName, value)) {
            return value;
        } else {
            return defaultValue(code, returnTypeName);
        }
    } catch (e) {
        //print(e)
        return defaultValue(code, returnTypeName);
    }
}

function isCorrectReturnType(returnTypeName, value) {
    switch (returnTypeName) {
        case "Boolean":
            return typeof value === "boolean";
            break;
        case "Date":
            return QlarrScripts.isValidSqlDateTime(value);
            break;
        case "Int":
            return typeof value === "number" && value % 1 == 0;
            break;
        case "Double":
            return typeof value === "number";
            break;
        case "List":
            return Array.isArray(value);
            break;
        case "String":
            return typeof value === "string";
            break;
        case "Map":
            return typeof value === "object";
            break;
        case "File":
            if (typeof value !== "object") {
                return false;
            } else {
                var keys = Object.keys(value);
                return keys.indexOf("filename") >= 0 && keys.indexOf("stored_filename") >= 0 && keys.indexOf("size") >= 0 && keys.indexOf("type") >= 0
            }
            break;
        default:
            return false;
    }
    return false;
}

function defaultValue(code, returnTypeName) {
    if (code == "value") {
        return undefined;
    } else if (code == "relevance" || code == "conditional_relevance" || code == "validity") {
        return true
    }
    switch (returnTypeName) {
        case "Boolean":
            return false;
            break;
        case "Date":
            return "1970-01-01 00:00:00";
            break;
        case "String":
            return "";
            break;
        case "Int":
        case "Double":
            return 0;
            break;
        case "List":
            return [];
            break;
        case "Map":
            return {};
            break;
        case "File":
            return {
                filename: "", stored_filename: "", size: 0, type: ""
            };
            break;
        default:
            return "";
    }
    return "";
}