package com.mancosalabolsa.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mancosalabolsa.ui.viewmodel.MancosViewModel

@Composable
fun InicioScreen(viewModel: MancosViewModel) {
    val jugadores by viewModel.allJugadores.collectAsState()
    val seleccionados by viewModel.selectedPlayerIds.collectAsState()
    val resultado by viewModel.resultadoSorteo.collectAsState()
    val error by viewModel.errorSorteo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Selecciona los presentes",
                style = MaterialTheme.typography.titleLarge
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "${seleccionados.size}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(jugadores) { jugador ->
                val isSelected = seleccionados.contains(jugador.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = isSelected,
                            onValueChange = { viewModel.togglePlayerSelection(jugador.id) },
                            role = Role.Checkbox
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null // La interacción la maneja el Row completo
                    )
                    Text(
                        text = jugador.nombre,
                        modifier = Modifier.padding(start = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.realizarSorteo() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1B5E20) // Verde oscuro deportivo
            )
        ) {
            Text("🎲 A LA BOLSA", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        if (resultado != null) {
            AlertDialog(
                onDismissRequest = { viewModel.limpiarResultado() },
                confirmButton = {
                    TextButton(onClick = { viewModel.limpiarResultado() }) {
                        Text("Cerrar")
                    }
                },
                title = { Text("Resultados del Sorteo") },
                text = {
                    Text(
                        text = resultado!!,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    )
                }
            )
        }
    }
}
