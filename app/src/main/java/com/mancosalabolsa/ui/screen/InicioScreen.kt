package com.mancosalabolsa.ui.screen

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mancosalabolsa.ui.components.ShakingBagAnimation
import com.mancosalabolsa.ui.viewmodel.MancosViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InicioScreen(viewModel: MancosViewModel) {
    val jugadores by viewModel.jugadoresOrdenados.collectAsState()
    val seleccionados by viewModel.selectedPlayerIds.collectAsState()
    val resultado by viewModel.resultadoSorteo.collectAsState()
    val error by viewModel.errorSorteo.collectAsState()
    val inconsistencia by viewModel.inconsistenciaPosiciones.collectAsState()
    val sorteando by viewModel.sorteando.collectAsState()

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
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
                items(jugadores, key = { it.id }) { jugador ->
                    val isSelected = seleccionados.contains(jugador.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
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
                            onCheckedChange = null
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = jugador.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            val posText = when(jugador.posicion) {
                                "D" -> "Drive"
                                "R" -> "Revés"
                                else -> "Ambos"
                            }
                            Text(
                                text = "$posText - C${jugador.categoria}",
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
                enabled = !sorteando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B5E20)
                )
            ) {
                Icon(
                    Icons.Default.SportsTennis, 
                    contentDescription = null, 
                    tint = Color(0xFFD4F05C)
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
        }

        // Capa de animación (Overlay)
        if (sorteando) {
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    ShakingBagAnimation()
                }
            }
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
                            try {
                                val bitmap = generateShareableBitmap(context, resultado!!)
                                val file = File(context.cacheDir, "sorteo_mancos.png")
                                FileOutputStream(file).use { out ->
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                }
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                
                                val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_TEXT, "🎾 Mancos a la Bolsa - $fechaActual\n\n$resultado")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Compartir Sorteo"))
                            } catch (e: Exception) {
                                val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Mancos a la Bolsa - $fechaActual\n\n$resultado")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }
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
                        color = Color.White // Blanco sobre gris oscuro
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
                            color = Color.LightGray, // Mejor visibilidad
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0D3311), // Verde muy oscuro
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4F05C).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                resultado!!.lines().forEach { linea ->
                                    Text(
                                        text = formatResultLine(linea),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            lineHeight = 28.sp,
                                            letterSpacing = 0.5.sp
                                        ),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                containerColor = Color(0xFF2E2E2E), // Gris oscuro pro
                tonalElevation = 8.dp
            )
        }
    }
}

@Composable
fun formatResultLine(linea: String): AnnotatedString {
    val fluoGreen = Color(0xFFD4F05C)
    val celeste = Color(0xFF2196F3)
    val rojo = Color(0xFFF44336)
    val blanco = Color.White

    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = fluoGreen)) {
            var currentIndex = 0
            val regex = Regex("\\((D|R|X|Suelto/a)\\)")
            val matches = regex.findAll(linea)

            if (matches.none()) {
                append(linea)
                return@withStyle
            }

            matches.forEach { match ->
                append(linea.substring(currentIndex, match.range.first))
                val content = match.groupValues[1]
                val color = when (content) {
                    "D" -> celeste
                    "R" -> rojo
                    "X" -> blanco
                    else -> if (content.contains("Suelto")) Color.LightGray else blanco
                }
                withStyle(style = SpanStyle(color = color, fontWeight = FontWeight.ExtraBold)) {
                    append("($content)")
                }
                currentIndex = match.range.last + 1
            }
            if (currentIndex < linea.length) append(linea.substring(currentIndex))
        }
    }
}

