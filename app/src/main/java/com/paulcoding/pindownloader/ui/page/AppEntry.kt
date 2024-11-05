package com.paulcoding.pindownloader.ui.page

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paulcoding.pindownloader.MainViewModel
import com.paulcoding.pindownloader.ui.page.home.HomePage


@Composable
fun AppEntry(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.HOME,
    ) {
        animatedComposable(Route.HOME) {
            HomePage(viewModel = viewModel, viewHistory = {
                navController.navigate(Route.HISTORY)
            })
        }

        animatedComposable(Route.HISTORY) {
            HistoryPage(goBack = { navController.popBackStack() })
        }
    }
}

fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            slideInVertically(
                initialOffsetY = { 1000 },
                animationSpec = tween(durationMillis = 500)
            )
        },
        exitTransition = {
            slideOutVertically(
                targetOffsetY = { -1000 },
                animationSpec = tween(durationMillis = 500)
            )
        },
        popEnterTransition = {
            slideInVertically(
                initialOffsetY = { -1000 },
                animationSpec = tween(durationMillis = 500)
            )
        },
        popExitTransition = {
            slideOutVertically(
                targetOffsetY = { 1000 },
                animationSpec = tween(durationMillis = 500)
            )
        },
        content = content
    )
}