package com.android.tests.basic;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.android.tests.basic.R;

public class Main extends Activity
{
	private TextView textView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView = (TextView) findViewById(R.id.text);
    }
}
