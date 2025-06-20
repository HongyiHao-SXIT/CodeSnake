package com.jiangdg.ausbc.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * VRTextureView - 自定义TextureView，用于显示左右眼相同内容的VR效果
 * 该视图将内容分割为左右两部分，分别渲染到左右眼区域
 */
public class VRTextureView extends TextureView implements IAspectRatio {

    private Paint mPaint;           // 画笔
    private Matrix mLeftMatrix;     // 左眼变换矩阵
    private Matrix mRightMatrix;    // 右眼变换矩阵
    private Rect mLeftRect;         // 左眼显示区域
    private Rect mRightRect;        // 右眼显示区域
    private float mEyeDistance;     // 眼间距（立体效果强度）
    private float mPerspective;     // 透视效果
    private Bitmap mContentBitmap;  // 要显示的内容位图
    private boolean mIsUpdating;    // 更新标志，防止递归调用

    public VRTextureView(Context context) {
        this(context, null);
    }

    public VRTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VRTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);

        // 初始化变换矩阵
        mLeftMatrix = new Matrix();
        mRightMatrix = new Matrix();

        // 初始化显示区域
        mLeftRect = new Rect();
        mRightRect = new Rect();

        // 设置默认参数
        mEyeDistance = 20f;  // 默认眼间距
        mPerspective = 0.002f;  // 默认透视效果
        mIsUpdating = false;     // 初始化更新标志

        // 设置SurfaceTextureListener以监听SurfaceTexture的创建和销毁
        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                // SurfaceTexture可用时的处理
                updateViewSize(width, height);
                drawVRContent();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                // SurfaceTexture大小改变时的处理
                updateViewSize(width, height);
                // 注意：不要在这里调用drawVRContent()，避免递归
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                // SurfaceTexture销毁时的处理
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // SurfaceTexture更新时的处理
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 只在非更新状态下更新视图尺寸
        if (!mIsUpdating) {
            updateViewSize(w, h);
        }
    }

    private void updateViewSize(int width, int height) {
        // 设置左右眼显示区域
        mLeftRect.set(0, 0, width / 2, height);
        mRightRect.set(width / 2, 0, width, height);

        // 计算透视效果
        applyPerspective();
    }

    private void applyPerspective() {
        // 创建透视变换
        float[] leftM = new float[9];
        float[] rightM = new float[9];

        // 左眼变换 - 向左偏移
        mLeftMatrix.getValues(leftM);
        leftM[2] = -mEyeDistance;  // X轴偏移

        // 右眼变换 - 向右偏移
        mRightMatrix.getValues(rightM);
        rightM[2] = mEyeDistance;  // X轴偏移

        mLeftMatrix.setValues(leftM);
        mRightMatrix.setValues(rightM);
    }

    /**
     * 绘制VR内容
     */
    public void drawVRContent() {
        if (!isAvailable() || mIsUpdating) return;

        // 设置更新标志，防止递归
        mIsUpdating = true;

        Canvas canvas = lockCanvas();
        if (canvas != null) {
            try {
                // 清除画布
                canvas.drawColor(Color.BLACK);

                // 绘制分隔线
                drawSeparator(canvas);

                // 绘制左眼视图
                drawEyeContent(canvas, mLeftRect, mLeftMatrix);

                // 绘制右眼视图
                drawEyeContent(canvas, mRightRect, mRightMatrix);

            } finally {
                // 解锁画布并提交更改
                unlockCanvasAndPost(canvas);
                // 重置更新标志
                mIsUpdating = false;
            }
        } else {
            // 重置更新标志
            mIsUpdating = false;
        }
    }

    private void drawSeparator(Canvas canvas) {
        // 绘制中间分隔线
        int width = getWidth();
        int height = getHeight();

        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(2);
        canvas.drawLine(width / 2, 0, width / 2, height, mPaint);
    }

    private void drawEyeContent(Canvas canvas, Rect rect, Matrix matrix) {
        // 保存当前画布状态
        int saveCount = canvas.save();

        // 裁剪到指定区域
        canvas.clipRect(rect);
        canvas.concat(matrix);

        // 在指定区域绘制内容
        if (mContentBitmap != null) {
            // 计算图片在目标区域的缩放比例
            float scaleX = (float) rect.width() / mContentBitmap.getWidth();
            float scaleY = (float) rect.height() / mContentBitmap.getHeight();
            float scale = Math.min(scaleX, scaleY);

            // 计算居中位置
            int offsetX = (int) ((rect.width() - mContentBitmap.getWidth() * scale) / 2);
            int offsetY = (int) ((rect.height() - mContentBitmap.getHeight() * scale) / 2);

            // 创建缩放矩阵
            Matrix contentMatrix = new Matrix();
            contentMatrix.postScale(scale, scale);
            contentMatrix.postTranslate(rect.left + offsetX, rect.top + offsetY);

            // 绘制图片
            canvas.drawBitmap(mContentBitmap, contentMatrix, mPaint);
        }

        // 绘制一些测试文字
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(30);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("VR Content", rect.centerX(), rect.centerY(), mPaint);

        // 恢复画布状态
        canvas.restoreToCount(saveCount);
    }

    /**
     * 设置要显示的位图
     */
    public void setContentBitmap(Bitmap bitmap) {
        mContentBitmap = bitmap;
        drawVRContent();
    }

    /**
     * 设置眼间距，控制立体效果强度
     */
    public void setEyeDistance(float eyeDistance) {
        mEyeDistance = eyeDistance;
        applyPerspective();
        drawVRContent();
    }

    /**
     * 设置透视效果
     */
    public void setPerspective(float perspective) {
        mPerspective = perspective;
        applyPerspective();
        drawVRContent();
    }

    /**
     * 更新显示内容
     * 当有新的内容需要显示时调用此方法
     */
    public void updateContent() {
        drawVRContent();
    }

    @Override
    public void setAspectRatio(int width, int height) {

    }

    @Override
    public int getSurfaceWidth() {
        return getWidth();
    }

    @Override
    public int getSurfaceHeight() {
        return getHeight();
    }

    @Nullable
    @Override
    public Surface getSurface() {
        return new Surface(getSurfaceTexture());
    }

    @Override
    public void postUITask(@NonNull Function0<Unit> task) {

    }
}
