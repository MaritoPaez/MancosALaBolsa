package com.mancosalabolsa.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mancosalabolsa.ui.viewmodel.MancosViewModel

@Composable
fun ConfiguracionScreen(viewModel: MancosViewModel) {
    var showDeleteHistoryDialog by remember { mutableStateOf(false) }
    var showResetAppDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Sección Información
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Mancos a la Bolsa", style = MaterialTheme.typography.titleMedium)
                    Text("Versión 1.0 - MVP", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección Acciones Peligrosas
        Text("Acciones", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { showDeleteHistoryDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Borrar Historial de Sorteos")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showResetAppDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reiniciar Aplicación (Borrar Todo)")
        }
    }

    // Diálogo Borrar Historial
    if (showDeleteHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteHistoryDialog = false },
            title = { Text("¿Borrar Historial?") },
            text = { Text("Se eliminarán todos los sorteos guardados. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrarHistorial()
                    showDeleteHistoryDialog = false
                }) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteHistoryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo Reiniciar App
    if (showResetAppDialog) {
        AlertDialog(
            onDismissRequest = { showResetAppDialog = false },
            title = { Text("¿Reiniciar Aplicación?") },
            text = { Text("Se borrarán TODOS los jugadores, exclusiones e historial. La aplicación quedará como nueva.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reiniciarApp()
                    showResetAppDialog = false
                }) {
                    Text("REINICIAR TODO", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAppDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
