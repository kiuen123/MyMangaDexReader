package com.example.mymangadexreader.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mymangadexreader.data.LanguagePreference
import com.example.mymangadexreader.data.model.MangaData
import com.example.mymangadexreader.data.model.buildCoverUrl
import com.example.mymangadexreader.ui.components.LanguagePickerDialog
import com.example.mymangadexreader.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMangaTab(
    modifier: Modifier = Modifier,
    onMangaClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    var showLangPicker by remember { mutableStateOf(false) }

    // Language picker dialog
    if (showLangPicker) {
        LanguagePickerDialog(
            currentLanguage = uiState.selectedLanguage,
            onLanguageSelected = { lang ->
                LanguagePreference.setLanguage(lang)
            },
            onDismiss = { showLangPicker = false }
        )
    }

    val shouldLoadMore by remember(uiState.mangaList.size, uiState.hasMore, uiState.isLoadingMore) {
        derivedStateOf {
            val totalItems = uiState.mangaList.size
            val visibleItems = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            visibleItems >= totalItems - 4 && totalItems > 0 && uiState.hasMore && !uiState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Inline top bar
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Tất cả truyện",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    // Language button
                    FilledTonalButton(
                        onClick = { showLangPicker = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${uiState.selectedLanguage.flag} ${uiState.selectedLanguage.displayName}",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "Chọn ngôn ngữ",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = { Text("Tìm kiếm truyện...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading && uiState.mangaList.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null && uiState.mangaList.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "Lỗi không xác định",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = viewModel::retry) { Text("Thử lại") }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        state = gridState,
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.mangaList, key = { it.id }) { manga ->
                            MangaCard(manga = manga, onClick = { onMangaClick(manga.id) })
                        }
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onMangaClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    AllMangaTab(onMangaClick = onMangaClick, viewModel = viewModel)
}

@Composable
internal fun MangaCard(
    manga: MangaData,
    onClick: () -> Unit
) {
    val coverRel = manga.relationships.firstOrNull { it.type == "cover_art" }
    val coverUrl = coverRel?.attributes?.fileName?.let { buildCoverUrl(manga.id, it) }
    val selectedLang = LanguagePreference.selectedLanguage
    val availableLangs = manga.attributes.availableTranslatedLanguages
        ?.filterNotNull() ?: emptyList()
    val lastChapter = manga.attributes.lastChapter

    // When a specific language is selected the list is already API-filtered by that
    // language, so we can always show its flag. Only "all" uses availableTranslatedLanguages.
    val langFlag: String? = if (selectedLang.code == "all") null else selectedLang.flag

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            AsyncImage(
                model = coverUrl,
                contentDescription = manga.attributes.getTitle(),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = manga.attributes.getTitle(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Chapter + language row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (!lastChapter.isNullOrBlank()) {
                        Text(
                            text = "Ch.$lastChapter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (selectedLang.code == "all") {
                        // Show up to 4 flag emojis of available languages
                        val flags = availableLangs.take(4).mapNotNull { code ->
                            LanguagePreference.supportedLanguages
                                .firstOrNull { it.code == code }?.flag
                        }
                        if (flags.isNotEmpty()) {
                            Text(
                                text = flags.joinToString(""),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    } else if (langFlag != null) {
                        Text(
                            text = langFlag,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    manga.attributes.status?.let { status ->
                        if (lastChapter.isNullOrBlank() && langFlag == null) {
                            Text(
                                text = status.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                // Show status only when chapter info is present
                if (!lastChapter.isNullOrBlank() || langFlag != null) {
                    manga.attributes.status?.let { status ->
                        Text(
                            text = status.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

    }
}

