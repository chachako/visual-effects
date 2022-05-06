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
package chachako.visual.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Represents a platform-specific bitmap that can render visual effect.
 *
 * @see VisualEffect
 * @author Chachako
 */
expect class VisualEffectBitmap

/**
 * Represents a visual effect that can be rendered to a platform-specific bitmap.
 *
 * @author Chachako
 */
interface VisualEffect {

  /**
   * Renders this visual effect to a platform-specific [bitmap].
   *
   * @param bitmap A bitmap to render to.
   * @return A bitmap result with this visual effect, or `null` if anything unexpected
   *   happened during the rendering process.
   */
  fun render(bitmap: VisualEffectBitmap): VisualEffectBitmap?

  /**
   * Destroys all resources allocated by this visual effect.
   */
  fun destroy()

  /**
   * A state of [VisualEffect].
   *
   * The usual way to create a visual effect state is by calling the
   * [rememberVisualEffectState] function.
   *
   * @author Chachako
   */
  class State(isEnabled: Boolean) {

    /**
     * Whether this visual effect is enabled. If it is not, it will not be rendered.
     */
    var isEnabled: Boolean by mutableStateOf(isEnabled)

    /**
     * Whether this visual effect is being rendered.
     */
    var isRendering: Boolean by mutableStateOf(false)

    /**
     * Re-render this effect manually.
     */
    fun render() {
      // FIXME: This will cause the LaunchedEffect to get stuck
      // isRendering = true
    }
  }

  /**
   * Represents a range of different policies for making the [VisualEffect] automatically
   * re-rendered.
   *
   * @author Chachako
   */
  enum class UpdatePolicy {

    /**
     * Render the visual effect only once.
     *
     * ## Important
     *
     * This will stop after rendering the visual effect once, and any subsequent content
     * changes will not trigger the recomposition, meaning that the rendered content is
     * always old, but you can always manually call the [State.render] function to trigger
     * the recomposition if necessary, which is the best practice to keep high performance.
     */
    Once,

    /**
     * Render the visual effect only when the content changes.
     *
     * ## Important
     *
     * This will re-render the visual effect every time the content changes whenever possible.
     * However, there is no guarantee that 100% of all content changes are captured, for example,
     * the content changes of [SurfaceView](https://developer.android.com/reference/android/view/SurfaceView)
     * in the Android platform cannot be known in real time.
     */
    RealTime,

    /**
     * Render the visual effect continuously.
     *
     * ## Important
     *
     * This will repeatedly trigger recomposition to re-render the visual effect, which is
     * the most performance costly option and should not be used unless necessary.
     */
    Continuous,
  }

  companion object {

    /**
     * The default [Saver] implementation for [State].
     */
    val StateSaver: Saver<State, *> = Saver(
      save = { it.isEnabled },
      restore = { State(it) }
    )
  }
}

/**
 * Creates a [VisualEffect.State] that is remembered across compositions to allow the
 * visual effect to be re-rendered at any time.
 *
 * @param isEnabled Whether to enable this visual effect initially.
 * @author Chachako
 */
@Composable
fun rememberVisualEffectState(isEnabled: Boolean = true): VisualEffect.State =
  rememberSaveable(saver = VisualEffect.StateSaver) { VisualEffect.State(isEnabled) }
