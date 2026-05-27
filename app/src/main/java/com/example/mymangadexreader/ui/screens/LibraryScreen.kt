package com.example.mymangadexreader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mymangadexreader.data.model.MangaData
import com.example.mymangadexreader.data.model.MangaStatus
import com.example.mymangadexreader.data.model.buildCoverUrl
import com.example.mymangadexreader.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTab(
    onMangaClick: (String) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Status filter chips ──
        PrimaryScrollableTabRow(
            selectedTabIndex = MangaStatus.all.indexOf(uiState.selectedStatus),
            edgePadding = 12.dp,
            divider = {}
        ) {
            MangaStatus.all.forEach { status ->
                val count = uiState.countFor(status)
                Tab(
                    selected = uiState.selectedStatus == status,
                    onClick = { viewModel.selectStatus(status) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${status.emoji} ${status.display}",
                                fontSize = 13.sp,
                                fontWeight = if (uiState.selectedStatus == status) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (count > 0) {
                                Badge { Text("$count", fontSize = 10.sp) }
                            }
                        }
                    }
                )
            }
        }

        HorizontalDivider()

        // ── Content ──
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoadingStatuses -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null && uiState.statusMap.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "Lỗi không xác định",
                            color = MaterialTheme.colorScheme.error
                        )
                        FilledTonalButton(onClick = { viewModel.loadLibrary() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Thử lại")
                        }
                    }
                }

                uiState.isLoadingManga -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.mangaList.isEmpty() && !uiState.isLoadingStatuses -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "Chưa có truyện nào",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Vào trang chi tiết truyện để thêm vào \"${uiState.selectedStatus.display}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.mangaList, key = { it.id }) { manga ->
                            LibraryMangaCard(manga = manga, onClick = { onMangaClick(manga.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryMangaCard(manga: MangaData, onClick: () -> Unit) {
    val coverRel = manga.relationships.firstOrNull { it.type == "cover_art" }
    val coverUrl = coverRel?.attributes?.fileName?.let { buildCoverUrl(manga.id, it, 256) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = coverUrl,
                contentDescription = manga.attributes.getTitle(),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            )
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = manga.attributes.getTitle(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                manga.attributes.status?.let {
                    Text(
                        text = it.replaceFirstChar { c -> c.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


