package com.example

import com.example.data.model.ActionPlan
import com.example.data.model.RiskLevel
import com.example.domain.ActionRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `action registry validates valid open_app action`() {
    val plan = ActionPlan(
      actionId = "open_app",
      title = "Open App",
      description = "Launch app",
      parameters = mapOf("app_name" to "YouTube")
    )
    val result = ActionRegistry.validatePlan(plan)
    assertTrue(result.isSuccess)
    val validated = result.getOrNull()
    assertNotNull(validated)
    assertEquals(RiskLevel.LOW, validated?.riskLevel)
  }

  @Test
  fun `action registry flags medium risk for dial phone`() {
    val plan = ActionPlan(
      actionId = "dial_phone",
      title = "Dial",
      description = "Dial",
      parameters = mapOf("target" to "1234567890")
    )
    val result = ActionRegistry.validatePlan(plan)
    assertTrue(result.isSuccess)
    val validated = result.getOrNull()
    assertEquals(RiskLevel.MEDIUM, validated?.riskLevel)
    assertTrue(validated?.requiresConfirmation == true)
  }

  @Test
  fun `action registry rejects missing required parameters`() {
    val plan = ActionPlan(
      actionId = "open_app",
      title = "Open App",
      description = "Launch app",
      parameters = emptyMap() // missing app_name
    )
    val result = ActionRegistry.validatePlan(plan)
    assertTrue(result.isFailure)
  }
}
