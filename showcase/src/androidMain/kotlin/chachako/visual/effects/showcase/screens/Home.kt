package chachako.visual.effects.showcase.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import chachako.visual.effects.BlurRenderScript
import chachako.visual.effects.VisualEffect
import chachako.visual.effects.showcase.R
import chachako.visual.effects.showcase.components.VideoPlayer
import chachako.visual.effects.showcase.components.rememberVideoPlayerState
import chachako.visual.effects.showcase.repository.VideoRepository
import chachako.visual.effects.visualEffectBehind


/**
 * @author Chachako
 */
@Composable
fun Home(visualEffectState: VisualEffect.State) = Box {
  val context = LocalContext.current
  val videos = remember(context) { VideoRepository.list(context.assets) }
  val playerState = rememberVideoPlayerState(videos)

  // TODO Mixing with TextureView/SurfaceView
//  VideoPlayer(playerState, Modifier.fillMaxSize())

  // TODO Remove: Use video instead of image
  var scale by remember { mutableStateOf(1f) }
  var rotation by remember { mutableStateOf(0f) }
  var offset by remember { mutableStateOf(Offset.Zero) }
  val transformableState = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
    scale *= zoomChange
    rotation += rotationChange
    offset += offsetChange
    visualEffectState.render()
  }

  Image(
    bitmap = ImageBitmap.imageResource(R.mipmap.img),
    modifier = Modifier
      .fillMaxSize()
      .graphicsLayer(
        scaleX = scale,
        scaleY = scale,
        rotationZ = rotation,
        translationX = offset.x,
        translationY = offset.y,
      ).transformable(transformableState),
    contentScale = ContentScale.Crop,
    contentDescription = null,
  )

  TopBar(visualEffectState)
}

@Composable
private fun TopBar(visualEffectState: VisualEffect.State) {
  val context = LocalContext.current
  // TODO Control the background video
  Row(
    Modifier
      .statusBarsPadding()
      .padding(horizontal = 34.dp)
      .fillMaxWidth()
      .height(66.dp)
      .clip(CircleShape)
      .visualEffectBehind(
//        state = visualEffectState,
        effect = VisualEffect.BlurRenderScript(context, 12f),
        updatePolicy = VisualEffect.UpdatePolicy.RealTime,
        downscale = 16f,
        baseColor = Color.Black,
      )
      .background(Color(0xFF4B6286).copy(alpha = 0.3f))
  ) {
    // TODO Add a button to play/pause the video
  }
}