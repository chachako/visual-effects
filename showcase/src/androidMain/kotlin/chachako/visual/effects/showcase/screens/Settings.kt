package chachako.visual.effects.showcase.screens

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.NoInspectorInfo
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnPreDraw
import chachako.visual.effects.BlurDefault
import chachako.visual.effects.BlurRenderScript
import chachako.visual.effects.VisualEffect
import chachako.visual.effects.VisualEffect.UpdatePolicy
import chachako.visual.effects.showcase.R
import chachako.visual.effects.visualEffectBehind

/**
 * @author Chachako
 */
private class State() {
  var isRealtime = false
}

/**
 * @author Chachako
 */
@Composable
fun Settings(visualEffectState: VisualEffect.State, maxHeight: Dp) {
  val context = LocalContext.current

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .height(maxHeight)
      .visualEffectBehind(
        // FIXME: Cannot share a state
//        state = visualEffectState,
        effect = VisualEffect.BlurRenderScript(context, 12f),
        updatePolicy = UpdatePolicy.RealTime,
        downscale = 16f,
        baseColor = Color.Black,
      )
      .background(Color(0xFF4B6286).copy(alpha = 0.3f))
      .padding(horizontal = 34.dp)
      .padding(bottom = 34.dp)
  ) {
    Spacer(
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(top = 16.dp, bottom = 24.dp)
        .height(6.dp)
        .fillMaxWidth(0.18f)
        .background(Color.White.copy(alpha = 0.06f), shape = CircleShape)
    )
    Header(trailingButton = {
      IconButton(onClick = {}) {
        Icon(
          imageVector = ImageVector.vectorResource(R.drawable.ic_sheet_play),
          contentDescription = "Play or pause background video",
          modifier = Modifier
            .shadow(15.dp, CircleShape)
            .size(46.dp)
            .background(Color.White, CircleShape)
            .padding(all = 12.dp)
        )
      }
    })
    Spacer(Modifier.height(36.dp))
    Spacer(
      Modifier
        .height(76.dp)
        .fillMaxWidth()
        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    )
    Spacer(Modifier.height(22.dp))
    Spacer(
      Modifier
        .fillMaxSize()
        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    )
  }
}

/**
 * @author Chachako
 */
@Composable
private fun Header(trailingButton: @Composable () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = "Real-time",
      fontSize = 36.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White,
    )
    trailingButton()
  }
}

/**
 * TODO Android Version
 *
 * @author Chachako
 */
class BlurView(private val parentCompose: ComposeView, children: @Composable () -> Unit) :
  FrameLayout(parentCompose.context) {
  init {
    addView(ComposeView(context).apply {
      setContent(children)
    }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    parentCompose.doOnPreDraw {

    }
  }
}