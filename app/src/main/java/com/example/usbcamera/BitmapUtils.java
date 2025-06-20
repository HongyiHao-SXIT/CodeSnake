package com.example.usbcamera;

import android.graphics.Bitmap;

public class BitmapUtils {
    /**
     * 将 RGBA 格式的 byte[] 数据转换为 Bitmap
     * @param rgbaData RGBA 像素数据（每个像素占 4 字节，顺序为 R、G、B、A）
     * @param width 图像宽度
     * @param height 图像高度
     * @return 转换后的 Bitmap
     */
    public static Bitmap rgbaToBitmap(byte[] rgbaData, int width, int height) {
        // 检查数据长度是否匹配
        if (rgbaData.length - 7  != width * height * 4) {
            throw new IllegalArgumentException("数据长度与图像尺寸不匹配");
        }

        // 创建 ARGB_8888 格式的 Bitmap（每个像素占 4 字节）
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 将 byte[] 转换为 int[]（每个像素用一个 int 表示）
        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length-7; i++) {
            int r = rgbaData[i * 4] & 0xFF;         // 提取 R 分量（0-255）
            int g = rgbaData[i * 4 + 1] & 0xFF;     // 提取 G 分量
            int b = rgbaData[i * 4 + 2] & 0xFF;     // 提取 B 分量
            int a = rgbaData[i * 4 + 3] & 0xFF;     // 提取 A 分量

            // 组合成 ARGB 格式（int 类型）
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        // 设置像素数据到 Bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}