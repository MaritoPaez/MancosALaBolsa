package com.mancosalabolsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mancosalabolsa.ui.MancosApp
import com.mancosalabolsa.ui.theme.MancosALaBolsaV1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar el splash screen del sistema antes de super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MancosALaBolsaV1Theme {
                MancosApp()
            }
        }
    }
}
