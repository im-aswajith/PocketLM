package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.DownloadState
import com.example.data.local.ModelEntity
import com.example.engine.HardwareMonitor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PocketLM", appName)
  }

  @Test
  fun `verify ModelEntity metadata fields`() {
    val model = ModelEntity(
      id = "Qwen/Qwen2.5-0.5B-Instruct:qwen.gguf",
      repoId = "Qwen/Qwen2.5-0.5B-Instruct",
      fileName = "qwen.gguf",
      displayName = "Qwen 2.5 0.5B",
      author = "Qwen",
      parameterSize = "0.5B",
      quantization = "Q4_K_M",
      fileSizeBytes = 398_000_000L,
      downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct",
      localPath = "/data/user/0/models/qwen.gguf"
    )

    assertEquals("Qwen 2.5 0.5B", model.displayName)
    assertEquals("/data/user/0/models/qwen.gguf", model.localPath)
    assertEquals(398_000_000L, model.fileSizeBytes)
    assertEquals("Q4_K_M", model.quantization)
    assertEquals(DownloadState.IDLE.name, model.downloadStatus)
  }

  @Test
  fun `verify RealtimeDiagnostics returns valid metrics`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val diagnostics = HardwareMonitor.getRealtimeDiagnostics(context, isGenerating = false)

    assertTrue("CPU usage should be positive", diagnostics.cpuUsagePercent >= 0)
    assertTrue("RAM available should be positive", diagnostics.availableRamMb > 0)
    assertTrue("RAM usage percent should be in 0..100", diagnostics.ramUsagePercent in 0..100)
    assertNotNull(diagnostics.deviceAssessment)
    assertTrue(diagnostics.deviceAssessment.isNotBlank())
  }

  @Test
  fun `verify ChatMessageEntity structure`() {
    val message = ChatMessageEntity(
      sessionId = "session-123",
      sender = "USER",
      content = "Hello local model!"
    )
    assertEquals("session-123", message.sessionId)
    assertEquals("USER", message.sender)
    assertEquals("Hello local model!", message.content)
  }

  @Test
  fun `verify curated models contain popular LLMs`() {
    val context: Context = ApplicationProvider.getApplicationContext()
    val repo = com.example.data.repository.ModelRepository(
      context,
      com.example.data.local.AppDatabase.getDatabase(context).modelDao()
    )
    val curated = repo.getCuratedAndPopularModels()
    assertTrue(curated.isNotEmpty())
    assertTrue(curated.any { it.displayName.contains("Phi-3", ignoreCase = true) })
    assertTrue(curated.any { it.displayName.contains("DeepSeek", ignoreCase = true) })
    assertTrue(curated.any { it.displayName.contains("Llama", ignoreCase = true) })
  }
}
