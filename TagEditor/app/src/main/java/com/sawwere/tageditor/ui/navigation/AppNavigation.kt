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
    object Edit : Screen("edit")
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

        composable(Screen.Edit.route) {
            EditScreen(navController = navController)
        }
    }
}