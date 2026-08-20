package io.github.immaghzbad.aetherst.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlin.math.hypot

@Composable
fun CircularRevealDashboard(
    isDark: Boolean,
    revealOrigin: Offset,
    modifier: Modifier = Modifier,
    content: @Composable (isDarkForContent: Boolean) -> Unit
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var previousIsDark by remember { mutableStateOf(isDark) }
    var targetIsDark by remember { mutableStateOf(isDark) }
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(isDark) {
        if (isDark != targetIsDark) {
            previousIsDark = targetIsDark
            targetIsDark = isDark
            isAnimating = true
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "circular_reveal",
        finishedListener = {
            isAnimating = false
            previousIsDark = targetIsDark
        }
    )

    val maxRadius = remember(screenSize, revealOrigin) {
        if (screenSize == IntSize.Zero) 1000f
        else {
            val maxDx = maxOf(revealOrigin.x, screenSize.width - revealOrigin.x)
            val maxDy = maxOf(revealOrigin.y, screenSize.height - revealOrigin.y)
            hypot(maxDx.toDouble(), maxDy.toDouble()).toFloat()
        }
    }

    val reusablePath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { screenSize = it.size }
    ) {
        if (isAnimating) {
            AetherTheme(darkTheme = previousIsDark) {
                content(previousIsDark)
            }

            val currentRadius = maxRadius * progress
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        reusablePath.reset()
                        reusablePath.addOval(
                            Rect(
                                center = revealOrigin,
                                radius = currentRadius
                            )
                        )
                        clipPath(reusablePath) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                AetherTheme(darkTheme = targetIsDark) {
                    content(targetIsDark)
                }
            }
        } else {
            AetherTheme(darkTheme = isDark) {
                content(isDark)
            }
        }
    }
}
