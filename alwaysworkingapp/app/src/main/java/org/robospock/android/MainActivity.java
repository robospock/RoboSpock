package org.robospock.android;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends Activity implements View.OnClickListener {

    @InjectView(R.id.magic_button)
    Button magicBtn;

    @InjectView(R.id.magic_text)
    TextView magicTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ButterKnife.inject(this);

        magicBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (TextUtils.isEmpty(magicTxt.getText())) {
            magicTxt.setText("Magic!");
        } else {
            magicTxt.setText("");
        }

    }
}
