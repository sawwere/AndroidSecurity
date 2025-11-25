package com.sawwere.tageditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.sawwere.tageditor.ui.navigation.AppNavigation
import com.sawwere.tageditor.ui.theme.TagEditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TagEditorTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExifEditorApp()
                }
            }
        }
    }
}

@Composable
fun ExifEditorApp() {
    val navController = rememberNavController()

    AppNavigation(navController = navController)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TagEditorTheme {
        ExifEditorApp()
    }
}