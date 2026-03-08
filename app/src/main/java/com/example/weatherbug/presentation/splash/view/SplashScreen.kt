package com.example.weatherbug.presentation.splash.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherbug.R
import com.example.weatherbug.presentation.splash.viewmodel.SplashNavEvent
import com.example.weatherbug.presentation.splash.viewmodel.SplashViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val SPLASH_DISPLAY_MS       = 2_400L
private const val ICON_SCALE_DURATION_MS  = 900
private const val ICON_ALPHA_DURATION_MS  = 500
private const val TITLE_DELAY_MS          = 350L
private const val TITLE_DURATION_MS       = 550
private const val TAGLINE_DELAY_MS        = 550L
private const val TAGLINE_DURATION_MS     = 500
private const val EXIT_FADE_MS            = 400


@Composable
fun SplashScreen(
    onNavigateToHome:      () -> Unit,
) {
    val viewModel: SplashViewModel = koinViewModel()
    val navEvent by viewModel.navEvent.collectAsStateWithLifecycle()

    LaunchedEffect(navEvent) {
        when (navEvent) {
            is SplashNavEvent.Idle                -> Unit
            is SplashNavEvent.NavigateToHome      -> onNavigateToHome()
        }
    }

    val iconScale = remember { Animatable(0.4f) }
    val iconAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(24f) }
    val taglineAlpha = remember { Animatable(0f) }
    val taglineOffsetY = remember { Animatable(20f) }
    val contentExitAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(
            targetValue   = 1f,
            animationSpec = keyframes {
                durationMillis = ICON_SCALE_DURATION_MS
                0.4f at 0
                1.08f at (ICON_SCALE_DURATION_MS * 0.65).toInt()
                0.98f at (ICON_SCALE_DURATION_MS * 0.85).toInt()
                1f at ICON_SCALE_DURATION_MS
            }
        )
        iconAlpha.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = ICON_ALPHA_DURATION_MS, easing = FastOutSlowInEasing)
        )

        delay(TITLE_DELAY_MS)
        coroutineScope {
            launch {
                titleAlpha.animateTo(
                    targetValue   = 1f,
                    animationSpec = tween(durationMillis = TITLE_DURATION_MS, easing = FastOutSlowInEasing)
                )
            }
            launch {
                titleOffsetY.animateTo(
                    targetValue   = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness   = Spring.StiffnessLow
                    )
                )
            }
        }

        delay(TAGLINE_DELAY_MS - TITLE_DELAY_MS)
        coroutineScope {
            launch {
                taglineAlpha.animateTo(
                    targetValue   = 1f,
                    animationSpec = tween(durationMillis = TAGLINE_DURATION_MS, easing = FastOutSlowInEasing)
                )
            }
            launch {
                taglineOffsetY.animateTo(
                    targetValue   = 0f,
                    animationSpec = tween(durationMillis = TAGLINE_DURATION_MS, easing = FastOutSlowInEasing)
                )
            }
        }

        val animTotal = TITLE_DELAY_MS + TITLE_DURATION_MS + (TAGLINE_DELAY_MS - TITLE_DELAY_MS) + TAGLINE_DURATION_MS
        val remaining = SPLASH_DISPLAY_MS - animTotal
        if (remaining > 0L) delay(remaining)

        contentExitAlpha.animateTo(
            targetValue   = 0f,
            animationSpec = tween(durationMillis = EXIT_FADE_MS, easing = FastOutSlowInEasing)
        )
        onNavigateToHome()

    }

    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val gradient = Brush.verticalGradient(
        colors = listOf(
            surface,
            surface.copy(alpha = 0.98f),
            primary.copy(alpha = 0.08f),
            surface
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(contentExitAlpha.value)
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text       = stringResource(R.string.app_name),
                style      = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color      = MaterialTheme.colorScheme.primary,
                modifier   = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffsetY.value.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text     = stringResource(R.string.splash_tagline),
                style    = MaterialTheme.typography.bodyLarge,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .alpha(taglineAlpha.value)
                    .offset(y = taglineOffsetY.value.dp)
            )
        }
    }
}
