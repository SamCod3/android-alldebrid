package com.samcod3.alldebrid.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.samcod3.alldebrid.R
import com.samcod3.alldebrid.ui.screens.devices.DevicesScreen
import com.samcod3.alldebrid.ui.screens.downloads.DownloadsScreen
import com.samcod3.alldebrid.ui.screens.login.ApiKeyManagerScreen
import com.samcod3.alldebrid.ui.screens.login.IpAuthorizationScreen
import com.samcod3.alldebrid.ui.screens.login.WebLoginScreen
import com.samcod3.alldebrid.ui.screens.search.SearchScreen
import com.samcod3.alldebrid.ui.screens.settings.SettingsScreen

sealed class Screen(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Downloads : Screen(
        route = "downloads",
        titleResId = R.string.nav_downloads,
        selectedIcon = Icons.Filled.Download,
        unselectedIcon = Icons.Outlined.Download
    )
    
    data object Search : Screen(
        route = "search",
        titleResId = R.string.nav_search,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )
    
    data object Devices : Screen(
        route = "devices",
        titleResId = R.string.nav_devices,
        selectedIcon = Icons.Filled.Devices,
        unselectedIcon = Icons.Outlined.Devices
    )
    
    data object Settings : Screen(
        route = "settings",
        titleResId = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

// Routes for login/key management (not in bottom nav)
const val WEB_LOGIN_ROUTE = "web_login"
const val API_KEY_MANAGER_ROUTE = "api_key_manager"
const val IP_AUTHORIZATION_ROUTE = "ip_authorization"

val bottomNavItems = listOf(
    Screen.Downloads,
    Screen.Search,
    Screen.Devices,
    Screen.Settings
)

// Routes that should hide the bottom bar
private val fullScreenRoutes = listOf(WEB_LOGIN_ROUTE, API_KEY_MANAGER_ROUTE)

@Composable
fun AllDebridNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Determine if we should show the bottom bar
    val showBottomBar = currentDestination?.route !in fullScreenRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = stringResource(screen.titleResId)
                                )
                            },
                            label = { Text(stringResource(screen.titleResId)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Downloads.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Downloads.route) {
                DownloadsScreen(
                    onNavigateToIpAuth = {
                        navController.navigate(IP_AUTHORIZATION_ROUTE)
                    },
                    onNavigateToDevices = {
                        navController.navigate(Screen.Devices.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen()
            }
            composable(Screen.Devices.route) {
                DevicesScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToApiKeyManager = {
                        navController.navigate(API_KEY_MANAGER_ROUTE)
                    }
                )
            }
            composable(API_KEY_MANAGER_ROUTE) {
                ApiKeyManagerScreen(
                    onKeySelected = { _ ->
                        // Key selected, can navigate back
                    },
                    onNavigateToLogin = {
                        navController.navigate(WEB_LOGIN_ROUTE)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(WEB_LOGIN_ROUTE) {
                WebLoginScreen(
                    onApiKeyExtracted = { _ ->
                        // After login, go back to API key manager
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(IP_AUTHORIZATION_ROUTE) {
                IpAuthorizationScreen(
                    onComplete = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
