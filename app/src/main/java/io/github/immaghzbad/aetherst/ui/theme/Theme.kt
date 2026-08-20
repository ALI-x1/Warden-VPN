package io.github.immaghzbad.aetherst.ui.theme

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.clipPath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import kotlin.math.hypot

@Composable
fun CircularRevealDashboard(
    isDark: Boolean,
    revealOrigin: Offset,
    modifier: Modifier = Modifier,
    content: @Composable (isDarkTheme: Boolean) -> Unit
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var previousDarkState by remember { mutableStateOf(isDark) }
    var animateToNewTheme by remember { mutableStateOf(false) }

    val revealRadius = remember { Animatable(0f) }

    val maxRadius = remember(revealOrigin, containerSize) {
        if (containerSize == IntSize.Zero) 0f
        else {
            val width = containerSize.width.toFloat()
            val height = containerSize.height.toFloat()
            val x = revealOrigin.x.coerceIn(0f, width)
            val y = revealOrigin.y.coerceIn(0f, height)

            val maxX = maxOf(x, width - x)
            val maxY = maxOf(y, height - y)
            hypot(maxX, maxY)
        }
    }

    LaunchedEffect(isDark) {
        if (isDark != previousDarkState) {
            animateToNewTheme = true
            revealRadius.snapTo(0f)
            revealRadius.animateTo(
                targetValue = maxRadius,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
            previousDarkState = isDark
            animateToNewTheme = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { containerSize = it.size }
    ) {
        // لایه پایینی (تم قبلی)
        content(if (animateToNewTheme) previousDarkState else isDark)

        // لایه بالایی (برش دایره‌ای تم جدید از نقطه لمس کاربر)
        if (animateToNewTheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val path = Path().apply {
                            addOval(
                                Rect(
                                    center = revealOrigin,
                                    radius = revealRadius.value
                                )
                            )
                        }
                        clipPath(path) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                content(isDark)
            }
        }
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = ThemeManager.currentTheme.isDark,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activeTheme = ThemeManager.currentTheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: context as? Activity
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !activeTheme.isDark
                insetsController.isAppearanceLightNavigationBars = !activeTheme.isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalAppTheme provides activeTheme
    ) {
        MaterialTheme(
            colorScheme = activeTheme.colorScheme,
            typography = Typography,
            content = content
        )
    }
}
