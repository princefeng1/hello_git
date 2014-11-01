
package com.example.animdemo;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class DragScrollView extends ViewGroup implements OnClickListener {
    private static final String TAG = "DragScrollView";

    private ViewGroup mAView;
    private ViewGroup mBView;
    private ViewGroup mHandleView;

    private int mHandleHeight;
    private int mContentHeight;

    private Scroller mScroller;

    private static final int MODE_A = 0;
    private static final int MODE_B = 1;
    private int mMode = MODE_A;

    public DragScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mAView = new FrameLayout(getContext());
        mAView.setBackgroundColor(Color.BLACK);
        addView(mAView);
        mBView = new FrameLayout(getContext());
        mBView.setBackgroundColor(Color.BLUE);
        addView(mBView);
        mHandleView = new FrameLayout(getContext());
        mHandleView.setBackgroundColor(Color.RED);
        addView(mHandleView);
        mHandleView.setOnClickListener(this);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mHandleHeight = (int) (50 * dm.density);

        mScroller = new Scroller(getContext());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAView.layout(l, 0, r, mContentHeight);
        mHandleView.layout(l, mContentHeight, r, mContentHeight + mHandleHeight);
        mBView.layout(l, mContentHeight + mHandleHeight, r, mContentHeight + mHandleHeight
                + mContentHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mContentHeight = height - mHandleHeight;
        mAView.measure(width, mContentHeight);
        mBView.measure(width, mContentHeight);
        mHandleView.measure(width, mHandleHeight);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    public void slideA() {

    }

    public void slideB() {

    }

    @Override
    public void onClick(View v) {
        if (v == mHandleView) {
            if (mMode == MODE_A) {
                mMode = MODE_B;
                mScroller.startScroll(0, 0, 0, mContentHeight, 1000);
                invalidate();
            } else {
                mMode = MODE_A;
                int scrollY = getScrollY();
                Log.d(TAG, "onClick scrollY = " + scrollY);
                mScroller.startScroll(0, scrollY, 0, -scrollY, 1000);
                invalidate();
            }

        }
    }
}
