package com.qlarr.surveyengine.usecase

import com.fasterxml.jackson.databind.node.ObjectNode
import com.qlarr.surveyengine.model.ComponentIndex

data class DesignerInput(
    val state: ObjectNode,
    val componentIndexList: List<ComponentIndex>
)