package pl.polidea.tddandroid.shadow;

import android.app.ActivityManager;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(ActivityManager.class)
public class MyActivityManagerShadow {

    public int getMemoryClass() {
        return 64;
    }
}
