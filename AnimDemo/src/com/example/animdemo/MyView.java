package com.example.animdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

public class MyView extends View {
    /**
     * 重要参数，两点之间分为几段描画，数字愈大分段越多，描画的曲线就越精细.
     */
    private static final int STEPS = 12;
    Paint paint;
    Path linePath;
    Path curvePath;
    List<Point> points;
    List<Integer> points_x;
    List<Integer> points_y;

    boolean drawLineFlag;
    boolean drawCurveFlag;

    /**
     * struct.
     * 
     * @param context
     */
    public MyView(Context context) {
        super(context);
        initObj();
    }

    /**
     * struct.
     * 
     * @param context
     * @param attrs
     */
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initObj();
    }

    /**
     * struct.
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initObj();
    }

    /**
     * 计算曲线.
     * 
     * @param x
     * @return
     */
    private List<Cubic> calculate(List<Integer> x) {
        int n = x.size() - 1;
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] D = new float[n + 1];
        int i;
        /*
         * We solve the equation [2 1 ] [D[0]] [3(x[1] - x[0]) ] |1 4 1 | |D[1]|
         * |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | | ..... | | . | | . | | 1 4
         * 1| | . | |3(x[n] - x[n-2])| [ 1 2] [D[n]] [3(x[n] - x[n-1])]
         * 
         * by using row operations to convert the matrix to upper triangular and
         * then back sustitution. The D[i] are the derivatives at the knots.
         */

        gamma[0] = 1.0f / 2.0f;
        for (i = 1; i < n; i++) {
            gamma[i] = 1 / (4 - gamma[i - 1]);
        }
        gamma[n] = 1 / (2 - gamma[n - 1]);

        delta[0] = 3 * (x.get(1) - x.get(0)) * gamma[0];
        for (i = 1; i < n; i++) {
            delta[i] = (3 * (x.get(i + 1) - x.get(i - 1)) - delta[i - 1])
                    * gamma[i];
        }
        delta[n] = (3 * (x.get(n) - x.get(n - 1)) - delta[n - 1]) * gamma[n];

        D[n] = delta[n];
        for (i = n - 1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i] * D[i + 1];
        }

        /* now compute the coefficients of the cubics */
        List<Cubic> cubics = new LinkedList<Cubic>();
        for (i = 0; i < n; i++) {
            Cubic c = new Cubic(x.get(i), D[i], 3 * (x.get(i + 1) - x.get(i))
                    - 2 * D[i] - D[i + 1], 2 * (x.get(i) - x.get(i + 1)) + D[i]
                    + D[i + 1]);
            cubics.add(c);
        }
        return cubics;
    }

    /**
     * 清除屏幕上的点.
     */
    public void clearPoints() {
        points.clear();
        invalidate();
    }

    /**
     * 画曲线.
     * 
     * @param canvas
     */
    private void drawCurve(Canvas canvas) {
        paint.setColor(Color.RED);
        points_x.clear();
        points_y.clear();
        for (int i = 0; i < points.size(); i++) {
            points_x.add(points.get(i).x);
            points_y.add(points.get(i).y);
        }

        List<Cubic> calculate_x = calculate(points_x);
        List<Cubic> calculate_y = calculate(points_y);
        curvePath
                .moveTo(calculate_x.get(0).eval(0), calculate_y.get(0).eval(0));

        for (int i = 0; i < calculate_x.size(); i++) {
            for (int j = 1; j <= STEPS; j++) {
                float u = j / (float) STEPS;
                curvePath.lineTo(calculate_x.get(i).eval(u), calculate_y.get(i)
                        .eval(u));
            }
        }
        canvas.drawPath(curvePath, paint);
    }

    /**
     * 画点.
     * 
     * @param canvas
     */
    private void drawPoints(Canvas canvas) {
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            canvas.drawCircle(p.x, p.y, 5, paint);
        }
    }

    /**
     * 初始化.
     */
    private void initObj() {
        paint = new Paint();
        linePath = new Path();
        curvePath = new Path();
        points = new LinkedList<Point>();
        points_x = new LinkedList<Integer>();
        points_y = new LinkedList<Integer>();
        drawLineFlag = true;
        drawCurveFlag = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        drawPoints(canvas);

        linePath.reset();
        curvePath.reset();

        if (drawLineFlag && points.size() > 1) {
            paint.setColor(Color.GREEN);
            linePath.moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1; i < points.size(); i++) {
                linePath.lineTo(points.get(i).x, points.get(i).y);
            }
            canvas.drawPath(linePath, paint);
        }

        if (points.size() > 2) {
            drawCurve(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        points.add(new Point((int) event.getX(), (int) event.getY()));
        invalidate();
        return super.onTouchEvent(event);
    }
    
    /** this class represents a cubic polynomial */

    public class Cubic {

      float a,b,c,d;         /* a + b*u + c*u^2 +d*u^3 */

      public Cubic(float a, float b, float c, float d){
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
      }

      
      /** evaluate cubic */
      public float eval(float u) {
        return (((d*u) + c)*u + b)*u + a;
      }
    }
}
