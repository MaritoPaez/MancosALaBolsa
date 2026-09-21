package com.mancosalabolsa.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mancosalabolsa.data.database.AppDatabase
import com.mancosalabolsa.data.repository.MancosRepository
import com.mancosalabolsa.ui.navigation.Screen
import com.mancosalabolsa.ui.screen.ConfiguracionScreen
import com.mancosalabolsa.ui.screen.ExclusionesScreen
import com.mancosalabolsa.ui.screen.HistorialScreen
import com.mancosalabolsa.ui.screen.InicioScreen
import com.mancosalabolsa.ui.screen.JugadoresScreen
import com.mancosalabolsa.ui.screen.SplashScreen
import com.mancosalabolsa.ui.viewmodel.MancosViewModel
import com.mancosalabolsa.ui.viewmodel.MancosViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MancosApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { MancosRepository(database.mancosDao()) }
    val viewModel: MancosViewModel = viewModel(factory = MancosViewModelFactory(repository))
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isSplash = currentRoute == Screen.Splash.route

    Scaffold(
        topBar = {
            if (!isSplash) {
                TopAppBar(
                    title = { Text("Mancos a la Bolsa") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            }
        },
        bottomBar = {
            if (!isSplash) {
                NavigationBar {
                    val items = listOf(
                        Screen.Inicio to Icons.Default.Home,
                        Screen.Jugadores to Icons.Default.Person,
                        Screen.Exclusiones to Icons.Default.List,
                        Screen.Historial to Icons.Default.History,
                        Screen.Configuracion to Icons.Default.Settings
                    )
                    items.forEach { (screen, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(if (isSplash) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onFinished = {
                    navController.navigate(Screen.Inicio.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Inicio.route) {
                InicioScreen(viewModel)
            }
            composable(Screen.Jugadores.route) {
                JugadoresScreen(viewModel)
            }
            composable(Screen.Exclusiones.route) {
                ExclusionesScreen(viewModel)
            }
            composable(Screen.Historial.route) {
                HistorialScreen(viewModel)
            }
            composable(Screen.Configuracion.route) {
                ConfiguracionScreen(viewModel)
            }
        }
    }
}
