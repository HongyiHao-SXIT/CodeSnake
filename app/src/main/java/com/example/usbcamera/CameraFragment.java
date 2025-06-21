package com.example.usbcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RenderEffect;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.usbcamera.databinding.FragmentCameraBinding;
import com.jiangdg.ausbc.MultiCameraClient;
import com.jiangdg.ausbc.callback.ICameraStateCallBack;
import com.jiangdg.ausbc.callback.ICaptureCallBack;
import com.jiangdg.ausbc.callback.IEncodeDataCallBack;
import com.jiangdg.ausbc.callback.IPlayCallBack;
import com.jiangdg.ausbc.callback.IPreviewDataCallBack;
import com.jiangdg.ausbc.camera.bean.CameraRequest;
import com.jiangdg.ausbc.camera.bean.PreviewSize;
import com.jiangdg.ausbc.render.effect.EffectSoul;
import com.jiangdg.ausbc.render.effect.bean.CameraEffect;
import com.jiangdg.ausbc.render.env.RotateType;
import com.jiangdg.ausbc.widget.IAspectRatio;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

public class CameraFragment extends com.jiangdg.ausbc.base.CameraFragment {
    private static final String TAG = "298292";
    FragmentCameraBinding mViewBinding;


    @Nullable
    @Override
    protected IAspectRatio getCameraView() {
        return mViewBinding.frame;
    }

    @Nullable
    @Override
    protected IAspectRatio getCameraView2() {
        return null;
    }

    @Nullable
    @Override
    protected ViewGroup getCameraViewContainer() {
        return null;
    }

    @Nullable
    @Override
    protected View getRootView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        mViewBinding = FragmentCameraBinding.inflate(getLayoutInflater());
        initview();
        return mViewBinding.getRoot();
    }

    RoundEffect effect;

    private void initview() {
        effect = new RoundEffect(getContext());

    }

    int[] imagedatas;

    private void setview() {
        mViewBinding.frame.setAspectRatio(1920, 1080);
        setZoom(0);
        setSharpness(50);
        setHue(50);
        setSaturation(50);
        setContrast(50);
        setGamma(20);
        setGain(20);
        setAutoFocus(true);
        setRotateType(RotateType.ANGLE_0);
        startPlayMic(new IPlayCallBack() {
            @Override
            public void onBegin() {


            }

            @Override
            public void onError(@NonNull String s) {
            }

            @Override
            public void onComplete() {

            }
        });
        addPreviewDataCallBack(new IPreviewDataCallBack() {
            @Override
            public void onPreviewData(@Nullable byte[] data, int width, int height, @NonNull DataFormat format) {

                if (data != null) {
                    Log.d(TAG, "onPreviewData: " + format + " " + width + " x " + height + " " + data.length);
                    // 设置像素数据
                    Bitmap bitmap = BitmapUtils.rgbaToBitmap(data, width, height);
                    getActivity().runOnUiThread(() -> {
                        mViewBinding.frame3.setImageBitmap(bitmap);
                    });
                }

            }
        });
        mViewBinding.frame.setOnClickListener(v -> {
        });
        addRenderEffect(new RoundEffect(getContext()));
    }


    @Override
    public void onCameraState(@NonNull MultiCameraClient.ICamera iCamera, @NonNull State state, @Nullable String s) {
        if (state == ICameraStateCallBack.State.OPENED) {
            Utils.toastinfo(getActivity(), "相机已连接");
            initcamerainfo();
            getcamerainfo();
        }
        if (state == State.CLOSED) {
            Utils.toastinfo(getActivity(), "相机已关闭");
        }
        if (state == State.ERROR) {
            Utils.toastinfo(getActivity(), "相机错误");
        }
    }

    private void initcamerainfo() {

        try {

            setview();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getcamerainfo() {
        mViewBinding.text.setText("");
        List<PreviewSize> previewSizes = getAllPreviewSizes(null);
        String camerainfo = "相机参数：\n";
        if (previewSizes.isEmpty()) {

            Utils.toastinfo(getActivity(), "获取相机分辨率错误！");
            return;
        } else {
            camerainfo += "分辨率：\n";
            for (PreviewSize previewSize : previewSizes) {
                int w = previewSize.getWidth();
                int h = previewSize.getHeight();
                camerainfo += (w + " x " + h + "   , ");
            }
        }
        camerainfo += "gama：" + getGamma() + "\n";
        camerainfo += "gain：" + getGain() + "\n";
        camerainfo += "Saturation：" + getSaturation() + "\n";
        camerainfo += "Sharpness：" + getSharpness() + "\n";
        camerainfo += "AutoFocus：" + getAutoFocus() + "\n";
        camerainfo += "Brightness：" + getBrightness() + "\n";
        camerainfo += "Hue：" + getHue() + "\n";
        mViewBinding.text.setText(camerainfo);
    }

    @NonNull
    @Override
    protected CameraRequest getCameraRequest() {
        return new CameraRequest.Builder()
                .setPreviewWidth(1920)
                .setPreviewHeight(1080)
                .setRenderMode(CameraRequest.RenderMode.OPENGL)
                .setDefaultRotateType(RotateType.ANGLE_180)
                .setAudioSource(CameraRequest.AudioSource.SOURCE_AUTO)

                .setCaptureRawImage(false)
                .setRawPreviewData(false)
                .create();
    }


    @Override
    protected int getGravity() {
        return Gravity.CENTER;
    }


}
