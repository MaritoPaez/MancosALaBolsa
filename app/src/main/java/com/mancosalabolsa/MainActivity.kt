package com.mancosalabolsa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mancosalabolsa.ui.MancosApp
import com.mancosalabolsa.ui.theme.MancosALaBolsaV1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MancosALaBolsaV1Theme {
                MancosApp()
            }
        }
    }
}
