package com.mancosalabolsa.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mancosalabolsa.R

@Composable
fun ShakingBagAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(90, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val offsetY by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = (-30).dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jump"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(350.dp)
                .offset(y = offsetY)
                .rotate(rotation)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bolsa_papel),
                contentDescription = "Bolsa de papel",
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "SORTEANDO...",
            style = MaterialTheme.typography.displaySmall,
            color = Color(0xFFD4F05C),
            fontWeight = FontWeight.Black,
            letterSpacing = 6.sp
        )
    }
}
