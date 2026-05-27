package com.example.mymangadexreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mymangadexreader.data.ReadingHistoryManager
import com.example.mymangadexreader.data.api.TokenManager
import com.example.mymangadexreader.navigation.AppNavGraph
import com.example.mymangadexreader.ui.theme.MyMangaDexReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(applicationContext)         // load saved credentials
        ReadingHistoryManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyMangaDexReaderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph()
                }
            }
        }
    }
}
