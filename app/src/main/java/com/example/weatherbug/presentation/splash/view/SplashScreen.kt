package com.example.weatherbug.presentation.splash.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherbug.R
import com.example.weatherbug.presentation.splash.viewmodel.SplashDestination
import com.example.weatherbug.presentation.splash.viewmodel.SplashViewModel
import com.example.weatherbug.presentation.splash.viewmodel.SplashViewModelFactory
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS    = 2000L
private const val ANIM_ICON_DURATION_MS = 800
private const val ANIM_TEXT_DURATION_MS = 600
private const val ANIM_TEXT_DELAY_MS    = 300L


@Composable
fun SplashScreen(
    onNavigateToHome:      () -> Unit,
    onNavigateToMapPicker: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: SplashViewModel = viewModel(
        factory = SplashViewModelFactory(context)
    )

    val destination by viewModel.destination.collectAsStateWithLifecycle()


    LaunchedEffect(destination) {
        when (destination) {
            is SplashDestination.Idle              -> Unit
            is SplashDestination.NavigateToHome    -> onNavigateToHome()
            is SplashDestination.NavigateToMapPicker -> onNavigateToMapPicker()
        }
    }


    val iconScale = remember { Animatable(0.3f) }
    val iconAlpha = remember { Animatable(0f)   }
    val textAlpha = remember { Animatable(0f)   }


    LaunchedEffect(Unit) {

        iconScale.animateTo(
            targetValue   = 1f,
            animationSpec = tween(
                durationMillis = ANIM_ICON_DURATION_MS,
                easing         = FastOutSlowInEasing
            )
        )
        iconAlpha.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = ANIM_ICON_DURATION_MS)
        )

        delay(ANIM_TEXT_DELAY_MS)

        textAlpha.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = ANIM_TEXT_DURATION_MS)
        )

        val elapsed   = ANIM_ICON_DURATION_MS + ANIM_TEXT_DELAY_MS + ANIM_TEXT_DURATION_MS
        val remaining = SPLASH_DURATION_MS - elapsed
        if (remaining > 0L) delay(remaining)

        viewModel.decideDestination()
    }


    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // App logo
            Icon(
                painter            = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint               = Color.Unspecified,
                modifier           = Modifier
                    .size(120.dp)
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App name
            Text(
                text       = stringResource(R.string.app_name),
                style      = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
                modifier   = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text     = stringResource(R.string.splash_tagline),
                style    = MaterialTheme.typography.bodyLarge,
                color    = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}