package com.qlarr.surveyengine.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "name",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(InstructionError.ForwardDependency::class, name = "ForwardDependency"),
    JsonSubTypes.Type(InstructionError.ScriptError::class, name = "ScriptError"),
    JsonSubTypes.Type(InstructionError.InvalidSkipReference::class, name = "InvalidSkipReference"),
    JsonSubTypes.Type(InstructionError.InvalidReference::class, name = "InvalidReference"),
    JsonSubTypes.Type(InstructionError.InvalidChildReferences::class, name = "InvalidChildReferences"),
    JsonSubTypes.Type(InstructionError.DuplicateRandomGroupItems::class, name = "DuplicateRandomGroupItems"),
    JsonSubTypes.Type(InstructionError.DuplicatePriorityGroupItems::class, name = "DuplicatePriorityGroupItems"),
    JsonSubTypes.Type(InstructionError.PriorityLimitMismatch::class, name = "PriorityLimitMismatch"),
    JsonSubTypes.Type(InstructionError.PriorityGroupItemNotChild::class, name = "PriorityGroupItemNotChild"),
    JsonSubTypes.Type(InstructionError.RandomGroupItemNotChild::class, name = "RandomGroupItemNotChild"),
    JsonSubTypes.Type(InstructionError.InvalidRandomItem::class, name = "InvalidRandomItem"),
    JsonSubTypes.Type(InstructionError.InvalidPriorityItem::class, name = "InvalidPriorityItem"),
    JsonSubTypes.Type(InstructionError.InvalidInstructionInEndGroup::class, name = "InvalidInstructionInEndGroup"),
    JsonSubTypes.Type(InstructionError.SkipToEndOfEndGroup::class, name = "SkipToEndOfEndGroup"),
    JsonSubTypes.Type(InstructionError.DuplicateInstructionCode::class, name = "DuplicateInstructionCode")
)
sealed class InstructionError(val name: String = "") {

    data class ForwardDependency(val dependency: Dependency) : InstructionError("ForwardDependency")
    data class ScriptError(
        val message: String,
        val start: Int,
        val end: Int
    ): InstructionError("ScriptError")
    data class InvalidSkipReference(val component: String) : InstructionError("InvalidSkipReference")
    data object SkipToEndOfEndGroup : InstructionError("SkipToEndOfEndGroup")
    data class InvalidReference(val reference: String, val invalidComponent: Boolean) :
        InstructionError("InvalidReference")

    data class InvalidChildReferences(val children: List<String>) : InstructionError("InvalidChildReferences")
    data object PriorityLimitMismatch : InstructionError("DuplicateLimitMismatch")
    data class DuplicatePriorityGroupItems(val items: List<String>) : InstructionError("DuplicatePriorityGroupItems")
    data class PriorityGroupItemNotChild(val items: List<String>) : InstructionError("PriorityGroupItemNotChild")
    data class InvalidPriorityItem(val items: List<String>) : InstructionError("InvalidPriorityItem")
    data class InvalidRandomItem(val items: List<String>) : InstructionError("InvalidRandomItem")
    data class DuplicateRandomGroupItems(val items: List<String>) : InstructionError("DuplicateRandomGroupItems")
    data class RandomGroupItemNotChild(val items: List<String>) : InstructionError("RandomGroupItemNotChild")
    data object DuplicateInstructionCode : InstructionError("DuplicateInstructionCode")
    data object InvalidInstructionInEndGroup : InstructionError("InvalidInstructionInEndGroup")

}
