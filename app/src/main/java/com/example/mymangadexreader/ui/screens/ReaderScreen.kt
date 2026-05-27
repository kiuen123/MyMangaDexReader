package com.example.mymangadexreader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.mymangadexreader.ui.viewmodel.ReadingMode
import com.example.mymangadexreader.ui.viewmodel.ReaderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapterId: String,
    chapterTitle: String,
    onBack: () -> Unit,
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
                        onPageChange = { viewModel.setCurrentPage(it) }
                    )
                }

                else -> {
                    PageReadingMode(
                        pages = uiState.pages,
                        currentPage = uiState.currentPage,
                        onPageChange = { viewModel.setCurrentPage(it) }
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
    onPageChange: (Int) -> Unit
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
            ZoomablePage(url = pageUrl, pageNumber = index + 1, fillHeight = false)
        }
    }
}

// ─── Mode 2: Swipe page-by-page (LTR: vuốt trái → trang sau) ─────────────────
@Composable
private fun PageReadingMode(
    pages: List<String>,
    currentPage: Int,
    onPageChange: (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { pages.size }
    )
    val scope = rememberCoroutineScope()

    // Sync pager → ViewModel
    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pagerState.currentPage)
    }

    // Sync ViewModel → pager (external navigation)
    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(currentPage)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── HorizontalPager: vuốt trái sang phải để chuyển trang ──
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIdx ->
            ZoomablePage(
                url = pages[pageIdx],
                pageNumber = pageIdx + 1,
                fillHeight = true
            )
        }

        // ── Bottom bar: số trang + nút prev/next ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Nút trang trước
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

            // Chỉ số trang + progress bar
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Trang ${pagerState.currentPage + 1} / ${pages.size}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { if (pages.size > 1) pagerState.currentPage / (pages.size - 1f) else 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }

            // Nút trang sau
            IconButton(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1)
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                enabled = pagerState.currentPage < pages.size - 1
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Trang sau",
                    tint = if (pagerState.currentPage < pages.size - 1) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Shared zoomable page ─────────────────────────────────────────────────────
@Composable
private fun ZoomablePage(
    url: String,
    pageNumber: Int,
    fillHeight: Boolean
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }

    // Pinch-to-zoom + 2-finger pan: cooperates with HorizontalPager/LazyColumn (doesn't consume single-touch)
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        if (scale > 1f) {
            offsetX += panChange.x
            offsetY += panChange.y
        } else {
            offsetX = 0f
            offsetY = 0f
        }
    }

    // Double-tap state tracked manually
    var lastTapMillis by remember { mutableLongStateOf(0L) }
    var lastTapOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .then(if (fillHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
            // Double-tap detection on the Box — uses requireUnconsumed=false so DOWN isn't blocked
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // Wait for finger up without consuming — returns null if drag takes over
                    val up = waitForUpOrCancellation()
                    if (up != null) {
                        val now = System.currentTimeMillis()
                        val dist = (down.position - lastTapOffset).getDistance()
                        if (now - lastTapMillis < 350L && dist < 80f) {
                            up.consume()
                            if (scale > 1f) { scale = 1f; offsetX = 0f; offsetY = 0f }
                            else { scale = 2.5f }
                            lastTapMillis = 0L
                        } else {
                            lastTapMillis = now
                            lastTapOffset = down.position
                        }
                    }
                }
            }
            // Single-finger pan when zoomed in (key = scale>1f so it restarts when zoom changes)
            .pointerInput(scale > 1f) {
                if (scale > 1f) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume() // consume DOWN only when zoomed — prevents pager page flip
                        var lastPos = down.position
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (change.pressed) {
                                change.consume()
                                offsetX += change.position.x - lastPos.x
                                offsetY += change.position.y - lastPos.y
                                lastPos = change.position
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Trang $pageNumber",
            contentScale = if (fillHeight) ContentScale.Fit else ContentScale.FillWidth,
            onState = { state -> isLoading = state is AsyncImagePainter.State.Loading },
            modifier = Modifier
                .then(
                    if (fillHeight) Modifier.fillMaxSize()
                    else Modifier.fillMaxWidth().wrapContentHeight()
                )
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .transformable(state = transformableState)
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(32.dp).size(40.dp)
            )
        }
    }
}
