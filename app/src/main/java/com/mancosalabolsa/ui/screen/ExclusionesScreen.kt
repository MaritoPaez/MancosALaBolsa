package com.mancosalabolsa.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mancosalabolsa.data.entity.Jugador
import com.mancosalabolsa.ui.viewmodel.MancosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExclusionesScreen(viewModel: MancosViewModel) {
    val jugadores by viewModel.allJugadores.collectAsState()
    val exclusiones by viewModel.allExclusiones.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedJugadorA by remember { mutableStateOf<Jugador?>(null) }
    var selectedJugadorB by remember { mutableStateOf<Jugador?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Añadir Exclusión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(exclusiones) { exclusion ->
                val jA = jugadores.find { it.id == exclusion.jugadorAId }
                val jB = jugadores.find { it.id == exclusion.jugadorBId }
                
                if (jA != null && jB != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${jA.nombre} 🚫 ${jB.nombre}")
                            IconButton(onClick = { viewModel.removeExclusion(jA.id, jB.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                selectedJugadorA = null
                selectedJugadorB = null
            },
            title = { Text("Nueva Exclusión") },
            text = {
                Column {
                    Text("Selecciona dos jugadores que no pueden estar juntos:")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Selector Jugador A
                    Text("Jugador 1:", style = MaterialTheme.typography.labelMedium)
                    JugadorDropdown(
                        jugadores = jugadores,
                        selectedJugador = selectedJugadorA,
                        onJugadorSelected = { selectedJugadorA = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector Jugador B
                    Text("Jugador 2:", style = MaterialTheme.typography.labelMedium)
                    JugadorDropdown(
                        jugadores = jugadores,
                        selectedJugador = selectedJugadorB,
                        onJugadorSelected = { selectedJugadorB = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = selectedJugadorA != null && selectedJugadorB != null && selectedJugadorA != selectedJugadorB,
                    onClick = {
                        viewModel.addExclusion(selectedJugadorA!!.id, selectedJugadorB!!.id)
                        showDialog = false
                        selectedJugadorA = null
                        selectedJugadorB = null
                    }
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDialog = false
                    selectedJugadorA = null
                    selectedJugadorB = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JugadorDropdown(
    jugadores: List<Jugador>,
    selectedJugador: Jugador?,
    onJugadorSelected: (Jugador) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedJugador?.nombre ?: "Seleccionar...",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            jugadores.forEach { jugador ->
                DropdownMenuItem(
                    text = { Text(jugador.nombre) },
                    onClick = {
                        onJugadorSelected(jugador)
                        expanded = false
                    }
                )
            }
        }
    }
}