fun generateShareableBitmap(context: android.content.Context, resultado: String): android.graphics.Bitmap {
    val titlePaint = android.text.TextPaint().apply { color = android.graphics.Color.WHITE; textSize = 48f; isAntiAlias = true; typeface = android.graphics.Typeface.DEFAULT_BOLD }
    val subtitlePaint = android.text.TextPaint().apply { color = android.graphics.Color.parseColor("#CCCCCC"); textSize = 32f; isAntiAlias = true }
    val linePaint = android.text.TextPaint().apply { color = android.graphics.Color.WHITE; textSize = 38f; isAntiAlias = true }
    
    val lines = resultado.lines()
    
    var maxLineWidth = 0f
    lines.forEach { line ->
        val w = linePaint.measureText(line)
        if (w > maxLineWidth) maxLineWidth = w
    }
    
    val subtitleWidth = subtitlePaint.measureText("Juegan primero las parejas 1 vs 2, y asi sucesivamente.")
    val titleWidth = titlePaint.measureText("Parejas Sorteadas")
    
    val contentMaxW = maxOf(maxLineWidth, subtitleWidth, titleWidth)
    val padding = 35f
    val innerPadding = 30f
    
    val width = (contentMaxW + (innerPadding * 2) + (padding * 2)).toInt().coerceIn(480, 800)
    
    val paintBg = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#2E2E2E") }
    val paintCardBg = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#0D3311"); style = android.graphics.Paint.Style.FILL; isAntiAlias = true }
    val paintCardBorder = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#D4F05C"); style = android.graphics.Paint.Style.STROKE; strokeWidth = 3f; isAntiAlias = true }
    
    val headerHeight = 150f
    val subtitleHeight = 80f
    val cardPaddingVertical = 40f * 2
    val lineHeight = 65f
    val cardContentHeight = lines.size * lineHeight + cardPaddingVertical
    val totalHeight = (headerHeight + subtitleHeight + cardContentHeight + (padding * 2)).toInt()
    
    val bitmap = android.graphics.Bitmap.createBitmap(width, totalHeight, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    canvas.drawRect(0f, 0f, width.toFloat(), totalHeight.toFloat(), paintBg)
    
    val x = padding
    var y = padding + 45f
    canvas.drawText("Parejas Sorteadas", x, y, titlePaint)
    
    y += 65f
    canvas.drawText("Juegan primero las parejas 1 vs 2, y asi sucesivamente.", x, y, subtitlePaint)
    
    y += 25f
    val cardTop = y
    val cardLeft = padding
    val cardRight = width - padding
    val cardBottom = cardTop + cardContentHeight
    
    val rectF = android.graphics.RectF(cardLeft, cardTop, cardRight, cardBottom)
    canvas.drawRoundRect(rectF, 24f, 24f, paintCardBg)
    canvas.drawRoundRect(rectF, 24f, 24f, paintCardBorder)
    
    var textY = cardTop + 40f + cardPaddingVertical / 2f
    val textX = cardLeft + innerPadding
    
    lines.forEach { line ->
        drawColoredLine(canvas, line, textX, textY, linePaint)
        textY += lineHeight
    }
    
    return bitmap
}

fun drawColoredLine(canvas: android.graphics.Canvas, line: String, startX: Float, y: Float, defaultPaint: android.text.TextPaint) {
    val fluoGreen = android.graphics.Color.parseColor("#D4F05C")
    val celeste = android.graphics.Color.parseColor("#2196F3")
    val rojo = android.graphics.Color.parseColor("#F44336")
    val blanco = android.graphics.Color.WHITE
    
    val parts = splitLineIntoColoredParts(line)
    var currentX = startX
    for (part in parts) {
        val paint = android.text.TextPaint(defaultPaint).apply {
            color = when (part.type) {
                PartType.NUMBER -> fluoGreen
                PartType.DRIVE -> celeste
                PartType.REVES -> rojo
                PartType.AMBOS -> blanco
                PartType.TEXT -> blanco
            }
        }
        canvas.drawText(part.text, currentX, y, paint)
        currentX += paint.measureText(part.text)
    }
}

enum class PartType { NUMBER, DRIVE, REVES, AMBOS, TEXT }
data class TextPart(val text: String, val type: PartType)

fun splitLineIntoColoredParts(line: String): List<TextPart> {
    val result = mutableListOf<TextPart>()
    val regex = Regex("(\\d+\\.|\\(D\\)|\\(R\\)|\\(X\\))")
    val matches = regex.findAll(line)
    var lastIndex = 0
    
    for (match in matches) {
        if (match.range.first > lastIndex) {
            val textBefore = line.substring(lastIndex, match.range.first)
            result.add(TextPart(textBefore, PartType.TEXT))
        }
        val matchStr = match.value
        val type = when {
            matchStr.matches(Regex("\\d+\\.")) -> PartType.NUMBER
            matchStr == "(D)" -> PartType.DRIVE
            matchStr == "(R)" -> PartType.REVES
            matchStr == "(X)" -> PartType.AMBOS
            else -> PartType.TEXT
        }
        result.add(TextPart(matchStr, type))
        lastIndex = match.range.last + 1
    }
    if (lastIndex < line.length) {
        result.add(TextPart(line.substring(lastIndex), PartType.TEXT))
    }
    return result
}
