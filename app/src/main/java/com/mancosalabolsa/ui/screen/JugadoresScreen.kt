package com.mancosalabolsa.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mancosalabolsa.data.entity.Jugador
import com.mancosalabolsa.ui.viewmodel.MancosViewModel

@Composable
fun JugadoresScreen(viewModel: MancosViewModel) {
    val jugadores by viewModel.allJugadores.collectAsState()
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaPosicion by remember { mutableStateOf("X") }
    var nuevaCategoria by remember { mutableIntStateOf(7) }
    var jugadorParaEditar by remember { mutableStateOf<Jugador?>(null) }
    var jugadorParaEliminar by remember { mutableStateOf<Jugador?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Formulario para añadir
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevoNombre,
                        onValueChange = { nuevoNombre = it },
                        label = { Text("Nombre del Jugador") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (nuevoNombre.isNotBlank()) {
                                viewModel.addJugador(nuevoNombre, nuevaPosicion, nuevaCategoria)
                                nuevoNombre = ""
                                nuevaPosicion = "X"
                                nuevaCategoria = 7
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Posición:", style = MaterialTheme.typography.labelMedium)
                PosicionSelector(
                    selectedPosicion = nuevaPosicion,
                    onPosicionSelected = { nuevaPosicion = it }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Categoría:", style = MaterialTheme.typography.labelMedium)
                CategoriaSelector(
                    selectedCategoria = nuevaCategoria,
                    onCategoriaSelected = { nuevaCategoria = it }
                )
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
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = jugador.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (jugador.activo) Color.Unspecified else MaterialTheme.colorScheme.outline
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(
                                    containerColor = when(jugador.posicion) {
                                        "D" -> Color(0xFF2196F3) // Azul para Drive
                                        "R" -> Color(0xFFF44336) // Rojo para Revés
                                        else -> Color(0xFF9C27B0) // Púrpura para Ambos
                                    }
                                ) {
                                    Text(
                                        text = when(jugador.posicion) {
                                            "D" -> "DRIVE"
                                            "R" -> "REVÉS"
                                            else -> "AMBOS"
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(
                                    containerColor = Color(0xFF4CAF50) // Verde para Categoría
                                ) {
                                    Text(
                                        text = "C${jugador.categoria}",
                                        modifier = Modifier.padding(horizontal = 6.dp),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { jugadorParaEditar = jugador }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }

                            IconButton(onClick = { jugadorParaEliminar = jugador }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error
                                )
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
        var posicionEditada by remember { mutableStateOf(jugadorParaEditar!!.posicion) }
        var categoriaEditada by remember { mutableIntStateOf(jugadorParaEditar!!.categoria) }
        
        AlertDialog(
            onDismissRequest = { jugadorParaEditar = null },
            title = { Text("Editar Jugador") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombreEditado,
                        onValueChange = { nombreEditado = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Posición:", style = MaterialTheme.typography.labelMedium)
                    PosicionSelector(
                        selectedPosicion = posicionEditada,
                        onPosicionSelected = { posicionEditada = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Categoría:", style = MaterialTheme.typography.labelMedium)
                    CategoriaSelector(
                        selectedCategoria = categoriaEditada,
                        onCategoriaSelected = { categoriaEditada = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateJugador(jugadorParaEditar!!.copy(
                            nombre = nombreEditado,
                            posicion = posicionEditada,
                            categoria = categoriaEditada
                        ))
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

    // Diálogo de eliminación
    if (jugadorParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { jugadorParaEliminar = null },
            title = { Text("¿Eliminar Jugador?") },
            text = { Text("¿Estás seguro de que quieres eliminar a ${jugadorParaEliminar!!.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteJugador(jugadorParaEliminar!!)
                        jugadorParaEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { jugadorParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PosicionSelector(
    selectedPosicion: String,
    onPosicionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val opciones = listOf("D" to "Drive", "R" to "Revés", "X" to "Ambos")
        opciones.forEach { (key, label) ->
            FilterChip(
                selected = selectedPosicion == key,
                onClick = { onPosicionSelected(key) },
                label = { Text(label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaSelector(
    selectedCategoria: Int,
    onCategoriaSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = "C$selectedCategoria",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..9).forEach { cat ->
                DropdownMenuItem(
                    text = { Text("C$cat" + when(cat) {
                        1 -> " (Pro/Muy Fuerte)"
                        7 -> " (Normal/Default)"
                        9 -> " (Principiante)"
                        else -> ""
                    }) },
                    onClick = {
                        onCategoriaSelected(cat)
                        expanded = false
                    }
                )
            }
        }
    }
}
