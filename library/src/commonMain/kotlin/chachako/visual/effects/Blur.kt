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
@file:Suppress("FunctionName")

package chachako.visual.effects

/**
 * A visual blur effect with a specific algorithm.
 *
 * @author Chachako
 */
abstract class BlurVisualEffect : VisualEffect {

  /**
   * The blur radius of this visual effect along the x-axis.
   *
   * Note that if the blurring algorithm does not support blurring in the x-axis direction,
   * then this value will be the same as the [radiusY] property.
   */
  abstract val radiusX: Float

  /**
   * The blur radius of this visual effect along the y-axis.
   *
   * Note that if the blurring algorithm does not support blurring in the y-axis direction,
   * then this value will be the same as the [radiusX] property.
   */
  abstract val radiusY: Float

  override fun toString(): String = "Blur($radiusX, $radiusY)"

  companion object
}
