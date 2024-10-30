package com.qlarr.surveyengine.context.execute

import com.qlarr.surveyengine.model.ReservedCode
import com.qlarr.surveyengine.dependency.toDependencyMap
import com.qlarr.surveyengine.model.Dependency
import com.qlarr.surveyengine.model.DependencyMap
import com.qlarr.surveyengine.model.Dependent
import org.junit.Assert.assertEquals
import org.junit.Test

class ContextRunnerTest {


    @Test
    fun `getSequence sorts instructions by Dependency from least dependent to most dependent`() {
        val dependencyMap: DependencyMap = mapOf(
            Dependency("Q1", ReservedCode.ConditionalRelevance) to listOf(
                Dependent("Q2", "conditional_relevance"),
                Dependent("Q3", "conditional_relevance")
            ),
            Dependency("Q2", ReservedCode.ConditionalRelevance) to listOf(
                Dependent("Q5", "conditional_relevance")
            ),
            Dependency("Q3", ReservedCode.ConditionalRelevance) to listOf(
                Dependent("Q2", "conditional_relevance"), Dependent("Q4", "conditional_relevance")
            ),
            Dependency("Q4", ReservedCode.ConditionalRelevance) to listOf(
                Dependent("Q5", "conditional_relevance")
            ),
            Dependency("Q5", ReservedCode.ConditionalRelevance) to listOf(
                Dependent("Q6", "conditional_relevance")
            ),
            Dependency("Q6", ReservedCode.ConditionalRelevance) to listOf(
                Dependent("Q12", "conditional_relevance")
            )
        ).toDependencyMap()
        val originalSequence = listOf(
            Dependency("Q12", ReservedCode.ConditionalRelevance),
            Dependency("Q11", ReservedCode.ConditionalRelevance),
            Dependency("Q6", ReservedCode.ConditionalRelevance),
            Dependency("Q1", ReservedCode.ConditionalRelevance),
            Dependency("Q4", ReservedCode.ConditionalRelevance),
            Dependency("Q3", ReservedCode.ConditionalRelevance),
            Dependency("Q5", ReservedCode.ConditionalRelevance),
            Dependency("Q2", ReservedCode.ConditionalRelevance)
        )
        val expectedSequence = listOf(
            Dependency("Q11", ReservedCode.ConditionalRelevance),
            Dependency("Q1", ReservedCode.ConditionalRelevance),
            Dependency("Q3", ReservedCode.ConditionalRelevance),
            Dependency("Q4", ReservedCode.ConditionalRelevance),
            Dependency("Q2", ReservedCode.ConditionalRelevance),
            Dependency("Q5", ReservedCode.ConditionalRelevance),
            Dependency("Q6", ReservedCode.ConditionalRelevance),
            Dependency("Q12", ReservedCode.ConditionalRelevance)
        )

        val actualSequence = dependencyMap.getSequence(originalSequence)
        assertEquals(expectedSequence, actualSequence)


        val originalSequence1 = listOf(
            Dependency("Q12", ReservedCode.ConditionalRelevance),
            Dependency("Q11", ReservedCode.ConditionalRelevance),
            Dependency("Q6", ReservedCode.ConditionalRelevance),
            Dependency("Q1", ReservedCode.ConditionalRelevance),
            Dependency("Q4", ReservedCode.ConditionalRelevance),
            Dependency("Q2", ReservedCode.ConditionalRelevance)
        )
        val expectedSequence1 = listOf(
            Dependency("Q11", ReservedCode.ConditionalRelevance),
            Dependency("Q6", ReservedCode.ConditionalRelevance),
            Dependency("Q12", ReservedCode.ConditionalRelevance),
            Dependency("Q1", ReservedCode.ConditionalRelevance),
            Dependency("Q4", ReservedCode.ConditionalRelevance),
            Dependency("Q2", ReservedCode.ConditionalRelevance)
        )
        val actualSequence1 = dependencyMap.getSequence(originalSequence1)
        assertEquals(expectedSequence1, actualSequence1)
    }

}

