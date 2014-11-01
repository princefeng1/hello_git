
package com.example.animdemo.facepay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;

/**
 * 水波
 * 
 * @author dang
 */
public class WaterWave {

    private static final int PAY_COLOR = 0xff3898f2;// 付款区域的颜色
    private static final int RECEIPT_COLOR = 0xfff6f6f6;// 收款区域的颜色

    private int mInitXOffset;// 初始起点
    private int mXOffset;// 波起点
    private int mWaveLength;// 波长
    private int mMaxCrest;// 最大波峰
    private int mCrest = 0;// 当前波峰，累加到最大波峰，往下减

    private int mWidth;// 画布宽度
    private int mHeight;// 画布高度
    int mBaseHeight;// 上下分界线

    private View mView;
    private float mDensity;

    private Path mPath;
    private Paint mPaint;

    private static final int INTERVAL_TIME = 50;// 动画间隔时间
    private static final int ANIM_CYCLE_TIME = 3000;// 动画一周期时长
    private static final float ANIM_RATE = INTERVAL_TIME * 1.0f / ANIM_CYCLE_TIME;
    private float mCycleRate;// 水波动画进行的周期比率:(0,1]
    private boolean mIsWaveStoped;

    private boolean mIsHideMode;// 隐藏模式
    private float mHideRate;// 隐藏比率

    WaterWave(View view, float density) {
        mView = view;
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(PAY_COLOR);

        mDensity = density;
        mMaxCrest = (int) (60 * density);
        mCrest = mMaxCrest;
    }

    /**
     * 根据父view宽高初始化数据
     * 
     * @param width
     * @param height
     */
    void init(int width, int height) {
        mWidth = width;
        mHeight = height;
        mWaveLength = mWidth;
        mBaseHeight = (mHeight >> 1);
        mInitXOffset = (mWaveLength * 3 / 4);
        mXOffset = mInitXOffset;
    }

    void draw(Canvas canvas) {
        Log.d("test_anim", "draw WaterWave");
        canvas.drawColor(RECEIPT_COLOR);

        mPath.reset();
        mPath.moveTo(0, mHeight);
        // 画水波，当水波是停止状态且波峰为0时直接会直线
        final float crest = mIsHideMode ? mHideRate * mCrest : mCrest;
        if (mIsWaveStoped && crest <= 0) {
            mPath.lineTo(0, mBaseHeight);
            mPath.lineTo(mWidth, mBaseHeight);
        } else {
            int x0 = mXOffset - mWaveLength;
            mPath.lineTo(x0, mBaseHeight);
            int x1 = 0;
            int count = 0;
            do {
                x1 = x0 + count * mWaveLength;
                mPath.cubicTo(x1 + (mWaveLength >> 1), mBaseHeight + crest,
                        x1 + (mWaveLength >> 1), mBaseHeight - crest, x1 + mWaveLength,
                        mBaseHeight);
                count++;
            } while (x1 < mWidth);
        }
        mPath.lineTo(mWidth, mHeight);
        mPath.close();

        canvas.save();
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
        Log.d("test_anim", "draw WaterWave end");
    }

    void scroll(float deltaY) {
        mBaseHeight = mBaseHeight + (int) deltaY;
        if (mBaseHeight < 0) {
            mBaseHeight = 0;
        } else if (mBaseHeight > mHeight) {
            mBaseHeight = mHeight;
        }
        mView.invalidate();
    }

    /**
     * 启动水波动画
     */
    public void startAnim() {
        mView.removeCallbacks(mWaveAnimRunnable);
        mView.postDelayed(mWaveAnimRunnable, INTERVAL_TIME);
        mIsWaveStoped = false;
    }

    /**
     * 停止水波动画
     */
    public void stopAnim() {
        mView.removeCallbacks(mWaveAnimRunnable);
        mIsWaveStoped = true;
    }

    /**
     * 水波动画
     */
    private Runnable mWaveAnimRunnable = new Runnable() {

        @Override
        public void run() {
            if (mIsWaveStoped) {
                return;
            }
            if (!isInvalidate()) {
                mView.removeCallbacks(mWaveAnimRunnable);
                mView.postDelayed(mWaveAnimRunnable, INTERVAL_TIME);
                return;
            }
            if (mCycleRate >= 1.0f) {
                mCycleRate = 0;
            }
            Log.d("test_anim", "animrun rate = " + mCycleRate);
            mCycleRate = mCycleRate + ANIM_RATE;
            // 往右移
            mXOffset = (int) (mInitXOffset + mCycleRate * mWaveLength) % mWaveLength;
            // 波峰从最大到0再从0到最大
            if (mCycleRate <= 0.5f) {
                mCrest = (int) ((1 - 2 * mCycleRate) * mMaxCrest);
            } else {
                mCrest = (int) ((1 - 2 * (1 - mCycleRate)) * mMaxCrest);
            }

            Log.d("test_anim", "animrun rate = " + mCycleRate + ", mXOffset = " + mXOffset
                    + ", mCrest = " + mCrest);

            mView.invalidate();
            mView.removeCallbacks(mWaveAnimRunnable);
            mView.postDelayed(mWaveAnimRunnable, INTERVAL_TIME);
        }
    };

    static final int PFLAG_DRAWN = 0x00000020;
    static final int PFLAG_HAS_BOUNDS = 0x00000010;
    static final int PFLAG_DRAWING_CACHE_VALID = 0x00008000;
    static final int PFLAG_INVALIDATED = 0x80000000;

    private boolean isInvalidate() {
        Class<View> classView = View.class;
        try {
            Field field = classView.getDeclaredField("mPrivateFlags");
            if (field != null) {
                field.setAccessible(true);
                try {
                    int mPrivateFlags = field.getInt(mView);
                    Log.d("test_anim", "mPrivateFlags = " + mPrivateFlags);
                    if ((mPrivateFlags & (PFLAG_DRAWN | PFLAG_HAS_BOUNDS)) == (PFLAG_DRAWN | PFLAG_HAS_BOUNDS)
                            || (mPrivateFlags & PFLAG_DRAWING_CACHE_VALID) == PFLAG_DRAWING_CACHE_VALID
                            || (mPrivateFlags & PFLAG_INVALIDATED) != PFLAG_INVALIDATED) {
                        Log.d("test_anim", "enable invalidate");
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                return false;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return true;
    }

    void startHide() {
        stopAnim();
        mIsHideMode = true;
        mHideRate = 1;
    }

    void onHided() {
        mIsHideMode = false;
        clear();
    }

    void setHideRate(float rate) {
        mHideRate = rate;
    }

    void clear() {
        mXOffset = mInitXOffset;
        mCrest = 0;
        mCycleRate = 0.5f;
    }

}
