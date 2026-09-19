package com.example

import com.example.engine.InferenceEngine
import com.example.engine.MathEvaluator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testMathEvaluator_onePlusOne() {
    val result1 = MathEvaluator.solveMath("1+1")
    assertNotNull("1+1 should be solved", result1)
    assertTrue("Result should contain 2", result1!!.contains("2"))

    val result2 = MathEvaluator.solveMath("1 + 1")
    assertNotNull("1 + 1 should be solved", result2)
    assertTrue("Result should contain 2", result2!!.contains("2"))

    val result3 = MathEvaluator.solveMath("what is 1+1")
    assertNotNull("what is 1+1 should be solved", result3)
    assertTrue("Result should contain 2", result3!!.contains("2"))

    val result4 = MathEvaluator.solveMath("what is 1 + 1?")
    assertNotNull("what is 1 + 1? should be solved", result4)
    assertTrue("Result should contain 2", result4!!.contains("2"))
  }

  @Test
  fun testMathEvaluator_complexExpressions() {
    val mul = MathEvaluator.solveMath("25 * 4")
    assertNotNull(mul)
    assertTrue(mul!!.contains("100"))

    val div = MathEvaluator.solveMath("100 / 4")
    assertNotNull(div)
    assertTrue(div!!.contains("25"))

    val sqrt = MathEvaluator.solveMath("sqrt(16)")
    assertNotNull(sqrt)
    assertTrue(sqrt!!.contains("4"))

    val parens = MathEvaluator.solveMath("(3 + 2) * 4")
    assertNotNull(parens)
    assertTrue(parens!!.contains("20"))
  }

  @Test
  fun testInferenceEngine_responseToOnePlusOne() {
    val response = InferenceEngine.synthesizeLocalResponse(
      modelName = "Qwen 2.5 0.5B",
      prompt = "1+1",
      history = emptyList(),
      systemPrompt = "You are a helpful AI"
    )
    assertNotNull(response)
    assertTrue("Response for 1+1 must contain 2", response.contains("2"))
  }

  @Test
  fun testInferenceEngine_responseToWhatIsOnePlusOne() {
    val response = InferenceEngine.synthesizeLocalResponse(
      modelName = "Llama 3.2 1B",
      prompt = "what is 1+1",
      history = emptyList(),
      systemPrompt = "You are a helpful AI"
    )
    assertNotNull(response)
    assertTrue("Response for 'what is 1+1' must contain 2", response.contains("2"))
  }

  @Test
  fun testInferenceEngine_generalKnowledgeAndIdentity() {
    val greeting = InferenceEngine.synthesizeLocalResponse(
      modelName = "Phi-3 Mini",
      prompt = "hello",
      history = emptyList(),
      systemPrompt = "You are a helpful AI"
    )
    assertTrue(greeting.isNotBlank())

    val identity = InferenceEngine.synthesizeLocalResponse(
      modelName = "Gemma 2 2B",
      prompt = "who are you?",
      history = emptyList(),
      systemPrompt = "You are a helpful AI"
    )
    assertTrue(identity.contains("Gemma 2 2B") || identity.contains("PocketLM") || identity.contains("local"))
  }

  @Test
  fun testInferenceEngine_generateStreamReturnsTokens() = runBlocking {
    val tokens = mutableListOf<String>()
    InferenceEngine.generateStream(
      modelName = "Qwen 2.5 0.5B",
      localFilePath = "/fake/qwen.gguf",
      systemPrompt = "You are a helpful AI",
      history = emptyList(),
      prompt = "1+1",
      useGpu = true,
      threadCount = 4
    ).collect { streamToken ->
      tokens.add(streamToken.token)
    }

    val fullOutput = tokens.joinToString("")
    assertTrue("Full streamed output should be non-empty", fullOutput.isNotBlank())
    assertTrue("Streamed output for 1+1 must contain 2", fullOutput.contains("2"))
  }

  @Test
  fun testInferenceEngine_howToMakeMoneyReturnsActionableAdvice() {
    val response = InferenceEngine.synthesizeLocalResponse(
      modelName = "Phi-3 Mini (3.8B)",
      prompt = "how to make money",
      history = emptyList(),
      systemPrompt = "You are a helpful AI"
    )

    assertNotNull(response)
    // Verify it contains practical, actionable wealth building concepts
    assertTrue("Should mention skills or freelance", response.contains("Skill", ignoreCase = true) || response.contains("Freelance", ignoreCase = true))
    assertTrue("Should mention assets or invest", response.contains("Invest", ignoreCase = true) || response.contains("Asset", ignoreCase = true))
    // CRITICAL: Verify the old robotic canned template is completely absent!
    assertFalse("Must NOT contain old robotic boilerplate", response.contains("The standard methodology involves sequential stages"))
  }

  @Test
  fun testInferenceEngine_scienceAndCodingResponses() {
    val skyResponse = InferenceEngine.synthesizeLocalResponse(
      modelName = "Phi-3 Mini (3.8B)",
      prompt = "why is the sky blue",
      history = emptyList(),
      systemPrompt = "You are a helpful AI"
    )
    assertTrue("Sky response should mention Rayleigh scattering", skyResponse.contains("Rayleigh", ignoreCase = true))

    val codeResponse = InferenceEngine.synthesizeLocalResponse(
      modelName = "Phi-3 Mini (3.8B)",
      prompt = "reverse string in kotlin",
      history = emptyList(),
      systemPrompt = "You are a helpful AI"
    )
    assertTrue("Code response should contain reversed() or two-pointer", codeResponse.contains("reversed()"))
  }
}

