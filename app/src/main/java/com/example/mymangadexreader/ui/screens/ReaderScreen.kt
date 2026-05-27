package com.example.mymangadexreader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.mymangadexreader.data.ChapterNavigationManager
import com.example.mymangadexreader.ui.viewmodel.ReadingMode
import com.example.mymangadexreader.ui.viewmodel.ReaderViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapterId: String,
    chapterTitle: String,
    onBack: () -> Unit,
    onNavigateToChapter: (chapterId: String, chapterTitle: String) -> Unit = { _, _ -> },
    viewModel: ReaderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(chapterId) {
        viewModel.loadChapter(chapterId, chapterTitle)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.chapterTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.pages.isNotEmpty()) {
                            Text(
                                text = "Trang ${uiState.currentPage + 1} / ${uiState.pages.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
                    IconButton(onClick = { viewModel.toggleReadingMode() }) {
                        Icon(
                            imageVector = if (uiState.readingMode == ReadingMode.SCROLL)
                                Icons.AutoMirrored.Filled.MenuBook
                            else
                                Icons.Default.ViewStream,
                            contentDescription = if (uiState.readingMode == ReadingMode.SCROLL)
                                "Chuyển đọc từng trang" else "Chuyển cuộn dài",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "Lỗi không xác định",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.loadChapter(chapterId, chapterTitle) }) {
                            Text("Thử lại")
                        }
                    }
                }

                uiState.pages.isEmpty() -> {
                    Text(
                        text = "Không tìm thấy trang nào.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.readingMode == ReadingMode.SCROLL -> {
                    ScrollReadingMode(
                        pages = uiState.pages,
                        hasNextChapter = uiState.hasNextChapter,
                        nextChapterTitle = uiState.nextChapterTitle,
                        onPageChange = { viewModel.setCurrentPage(it) },
                        onNextChapter = {
                            val next = ChapterNavigationManager.nextChapter ?: return@ScrollReadingMode
                            onNavigateToChapter(next.id, next.title)
                        }
                    )
                }

                else -> {
                    PageReadingMode(
                        pages = uiState.pages,
                        currentPage = uiState.currentPage,
                        hasNextChapter = uiState.hasNextChapter,
                        nextChapterTitle = uiState.nextChapterTitle,
                        onPageChange = { viewModel.setCurrentPage(it) },
                        onNextChapter = {
                            val next = ChapterNavigationManager.nextChapter ?: return@PageReadingMode
                            onNavigateToChapter(next.id, next.title)
                        }
                    )
                }
            }

            // Mode indicator badge (bottom-left)
            if (uiState.pages.isNotEmpty() && uiState.readingMode == ReadingMode.SCROLL) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewStream,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Cuộn dài",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }
        }
    }
}

// ─── Mode 1: Vertical Scroll ─────────────────────────────────────────────────
@Composable
private fun ScrollReadingMode(
    pages: List<String>,
    hasNextChapter: Boolean,
    nextChapterTitle: String,
    onPageChange: (Int) -> Unit,
    onNextChapter: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState.firstVisibleItemIndex) {
        onPageChange(listState.firstVisibleItemIndex)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(pages) { index, pageUrl ->
            PageImage(url = pageUrl, pageNumber = index + 1, fillHeight = false)
        }
        if (hasNextChapter) {
            item {
                NextChapterCard(nextChapterTitle = nextChapterTitle, onNavigate = onNextChapter)
            }
        }
    }
}

// ─── Mode 2: Swipe page-by-page với hiệu ứng slide + fade ────────────────────
@Composable
private fun PageReadingMode(
    pages: List<String>,
    currentPage: Int,
    hasNextChapter: Boolean,
    nextChapterTitle: String,
    onPageChange: (Int) -> Unit,
    onNextChapter: () -> Unit
) {
    // Tổng số "trang" = pages + 1 trang kết thúc (nếu có chương tiếp)
    val totalPageCount = if (hasNextChapter) pages.size + 1 else pages.size
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { totalPageCount }
    )
    val scope = rememberCoroutineScope()

    // Sync pager → ViewModel (chỉ sync cho trang ảnh thực, không sync trang kết thúc)
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage < pages.size) {
            onPageChange(pagerState.currentPage)
        }
    }

    // Sync ViewModel → pager (nút prev/next)
    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage && currentPage < pages.size) {
            pagerState.animateScrollToPage(currentPage)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            pageSpacing = 16.dp,
            beyondViewportPageCount = 1
        ) { pageIdx ->
            val pageOffset = (pagerState.currentPage - pageIdx) +
                    pagerState.currentPageOffsetFraction
            val scale = 1f - (pageOffset.absoluteValue * 0.12f).coerceIn(0f, 0.12f)
            val alpha = 1f - (pageOffset.absoluteValue * 0.4f).coerceIn(0f, 0.4f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha },
                contentAlignment = Alignment.Center
            ) {
                if (pageIdx < pages.size) {
                    PageImage(url = pages[pageIdx], pageNumber = pageIdx + 1, fillHeight = true)
                } else {
                    // Trang kết thúc — vuốt sang để đọc chương tiếp
                    ChapterEndPage(
                        nextChapterTitle = nextChapterTitle,
                        onNavigate = onNextChapter
                    )
                }
            }
        }

        // Bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    if (pagerState.currentPage > 0)
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                },
                enabled = pagerState.currentPage > 0
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Trang trước",
                    tint = if (pagerState.currentPage > 0) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayPage = (pagerState.currentPage + 1).coerceAtMost(pages.size)
                Text(
                    text = "Trang $displayPage / ${pages.size}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { if (pages.size > 1) (pagerState.currentPage.coerceAtMost(pages.size - 1)) / (pages.size - 1f) else 1f },
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }

            IconButton(
                onClick = {
                    if (pagerState.currentPage < totalPageCount - 1)
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                enabled = pagerState.currentPage < totalPageCount - 1
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Trang sau",
                    tint = if (pagerState.currentPage < totalPageCount - 1) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Trang kết thúc chương (PAGE mode) ───────────────────────────────────────
@Composable
private fun ChapterEndPage(
    nextChapterTitle: String,
    onNavigate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
            .clickable { onNavigate() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Đã đọc xong chương này!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
            Text(
                text = "Chương tiếp theo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = nextChapterTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = onNavigate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đọc chương tiếp theo", fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "hoặc nhấn vào bất kỳ đâu để tiếp tục",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Card chương tiếp theo (SCROLL mode) ─────────────────────────────────────
@Composable
private fun NextChapterCard(
    nextChapterTitle: String,
    onNavigate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Đã đọc xong chương này!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = nextChapterTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Button(onClick = onNavigate, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đọc chương tiếp theo")
            }
        }
    }
}

// ─── Ảnh trang đơn giản ──────────────────────────────────────────────────────
@Composable
private fun PageImage(
    url: String,
    pageNumber: Int,
    fillHeight: Boolean
) {
    var isLoading by remember { mutableStateOf(true) }

    Box(
        modifier = if (fillHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Trang $pageNumber",
            contentScale = if (fillHeight) ContentScale.Fit else ContentScale.FillWidth,
            onState = { state -> isLoading = state is AsyncImagePainter.State.Loading },
            modifier = if (fillHeight) Modifier.fillMaxSize()
                       else Modifier.fillMaxWidth().wrapContentHeight()
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(32.dp).size(40.dp)
            )
        }
    }
}
