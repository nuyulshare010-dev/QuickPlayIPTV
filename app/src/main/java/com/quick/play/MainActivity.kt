package com.quick.play

import androidx.compose.foundation.layout.WindowInsets
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quick.play.data.Channel
import com.quick.play.ui.screens.ChannelsScreen
import com.quick.play.ui.screens.PlayerScreen
import com.quick.play.ui.screens.PlaylistsScreen
import com.quick.play.ui.screens.SettingsScreen
import com.quick.play.ui.theme.AgonAppTheme
import com.quick.play.viewmodel.SettingsViewModel
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge full screen configuration
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themePref by settingsViewModel.themePreference.collectAsState()
            
            val useDarkTheme = when (themePref) {
                1 -> false // Light
                2 -> true  // Dark
                else -> isSystemInDarkTheme() // System
            }
            
            AgonAppTheme(darkTheme = useDarkTheme) {
                MainApp()
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute == "playlists" || currentRoute == "settings"

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "playlists",
                        onClick = {
                            navController.navigate("playlists") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == "settings",
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // We only apply inner padding to non-player screens so they don't overlap with bottom nav
        val isPlayer = currentRoute?.startsWith("player") == true
        val paddingModifier = if (isPlayer) Modifier else Modifier.padding(innerPadding)
        
        NavHost(
            navController = navController, 
            startDestination = "playlists",
            modifier = paddingModifier.fillMaxSize()
        ) {
            composable("playlists") {
                PlaylistsScreen(
                    onPlaylistClick = { name, url, ua ->
                        navController.navigate("channels/$name/$url/$ua")
                    }
                )
            }
            
            composable("settings") {
                SettingsScreen()
            }
            
            composable(
                route = "channels/{playlistName}/{playlistUrl}/{playlistUa}",
                arguments = listOf(
                    navArgument("playlistName") { type = NavType.StringType },
                    navArgument("playlistUrl") { type = NavType.StringType },
                    navArgument("playlistUa") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val nameB64 = backStackEntry.arguments?.getString("playlistName") ?: ""
                val urlB64 = backStackEntry.arguments?.getString("playlistUrl") ?: ""
                val uaB64 = backStackEntry.arguments?.getString("playlistUa") ?: ""
                
                val name = try { String(Base64.decode(nameB64, Base64.URL_SAFE or Base64.NO_WRAP)) } catch (e: Exception) { "" }
                val url = try { String(Base64.decode(urlB64, Base64.URL_SAFE or Base64.NO_WRAP)) } catch (e: Exception) { "" }
                val ua = try { String(Base64.decode(uaB64, Base64.URL_SAFE or Base64.NO_WRAP)) } catch (e: Exception) { "" }
                
                ChannelsScreen(
                    playlistName = name,
                    playlistUrl = url,
                    playlistUserAgent = ua,
                    onBackClick = { navController.popBackStack() },
                    onChannelClick = { channelJsonB64 ->
                        navController.navigate("player/$channelJsonB64")
                    }
                )
            }
            
            composable(
                route = "player/{channelJson}",
                arguments = listOf(
                    navArgument("channelJson") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val jsonB64 = backStackEntry.arguments?.getString("channelJson") ?: ""
                
                val json = try { String(Base64.decode(jsonB64, Base64.URL_SAFE or Base64.NO_WRAP)) } catch (e: Exception) { "" }
                val channel = try { Json.decodeFromString<Channel>(json) } catch (e: Exception) { null }
                
                if (channel != null) {
                    PlayerScreen(
                        channel = channel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
