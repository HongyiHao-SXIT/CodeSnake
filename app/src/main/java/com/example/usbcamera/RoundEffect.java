package com.example.usbcamera;

import android.content.Context;

import androidx.annotation.NonNull;

import com.jiangdg.ausbc.render.effect.AbstractEffect;
import com.jiangdg.ausbc.render.effect.bean.CameraEffect;

public class RoundEffect extends AbstractEffect {
    int id = 200;
    public RoundEffect(@NonNull Context ctx) {
        super(ctx);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getClassifyId() {
        return CameraEffect.CLASSIFY_ID_FILTER;
    }

    @Override
    protected int getVertexSourceId() {
        return R.raw.vertex_shader;
    }

    @Override
    protected int getFragmentSourceId() {
        return R.raw.fragment_shader;
    }
}
