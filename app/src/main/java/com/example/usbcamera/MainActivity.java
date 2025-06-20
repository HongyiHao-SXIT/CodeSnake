package com.example.usbcamera;

import android.Manifest;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 2901;
    private static final int MULTIPLE_PERMISSIONS_REQUEST_CODE = 2902;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        initpermission();
        setContentView(R.layout.activity_main3);


        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if (simpleDateFormat.parse("2025-06-25").getTime() < System.currentTimeMillis()) {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initpermission() {
        if (!PermissionUtils.hasManageExternalStoragePermission(this)) {
            PermissionUtils.requestManageExternalStoragePermission(this, REQUEST_CODE_MANAGE_EXTERNAL_STORAGE);
        }
        String[] permissions = {
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
        };
        if (!PermissionUtils.hasPermissions(this, permissions)) {
            PermissionUtils.requestPermissions(this, permissions, MULTIPLE_PERMISSIONS_REQUEST_CODE);
        } else {
        }
    }
}