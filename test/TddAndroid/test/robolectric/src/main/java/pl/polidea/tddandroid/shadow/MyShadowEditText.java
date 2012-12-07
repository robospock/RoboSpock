package pl.polidea.tddandroid.shadow;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowEditText;

@Implements(EditText.class)
public class MyShadowEditText extends ShadowEditText {
    private OnEditorActionListener editorActionListener;

    public void onEditorAction(final int actionCode) {
        if (editorActionListener != null) {
            editorActionListener.onEditorAction((TextView) this.realView, actionCode, null);
        }
    }

    @Override
    public void setOnEditorActionListener(final OnEditorActionListener listener) {
        editorActionListener = listener;

    }
}
