
package com.example.animdemo.facepay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * 气泡动画 3个气泡依次循环变化：半径大小、透明度、位置
 * 
 * @author dang
 */
public class BubbleView extends View {

    private int mWidth;
    private int mHeight;

    private Bubble mBubble1;
    private Bubble mBubble2;
    private Bubble mBubble3;

    private boolean mIsStartAnim;
    private static final int ANIM_INTERVAL_TIME = 50;// 动画间隔
    private static final int ANIM_CYCLE_TIME = 3000;// 动画一周期时间
    private static final float ANIM_RATE = ANIM_INTERVAL_TIME * 1.0f / ANIM_CYCLE_TIME;//
    private float mCycleRate1 = 0;// 气泡1已动画的周期比率 0:刚开始显示
    private float mCycleRate2 = -1;// 气泡2已动画的周期比率 -1:不显示
    private float mCycleRate3 = -1;// 气泡3已动画的周期比率 -1:不显示

    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int radius = (int) (15 * dm.density);// 默认半径
        // 默认气泡
        mBubble1 = new Bubble(radius, 0.25f, 0xffffffff, dm.density);
        mBubble2 = new Bubble(radius, 0.25f, 0xffffffff, dm.density);
        mBubble3 = new Bubble(radius, 0.25f, 0xffffffff, dm.density);

        DecelerateInterpolator interpolator = new DecelerateInterpolator();
        mBubble1.setInterpolator(interpolator);
        mBubble2.setInterpolator(interpolator);
        mBubble3.setInterpolator(interpolator);
    }

    /**
     * 设置气泡颜色、透明度比率
     * 
     * @param color
     * @param alphaRate
     */
    public void setColor(int color, float alphaRate) {
        mBubble1.setColor(color, alphaRate);
        mBubble2.setColor(color, alphaRate);
        mBubble3.setColor(color, alphaRate);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mWidth == 0 || mHeight == 0) {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            mWidth = width;
            mHeight = height;

            mBubble1.init((mWidth >> 2), mHeight, width, height);
            mBubble2.init((mWidth >> 1), mHeight, width, height);
            mBubble3.init((mWidth >> 1) + (mWidth >> 2), mHeight, width, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mBubble1.draw(canvas);
        mBubble2.draw(canvas);
        mBubble3.draw(canvas);
    }

    /**
     * 开启动画 view未显示不开启
     */
    public void startAnim() {
        if (getVisibility() != View.VISIBLE) {
            return;
        }
        mIsStartAnim = true;
        removeCallbacks(mAnimRunnable);
        postDelayed(mAnimRunnable, ANIM_INTERVAL_TIME);
    }

    /**
     * 停止动画
     */
    public void stopAnim() {
        mIsStartAnim = false;
        removeCallbacks(mAnimRunnable);
    }

    /**
     * 清理数据
     */
    public void clear() {
        mCycleRate1 = 0;
        mCycleRate2 = -1;
        mCycleRate3 = -1;
    }

    private Runnable mAnimRunnable = new Runnable() {

        @Override
        public void run() {
            if (!mIsStartAnim) {// 动画已停止直接返回
                return;
            }
            final float rate = ANIM_RATE;
            // 气泡的动画比率大于等于0的开始累加，累加到1时改为-1（不显示）；
            if (mCycleRate1 >= 0) {
                mCycleRate1 += rate;
                if (mCycleRate1 > 1.0f) {
                    mCycleRate1 = -1;
                }
            } else {
                // 气泡3跑到某一刻时激活气泡1
                if (mCycleRate3 > 0.5f) {
                    mCycleRate1 = 0;
                }
            }
            if (mCycleRate2 >= 0) {
                mCycleRate2 += rate;
                if (mCycleRate2 > 1.0f) {
                    mCycleRate2 = -1;
                }
            } else {
                // 气泡1跑到某一刻时激活气泡2
                if (mCycleRate1 > 0.3f) {
                    mCycleRate2 = 0;
                }
            }
            if (mCycleRate3 >= 0) {
                mCycleRate3 += rate;
                if (mCycleRate3 > 1.0f) {
                    mCycleRate3 = -1;
                }
            } else {
                // 气泡2跑到某一刻时激活气泡3
                if (mCycleRate2 > 0.5f) {
                    mCycleRate3 = 0;
                }
            }
            mBubble1.changeData(mCycleRate1);
            mBubble2.changeData(mCycleRate2);
            mBubble3.changeData(mCycleRate3);

            invalidate();
            removeCallbacks(mAnimRunnable);
            postDelayed(mAnimRunnable, ANIM_INTERVAL_TIME);
        }
    };

    static class Bubble {
        private int mRadius;// 最大半径
        private float mRadiusRate;// 半径变化比率 [0.5, 1]
        private int mAlpha;// 最大透明度

        private Paint mPaint;

        private int mWidth;
        private int mHeight;
        private int mInitY;// 起点y值
        private int mInitX;// 起点x值
        private int mY;// 当前y值
        private int mX;// 当前x值

        private boolean mIsDraw = true;

        private Interpolator mInterpolator;

        Bubble(int radius, float alphaRate, int color, float density) {
            mRadius = radius;
            mAlpha = (int) (255 * alphaRate);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(color);
            mPaint.setAlpha(mAlpha);

        }

        /**
         * 初始化初始位置
         * 
         * @param initX
         * @param initY
         * @param width
         * @param height
         */
        void init(int initX, int initY, int width, int height) {
            mWidth = width;
            mHeight = height;
            mInitX = initX;
            mInitY = initY;
            mX = mInitX;
            mY = mInitY;
        }

        void setColor(int color, float alphaRate) {
            mPaint.setColor(color);
            mAlpha = (int) (255 * alphaRate);
            mPaint.setAlpha(mAlpha);
        }

        void changeData(float rate) {
            if (rate < 0) {
                mIsDraw = false;
                return;
            }
            mIsDraw = true;
            if (mInterpolator != null) {
                rate = mInterpolator.getInterpolation(rate);
            }
            float yOffset = mHeight * rate;
            int h = mHeight;
            int w = (int) ((mWidth >> 2) - mRadius);

            // 一个抛物线
            float xOffset = 4.0f * w * yOffset * yOffset / (h * h) - 4.0f * w * yOffset / h;
            mX = (int) (mInitX + xOffset);
            mY = (int) (mInitY - yOffset);
            if (rate > 0.5) {
                mRadiusRate = 0.5f + 0.5f * (1 - rate) * 2;// 半径减小(1->0.5)
                float alphaRate = (1 - rate) * 2;// 透明度减小(1->0)
                mPaint.setAlpha((int) (mAlpha * alphaRate));
            } else {
                mRadiusRate = 0.5f + 0.5f * rate * 2;// 半径增大 (0.5->1)
                float alphaRate = 0.7f + 0.3f * rate * 2;// 透明度增大(0.7->1)
                mPaint.setAlpha((int) (mAlpha * alphaRate));
            }
        }

        void draw(Canvas canvas) {
            if (!mIsDraw) {
                return;
            }
            canvas.drawCircle(mX, mY, mRadius * mRadiusRate, mPaint);
        }

        void setInterpolator(Interpolator interpolator) {
            mInterpolator = interpolator;
        }
    }

}
