package com.eye_track_app

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eye_track_app.usbvrcamera.ui.theme.UsbVRCameraTheme
import com.serenegiant.usb.*
import com.serenegiant.usb.widget.CameraViewInterface

class MainActivity : ComponentActivity() {
    private var cameraView: CameraViewInterface? = null
    private lateinit var usbMonitor: USBMonitor
    private var isCameraConnected = false
    private var uvcCamera: UVCCamera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化USB监控器
        usbMonitor = USBMonitor(this, usbReceiver)

        setContent {
            UsbVRCameraTheme {
                MainScreen(
                    isCameraConnected = isCameraConnected,
                    onStartCamera = ::startCamera,
                    onStopCamera = ::stopCamera
                )
            }
        }
    }

    private val usbReceiver = object : USBMonitor.OnDeviceConnectListener {
        override fun onAttach(device: UsbDevice?) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "USB设备已连接", Toast.LENGTH_SHORT).show()
                device?.let { usbMonitor.requestPermission(it) }
            }
        }

        override fun onConnect(
            device: UsbDevice?,
            ctrlBlock: USBMonitor.UsbControlBlock?,
            createNew: Boolean
        ) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "摄像头已连接", Toast.LENGTH_SHORT).show()
                isCameraConnected = true

                ctrlBlock?.let {
                    openCamera(it)
                }
            }
        }

        override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "摄像头已断开", Toast.LENGTH_SHORT).show()
                isCameraConnected = false
                releaseCamera()
            }
        }

        override fun onDettach(device: UsbDevice?) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "USB设备已断开", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onCancel(device: UsbDevice?) {
            // 权限请求被取消
        }
    }

    private fun startCamera() {
        if (!isCameraConnected) {
            // 检查USB设备
            val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            val deviceList = usbManager.deviceList
            if (deviceList.isEmpty()) {
                Toast.makeText(this, "未检测到USB设备", Toast.LENGTH_SHORT).show()
                return
            }

            // 请求权限
            for ((_, device) in deviceList) {
                if (isCameraDevice(device)) {
                    usbMonitor.requestPermission(device)
                    return
                }
            }

            Toast.makeText(this, "未检测到USB摄像头", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopCamera() {
        if (isCameraConnected) {
            releaseCamera()
            isCameraConnected = false
            Toast.makeText(this, "摄像头已停止", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isCameraDevice(device: UsbDevice): Boolean {
        // 检查设备是否为摄像头
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)
            if (usbInterface.interfaceClass == USB_CLASS_VIDEO) {
                return true
            }
        }
        return false
    }

    private fun openCamera(ctrlBlock: USBMonitor.UsbControlBlock) {
        try {
            uvcCamera = UVCCamera()
            uvcCamera?.open(ctrlBlock)

            // 设置摄像头参数
            uvcCamera?.setPreviewSize(
                UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT,
                UVCCamera.FRAME_FORMAT_MJPEG
            )

            // 设置预览
            cameraView?.let { view ->
                uvcCamera?.setPreviewDisplay(view)
                uvcCamera?.startPreview()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "打开摄像头失败", e)
            Toast.makeText(this, "打开摄像头失败: ${e.message}", Toast.LENGTH_SHORT).show()
            releaseCamera()
        }
    }

    private fun releaseCamera() {
        uvcCamera?.apply {
            stopPreview()
            close()
        }
        uvcCamera = null
    }

    override fun onResume() {
        super.onResume()
        usbMonitor.register()
        cameraView?.onResume()
    }

    override fun onPause() {
        cameraView?.onPause()
        releaseCamera()
        usbMonitor.unregister()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        usbMonitor.destroy()
    }
}

@Composable
fun MainScreen(
    isCameraConnected: Boolean,
    onStartCamera: () -> Unit,
    onStopCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var cameraView: CameraViewInterface? = null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isCameraConnected) "摄像头已连接" else "摄像头未连接",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // VR视图容器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // 这里使用自定义的VR相机视图
            VRPreviewView(
                modifier = Modifier.fillMaxSize(),
                onCameraViewCreated = { view ->
                    cameraView = view
                }
            )
        }

        // 控制按钮
        Button(
            onClick = onStartCamera,
            enabled = !isCameraConnected,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("启动摄像头")
        }

        Button(
            onClick = onStopCamera,
            enabled = isCameraConnected,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("停止摄像头")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    UsbVRCameraTheme {
        MainScreen(
            isCameraConnected = false,
            onStartCamera = {},
            onStopCamera = {}
        )
    }
}

// USB类常量
private const val USB_CLASS_VIDEO = 0x0E    