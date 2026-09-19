package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DownloadState
import com.example.data.local.ModelEntity
import com.example.ui.components.HardwareDiagnosticPanel
import com.example.ui.components.ModelCard
import com.example.ui.theme.HfAmberPrimary
import com.example.ui.theme.HfCyanAccent
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModelManagerScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allModels by viewModel.allModels.collectAsStateWithLifecycle()
    val downloadedModels by viewModel.downloadedModels.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchError by viewModel.searchError.collectAsStateWithLifecycle()
    val diagnostics by viewModel.realtimeDiagnostics.collectAsStateWithLifecycle()
    val loadedModel by viewModel.loadedModel.collectAsStateWithLifecycle()
    val isLoadingModel by viewModel.isLoadingModel.collectAsStateWithLifecycle()
    val loadingTargetId by viewModel.loadingTargetId.collectAsStateWithLifecycle()
    val modelLoadingStatus by viewModel.modelLoadingStatus.collectAsStateWithLifecycle()

    var repoInput by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "downloaded"
    val keyboardController = LocalSoftwareKeyboardController.current

    val popularRepos = listOf(
        "Qwen/Qwen2.5-0.5B-Instruct",
        "meta-llama/Llama-3.2-1B-Instruct",
        "google/gemma-2-2b-it",
        "microsoft/Phi-3-mini-4k-instruct",
        "TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF"
    )

    // Compute storage used by downloaded models
    val downloadedBytesTotal = downloadedModels.sumOf { it.downloadedBytes }
    val downloadedMb = downloadedBytesTotal / (1024 * 1024)

    val displayedModels = when (selectedFilter) {
        "downloaded" -> downloadedModels
        else -> {
            // Combine any actively searched models with stored models (avoiding duplicates)
            val storedIds = allModels.map { it.id }.toSet()
            val extraSearched = searchResults.filter { it.id !in storedIds }
            extraSearched + allModels
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("model_manager_screen")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Title & Stats
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Model Manager",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = HfAmberPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HfAmberPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = HfAmberPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${downloadedModels.size} Local (${downloadedMb} MB)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HfAmberPrimary
                            )
                        }
                    }
                }

                Text(
                    text = "Download and run Hugging Face LLMs locally on device with zero telemetry.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Real-Time Hardware Diagnostic Panel
        item {
            HardwareDiagnosticPanel(
                diagnostics = diagnostics,
                modifier = Modifier.testTag("model_manager_diagnostics")
            )
        }

        // 3. Hugging Face Repo Input & Background Download Trigger
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Pull from Hugging Face",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = repoInput,
                        onValueChange = { repoInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hf_repo_input"),
                        placeholder = {
                            Text(
                                "e.g. Qwen/Qwen2.5-0.5B-Instruct",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = HfAmberPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (repoInput.isNotBlank()) {
                                IconButton(onClick = { repoInput = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            if (repoInput.isNotBlank()) {
                                keyboardController?.hide()
                                viewModel.pullModelFromRepo(repoInput)
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HfAmberPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    // Pull / Download Action Button
                    Button(
                        onClick = {
                            if (repoInput.isNotBlank()) {
                                keyboardController?.hide()
                                viewModel.pullModelFromRepo(repoInput)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pull_download_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HfAmberPrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = repoInput.isNotBlank() && !isSearching
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Inspecting Hugging Face Repo...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Pull & Download Model",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (searchError != null) {
                        Text(
                            text = searchError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Quick Repo Suggestions
                    Text(
                        text = "Popular Hugging Face Repositories:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popularRepos.forEach { repo ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.clickable {
                                    repoInput = repo
                                    keyboardController?.hide()
                                    viewModel.pullModelFromRepo(repo)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = repo.substringAfter("/"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = HfAmberPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Stored Local Models Section Header & Filter Chips
        item {
            if (isLoadingModel) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HfAmberPrimary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HfAmberPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = HfAmberPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Loading LLM into Memory...",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = HfAmberPrimary
                            )
                            Text(
                                text = modelLoadingStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else if (loadedModel != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StatusSuccess.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Active Loaded Model in Memory",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${loadedModel?.displayName} (${loadedModel?.parameterSize} • ${loadedModel?.quantization})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Button(
                            onClick = onNavigateToChat,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusSuccess,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Open Chat", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stored Local Models",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = { Text("All (${allModels.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HfAmberPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = HfAmberPrimary
                        )
                    )

                    FilterChip(
                        selected = selectedFilter == "downloaded",
                        onClick = { selectedFilter = "downloaded" },
                        label = { Text("Offline Ready (${downloadedModels.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusSuccess.copy(alpha = 0.2f),
                            selectedLabelColor = StatusSuccess
                        )
                    )
                }
            }
        }

        // Empty state for filtered models
        if (displayedModels.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = if (selectedFilter == "downloaded") "No models downloaded yet" else "No local models found",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enter a Hugging Face repository name above to download and run it offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 5. Model Cards List displaying status, progress, and the "Run & Chat" button
        items(displayedModels, key = { it.id }) { model ->
            ModelCard(
                model = model,
                onDownloadClick = { viewModel.downloadModel(model) },
                onCancelClick = { viewModel.cancelDownload(model.id) },
                onChatClick = {
                    viewModel.startChatWithModel(model) {
                        onNavigateToChat()
                    }
                },
                onDeleteClick = { viewModel.deleteModel(model) },
                isLoaded = loadedModel?.id == model.id,
                isLoading = isLoadingModel && loadingTargetId == model.id,
                modifier = Modifier.testTag("model_item_${model.id}")
            )
        }
    }
}
