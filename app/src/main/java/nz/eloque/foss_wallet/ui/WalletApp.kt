@file:OptIn(ExperimentalMaterial3Api::class)

package nz.eloque.foss_wallet.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.shortcut.Shortcut
import nz.eloque.foss_wallet.ui.screens.AboutScreen
import nz.eloque.foss_wallet.ui.screens.PassScreen
import nz.eloque.foss_wallet.ui.screens.AddMembershipCardScreen
import nz.eloque.foss_wallet.ui.screens.EditMembershipCardScreen
import nz.eloque.foss_wallet.ui.screens.SettingsScreen
import nz.eloque.foss_wallet.ui.screens.UpdateFailureScreen
import nz.eloque.foss_wallet.ui.screens.WalletScreen
import nz.eloque.foss_wallet.ui.screens.CustomChecklistScreen
import nz.eloque.foss_wallet.ui.view.settings.SettingsViewModel
import nz.eloque.foss_wallet.ui.view.wallet.PassViewModel
import nz.eloque.foss_wallet.persistence.ThemeMode
import nz.eloque.foss_wallet.persistence.AccentColor
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

sealed class Screen(val route: String, val icon: ImageVector, @StringRes val resourceId: Int) {
    data object Wallet : Screen("wallet", Icons.Default.Wallet, R.string.wallet)
    data object About : Screen("about", Icons.Default.Info, R.string.about)
    data object Settings : Screen("settings", Icons.Default.Settings, R.string.settings)
    data object AddMembershipCard : Screen("add_membership_card", Icons.Default.Add, R.string.add_membership_card)
    data object EditMembershipCard : Screen("edit_membership_card", Icons.Default.Edit, R.string.edit)
    data object CustomChecklist : Screen("custom_checklist", Icons.Default.Edit, R.string.custom_travellist_items)
}

@Composable
fun WalletApp(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    passViewModel: PassViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val settings by settingsViewModel.uiState.collectAsState()

    val accentColor = remember(settings.accentColor, settings.customAccentColor) {
        if (settings.accentColor == AccentColor.CUSTOM && settings.customAccentColor != null) {
            try {
                Color(android.graphics.Color.parseColor(settings.customAccentColor))
            } catch (e: IllegalArgumentException) {
                Color(0xFF6200EE) // Default to purple if custom hex is invalid
            }
        } else {
            settings.accentColor.colorInt?.let { Color(it) } ?: Color(0xFF6200EE) // Default to purple
        }
    }

    val lightColors = lightColorScheme(
        primary = accentColor,
        secondary = Color(0xFF03DAC6),
        background = Color.White,
        surface = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.Black,
        onSurface = Color.Black,
    )

    val darkColors = darkColorScheme(
        primary = accentColor,
        secondary = Color(0xFF03DAC6),
        background = Color(0xFF121212),
        surface = Color(0xFF121212),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
    )

    val blackColors = darkColorScheme(
        primary = accentColor,
        secondary = Color.Black,
        background = Color.Black,
        surface = Color.Black,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White,
    )

    val colors = when (settings.themeMode) {
        ThemeMode.LIGHT -> lightColors
        ThemeMode.DARK -> darkColors
        ThemeMode.BLACK -> blackColors
    }

    MaterialTheme(
        colorScheme = colors
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Wallet.route,
                enterTransition = { slideIntoContainer(SlideDirection.Start, tween()) },
                exitTransition = { slideOutOfContainer(SlideDirection.Start, tween()) },
                popEnterTransition = { slideIntoContainer(SlideDirection.End, tween()) },
                popExitTransition = { slideOutOfContainer(SlideDirection.End, tween()) }
            ) {
                composable(Screen.Wallet.route) {
                    WalletScreen(navController, passViewModel, settingsViewModel)
                }
                composable(Screen.About.route) {
                    AboutScreen(navController)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(navController, settingsViewModel)
                }
                composable(Screen.CustomChecklist.route) {
                    CustomChecklistScreen(navController, settingsViewModel)
                }
                composable(Screen.AddMembershipCard.route) {
                    AddMembershipCardScreen(navController)
                }
                composable(
                    route = Screen.EditMembershipCard.route + "/{passId}",
                    arguments = listOf(navArgument("passId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val passId = backStackEntry.arguments?.getString("passId")!!
                    EditMembershipCardScreen(passId, navController)
                }
                composable(
                    route = "pass/{passId}",
                    deepLinks = listOf(navDeepLink {
                        uriPattern = "${Shortcut.BASE_URI}/{passId}"
                    }),
                    arguments = listOf(navArgument("passId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val passId = backStackEntry.arguments?.getString("passId")!!
                    PassScreen(passId, navController, passViewModel)
                }
                composable(
                    route = "updateFailure/{reason}/{rationale}",
                    arguments = listOf(
                        navArgument("reason") { type = NavType.StringType },
                        navArgument("rationale") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val reason = backStackEntry.arguments?.getString("reason")!!
                    val rationale = backStackEntry.arguments?.getString("rationale")!!
                    UpdateFailureScreen(reason, rationale, navController)
                }
            }
        }
    }
}
