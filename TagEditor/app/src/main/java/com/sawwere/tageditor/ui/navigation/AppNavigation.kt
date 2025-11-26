package com.sawwere.tageditor.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sawwere.tageditor.ui.SharedState
import com.sawwere.tageditor.ui.edit.EditScreen
import com.sawwere.tageditor.ui.main.MainScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Edit : Screen("edit")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(navController = navController)
        }

        composable(Screen.Edit.route) {
            val imageUri = SharedState.selectedImageUri
            if (imageUri != null) {
                EditScreen(navController = navController, imageUri = imageUri)
            } else {
                Text("Ошибка: изображение не выбрано")
            }
        }
    }
}