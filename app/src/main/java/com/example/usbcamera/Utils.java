package com.example.usbcamera;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

public class Utils {
    private static String TAG  = " myapp ";

    public static void toastinfo(Activity activity, String info) {
        activity.runOnUiThread(() -> {
            Toast.makeText(activity,info,Toast.LENGTH_SHORT).show();
            Log.d(TAG, "debug: " + info);
        });
    }
    public static void loginfo(String info) {
        Log.d(TAG, "debug: " + info);
    }
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 10;

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
