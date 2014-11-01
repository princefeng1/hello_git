
package com.example.animdemo.facepay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/**
 * 向下、向上箭头：先出现向下的，当第三个点出来时再开始出现向上的
 * 
 * @author dang
 */
public class Arrows {

    private Arrow mUpArrow;// 向上箭头
    private Arrow mDownArrow;// 向下箭头

    private View mView;

    private boolean mUpNeedAnim;// 向上箭头开始不需要动画
    private boolean mDownNeedAnim = true;// 向下箭头开始需要动画 
    
    private boolean mIsStartAnim;
    private static final int ANIM_INTERVAL_TIME = 150;// 动画时间间隔

    Arrows(View view, float density) {
        mView = view;
        mUpArrow = new Arrow(Arrow.DIRECT_UP, density);
        mDownArrow = new Arrow(Arrow.DIRECT_DOWN, density);
    }

    /**
     * 根据父view的宽高初始化箭头数据
     * @param width
     * @param height
     */
    void init(int width, int height) {
        mUpArrow.init(width, height);
        mDownArrow.init(width, height);
    }

    void draw(Canvas canvas) {
        mUpArrow.draw(canvas);
        mDownArrow.draw(canvas);
    }

    /**
     * 开启动画
     */
    void startAnim() {
        if (!mDownNeedAnim || !mDownArrow.mIsAnimMode) {
            return;
        }
        mIsStartAnim = true;
        mView.removeCallbacks(mAnimRunnable);
        mView.postDelayed(mAnimRunnable, ANIM_INTERVAL_TIME);
    }

    /**
     * 停止动画
     */
    void stopAnim() {
        mDownNeedAnim = true;
        mUpNeedAnim = false;
        mIsStartAnim = false;
        mView.removeCallbacks(mAnimRunnable);
    }

    private Runnable mAnimRunnable = new Runnable() {

        @Override
        public void run() {
            if (!mIsStartAnim) {
                return;
            }
            if (mDownNeedAnim) {
                mDownArrow.changeAlphas();
                // 在向下箭头动画到某一刻时向下箭头开始
                if (!mUpNeedAnim && mDownArrow.mAlphasIndex[2] > 0) {
                    mUpNeedAnim = true;
                }
                if (mDownArrow.isHeadLight()) {
                    mDownNeedAnim = false;
                }
            }
            if (mUpNeedAnim) {
                mUpArrow.changeAlphas();
                if (mUpArrow.isHeadLight()) {
                    mUpNeedAnim = false;
                    mIsStartAnim = false;
                }
            }

            mView.invalidate();
            mView.removeCallbacks(mAnimRunnable);
            mView.postDelayed(mAnimRunnable, ANIM_INTERVAL_TIME);
        }
    };

    /**
     * 开始隐藏
     */
    void startHide() {
        stopAnim();
        mUpArrow.startHide();
        mDownArrow.startHide();
    }

    /**
     * 已隐藏
     */
    void onHided() {
        mUpArrow.stopHide();
        mDownArrow.stopHide();

        mUpArrow.clear();
        mDownArrow.clear();
    }

    /**
     * 改变隐藏比率
     * @param rate
     */
    void setHideAlphasRate(float rate) {
        mUpArrow.setHideAlphasRate(rate);
        mDownArrow.setHideAlphasRate(rate);
    }

    /**
     * 箭头，四个点、头 动画：透明度变化依次显示出来
     * 
     * @author dang
     */
    static class Arrow {

        private static final int UP_COLOR = 0xffffffff;
        private static final int DOWN_COLOR = 0xff3898f2;

        static final int DOT_COUNT = 4;
        static final int DIRECT_UP = 0;// 指向上
        static final int DIRECT_DOWN = 1;// 指向下

        private static final int DOT_WIDTH = 6;// dp
        private static final int DOTS_DISTANCE = 12;// dp
        private static final int HEAD_WIDTH = 20;// dp
        private static final int HEAD_HEIGHT = 10;// dp
        private static final int HEAD_STROKE_WIDTH = 3;

        private int mDirect = DIRECT_UP;// 指向

        private Paint mDotPaint;
        private Paint mHeadPaint;
        private Path mHeadPath;
        private RectF[] mDotRects;

        boolean mIsAnimMode = true;// 是否动画模式

        private static final float[] ALPHA_VALUES = {
                0f, 0.25f, 0.5f, 0.75f, 1f
        };

        static final int ALPHA_MAX_INDEX = ALPHA_VALUES.length - 1;// 最大透明度
        private static final int ALPHA_START_INDEX = 1;// 开始的透明度
        private static final int ALPHA_PAUSE_INDEX = 2;// 不动的点得透明度

        int[] mAlphasIndex;// 点和箭头的透明度
        int mAnimCursor = 0;// 指向当前开始亮的

        private float mDensity;

        private boolean mIsStartHide;// 是否开始隐藏
        private float mHideAlphasRate = 1;// 隐藏时透明度变化的比率

        Arrow(int direct, float density) {
            mDirect = direct;
            mDensity = density;

            mDotRects = new RectF[DOT_COUNT];
            mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mHeadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mHeadPaint.setStyle(Paint.Style.STROKE);
            mHeadPaint.setStrokeWidth(HEAD_STROKE_WIDTH * density);
            if (mDirect == DIRECT_DOWN) {
                mDotPaint.setColor(DOWN_COLOR);
                mHeadPaint.setColor(DOWN_COLOR);
            } else {
                mDotPaint.setColor(UP_COLOR);
                mHeadPaint.setColor(UP_COLOR);
            }
            mHeadPath = new Path();

            mAlphasIndex = new int[DOT_COUNT + 1];
            if (!mIsAnimMode) {
                for (int i = 0; i < DOT_COUNT; i++) {
                    mAlphasIndex[i] = ALPHA_PAUSE_INDEX;
                }
                mAlphasIndex[DOT_COUNT] = ALPHA_MAX_INDEX;
            }
        }

