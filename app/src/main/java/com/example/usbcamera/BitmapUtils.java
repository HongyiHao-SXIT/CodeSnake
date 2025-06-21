package com.example.usbcamera;

import android.graphics.Bitmap;

public class BitmapUtils {

    public static Bitmap rgbaToBitmap(byte[] rgbaData, int width, int height) {
        if (rgbaData.length - 7 != width * height * 4) {
            throw new IllegalArgumentException("数据长度与图像尺寸不匹配");
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length - 7; i++) {
            int r = rgbaData[i * 4] & 0xFF;
            int g = rgbaData[i * 4 + 1] & 0xFF;
            int b = rgbaData[i * 4 + 2] & 0xFF;
            int a = rgbaData[i * 4 + 3] & 0xFF;

            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
