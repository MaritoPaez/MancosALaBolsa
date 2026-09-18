package com.mancosalabolsa.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.*
import com.mancosalabolsa.R

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.intro_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    LaunchedEffect(progress) {
        if (progress == 1f) {
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress }
        )
    }
}
