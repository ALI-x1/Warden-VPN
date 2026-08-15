package io.github.immaghzbad.aetherst.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import kotlin.math.hypot

/**
 * CircularRevealThemeAdvanced
 * 
 * دقیقاً مثل تلگرام: دایره‌ای از نقطه‌ی toggle شروع می‌شود و تمام صفحه را پوشش می‌دهد.
 * 
 * مثال استفاده:
 * @Composable
 * fun DashboardContent(isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
 *     var toggleButtonCenter by remember { mutableStateOf(Offset.Zero) }
 *     
 *     CircularRevealThemeAdvanced(
 *         isDark = isDarkTheme,
 *         revealOrigin = toggleButtonCenter,
 *         transitionContent = {
 *             // محتوای اصلی Dashboard
 *             DashboardBody(isDarkTheme, onToggleTheme) { center ->
 *                 toggleButtonCenter = center
 *             }
 *         }
 *     )
 * }
 */
@Composable
fun CircularRevealThemeAdvanced(
    isDark: Boolean,
    revealOrigin: Offset = Offset.Zero,
    transitionContent: @Composable () -> Unit
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var targetIsDark by remember { mutableStateOf(isDark) }
    var isAnimating by remember { mutableStateOf(false) }

    // تشخیص تغییر در isDark
    LaunchedEffect(isDark) {
        if (isDark != targetIsDark) {
            isAnimating = true
            targetIsDark = isDark
        }
    }

    // انیمیشن برای پیشرفت آشکارسازی (0 تا 1)
    val revealProgress by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "circular_reveal",
        finishedListener = { isAnimating = false }
    )

    // محاسبه‌ی radius دایره برای پوشش کل صفحه
    val screenDiagonal = hypot(screenSize.width.toDouble(), screenSize.height.toDouble())
    val maxDistanceFromCenter = hypot(
        maxOf(revealOrigin.x, screenSize.width - revealOrigin.x).toDouble(),
        maxOf(revealOrigin.y, screenSize.height - revealOrigin.y).toDouble()
    )
    val currentRadius = maxDistanceFromCenter * revealProgress

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                screenSize = layoutCoordinates.size
            }
    ) {
        // محتوای اصلی (داخل انیمیشن)
        transitionContent()

        // لایه‌ی هندسی برای رسم دایره‌ی آشکارکننده
        if (isAnimating && revealProgress < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // رسم یک مسیر دایره‌ای که کل صفحه را پوشش می‌دهد
                drawRevealCircle(
                    center = revealOrigin,
                    radius = currentRadius.toFloat(),
                    screenWidth = screenSize.width,
                    screenHeight = screenSize.height,
                    overlayColor = if (targetIsDark) Color(0xFF12141c) else Color(0xFFeef1f6),
                    progress = revealProgress
                )
            }
        }
    }
}

/**
 * توابع کمکی برای رسم دایره‌ی آشکارکننده
 */
private fun DrawScope.drawRevealCircle(
    center: Offset,
    radius: Float,
    screenWidth: Int,
    screenHeight: Int,
    overlayColor: Color,
    progress: Float
) {
    // مسیر خارجی (تمام صفحه)
    val outerPath = Path().apply {
        addRect(
            androidx.compose.ui.geometry.Rect(
                left = 0f,
                top = 0f,
                right = screenWidth.toFloat(),
                bottom = screenHeight.toFloat()
            )
        )
    }

    // مسیر دایره (منطقه‌ی آشکارشده)
    val circlePath = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = center.x - radius,
                top = center.y - radius,
                right = center.x + radius,
                bottom = center.y + radius
            )
        )
    }

    // عملیات تفاضل: تمام صفحه منهای دایره (= overlay)
    val overlayPath = Path.combine(
        androidx.compose.ui.graphics.PathOperation.Difference,
        outerPath,
        circlePath
    )

    // رسم overlay شفاف
    drawPath(
        overlayPath,
        color = overlayColor.copy(alpha = 0.95f * (1f - progress))
    )

    // optional: یک حاشیه‌ی ظریف دور دایره برای تأثیر بهتر
    drawCircle(
        color = overlayColor.copy(alpha = 0.15f * (1f - progress)),
        radius = radius,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
    )
}

/**
 * نسخه‌ی ساده‌تر با AnimatedContent
 * برای داخل DashboardScreen استفاده کنید
 */
@Composable
fun CircularRevealDashboard(
    isDark: Boolean,
    revealOrigin: Offset = Offset(0f, 0f),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var previousIsDark by remember { mutableStateOf(isDark) }
    var animating by remember { mutableStateOf(false) }

    LaunchedEffect(isDark) {
        if (isDark != previousIsDark) {
            animating = true
            previousIsDark = isDark
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (animating) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "dashboard_reveal",
        finishedListener = { animating = false }
    )

    val maxDist = if (screenSize != IntSize.Zero) {
        hypot(
            maxOf(revealOrigin.x, screenSize.width - revealOrigin.x).toDouble(),
            maxOf(revealOrigin.y, screenSize.height - revealOrigin.y).toDouble()
        )
    } else {
        1000.0
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { screenSize = it.size }
    ) {
        content()

        if (animating) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val currentRadius = (maxDist * progress).toFloat()
                val bgColor = if (isDark) Color(0xFF12141c) else Color(0xFFeef1f6)

                // رسم overlay
                drawRect(color = bgColor.copy(alpha = 0.98f * (1f - progress)))

                // رسم دایره‌ی reveal
                drawCircle(
                    color = bgColor,
                    radius = currentRadius,
                    center = revealOrigin
                )
            }
        }
    }
}
