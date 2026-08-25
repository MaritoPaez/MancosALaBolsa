package com.mancosalabolsa.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mancosalabolsa.data.entity.Jugador
import com.mancosalabolsa.ui.viewmodel.MancosViewModel

@Composable
fun JugadoresScreen(viewModel: MancosViewModel) {
    val jugadores by viewModel.allJugadores.collectAsState()
    var nuevoNombre by remember { mutableStateOf("") }
    var jugadorParaEditar by remember { mutableStateOf<Jugador?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Formulario para añadir
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nuevoNombre,
                onValueChange = { nuevoNombre = it },
                label = { Text("Nuevo Jugador") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (nuevoNombre.isNotBlank()) {
                        viewModel.addJugador(nuevoNombre)
                        nuevoNombre = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de jugadores
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(jugadores) { jugador ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (jugador.activo) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = jugador.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (jugador.activo) Color.Unspecified else MaterialTheme.colorScheme.outline
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { jugadorParaEditar = jugador }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            
                            Switch(
                                checked = jugador.activo,
                                onCheckedChange = { 
                                    viewModel.updateJugador(jugador.copy(activo = it)) 
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de edición
    if (jugadorParaEditar != null) {
        var nombreEditado by remember { mutableStateOf(jugadorParaEditar!!.nombre) }
        AlertDialog(
            onDismissRequest = { jugadorParaEditar = null },
            title = { Text("Editar Jugador") },
            text = {
                OutlinedTextField(
                    value = nombreEditado,
                    onValueChange = { nombreEditado = it },
                    label = { Text("Nombre") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateJugador(jugadorParaEditar!!.copy(nombre = nombreEditado))
                        jugadorParaEditar = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { jugadorParaEditar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Para manejar el color que faltaba arriba si no se importa
private val Color = androidx.compose.ui.graphics.Color
