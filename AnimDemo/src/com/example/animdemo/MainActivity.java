
package com.example.animdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.animdemo.facepay.WaterWaveActivity;
import com.example.animdemo.slidecut.SlidecutListActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

    }

    public void intoFacePay(View view) {
        startActivity(new Intent(this, WaterWaveActivity.class));
    }

    public void intoSlidecut(View view) {
        startActivity(new Intent(this, SlidecutListActivity.class));
    }

    public void intoScrollDemo(View view) {
        startActivity(new Intent(this, ScrollDemoActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
