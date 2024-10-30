package com.qlarr.surveyengine.model

data class NavigationInfo(
    val navigationIndex: NavigationIndex? = null,
    val navigationDirection: NavigationDirection = NavigationDirection.Start
)