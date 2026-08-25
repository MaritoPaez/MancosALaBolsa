package com.mancosalabolsa.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Inicio : Screen("inicio", "A la Bolsa")
    object Jugadores : Screen("jugadores", "Jugadores")
    object Exclusiones : Screen("exclusiones", "Exclusiones")
    object Historial : Screen("historial", "Historial")
    object Configuracion : Screen("configuracion", "Configuración")
}
