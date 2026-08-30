package com.mancosalabolsa.ui.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mancosalabolsa.ui.viewmodel.MancosViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InicioScreen(viewModel: MancosViewModel) {
    val jugadores by viewModel.jugadoresActivos.collectAsState()
    val seleccionados by viewModel.selectedPlayerIds.collectAsState()
    val resultado by viewModel.resultadoSorteo.collectAsState()
    val error by viewModel.errorSorteo.collectAsState()
    val inconsistencia by viewModel.inconsistenciaPosiciones.collectAsState()
    val context = LocalContext.current

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
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = jugador.nombre,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when(jugador.posicion) {
                                "D" -> "Drive"
                                "R" -> "Revés"
                                else -> "Ambos"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when(jugador.posicion) {
                                "D" -> Color(0xFF2196F3)
                                "R" -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    }
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
            Icon(
                Icons.Default.SportsTennis, 
                contentDescription = null, 
                tint = Color(0xFFD4F05C) // Amarillo pelota de tenis para que resalte
            )
            Spacer(Modifier.width(8.dp))
            Text("A LA BOLSA", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                dismissButton = {
                    Button(
                        onClick = {
                            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Mancos a la Bolsa - $fechaActual\n\n$resultado")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir")
                    }
                },
                title = { 
                    Text(
                        "Parejas Sorteadas", 
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                text = {
                    Column {
                        if (inconsistencia) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ LA CANTIDAD DE DRIVE-REVÉS NO ES CONSISTENTE PARA ARMAR PAREJAS IDEALES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Text(
                            text = "Juegan primero las parejas 1 vs 2, y asi sucesivamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = resultado!!,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 28.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            )
        }
    }
}
