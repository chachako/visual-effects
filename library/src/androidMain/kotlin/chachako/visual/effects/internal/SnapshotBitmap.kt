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
package chachako.visual.effects.internal

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Shader.TileMode
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawContext
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.withSave
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import chachako.visual.effects.VisualEffect
import com.meowool.sweekt.onNotNull
import com.meowool.sweekt.onNull
import com.meowool.sweekt.runOrNull
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A bitmap that snapshot contents from the view.
 *
 * @see SnapshotBitmap.snapshotFrom
 * @author Chachako
 */
internal class SnapshotBitmap(
  private val effect: VisualEffect,
  private val downscale: Float,
  effectAlpha: Float,
  baseColor: Color,
) {
  var shooting: Boolean by mutableStateOf(false)

  private var inner: Bitmap? = null
  private val canvas = Canvas()
  private var originalBounds = Rect.Zero
  private val paint = Paint().apply { alpha = effectAlpha }
  private val backgroundColor = baseColor.toArgb()

  /**
   * Update the [bounds] of this bitmap, which will recreate the [inner] bitmap
   * with the new size.
   */
  fun updateBounds(bounds: Rect) {
    // There may not actually be anything to update
    if (inner != null && bounds == originalBounds) return

    // Create a new bitmap and update to the canvas
    inner = runOrNull {
      when {
        bounds.isEmpty -> null
        else -> createBitmap(bounds.width.downscale(), bounds.height.downscale())
      }
    }.onNull(System::gc).onNotNull(canvas::setBitmap)

    originalBounds = bounds
  }

  /**
   * Set a snapshot of the [view]'s content into this bitmap.
   */
  fun snapshotFrom(view: View, retry: Byte = 0) {
    var bitmap = inner ?: return

    try {
      // Erase old content
      if (bitmap.isMutable) {
        bitmap.eraseColor(backgroundColor)
      } else {
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
      }

      // Draw the new content into the bitmap
      canvas.withSave {
        // First we have to make sure that the subsequent drawing steps are
        //   done in the correct coordinates
        val (x, y) = view.coordinates()
        scale(bitmap.width / originalBounds.width, bitmap.height / originalBounds.height)
        translate(-x - originalBounds.left, -y - originalBounds.top)
        clipRect(originalBounds.left, originalBounds.top, originalBounds.right, originalBounds.bottom)

        // Then tell the others we're in a shooting state
        shooting = true

        // Finally, draw all the contents of the view to this bitmap
//        view.rootView.cast<ViewGroup>().descendants.forEach {
//          it.draw(this)
//        } // TODO Mixing with SurfaceView/TextureView
        view.draw(this)
      }
      inner = effect.render(bitmap)
    } catch (e: IllegalArgumentException) {
      // For possible 'Software rendering doesn't support hardware bitmaps',
      //   we need some fixed retries
      when (retry) {
        in 1..3 -> inner = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        4.toByte() -> view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        else -> throw e
      }
      snapshotFrom(view, (retry + 1).toByte())
    } finally {
      // Regardless, we're done shooting the view
      paint.shader = BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP)
      shooting = false
    }
  }

  fun drawTo(context: DrawContext) {
    val bitmap = inner ?: return

    with(context.canvas) {
      withSave {
        scale(originalBounds.width / bitmap.width, originalBounds.height / bitmap.height)
        drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
      }
    }
  }

  fun destroy() {
    effect.destroy()
    inner?.recycle()
    inner = null
  }

  private fun Float.downscale() = ceil(this / max(1f, downscale)).roundToInt()
  private fun View.coordinates() = IntArray(2).also(::getLocationOnScreen)
}
