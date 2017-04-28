package com.camnter.newlife.widget.canvas;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Description：CanvasClipView
 * Created by：CaMnter
 * Time：2016-02-29 17:02
 */
public class CanvasClipView extends View {

    private Paint mPaint;
    private Path mPath;


    public CanvasClipView(Context context) {
        super(context);
        this.init(context);
    }


    public CanvasClipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }


    public CanvasClipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CanvasClipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init(context);
    }


    private void init(Context context) {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(6.6f);
        this.mPaint.setTextSize(32.0f);
        this.mPaint.setTextAlign(Paint.Align.CENTER);
        this.mPath = new Path();
    }


    private void drawOriginalView(Canvas canvas) {
        canvas.clipRect(0, 0, 280, 280);
        canvas.drawColor(Color.WHITE);

        this.mPaint.setColor(0xff377FC1);
        canvas.drawLine(0, 0, 280, 280, this.mPaint);

        this.mPaint.setColor(0xffFCD20A);
        canvas.drawCircle(80, 200, 70, this.mPaint);

        this.mPaint.setColor(0xffE02026);
        canvas.drawText("CaMnter", 210, 70, this.mPaint);
    }


    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override protected void onDraw(Canvas canvas) {

        /**
         * android.graphics.Region.Op:
         *
         * Region.Op.INTERSECT 取两者交集，默认的方式
         * Region.Op.DIFFERENCE 第一次上减去与第而次的交集
         * Region.Op.REPLACE 显示第二次的
         * Region.Op.REVERSE_DIFFERENCE 第二次上减去与第一次的交集
         * Region.Op.UNION 取全集
         * Region.Op.XOR 取补集，就是全集的减去交集的剩余部分显示
         *
         */

        // 正常
        canvas.drawColor(Color.GRAY);
        canvas.save();
        canvas.translate(60, 60);
        this.drawOriginalView(canvas);
        canvas.restore();

        // Region.Op.DIFFERENCE 第一次上减去与第而次的交集
        canvas.save();
        canvas.translate(600, 60);
        canvas.clipRect(0, 0, 260, 260);
        canvas.clipRect(200, 200, 280, 280, Region.Op.DIFFERENCE);
        this.drawOriginalView(canvas);
        canvas.restore();

        // Region.Op.REPLACE 显示第二次的
        canvas.save();
        canvas.translate(60, 600);
        this.mPath.reset();
        canvas.clipPath(this.mPath);
        this.mPath.addCircle(100, 100, 100, Path.Direction.CCW);
        canvas.clipPath(this.mPath, Region.Op.REPLACE);
        this.drawOriginalView(canvas);
        canvas.restore();

        // Region.Op.UNION 取全集
        canvas.save();
        canvas.translate(600, 600);
        canvas.clipRect(0, 0, 160, 160);
        canvas.clipRect(120, 120, 280, 280, Region.Op.UNION);
        this.drawOriginalView(canvas);
        canvas.restore();

        // Region.Op.XOR 取补集，就是全集的减去交集的剩余部分显示
        canvas.save();
        canvas.translate(60, 1200);
        canvas.clipRect(0, 0, 160, 160);
        canvas.clipRect(120, 120, 280, 280, Region.Op.XOR);
        this.drawOriginalView(canvas);
        canvas.restore();

        // Region.Op.REVERSE_DIFFERENCE 第二次上减去与第一次的交集
        canvas.save();
        canvas.translate(600, 1200);
        canvas.clipRect(0, 0, 170, 170);
        canvas.clipRect(140, 140, 280, 280, Region.Op.REVERSE_DIFFERENCE);
        this.drawOriginalView(canvas);
        canvas.restore();
    }
}
