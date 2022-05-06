/*
 * Copyright (c) 2022. Chachako
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * In addition, if you fork this project, your forked code file must contain
 * the URL of the original project: https://github.com/chachako/visual-effects
 */
@file:Suppress("FunctionName", "NOTHING_TO_INLINE", "NAME_SHADOWING")

package chachako.visual.effects

import android.content.Context
import android.os.Build
import android.view.Choreographer
import android.view.View
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.toAndroidTileMode
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.debugInspectorInfo
import chachako.visual.effects.internal.BlurVisualEffectImpl
import chachako.visual.effects.internal.SnapshotBitmap
import com.meowool.sweekt.findWindow
import kotlin.math.max
import chachako.visual.effects.VisualEffect.UpdatePolicy.Once as UpdateOnce

/**
 * Creates a default visual blur effect on the Android platform.
 *
 * ## Important
 *
 * This uses [VisualEffect.Companion.BlurRenderEffect] for Android API levels greater than `31`
 * and [VisualEffect.Companion.BlurRenderScript] for APIs below that level, so the x-axis and
 * y-axis radius are the same on lower versions, and the maximum radius is `25`, even if the
 * incoming radius is larger than it.
 *
 * ## Example
 *
 * ```
 * var bitmap: Bitmap? by remember { null }
 * val context = LocalContext.current
 * val blur = remember(context) {
 *   VisualEffect.BlurDefault(context, radiusX = 5f, radiusY = 5f)
 * }
 *
 * // Once the bitmap is changed we can render the blur effect to it
 * LaunchedEffect(bitmap) {
 *   blur.render(bitmap)
 * }
 *
 * Disposable(blur) {
 *   onDispose { blur.destroy() }
 * }
 * ```
 *
 * @param context The context for creating the effect instance.
 * @param radiusX Radius of the blur along the x-axis, `0` means no blur effect.
 * @param radiusY Radius of the blur along the y-axis, `0` means no blur effect.
 * @param edgeTreatment Policy for how to blur content near edges of the blur kernel.
 *
 * @author Chachako
 */
fun VisualEffect.Companion.BlurDefault(
  context: Context,
  @FloatRange(from = .0) radiusX: Float,
  @FloatRange(from = .0) radiusY: Float = radiusX,
  edgeTreatment: TileMode = TileMode.Mirror,
): BlurVisualEffect = when {
  // FIXME: Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> VisualEffect.BlurRenderEffect(radiusX, radiusY, edgeTreatment)
  else -> VisualEffect.BlurRenderScript(context, max(radiusX, radiusY).coerceAtMost(25f))
}

/**
 * Creates a visual blur effect wrapped around [android.renderscript.ScriptIntrinsicBlur].
 *
 * ## Disclaimer
 *
 * This is the default visual blur effect on Android platform with API levels below `31`.
 * However, please note that android has announced the deprecation of the "RenderScript" api,
 * and while it is still available, we recommend that you use [VisualEffect.Companion.BlurVulkan]
 * or [VisualEffect.Companion.BlurToolkit], which also is Android's migration suggestion, see
 * [Migrate from RenderScript](https://developer.android.com/guide/topics/renderscript/migrate)
 * for more details.
 *
 * ## Example
 *
 * ```
 * var bitmap: Bitmap? by remember { null }
 * val context = LocalContext.current
 * val blur = remember(context) {
 *   VisualEffect.BlurRenderScript(context, radius = 5f)
 * }
 *
 * // Once the bitmap is changed we can render the blur effect to it
 * LaunchedEffect(bitmap) {
 *   blur.render(bitmap)
 * }
 *
 * Disposable(blur) {
 *   onDispose { blur.destroy() }
 * }
 * ```
 *
 * @param context The context for creating the [android.renderscript.RenderScript] instance.
 * @param radius The radius of the blur, `0` means no blur effect.
 *
 * @author Chachako
 */
inline fun VisualEffect.Companion.BlurRenderScript(
  context: Context,
  @FloatRange(from = .0, to = 25.0) radius: Float,
): BlurVisualEffect = BlurVisualEffectImpl.RenderScript(context, radius)

/**
 * Creates a visual blur effect wrapped around [android.graphics.RenderEffect].
 *
 * The render effect was introduced after Android version `12`, it is also the renderer returned
 * by [VisualEffect.Companion.BlurDefault] after Android API level `31`.
 *
 * ## Example
 *
 * ```
 * var bitmap: Bitmap? by remember { null }
 * val blur = remember { VisualEffect.BlurRenderEffect(radiusX = 5f, radiusY = 5f) }
 *
 * // Once the bitmap is changed we can render the blur effect to it
 * LaunchedEffect(bitmap) {
 *   blur.render(bitmap)
 * }
 *
 * Disposable(blur) {
 *   onDispose { blur.destroy() }
 * }
 * ```
 *
 * @author Chachako
 */
@RequiresApi(Build.VERSION_CODES.S)
inline fun VisualEffect.Companion.BlurRenderEffect(
  radiusX: Float,
  radiusY: Float,
  edgeTreatment: TileMode = TileMode.Mirror,
): BlurVisualEffect = BlurVisualEffectImpl.RenderEffect(radiusX, radiusY, edgeTreatment.toAndroidTileMode())

