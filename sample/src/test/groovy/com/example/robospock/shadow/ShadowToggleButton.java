package com.example.robospock.shadow;

import android.widget.ToggleButton;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowTextPaint;

@Implements(ToggleButton.class)
public class ShadowToggleButton extends ShadowTextPaint {
    private CharSequence textOff;

    public void setTextOff(final CharSequence textOff) {
        this.textOff = textOff;
    }

    public CharSequence getTextOff() {
        return textOff;
    }
}
