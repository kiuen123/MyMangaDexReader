package com.example.mymangadexreader.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymangadexreader.data.model.MdListData
import com.example.mymangadexreader.ui.viewmodel.MdListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MdListTab(
    modifier: Modifier = Modifier,
    onMangaClick: (String) -> Unit,
    viewModel: MdListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle back button when a list is selected
    BackHandler(enabled = uiState.selectedList != null) {
        viewModel.clearSelectedList()
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top bar
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.selectedList != null) {
                    IconButton(onClick = { viewModel.clearSelectedList() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = uiState.selectedList!!.attributes.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        "MDLists của tôi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                // ── List overview ──────────────────────────────────────────────
                uiState.selectedList == null -> {
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        uiState.error != null -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = uiState.error ?: "Lỗi",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Button(onClick = viewModel::loadLists) { Text("Thử lại") }
                            }
                        }
                        uiState.lists.isEmpty() -> {
                            Text(
                                text = "Chưa có danh sách nào.",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            LazyColumn(contentPadding = PaddingValues(12.dp)) {
                                items(uiState.lists, key = { it.id }) { list ->
                                    MdListCard(list = list, onClick = { viewModel.selectList(list) })
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                // ── Selected list manga grid ────────────────────────────────────
                else -> {
                    when {
                        uiState.isMangaLoading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        uiState.mangaError != null -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    uiState.mangaError ?: "Lỗi",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Button(onClick = { viewModel.selectList(uiState.selectedList!!) }) {
                                    Text("Thử lại")
                                }
                            }
                        }
                        uiState.listManga.isEmpty() -> {
                            Text(
                                "Danh sách này trống.",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.listManga, key = { it.id }) { manga ->
                                    MangaCard(manga = manga, onClick = { onMangaClick(manga.id) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MdListCard(
    list: MdListData,
    onClick: () -> Unit
) {
    val isPublic = list.attributes.visibility == "public"
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isPublic) Icons.Default.Public else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isPublic) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = list.attributes.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${list.getMangaCount()} truyện • ${if (isPublic) "Công khai" else "Riêng tư"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .then(Modifier),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

