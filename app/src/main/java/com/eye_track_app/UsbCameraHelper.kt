package com.eye_track_app

import android.content.Context
import android.hardware.usb.UsbDevice
import android.view.Surface
import android.view.SurfaceTexture
import android.widget.Toast
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera

class UsbCameraHelper(
    private val context: Context,
    private val usbMonitor: USBMonitor
) {
    private var uvcCamera: UVCCamera? = null

    fun startCamera(surfaceTexture: SurfaceTexture) {
        usbMonitor.setDeviceConnectListener(object : USBMonitor.OnDeviceConnectListener {
            override fun onAttach(device: UsbDevice?) {
                Toast.makeText(context, "USB 摄像头已连接", Toast.LENGTH_SHORT).show()
                usbMonitor.requestPermission(device)
            }

            override fun onConnect(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock?,
                createNew: Boolean
            ) {
                uvcCamera = UVCCamera().apply {
                    open(ctrlBlock)
                    setPreviewSize(640, 480, UVCCamera.FRAME_FORMAT_MJPEG)
                    setPreviewTexture(surfaceTexture)
                    startPreview()
                }
            }

            override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
                uvcCamera?.close()
                uvcCamera = null
            }

            override fun onDettach(device: UsbDevice?) {}
            override fun onCancel(device: UsbDevice?) {}
        })
        usbMonitor.register()
    }

    fun stopCamera() {
        usbMonitor.unregister()
        uvcCamera?.destroy()
        uvcCamera = null
    }
}
