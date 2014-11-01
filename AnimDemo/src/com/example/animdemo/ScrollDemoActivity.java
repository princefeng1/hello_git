
package com.example.animdemo;

import android.app.Activity;
import android.os.Bundle;

public class ScrollDemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_demo);

        final DragScrollView dragScrollView = (DragScrollView) findViewById(R.id.dragScrollView);

    }

}
