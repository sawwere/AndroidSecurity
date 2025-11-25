package com.sawwere.tageditor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sawwere.tageditor.ui.edit.EditScreen
import com.sawwere.tageditor.ui.main.MainScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Edit : Screen("edit/{imageUri}") {
        fun createRoute(imageUri: String) = "edit/$imageUri"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }

        composable(
            route = Screen.Edit.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            EditScreen(
                navController = navController,
                imageUri = imageUri
            )
        }
    }
}