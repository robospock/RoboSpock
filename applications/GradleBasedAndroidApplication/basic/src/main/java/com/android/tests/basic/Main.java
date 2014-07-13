package com.android.tests.basic;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import pl.polidea.robospock.R;

public class Main extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
