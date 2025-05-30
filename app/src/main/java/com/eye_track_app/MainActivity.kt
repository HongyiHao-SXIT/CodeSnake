import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Log
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera

class UsbCameraHelper(
    private val context: Context,
    private val usbMonitor: USBMonitor
) {

    private var uvcCamera: UVCCamera? = null

    init {
        usbMonitor.setDeviceConnectListener(object : USBMonitor.OnDeviceConnectListener {
            override fun onDeviceAttach(device: UsbDevice?) {
                Log.d(TAG, "onDeviceAttach: ${device?.deviceName}")
                usbMonitor.requestPermission(device) // 请求用户授权
            }

            override fun onDeviceConnect(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock?,
                createNew: Boolean
            ) {
                Log.d(TAG, "onDeviceConnect: ${device?.deviceName}")
                uvcCamera = UVCCamera().apply {
                    open(ctrlBlock)
                    setPreviewSize(640, 480)
                    setFrameCallback({ frame ->
                        // 可以在这里处理帧数据
                    }, UVCCamera.PIXEL_FORMAT_YUV420SP)
                    startPreview()
                }
            }

            override fun onDeviceDisconnect(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock?
            ) {
                Log.d(TAG, "onDeviceDisconnect: ${device?.deviceName}")
                uvcCamera?.stopPreview()
                uvcCamera?.destroy()
                uvcCamera = null
            }

            override fun onDeviceDettach(device: UsbDevice?) {
                Log.d(TAG, "onDeviceDettach: ${device?.deviceName}")
            }

            override fun onCancel(device: UsbDevice?) {
                Log.d(TAG, "onCancel: ${device?.deviceName}")
            }
        })
    }

    fun startCamera(surfaceTexture: android.graphics.SurfaceTexture) {
        uvcCamera?.setPreviewDisplay(android.view.Surface(surfaceTexture))
    }

    fun stopCamera() {
        uvcCamera?.stopPreview()
        uvcCamera?.destroy()
        uvcCamera = null
    }

    companion object {
        private const val TAG = "UsbCameraHelper"
    }
}
