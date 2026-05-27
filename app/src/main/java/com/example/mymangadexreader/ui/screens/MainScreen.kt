package com.example.mymangadexreader.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

data class TabItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    onMangaClick: (String) -> Unit,
    onChapterClick: (chapterId: String, chapterTitle: String) -> Unit,
    onLogout: () -> Unit
) {
    val tabs = listOf(
        TabItem("Tất cả truyện", Icons.Default.CollectionsBookmark),
        TabItem("MDLists", Icons.AutoMirrored.Filled.FormatListBulleted),
        TabItem("Thư viện", Icons.Default.LocalLibrary),
        TabItem("Đọc gần đây", Icons.Default.History),
        TabItem("Cá nhân", Icons.Default.Person)
    )
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = {
                            Text(
                                tab.title,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold
                                             else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> AllMangaTab(onMangaClick = onMangaClick)
                1 -> MdListTab(onMangaClick = onMangaClick)
                2 -> LibraryTab(onMangaClick = onMangaClick)
                3 -> RecentlyReadTab(
                    onChapterClick = onChapterClick,
                    onMangaClick = onMangaClick
                )
                4 -> ProfileTab(onLogout = onLogout)
            }
        }
    }
}
