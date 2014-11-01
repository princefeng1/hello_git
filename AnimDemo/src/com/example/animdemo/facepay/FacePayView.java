
package com.example.animdemo.facepay;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.animdemo.LogUtil;
import com.example.animdemo.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class FacePayView extends View {
    private static final String TAG = "WaterWaveView";

    private PaintFlagsDrawFilter mDrawFilter;

    private float mDensity;

    private int mWidth;
    private int mHeight;

    private static final int DOWN_TEXT_COLOR = 0xff3898f2;// 向下的区域的文字颜色
    private static final int UP_TEXT_COLOR = 0xffffffff;// 向上的区域的文字颜色

    private WaterWave mWaterWave;
    private Arrows mArrows;

    private TextPaint mTextPaint;

    private String mUpText;// 向上的区域的文字
    private String mDownText;// 向下的区域的文字
    private float mUpTextX;
    private float mUpTextY;
    private float mDownTextX;
    private float mDownTextY;

    private static final int HIDE_ANIM_DURTION = 1000;
    private ValueAnimator mHideAnim;
    private boolean mIsHideAfterStartAnim = false;// 隐藏动画完成后是否需要启动

    /************************** 滑动相关begin ******************************/
    private float mLastMotionX;
    private float mLastMotionY;
    private static final int INVALID_POINTER = -1;
    private int mActivePointerId = INVALID_POINTER;
    private static final int SNAP_VELOCITY = 1000;// 抛得速度阀值
    private int mTouchSlop;
    private int mMaximumVelocity;

    private VelocityTracker mVelocityTracker;

    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;

    private ValueAnimator mSlideAnim;
    private static final int SLIDE_ANIM_DURTION = 1000;// 滑动的时间间隔

    /************************** 滑动相关end ******************************/

    private final static int MODE_SELECT = 0;// 选择模式
    private final static int MODE_PAY = 1;// 付款模式
    private final static int MODE_RECEIPT = 2;// 收款模式
    private int mMode = MODE_SELECT;

    public FacePayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mDensity = dm.density;

        mWaterWave = new WaterWave(this, mDensity);

        mArrows = new Arrows(this, mDensity);

        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(18 * mDensity);

    }

    public int getMode() {
        return mMode;
    }

    /**
     * 恢复
     * 
     * @param mode
     */
    public void restoreMode(int mode) {
        mMode = mode;
        if (mode == MODE_PAY) {
            post(new Runnable() {

                @Override
                public void run() {
                    stopAnim();
                    mWaterWave.mBaseHeight = 0;
                    mWaterWave.clear();
                    invalidate();
                }
            });
        } else if (mode == MODE_RECEIPT) {
            post(new Runnable() {

                @Override
                public void run() {
                    stopAnim();
                    mWaterWave.mBaseHeight = mHeight;
                    mWaterWave.clear();
                    invalidate();
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mWidth == 0 || mHeight == 0) {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            mWidth = width;
            mHeight = height;

            mWaterWave.init(width, height);

            mArrows.init(width, height);

            initSelectText();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("test_anim", "onDraw");
        canvas.setDrawFilter(mDrawFilter);

        mWaterWave.draw(canvas);

        mArrows.draw(canvas);

        if (mMode == MODE_SELECT) {
            drawSelectText(canvas);
        }

    }

    private void initSelectText() {
        mUpText = getResources().getString(R.string.facepay_select_pay_text);
        float textWidth = mTextPaint.measureText(mUpText);
        mUpTextX = (mWidth - textWidth) / 2;
        mUpTextY = ((mHeight >> 1) + (mHeight >> 2) + 60 * mDensity);
        mDownText = getResources().getString(R.string.facepay_select_receipt_text);
        textWidth = mTextPaint.measureText(mDownText);
        mDownTextX = (mWidth - textWidth) / 2;
        mDownTextY = (mHeight >> 2) - 60 * mDensity;

    }

    private void drawSelectText(Canvas canvas) {
        canvas.save();
        mTextPaint.setColor(UP_TEXT_COLOR);
        canvas.drawText(mUpText, mUpTextX, mUpTextY, mTextPaint);
        mTextPaint.setColor(DOWN_TEXT_COLOR);
        canvas.drawText(mDownText, mDownTextX, mDownTextY, mTextPaint);
        canvas.restore();
    }

    /**
     * 启动动画
     */
    public void startAnim() {
        if (LogUtil.DDBG) {
            LogUtil.d(TAG, "startAnim");
        }
        if (mMode != MODE_SELECT || mTouchState == TOUCH_STATE_SCROLLING) {
            return;
        }
        mWaterWave.startAnim();
        mArrows.startAnim();
    }

    /**
     * 停止动画
     */
    public void stopAnim() {
        if (LogUtil.DDBG) {
            LogUtil.d(TAG, "stopAnim");
        }
        mWaterWave.stopAnim();
        mArrows.stopAnim();
    }

    /**
     * 滑动时
     */
    private void hideAnim() {
        if (LogUtil.DDBG) {
            LogUtil.d(TAG, "onScrolling");
        }
        mIsHideAfterStartAnim = false;

        if (mHideAnim != null && !mHideAnim.isRunning()) {
            mHideAnim.start();
            return;
        }
        ValueAnimator va = ValueAnimator.ofFloat(1f, 0f);
        mHideAnim = va;
        va.setDuration(HIDE_ANIM_DURTION);
        va.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float rate = ((Float) (animation.getAnimatedValue())).floatValue();
                mWaterWave.setHideRate(rate);
                mArrows.setHideAlphasRate(rate);
                invalidate();
            }
        });
        va.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mWaterWave.startHide();
                mArrows.startHide();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mWaterWave.onHided();
                mArrows.onHided();
                // 需要再启动
                if (mIsHideAfterStartAnim) {
                    if (LogUtil.DDBG) {
                        LogUtil.d(TAG, "hideWave onAnimationEnd");
                    }
                    startAnim();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        va.start();
    }

    private final static int MOVE_MODE_NONE = -1;
    private final static int MOVE_MODE_PREPARE = 0;// 进入滑动状态
    private final static int MOVE_MODE_UP = 1;// 向上滑
    private final static int MOVE_MODE_DOWN = 2;// 向下滑
    private int mMoveMode = MOVE_MODE_NONE;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mMode != MODE_SELECT) {
            return super.onTouchEvent(event);
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX();
                final float y = event.getY();
                mLastMotionX = x;
                mLastMotionY = y;
                mActivePointerId = event.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    float deltaY = y - mLastMotionY;
                    if (mMoveMode == MOVE_MODE_UP) {
                        if (deltaY > 0 && (mWaterWave.mBaseHeight + deltaY > (mHeight >> 1))) {
                            deltaY = 0;
                        }
                    } else {
                        if (deltaY < 0 && (mWaterWave.mBaseHeight + deltaY < (mHeight >> 1))) {
                            deltaY = 0;
                        }
                    }
                    mLastMotionY = y;
                    if (deltaY != 0) {
                        mWaterWave.scroll(deltaY);
                    }
                } else {
                    final int xDiff = (int) Math.abs(x - mLastMotionX);
                    final int yDiff = (int) Math.abs(y - mLastMotionY);
                    final int touchSlop = mTouchSlop;
                    boolean xMoved = xDiff > touchSlop;
                    boolean yMoved = yDiff > touchSlop;
                    if (mMoveMode == MOVE_MODE_NONE && (yMoved || xMoved)) {
                        mMoveMode = MOVE_MODE_PREPARE;
                    }
                    if (yMoved && !xMoved) {
                        if (mMoveMode == MOVE_MODE_PREPARE) {
                            if (mLastMotionY >= (mHeight >> 1)) {
                                mMoveMode = MOVE_MODE_UP;
                            } else {
                                mMoveMode = MOVE_MODE_DOWN;
                            }
                        }
                        if (mMoveMode == MOVE_MODE_UP) {
                            if (y > mLastMotionY) {
                                mLastMotionX = x;
                                mLastMotionY = y;
                                break;
                            }
                        } else {
                            if (y < mLastMotionY) {
                                mLastMotionX = x;
                                mLastMotionY = y;
                                break;
                            }
                        }
                        mTouchState = TOUCH_STATE_SCROLLING;
                        hideAnim();
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    // 向上小于四分之一高时滑上去，向下低于四分之三高时滑下去
                    int baseHeight = mWaterWave.mBaseHeight;
                    if (baseHeight <= (mHeight >> 1) - 50 * mDensity) {
                        startSlideUp();
                    } else if (baseHeight >= (mHeight >> 1) + 50 * mDensity) {
                        startSlideDown();
                    } else {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        final int velocityY = (int) velocityTracker.getYVelocity();
                        if (mMoveMode == MOVE_MODE_DOWN && velocityY > SNAP_VELOCITY
                                && baseHeight >= (mHeight >> 1)) {// 向下抛
                            startSlideDown();
                        } else if (mMoveMode == MOVE_MODE_UP && velocityY < -SNAP_VELOCITY
                                && baseHeight <= (mHeight >> 1)) {// 向上抛
                            startSlideUp();
                        } else {
                            // 返回中间 并开启水波
                            startSlideMiddle();
                        }
                    }
                } else if (mMoveMode == MOVE_MODE_NONE) {
                    final int pointerIndex = event.findPointerIndex(mActivePointerId);
                    final float y = event.getY(pointerIndex);
                    if (y < mWaterWave.mBaseHeight) {
                        hideAnim();
                        startSlideDown();
                    } else {
                        hideAnim();
                        startSlideUp();
                    }
                }
                mTouchState = TOUCH_STATE_REST;
                mMoveMode = MOVE_MODE_NONE;
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                mMoveMode = MOVE_MODE_NONE;
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = ev.getX(newPointerIndex);
            mLastMotionY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    /**
     * 开启动画向上滑
     */
    public void startSlideUp() {
        mMode = MODE_PAY;
        startSlide(0);
    }

    /**
     * 开启动画向下滑
     */
    public void startSlideDown() {
        mMode = MODE_RECEIPT;
        startSlide(mHeight);
    }

    /**
     * 开启动画返回到中间
     */
    public void startSlideMiddle() {
        mMode = MODE_SELECT;
        startSlide((mHeight >> 1));
    }

    /**
     * 动画滑向某个高度
     * 
     * @param targetHeight
     */
    private void startSlide(int targetHeight) {
        final int baseHeight = mWaterWave.mBaseHeight;
        final int distance = targetHeight - baseHeight;
        if (LogUtil.DDBG) {
            LogUtil.d(TAG, "startSlide targetHeight = " + targetHeight + ", mBaseHeight = "
                    + baseHeight);
        }
        int duration = Math.abs(distance) * SLIDE_ANIM_DURTION / (mHeight >> 1);

        if (mSlideAnim != null && !mSlideAnim.isRunning()) {
            mSlideAnim.setIntValues(baseHeight, baseHeight + distance);
            mSlideAnim.setDuration(duration);
            mSlideAnim.start();
            return;
        }
        ValueAnimator va = ValueAnimator.ofInt(baseHeight, baseHeight + distance);
        mSlideAnim = va;
        va.setDuration(duration);
        va.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWaterWave.mBaseHeight = ((Integer) animation.getAnimatedValue()).intValue();
                invalidate();
            }
        });
        va.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                if (mMode == MODE_SELECT) {
                    if (mHideAnim != null && mHideAnim.isRunning()) {
                        mIsHideAfterStartAnim = true;
                    } else {
                        post(new Runnable() {

                            @Override
                            public void run() {
                                startAnim();
                            }
                        });
                    }
                } else if (mMode == MODE_RECEIPT) {
                    if (mOnListener != null) {
                        mOnListener.onIntoReceiptMode();
                    }
                } else if (mMode == MODE_PAY) {
                    if (mOnListener != null) {
                        mOnListener.onIntoPayMode();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
            }
        });
        va.start();
    }

    private OnListener mOnListener;

    public void setOnListener(OnListener listener) {
        mOnListener = listener;
    }

    public interface OnListener {
        void onIntoPayMode();

        void onIntoReceiptMode();
    }

}
