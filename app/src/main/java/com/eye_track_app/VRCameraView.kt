package com.eye_track_app

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.widget.LinearLayout
import com.serenegiant.usb.widget.UVCCameraTextureView

class VRCameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val leftView: UVCCameraTextureView
    private val rightView: UVCCameraTextureView

    init {
        orientation = HORIZONTAL

        leftView = UVCCameraTextureView(context)
        rightView = UVCCameraTextureView(context)

        val params = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
        addView(leftView, params)
        addView(rightView, params)
    }
    fun setSharedSurface(surfaceTexture: SurfaceTexture) {
        leftView.surfaceTexture = surfaceTexture
        rightView.surfaceTexture = surfaceTexture
    }

    fun getSurfaceTexture(): SurfaceTexture? {
        return leftView.surfaceTexture
    }

    fun getLeftView(): UVCCameraTextureView = leftView
    fun getRightView(): UVCCameraTextureView = rightView
}
