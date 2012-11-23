package pl.polidea.tddandroid.shadow;

import android.widget.ToggleButton;

import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowButton;

@Implements(ToggleButton.class)
public class ShadowToggleButton extends ShadowButton {

    private CharSequence textOff;

    public void setTextOff(final CharSequence textOff) {
        this.textOff = textOff;
    }

    public CharSequence getTextOff() {
        return textOff;
    }
}
