package chachako.visual.effects.showcase

import android.graphics.Color.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import androidx.core.view.WindowInsetsControllerCompat
import chachako.visual.effects.rememberVisualEffectState
import chachako.visual.effects.showcase.screens.Home
import chachako.visual.effects.showcase.screens.Settings
import com.meowool.sweekt.windowRootLayout

/**
 * @author Chachako
 */
class LauncherActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setDecorFitsSystemWindows(window, false)
    setContent { Launcher() }
  }

  override fun onResume() {
    super.onResume()
    updateSystemBars()
  }

  private fun updateSystemBars() = WindowInsetsControllerCompat(window, windowRootLayout).apply {
    isAppearanceLightStatusBars = false
    isAppearanceLightNavigationBars = false
    window.statusBarColor = TRANSPARENT
    window.navigationBarColor = TRANSPARENT
  }
}

/**
 * @author Chachako
 */
@Composable
fun Launcher() = MaterialTheme(
  colors = darkColors(),
  shapes = Shapes(large = RoundedCornerShape(54.dp))
) {
  BoxWithConstraints {
    val sheetMaxHeight = maxHeight * 0.8f
    val visualEffectState = rememberVisualEffectState()

    BottomSheetScaffold(
      sheetContent = { Settings(visualEffectState, sheetMaxHeight) },
      sheetElevation = Dp.Hairline,
      sheetBackgroundColor = Color.Transparent,
      sheetContentColor = LocalContentColor.current,
      sheetShape = MaterialTheme.shapes.large.copy(bottomStart = CornerSize(0), bottomEnd = CornerSize(0)),
      sheetPeekHeight = maxHeight * 0.4f,
      modifier = Modifier.fillMaxSize(),
    ) { Home(visualEffectState) }
  }
}