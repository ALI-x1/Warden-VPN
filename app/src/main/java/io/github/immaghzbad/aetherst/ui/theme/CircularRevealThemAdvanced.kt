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
    revealOrigin: Offset? = null,
    modifier: Modifier = Modifier,
    content: @Composable (isDarkForContent: Boolean) -> Unit
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    
    var previousIsDark by remember { mutableStateOf(isDark) }
    var currentIsDark by remember { mutableStateOf(isDark) }
    var isAnimating by remember { mutableStateOf(false) }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(isDark) {
        if (isDark != currentIsDark) {
            previousIsDark = currentIsDark
            currentIsDark = isDark
            isAnimating = true
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
            isAnimating = false
            previousIsDark = currentIsDark
        }
    }

    // نقطه شروع انیمیشن (در صورت عدم ارسال، پیش‌فرض روی سمت راست بالای صفحه یعنی محل دکمه تم قرار می‌گیرد)
    val actualOrigin = remember(revealOrigin, screenSize) {
        revealOrigin ?: if (screenSize != IntSize.Zero) {
            Offset(screenSize.width * 0.85f, screenSize.height * 0.07f)
        } else {
            Offset.Zero
        }
    }

    // محاسبه دقیق قطر صفحه تا دایره ۱۰۰٪ تمام گوشه‌ها را پوشش دهد
    val maxRadius = remember(screenSize, actualOrigin) {
        if (screenSize == IntSize.Zero) 3500f
        else {
            val maxDx = maxOf(actualOrigin.x, screenSize.width - actualOrigin.x)
            val maxDy = maxOf(actualOrigin.y, screenSize.height - actualOrigin.y)
            hypot(maxDx.toDouble(), maxDy.toDouble()).toFloat() * 1.05f
        }
    }

    val reusablePath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { screenSize = it.size }
    ) {
        // لایه پایه (تم قبلی یا تم جاری هنگام عدم اجرای انیمیشن)
        MyApplicationTheme(darkTheme = if (isAnimating) previousIsDark else currentIsDark) {
            content(if (isAnimating) previousIsDark else currentIsDark)
        }

        // لایه رویی (تم جدید که به شکل دایره بزرگ می‌شود)
        if (isAnimating) {
            val currentRadius = maxRadius * animProgress.value

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        reusablePath.reset()
                        reusablePath.addOval(
                            Rect(
                                center = actualOrigin,
                                radius = currentRadius
                            )
                        )
                        clipPath(reusablePath) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                MyApplicationTheme(darkTheme = currentIsDark) {
                    content(currentIsDark)
                }
            }
        }
    }
}
