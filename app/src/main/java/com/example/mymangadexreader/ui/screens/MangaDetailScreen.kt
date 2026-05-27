package com.example.mymangadexreader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.mymangadexreader.data.ChapterNavigationManager
import com.example.mymangadexreader.data.model.ChapterData
import com.example.mymangadexreader.data.model.MangaData
import com.example.mymangadexreader.data.model.MangaStatus
import com.example.mymangadexreader.data.ReadingHistoryEntry
import com.example.mymangadexreader.data.ReadingHistoryManager
import com.example.mymangadexreader.data.model.buildCoverUrl
import com.example.mymangadexreader.ui.components.LanguagePickerDialog
import com.example.mymangadexreader.ui.viewmodel.MangaDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailScreen(
    mangaId: String,
    onChapterClick: (chapterId: String, chapterTitle: String) -> Unit,
    onBack: () -> Unit,
    viewModel: MangaDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLangPicker by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    LaunchedEffect(mangaId) { viewModel.loadManga(mangaId) }

    if (showLangPicker) {
        LanguagePickerDialog(
            currentLanguage = uiState.selectedLanguage,
            onLanguageSelected = { lang -> viewModel.setLanguage(lang) },
            onDismiss = { showLangPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            uiState.manga?.attributes?.getTitle() ?: "Đang tải...",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val langCode = uiState.selectedLanguage.code
                        val chapterCount = uiState.chapters.size
                        val isAllLang = langCode == "all"
                        if (!uiState.isChaptersLoading && chapterCount > 0) {
                            Text(
                                text = if (isAllLang)
                                    "$chapterCount chương"
                                else
                                    "$chapterCount chương • ${uiState.selectedLanguage.flag} ${uiState.selectedLanguage.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Status button
                    Box {
                        IconButton(onClick = { showStatusMenu = true }) {
                            if (uiState.isSettingStatus) {
                                CircularProgressIndicator(modifier = androidx.compose.ui.Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = if (uiState.mangaStatus != null) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Trạng thái đọc",
                                    tint = if (uiState.mangaStatus != null)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            Text(
                                text = "Trạng thái đọc",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = androidx.compose.ui.Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            HorizontalDivider()
                            MangaStatus.all.forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(status.emoji)
                                            Text(status.display)
                                            if (uiState.mangaStatus == status) {
                                                Spacer(modifier = androidx.compose.ui.Modifier.weight(1f))
                                                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = androidx.compose.ui.Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setMangaStatus(status)
                                        showStatusMenu = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("🚫  Xóa trạng thái", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    viewModel.setMangaStatus(null)
                                    showStatusMenu = false
                                },
                                enabled = uiState.mangaStatus != null
                            )
                        }
                    }
                    // Language picker button
                    TextButton(
                        onClick = { showLangPicker = true },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            "${uiState.selectedLanguage.flag} ${uiState.selectedLanguage.code.uppercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "Ngôn ngữ",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Lỗi không xác định",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.manga != null -> {
                    MangaDetailContent(
                        manga = uiState.manga!!,
                        chapters = uiState.chapters,
                        isChaptersLoading = uiState.isChaptersLoading,
                        selectedLanguageCode = uiState.selectedLanguage.code,
                        selectedLanguageDisplay = "${uiState.selectedLanguage.flag} ${uiState.selectedLanguage.displayName}",
                        readChapterIds = uiState.readChapterIds,
                        lastReadChapterId = uiState.lastReadChapterId,
                        lastReadChapterTitle = uiState.lastReadChapterTitle,
                        onChapterClick = onChapterClick,
                        onPickLanguage = { showLangPicker = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun MangaDetailContent(
    manga: MangaData,
    chapters: List<ChapterData>,
    isChaptersLoading: Boolean,
    selectedLanguageCode: String,
    selectedLanguageDisplay: String,
    readChapterIds: Set<String>,
    lastReadChapterId: String?,
    lastReadChapterTitle: String?,
    onChapterClick: (chapterId: String, chapterTitle: String) -> Unit,
    onPickLanguage: () -> Unit
) {
    val coverRel = manga.relationships.firstOrNull { it.type == "cover_art" }
    val coverUrl = coverRel?.attributes?.fileName?.let { buildCoverUrl(manga.id, it, 512) }
    val author = manga.relationships.firstOrNull { it.type == "author" }?.attributes?.name

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        // Cover + Info
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = manga.attributes.getTitle(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(130.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(manga.attributes.getTitle(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (author != null) {
                        Text(author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    manga.attributes.status?.let {
                        AssistChip(onClick = {}, label = { Text(it.replaceFirstChar { c -> c.uppercase() }) })
                    }
                    manga.attributes.year?.let {
                        Text("Năm: $it", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Tags
        val tags = manga.attributes.tags?.filter { it.attributes != null }
        if (!tags.isNullOrEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.take(5).forEach { tag ->
                        SuggestionChip(onClick = {}, label = {
                            Text(tag.attributes!!.getName(), style = MaterialTheme.typography.labelSmall)
                        })
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Description
        item {
            val desc = manga.attributes.getDescription()
            if (desc.isNotBlank()) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Continue reading card
        if (lastReadChapterId != null && lastReadChapterTitle != null) {
            item {
                ContinueReadingCard(
                    chapterTitle = lastReadChapterTitle,
                    onClick = {
                        val chapter = chapters.firstOrNull { it.id == lastReadChapterId } ?: return@ContinueReadingCard
                        ChapterNavigationManager.setChapterList(
                            list = chapters.map { c ->
                                ChapterNavigationManager.ChapterInfo(c.id, c.attributes.getDisplayTitle())
                            },
                            clickedChapterId = chapter.id
                        )
                        onChapterClick(chapter.id, chapter.attributes.getDisplayTitle())
                    }
                )
            }
        }

        // Chapter list header with language selector
        item {
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    val isAllLanguages = selectedLanguageCode == "all"
                    Text(
                        text = when {
                            isChaptersLoading -> "Đang tải..."
                            isAllLanguages -> "Danh sách chương"
                            else -> "${chapters.size} chương • $selectedLanguageDisplay"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Per-manga language picker
                OutlinedButton(
                    onClick = onPickLanguage,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(selectedLanguageDisplay, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
            HorizontalDivider()
        }

        // Loading chapters
        if (isChaptersLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        } else if (chapters.isEmpty()) {
            item {
                Text(
                    text = "Không có chương nào cho ngôn ngữ \"$selectedLanguageDisplay\".",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(chapters, key = { it.id }) { chapter ->
                ChapterItem(
                    chapter = chapter,
                    isRead = chapter.id in readChapterIds,
                    onClick = {
                        // Save chapter list for in-reader chapter navigation
                        ChapterNavigationManager.setChapterList(
                            list = chapters.map { c ->
                                ChapterNavigationManager.ChapterInfo(c.id, c.attributes.getDisplayTitle())
                            },
                            clickedChapterId = chapter.id
                        )
                        // Save reading history before navigating
                        ReadingHistoryManager.saveEntry(
                            ReadingHistoryEntry(
                                mangaId = manga.id,
                                mangaTitle = manga.attributes.getTitle(),
                                coverUrl = coverUrl,
                                chapterId = chapter.id,
                                chapterTitle = chapter.attributes.getDisplayTitle(),
                                totalPages = chapter.attributes.pages
                            )
                        )
                        onChapterClick(chapter.id, chapter.attributes.getDisplayTitle())
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(
    chapterTitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tiếp tục đọc",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Tiếp tục",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ChapterItem(chapter: ChapterData, isRead: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = if (isRead)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = chapter.attributes.getDisplayTitle(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isRead)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    if (isRead) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Đã đọc",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                chapter.attributes.publishAt?.let {
                    Text(
                        text = it.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "${chapter.attributes.pages} trang",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

