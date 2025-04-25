package com.qlarr.surveyengine.model



data class NavigationUseCaseInput(
    val values: Map<String, Any> = mapOf(),
    val lang: String? = null,
    val navigationMode: NavigationMode? = null,
    val navigationInfo: NavigationInfo = NavigationInfo()
)