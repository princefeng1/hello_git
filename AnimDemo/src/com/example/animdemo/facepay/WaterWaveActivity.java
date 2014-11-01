
package com.example.animdemo.facepay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.animdemo.R;
import com.example.animdemo.slidecut.SlidecutListActivity;

public class WaterWaveActivity extends Activity implements FacePayView.OnListener {
    private FacePayView mWaveView;
    private BubbleView mBubbleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waterwave);

        mWaveView = (FacePayView) findViewById(R.id.waveView);
        mWaveView.setOnListener(this);
        mBubbleView = (BubbleView) findViewById(R.id.bubbleView);
        mBubbleView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("WaterWaveView", "MainActivity onResume");
        mWaveView.startAnim();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("WaterWaveView", "MainActivity onPause");
        mWaveView.stopAnim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onIntoPayMode() {
        mBubbleView.setVisibility(View.VISIBLE);
        mBubbleView.startAnim();
    }

    @Override
    public void onIntoReceiptMode() {
        Intent intent = new Intent(this, SlidecutListActivity.class);
        startActivity(intent);
    }
}