/**
 * Add a specified visual [effect] to the content behind.
 *
 * ## Suggestions
 *
 * - For certain visual effects that do not require clear content, you should always optimize
 *   rendering efficiency in advance by passing a suitable [downscale] value, especially in
 *   scenes that require real-time rendering.
 *
 * - In any case, the [behindView] argument passed in should always be the closest possible root
 *   layout to hierarchy of the component that uses this modifier. This will greatly reduce the
 *   amount of work required to create a snapshot of the contents behind.
 *
 *    For example, if the view passed in is a root layout of the window (like [Window.getDecorView]),
 *    it will need to draw the entire window's contents to the canvas before rendering, which
 *    will add a lot of unnecessary rendering overhead.
 *
 * ## Example
 *
 * Usage of rendering a blur effect:
 * ```
 * val context = LocalContext.current
 * val blur = remember { VisualEffect.BlurDefault(context, radiusX = 5f, radiusY = 5f) }
 *
 * Box {
 *   // Backgrounds are here
 *   ...
 *   Text("Hello Blur", modifier = Modifier.visualEffectBehind(blur, downscale = 4f))
 * }
 * ```
 *
 * @param effect The visual effect to render.
 *
 * @param downscale Backdrop downscaling value, which is useful for improving the rendering
 *   performance of certain effects such as blurring and pixelation. The higher the value,
 *   the more efficient the rendering, `1` means no downscaling.
 *
 * @param alpha The alpha value of the effect between `0f` to `1f`, which does not affect
 *   the [baseColor].
 *
 * @param baseColor The base color of the effect, which is the background color of the
 *   rendered bitmap is drawing.
 *
 * @param state The state for observing or changing the visual effect. If it is `null`,
 *   a default state will be created and remembered (via [rememberVisualEffectState]).
 *
 * @param updatePolicy The update policy for the effect, the value should always be passed
 *   [VisualEffect.UpdatePolicy.Once] whenever possible and manually modify the [state] to
 *   trigger the recomposition only when needed to reduce the application's overhead.
 *
 * @param behindView The view that is behind the content, or if `null`, the view of Compose
 *   will be used, which is `LocalView.current`.
 *
 * @author Chachako
 */
fun Modifier.visualEffectBehind(
  effect: VisualEffect,
  alpha: Float = 1f,
  @FloatRange(from = 1.0) downscale: Float = 1f,
  baseColor: Color = Color.Transparent,
  state: VisualEffect.State? = null,
  updatePolicy: VisualEffect.UpdatePolicy = UpdateOnce,
  behindView: View? = null,
): Modifier = composed(
  inspectorInfo = debugInspectorInfo {
    name = "visualEffectBehind"
    properties["effect"] = effect
    properties["downscale"] = downscale
    properties["behindView"] = behindView
  },
  factory = {
    val view = behindView ?: LocalView.current
    val rootView = remember(view) { view.context.findWindow()?.decorView ?: view.rootView }
    val bitmap = remember(effect, downscale) { SnapshotBitmap(effect, downscale, alpha, baseColor) }
    val state = state ?: rememberVisualEffectState(true)

    val drawModifier = Modifier.drawWithContent {
      // Skip drawing any content for this component while shooting,
      //   as we only need the content behind it.
      if (!bitmap.shooting) {
        bitmap.drawTo(drawContext)
        drawContent()
      }
    }

    // FIXME No updates received from other composable
    LaunchedEffect(state.isRendering) {
      if (state.isRendering) {
        bitmap.snapshotFrom(view)
        state.isRendering = false
      }
    }

    DisposableEffect(bitmap) {
      onDispose { bitmap.destroy() }
    }

    when (updatePolicy) {
      VisualEffect.UpdatePolicy.Once -> {
        var initialized by remember { mutableStateOf(false) }

        // Render only once
        drawModifier.onGloballyPositioned {
          if (!initialized) {
            bitmap.updateBounds(it.boundsInWindow())
            state.isRendering = true
            initialized = true
          }
        }
      }

      VisualEffect.UpdatePolicy.RealTime -> drawModifier.onGloballyPositioned {
        // We update once the content bounds changes
        bitmap.updateBounds(it.boundsInWindow())

//        // FIXME: Other composable 'graphicsLayers' do not cause the compose view to become dirty
//        if (rootView.isDirty) state.isRendering = true
        state.isRendering = true
      }

      VisualEffect.UpdatePolicy.Continuous -> {
        val frameCallBack = remember {
          object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
              state.isRendering = true
              Choreographer.getInstance().postFrameCallback(this)
            }
          }
        }

        DisposableEffect(frameCallBack) {
          Choreographer.getInstance().postFrameCallback(frameCallBack)
          onDispose { Choreographer.getInstance().removeFrameCallback(frameCallBack) }
        }

        drawModifier.onGloballyPositioned {
          // We update once the content bounds changes
          bitmap.updateBounds(it.boundsInWindow())
        }
      }
    }
  }
)
