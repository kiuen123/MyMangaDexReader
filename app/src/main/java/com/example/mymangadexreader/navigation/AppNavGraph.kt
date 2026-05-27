package com.example.mymangadexreader.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mymangadexreader.ui.screens.LoginScreen
import com.example.mymangadexreader.ui.screens.MainScreen
import com.example.mymangadexreader.ui.screens.MangaDetailScreen
import com.example.mymangadexreader.ui.screens.ReaderScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val MANGA_DETAIL = "manga/{mangaId}"
    const val READER = "reader/{chapterId}/{chapterTitle}"

    fun mangaDetail(mangaId: String) = "manga/$mangaId"
    fun reader(chapterId: String, chapterTitle: String): String {
        val encoded = URLEncoder.encode(chapterTitle, StandardCharsets.UTF_8.toString())
        return "reader/$chapterId/$encoded"
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onMangaClick = { mangaId ->
                    navController.navigate(Routes.mangaDetail(mangaId))
                },
                onChapterClick = { chapterId, chapterTitle ->
                    navController.navigate(Routes.reader(chapterId, chapterTitle))
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.MANGA_DETAIL,
            arguments = listOf(navArgument("mangaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: return@composable
            MangaDetailScreen(
                mangaId = mangaId,
                onChapterClick = { chapterId, chapterTitle ->
                    navController.navigate(Routes.reader(chapterId, chapterTitle))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.READER,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("chapterTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
            val chapterTitleEncoded = backStackEntry.arguments?.getString("chapterTitle") ?: ""
            val chapterTitle = URLDecoder.decode(chapterTitleEncoded, StandardCharsets.UTF_8.toString())
            ReaderScreen(
                chapterId = chapterId,
                chapterTitle = chapterTitle,
                onBack = { navController.popBackStack() },
                onNavigateToChapter = { newId, newTitle ->
                    // Replace current reader with new chapter (no extra back stack)
                    navController.navigate(Routes.reader(newId, newTitle)) {
                        popUpTo(Routes.READER) { inclusive = true }
                    }
                }
            )
        }
    }
}
