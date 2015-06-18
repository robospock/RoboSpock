package com.example.robospock.shadow;

import android.app.ActivityManager;
import org.robolectric.annotation.Implements;

@Implements(ActivityManager.class)
public class MyActivityManagerShadow {
    public int getMemoryClass() {
        return 64;
    }
}
