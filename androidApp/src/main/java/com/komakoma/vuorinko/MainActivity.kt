package com.komakoma.vuorinko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.komakoma.vuorinko.ui.navigation.AppNavigation
import com.komakoma.vuorinko.ui.theme.VuorinkoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VuorinkoTheme {
                AppNavigation()
            }
        }
    }
}