        /**
         * 初始化点和头在画布上的区域
         * 
         * @param width 画布宽度
         * @param height 画布高度
         */
        void init(int width, int height) {
            int beginHeight;
            if (mDirect == DIRECT_DOWN) {
                beginHeight = (height >> 2);// 向下起点在四分之一高度处
            } else {
                beginHeight = (height >> 1) + (height >> 2);// 向上起点在四分之三高度处
            }
            // 初始化四个点的区域
            int dotsDistance = (int) (DOTS_DISTANCE * mDensity);
            int dotWidth = (int) (DOT_WIDTH * mDensity);
            int dotLeft = ((width - dotWidth) >> 1);
            initDots(beginHeight, dotsDistance, dotWidth, dotLeft);

            // 箭头path （向下或向上）
            int headWidth = (int) (HEAD_WIDTH * mDensity);
            int headHeight = (int) (HEAD_HEIGHT * mDensity);
            int headLeft = ((width - headWidth) >> 1);
            initHead(headWidth, headHeight, headLeft, (dotsDistance >> 1));
        }

        /**
         * 初始化点的区域
         * 
         * @param firstDotTop 第一个点的上边
         * @param firstDotBottom 第一个点的下边
         * @param dotsDistance 点间距
         * @param dotWidth 点直径
         * @param dotLeft 左边
         */
        private void initDots(int beginHeight, int dotsDistance, int dotWidth,
                int dotLeft) {
            int firstDotTop;
            int firstDotBottom;
            if (mDirect == DIRECT_DOWN) {
                firstDotTop = beginHeight;
                firstDotBottom = beginHeight + dotWidth;
            } else {
                dotsDistance = -dotsDistance;
                firstDotTop = beginHeight - dotWidth;
                firstDotBottom = beginHeight;
            }
            for (int i = 0; i < DOT_COUNT; i++) {
                mDotRects[i] = new RectF(dotLeft, firstDotTop + i * dotsDistance, dotLeft
                        + dotWidth, firstDotBottom + i * dotsDistance);
            }
        }

        /**
         * 初始化头的区域
         * 
         * @param headWidth
         * @param headHeight
         * @param headLeft
         * @param distance 距离点的距离
         */
        private void initHead(int headWidth, int headHeight, int headLeft, int distance) {
            float headTopX = headLeft + (headWidth >> 1);
            float headTopY;
            float headBottomY;
            if (mDirect == DIRECT_DOWN) {
                headBottomY = mDotRects[DOT_COUNT - 1].bottom + distance;
                headTopY = headBottomY + headHeight;
            } else {
                headBottomY = mDotRects[DOT_COUNT - 1].top - distance;
                headTopY = headBottomY - headHeight;
            }
            mHeadPath.moveTo(headLeft, headBottomY);
            mHeadPath.lineTo(headTopX, headTopY);
            mHeadPath.lineTo(headLeft + headWidth, headBottomY);
        }

        void draw(Canvas canvas) {
            canvas.save();

            for (int i = 0; i < DOT_COUNT; i++) {
                int alpha = getAlpha(mAlphasIndex[i]);
                if (alpha == 0) {
                    continue;
                }
                mDotPaint.setAlpha(alpha);
                canvas.drawOval(mDotRects[i], mDotPaint);
            }
            int alpha = getAlpha(mAlphasIndex[DOT_COUNT]);
            if (alpha > 0) {
                mHeadPaint.setAlpha(alpha);
                canvas.drawPath(mHeadPath, mHeadPaint);
            }
            canvas.restore();
        }

        private int getAlpha(int index) {
            float a = ALPHA_VALUES[index] * 255;
            if (mIsStartHide) {
                a = a * mHideAlphasRate;
            }
            return (int) a;
        }

        /**
         * 改变透明度
         */
        void changeAlphas() {
            // 当前指向的 从1加到3 到3时，如果前面的大于2则依次降到2，之后下一个从1开始
            int count = mAlphasIndex.length;
            int alphaIndex = mAlphasIndex[mAnimCursor];
            if (alphaIndex < ALPHA_MAX_INDEX) {
                mAlphasIndex[mAnimCursor]++;
            } else {
                int lastIndex = (mAnimCursor - 1 + count) % count;
                if (mAlphasIndex[lastIndex] > ALPHA_PAUSE_INDEX) {
                    mAlphasIndex[lastIndex]--;
                } else {
                    mAnimCursor = (mAnimCursor + 1) % count;
                    if (mAlphasIndex[mAnimCursor] == 0) {
                        mAlphasIndex[mAnimCursor] = ALPHA_START_INDEX;
                    }
                }
            }
        }

        /**
         * 头亮了：仅箭头透明度为最大
         * 
         * @return
         */
        boolean isHeadLight() {
            return mAlphasIndex[DOT_COUNT] == ALPHA_MAX_INDEX && mAnimCursor == 0;
        }

        void startHide() {
            mIsStartHide = true;
            mHideAlphasRate = 1;
        }

        void stopHide() {
            mIsStartHide = false;
        }

        void clear() {
            if (mIsAnimMode) {
                for (int i = 0; i < mAlphasIndex.length; i++) {
                    mAlphasIndex[i] = 0;
                }
                mAnimCursor = 0;
            }
        }

        void setHideAlphasRate(float rate) {
            mHideAlphasRate = rate;
        }
    }
}
