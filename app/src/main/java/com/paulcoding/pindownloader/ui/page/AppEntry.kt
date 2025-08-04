package com.paulcoding.pindownloader.ui.page

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paulcoding.pindownloader.ui.page.home.HomePage


@Composable
fun AppEntry() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.HOME,
    ) {
        animatedComposable(Route.HOME) {
            HomePage(viewHistory = {
                navController.navigate(Route.HISTORY)
            }, navToPremium = {
                navController.navigate(Route.PREMIUM)
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
            fadeInWithBlur()
        },
        exitTransition = {
            fadeOutWithBlur()
        },
        popEnterTransition = {
            fadeInWithBlur()
        },
        popExitTransition = {
            fadeOutWithBlur()
        },
        content = content
    )
}


fun fadeInWithBlur(): EnterTransition {
//    return fadeIn(animationSpec = tween(500)) + scaleIn(animationSpec = tween(500))
    return fadeIn(animationSpec = tween(500))
}

fun fadeOutWithBlur(): ExitTransition {
    return fadeOut(animationSpec = tween(500))
}