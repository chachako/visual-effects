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
@file:Suppress("DEPRECATION")

package chachako.visual.effects.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.HardwareRenderer
import android.graphics.PixelFormat
import android.graphics.RenderNode
import android.graphics.Shader.TileMode
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.ScriptIntrinsicBlur
import androidx.annotation.RequiresApi
import chachako.visual.effects.BlurVisualEffect
import chachako.visual.effects.VisualEffectBitmap
import android.graphics.RenderEffect as AndroidRenderEffect
import android.renderscript.RenderScript as AndroidRenderScript

/**
 * @author Chachako
 */
@PublishedApi
internal sealed class BlurVisualEffectImpl : BlurVisualEffect() {
  class RenderScript(context: Context, private val radius: Float) : BlurVisualEffectImpl() {
    private val script: AndroidRenderScript = AndroidRenderScript.create(context)
    private val intrinsic: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(script, Element.U8_4(script))

    private var inputAlloc: Allocation? = null
    private var outputAlloc: Allocation? = null

    override val radiusX: Float get() = radius
    override val radiusY: Float get() = radius

    override fun render(bitmap: VisualEffectBitmap): VisualEffectBitmap {
      if (radius == 0f) return bitmap

      ensureAllocationsValid(bitmap)

      intrinsic.apply {
        setRadius(radius)
        setInput(inputAlloc)
        forEach(outputAlloc)
      }

      outputAlloc!!.copyTo(bitmap)

      return bitmap
    }

    override fun destroy() = runCatching {
      destroyAllocations()
      script.destroy()
      intrinsic.destroy()
    }.getOrDefault(Unit)

    private fun destroyAllocations() = runCatching {
      inputAlloc?.run { destroy(); inputAlloc = null }
      outputAlloc?.run { destroy(); outputAlloc = null }
    }.getOrDefault(Unit)

    /**
     * This function ensures that the allocation properties are valid so that we can reuse them
     * to avoid frequent creation of new allocations.
     */
    private fun ensureAllocationsValid(bitmap: VisualEffectBitmap) {
      if (inputAlloc?.type?.x != bitmap.width || inputAlloc?.type?.y != bitmap.height) {
        // Create new allocations
        destroyAllocations()
        inputAlloc = Allocation.createFromBitmap(script, bitmap).also {
          outputAlloc = Allocation.createTyped(script, it.type)
        }
        return
      }

      // Reuse existing allocations
      runCatching {
        inputAlloc?.copyFrom(bitmap)
      }.onFailure {
        destroyAllocations()
        ensureAllocationsValid(bitmap)
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.S)
  class RenderEffect(
    override val radiusX: Float,
    override val radiusY: Float,
    private val edgeTreatment: TileMode,
  ) : BlurVisualEffectImpl() {
    private var default: Default? = Default(AndroidRenderEffect.createBlurEffect(radiusX, radiusY, edgeTreatment))
    private var fallback: Fallback? = null

    private var retryCount = 0

    override fun render(bitmap: VisualEffectBitmap): VisualEffectBitmap? {
      // When the count of retries exceeds 4, we will fall back
      return when {
        retryCount > 4 -> {
          default?.destroy()
          default = null
          Fallback(radiusX, radiusY, edgeTreatment).let {
            fallback = it
            it.render(bitmap)
          }
        }
        else -> {
          retryCount++
          default!!.render(bitmap)
        }
      }
    }

    override fun destroy() {
      default?.destroy() ?: fallback?.destroy()
    }

    private class Default(effect: AndroidRenderEffect) {
      private val renderNode: RenderNode = RenderNode("BlurVisualEffect").apply { setRenderEffect(effect) }
      private val hardwareRenderer: HardwareRenderer = HardwareRenderer().apply { setContentRoot(renderNode) }
      private var imageReader: ImageReader? = null

      fun render(bitmap: VisualEffectBitmap): VisualEffectBitmap? {
        ensureImageReaderValid(bitmap)

        renderNode.apply {
          beginRecording().apply { drawBitmap(bitmap, 0f, 0f, null) }
          endRecording()
        }

        hardwareRenderer.createRenderRequest().setWaitForPresent(true).syncAndDraw()

        val image = imageReader!!.acquireNextImage() ?: return null
        val hardwareBuffer = image.hardwareBuffer ?: return null
        val blurred = Bitmap.wrapHardwareBuffer(hardwareBuffer, null)

        hardwareBuffer.close()
        image.close()

        return blurred
      }

      fun destroy() = runCatching {
        imageReader?.close()
        hardwareRenderer.destroy()
        renderNode.discardDisplayList()

        imageReader = null
      }.getOrDefault(Unit)

      /**
       * This function ensures that the image reader instance is valid so that we can reuse it
       * to avoid frequent creation of new one.
       */
      @SuppressLint("WrongConstant")
      private fun ensureImageReaderValid(bitmap: VisualEffectBitmap) {
        if (imageReader?.width != bitmap.width || imageReader?.height != bitmap.height) {
          imageReader?.close()
          imageReader = ImageReader.newInstance(
            bitmap.width, bitmap.height,
            PixelFormat.RGBA_8888, 1,
            HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
          ).apply {
            hardwareRenderer.setSurface(surface)
            renderNode.setPosition(0, 0, width, height)
          }
        }
      }
    }

    private class Fallback(
      private val radiusX: Float,
      private val radiusY: Float,
      private val edgeTreatment: TileMode,
    ) {
      private val renderNode: RenderNode = RenderNode("BlurFallbackVisualEffect")

      private var outputBitmap: Bitmap? = null
      private lateinit var outputCanvas: Canvas

      fun render(bitmap: VisualEffectBitmap): VisualEffectBitmap? {
        ensureInstancesValid(bitmap)
        if (!outputCanvas.isHardwareAccelerated) return null

        val srcEffect = AndroidRenderEffect.createBitmapEffect(bitmap)

        AndroidRenderEffect.createBlurEffect(radiusX, radiusY, srcEffect, edgeTreatment)
          .also(renderNode::setRenderEffect)

        outputCanvas.drawRenderNode(renderNode)

        return outputBitmap
      }

      fun destroy() = runCatching {
        outputBitmap?.recycle()
        renderNode.discardDisplayList()

        outputBitmap = null
      }.getOrDefault(Unit)

      /**
       * This function ensures that the fallback instances are valid so that we can reuse them
       * to avoid frequent creation of new instances.
       */
      private fun ensureInstancesValid(bitmap: VisualEffectBitmap) {
        // First erase the contents of the output bitmap
        outputBitmap?.eraseColor(0)

        if (outputBitmap?.width != bitmap.width || outputBitmap?.height != bitmap.height) {
          outputBitmap?.recycle()
          outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888).also {
            outputCanvas = Canvas(it)
          }
        }
      }
    }
  }
}
