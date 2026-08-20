package io.github.immaghzbad.aetherst.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = ThemeManager.currentTheme.isDark,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // دریافت تم فعال بر اساس تنظیمات ThemeManager
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
