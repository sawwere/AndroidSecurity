package com.sawwere.makeitso

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.sawwere.makeitso.ui.signin.SignInRoute
import kotlinx.serialization.Serializable

@Serializable
open class Route(
    val route: String,
    val requiresAuth: Boolean = false
) {

}

// Класс для управления навигацией с проверкой аутентификации
class AuthNavController(
    private val navController: NavHostController,
    private val viewModel: AuthViewModel,
) {
    fun navigate(route: Route,  builder: NavOptionsBuilder.() -> Unit) {
        if (route.requiresAuth && !viewModel.isAuthenticated()) {
            // Сохраняем целевой маршрут для перенаправления после логина
            //navController.currentBackStackEntry?.savedStateHandle?.set("destination_after_login", route)
            navController.navigate(SignInRoute, builder)
        } else {
            navController.navigate(route, builder)
        }
    }
//
//    fun navigate(route: String) {
//        navigate(Route.fromRoute(route))
//    }
//
//    fun popBackStack() {
//        navController.popBackStack()
//    }
//
//    fun getCurrentRoute(): Route {
//        return Route.fromRoute(navController.currentDestination?.route)
//    }
}