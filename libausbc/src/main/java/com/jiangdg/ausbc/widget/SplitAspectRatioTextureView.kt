/*
 * Copyright 2017-2023 Jiangdg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jiangdg.ausbc.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import com.jiangdg.ausbc.utils.Logger
import kotlin.math.abs

/** Adaptive TextureView with left-right split display
 * Aspect ratio (width:height, such as 4:3, 16:9).
 *
 * @author Modified by Doubao on 2023/11/27
 */
class SplitAspectRatioTextureView: TextureView, IAspectRatio, SurfaceTextureListener {

    private var mAspectRatio = -1.0
    private var mBitmap: Bitmap? = null
    private var mLeftRect = Rect()
    private var mRightRect = Rect()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        // 设置SurfaceTextureListener
        surfaceTextureListener = this
    }

    override fun setAspectRatio(width: Int, height: Int) {
        post {
            setAspectRatio(width.toDouble() / height)
        }
    }

    override fun getSurfaceWidth(): Int = width

    override fun getSurfaceHeight(): Int = height

    override fun getSurface(): Surface? {
        return try {
            Surface(surfaceTexture)
        } catch (e: Exception) {
            null
        }
    }

    override fun postUITask(task: () -> Unit) {
        post {
            task()
        }
    }

    private fun setAspectRatio(aspectRatio: Double) {
        if (aspectRatio < 0 || mAspectRatio == aspectRatio) {
            return
        }
        mAspectRatio = aspectRatio
        Logger.i(TAG, "AspectRatio = $mAspectRatio")
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var initialWidth = MeasureSpec.getSize(widthMeasureSpec)
        var initialHeight = MeasureSpec.getSize(heightMeasureSpec)
        val horizontalPadding = paddingLeft - paddingRight
        val verticalPadding = paddingTop - paddingBottom
        initialWidth -= horizontalPadding
        initialHeight -= verticalPadding

        // 比较预览与TextureView(内容)纵横比
        // 如果有变化，重新设置TextureView尺寸
        val viewAspectRatio = initialWidth.toDouble() / initialHeight
        val diff = mAspectRatio / viewAspectRatio - 1
        var wMeasureSpec = widthMeasureSpec
        var hMeasureSpec = heightMeasureSpec
        if (mAspectRatio > 0 && abs(diff) > 0.01) {
            // diff > 0， 按宽缩放
            // diff < 0， 按高缩放
            if (diff > 0) {
                initialHeight = (initialWidth / mAspectRatio).toInt()
            } else {
                initialWidth = (initialHeight * mAspectRatio).toInt()
            }
            // 重新设置TextureView尺寸
            // 注意加回padding大小
            initialWidth += horizontalPadding
            initialHeight += verticalPadding
            wMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY)
            hMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY)
        }
        super.onMeasure(wMeasureSpec, hMeasureSpec)
    }

    // SurfaceTextureListener接口实现
    override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
        // SurfaceTexture可用时的处理
        updateSplitRegions(width, height)
    }

    override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
        // SurfaceTexture大小改变时的处理
        updateSplitRegions(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
        // SurfaceTexture销毁时的处理
        mBitmap?.recycle()
        mBitmap = null
        return true
    }

    override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {
        // SurfaceTexture更新时的处理
        if (isAvailable) {
            // 创建一个Bitmap来存储TextureView的内容
            val width = width
            val height = height
            if (width > 0 && height > 0) {
                if (mBitmap == null || mBitmap!!.width != width || mBitmap!!.height != height) {
                    mBitmap?.recycle()
                    mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                }

                if (mBitmap != null) {
                    // 获取当前TextureView的内容
                    val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    getBitmap(tempBitmap)

                    // 创建一个Canvas，在上面绘制左右分屏内容
                    val canvas = Canvas(mBitmap!!)

                    // 绘制左半部分
                    canvas.save()
                    canvas.clipRect(mLeftRect)
                    canvas.drawBitmap(tempBitmap, 0f, 0f, null)
                    canvas.restore()

                    // 绘制右半部分，使用相同的图像内容
                    canvas.save()
                    canvas.clipRect(mRightRect)
                    canvas.drawBitmap(tempBitmap, 0f, 0f, null)
                    canvas.restore()

                    // 释放临时Bitmap
                    tempBitmap.recycle()

                    // 将Bitmap绘制到TextureView上
                    drawBitmapToTextureView()
                }
            }
        }
    }

    // 更新左右分屏区域
    private fun updateSplitRegions(width: Int, height: Int) {
        val halfWidth = width / 2
        mLeftRect.set(0, 0, halfWidth, height)
        mRightRect.set(halfWidth, 0, width, height)
    }

    // 将Bitmap绘制到TextureView上
    private fun drawBitmapToTextureView() {
        if (mBitmap != null && isAvailable) {
            try {
                // 使用lockCanvas和unlockCanvasAndPost方法将Bitmap绘制到TextureView上
                val canvas = lockCanvas()
                if (canvas != null) {
                    canvas.drawBitmap(mBitmap!!, 0f, 0f, null)
                    unlockCanvasAndPost(canvas)
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error drawing bitmap to TextureView: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "SplitAspectRatioTextureView"
    }
}
