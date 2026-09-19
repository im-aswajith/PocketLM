package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Warning
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.HardwareDiagnosticPanel
import com.example.ui.components.HardwareTelemetryBar
import com.example.ui.components.ThinkingMessageBubble
import com.example.ui.theme.HfAmberPrimary
import com.example.ui.theme.HfCyanAccent
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val loadedModel by viewModel.loadedModel.collectAsStateWithLifecycle()
    val isLoadingModel by viewModel.isLoadingModel.collectAsStateWithLifecycle()
    val modelLoadingStatus by viewModel.modelLoadingStatus.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val downloadedModels by viewModel.downloadedModels.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val streamingResponse by viewModel.streamingResponse.collectAsStateWithLifecycle()
    val liveTelemetry by viewModel.liveTelemetry.collectAsStateWithLifecycle()
    val useGpu by viewModel.useGpuSetting.collectAsStateWithLifecycle()
    val cpuThreads by viewModel.cpuThreadSetting.collectAsStateWithLifecycle()
    val diagnostics by viewModel.realtimeDiagnostics.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showModelDropdown by remember { mutableStateOf(false) }
    var showDiagnosticsHUD by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val isModelReady = (loadedModel != null && downloadedModels.any { it.id == loadedModel?.id }) ||
            (currentSession != null && downloadedModels.any { it.id == currentSession?.modelId })

    // Auto scroll to bottom on new message, stream, or when generating/thinking starts
    LaunchedEffect(messages.size, streamingResponse, isGenerating) {
        val extraItems = (if (streamingResponse.isNotBlank()) 1 else 0) + (if (isGenerating && streamingResponse.isBlank()) 1 else 0)
        val totalCount = messages.size + extraItems
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    val suggestionChips = listOf(
        "Explain Ollama architecture on mobile",
        "Write a Kotlin Coroutine for local inference",
        "Compare CPU NEON vs Vulkan GPU inference speed",
        "What are GGUF 4-bit quantizations?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("chat_screen")
    ) {
        // Chat Header with Model Selector & Quick Actions
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Active Model Dropdown Trigger
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showModelDropdown = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("model_selector_dropdown"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLoadingModel) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = HfAmberPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Loading LLM...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HfAmberPrimary
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (loadedModel != null) StatusSuccess else HfAmberPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = loadedModel?.displayName ?: currentSession?.modelDisplayName ?: "Select Model",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Model",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showModelDropdown,
                            onDismissRequest = { showModelDropdown = false }
                        ) {
                            if (downloadedModels.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No models ready. Pull one from Hub.") },
                                    onClick = {
                                        showModelDropdown = false
                                        onNavigateToHome()
                                    }
                                )
                            } else {
                                downloadedModels.forEach { model ->
                                    val isCurrent = loadedModel?.id == model.id
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (isCurrent) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Active",
                                                        tint = StatusSuccess,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(model.displayName, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        "${model.parameterSize} • ${model.quantization}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.loadModel(model)
                                            showModelDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Action: Clear Chat
                    IconButton(
                        onClick = { viewModel.clearCurrentChat() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Real-time Diagnostic Panel toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HardwareTelemetryBar(
                        hardwareInfo = viewModel.hardwareInfo,
                        useGpu = useGpu,
                        cpuThreads = cpuThreads,
                        liveTelemetry = liveTelemetry,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showDiagnosticsHUD) HfAmberPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showDiagnosticsHUD) HfAmberPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { showDiagnosticsHUD = !showDiagnosticsHUD }
                    ) {
                        Text(
                            text = if (showDiagnosticsHUD) "Hide HUD" else "Diag HUD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (showDiagnosticsHUD) HfAmberPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                if (showDiagnosticsHUD) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HardwareDiagnosticPanel(
                        diagnostics = diagnostics,
                        modifier = Modifier.testTag("chat_diagnostic_panel")
                    )
                }
            }
        }

        if (isLoadingModel) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HfAmberPrimary.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, HfAmberPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = HfAmberPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = modelLoadingStatus.ifBlank { "Loading weights into RAM / GPU..." },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Message Thread Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (downloadedModels.isEmpty()) {
                // Mandatory Download Enforcement State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .testTag("no_downloaded_models_banner"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(HfAmberPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "No Models",
                            tint = HfAmberPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Models Downloaded Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "PocketLM requires downloading an LLM model before starting offline chat. Please download a model from the Model Manager first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onNavigateToHome,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HfAmberPrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("open_model_manager_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Model Manager", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (messages.isEmpty() && streamingResponse.isBlank() && !isGenerating) {
                // Empty State Banner & Suggestion Chips
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = HfAmberPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "100% Offline On-Device LLM",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Model: ${loadedModel?.displayName ?: currentSession?.modelDisplayName ?: "Local AI"}\nZero network requests during inference.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Suggestion Chips
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestionChips.forEach { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        inputText = suggestion
                                        viewModel.sendMessage(suggestion)
                                        inputText = ""
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = HfAmberPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        ChatMessageItem(
                            sender = msg.sender,
                            content = msg.content,
                            tokensPerSecond = msg.tokensPerSecond,
                            tokenCount = msg.tokenCount,
                            timeToFirstTokenMs = msg.timeToFirstTokenMs,
                            computeDevice = msg.computeDevice
                        )
                    }

                    // LLM Thinking Animation Bubble (before token streaming starts)
                    if (isGenerating && streamingResponse.isBlank()) {
                        item {
                            ThinkingMessageBubble(
                                modelName = loadedModel?.displayName ?: currentSession?.modelDisplayName ?: "PocketLM",
                                computeDevice = liveTelemetry?.computeDevice ?: if (useGpu) "GPU (Vulkan)" else "CPU (${cpuThreads}T)"
                            )
                        }
                    }

                    // Live Streaming Response Chunk
                    if (streamingResponse.isNotBlank()) {
                        item {
                            ChatMessageItem(
                                sender = "ASSISTANT",
                                content = streamingResponse,
                                tokensPerSecond = liveTelemetry?.tokensPerSecond ?: 0f,
                                tokenCount = liveTelemetry?.totalTokens ?: 0,
                                timeToFirstTokenMs = liveTelemetry?.timeToFirstTokenMs ?: 0L,
                                computeDevice = liveTelemetry?.computeDevice ?: "",
                                isStreaming = true
                            )
                        }
                    }
                }
            }
        }

        // Bottom Input Row & Stop Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    placeholder = {
                        Text(
                            text = when {
                                downloadedModels.isEmpty() -> "Download a model first to chat..."
                                isGenerating -> "Generating tokens..."
                                else -> "Message ${currentSession?.modelDisplayName ?: "model"}..."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    enabled = !isGenerating && downloadedModels.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HfAmberPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                if (isGenerating) {
                    // Stop generation button
                    Button(
                        onClick = { viewModel.stopGeneration() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusError,
                            contentColor = Color.White
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("stop_generation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    // Send message button
                    Button(
                        onClick = {
                            if (inputText.isNotBlank() && downloadedModels.isNotEmpty()) {
                                val text = inputText
                                inputText = ""
                                viewModel.sendMessage(text)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HfAmberPrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = CircleShape,
                        enabled = inputText.isNotBlank() && downloadedModels.isNotEmpty(),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
