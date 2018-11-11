package me.ele.uetool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import me.ele.uetool.base.Element;

import static me.ele.uetool.base.DimenUtil.dip2px;

/**
 * 「相对坐标」功能的「相对坐标布局」
 */
@SuppressWarnings("DanglingJavadoc")
public class RelativePositionLayout extends CollectViewsLayout {

    private final int elementsNum = 2;
    private Paint areaPaint = new Paint() {
        {
            setAntiAlias(true);
            setColor(Color.RED);
            setStyle(Style.STROKE);
            setStrokeWidth(dip2px(1));
        }
    };

    private Element[] relativeElements = new Element[elementsNum];
    private int searchCount = 0;

    public RelativePositionLayout(Context context) {
        this(context, null);
    }

    public RelativePositionLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelativePositionLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:

                /**
                 * 根据坐标拿到目标元素，塞到 relativeElements 里
                 * relativeElements 对应的 index 变化 0 1 0 1 0 1 0 1... 循环
                 * 「invalidate」触发重绘「onDraw」
                 */
                final Element element = getTargetElement(event.getX(), event.getY());
                if (element != null) {
                    relativeElements[searchCount % elementsNum] = element;
                    searchCount++;
                    invalidate();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (relativeElements == null) {
            return;
        }

        boolean doubleNotNull = true;

        /**
         * 先绘制 元素到屏幕的相对 dp
         */
        for (Element element : relativeElements) {
            if (element != null) {
                Rect rect = element.getRect();
                canvas.drawLine(0, rect.top, screenWidth, rect.top, dashLinePaint);
                canvas.drawLine(0, rect.bottom, screenWidth, rect.bottom, dashLinePaint);
                canvas.drawLine(rect.left, 0, rect.left, screenHeight, dashLinePaint);
                canvas.drawLine(rect.right, 0, rect.right, screenHeight, dashLinePaint);
                canvas.drawRect(rect, areaPaint);
            } else {
                doubleNotNull = false;
            }
        }

        /**
         * 再绘制 两个元素之间的相对 dp
         */
        if (doubleNotNull) {
            Rect firstRect = relativeElements[searchCount % elementsNum].getRect();
            Rect secondRect = relativeElements[(searchCount - 1) % elementsNum].getRect();

            /**
             * 第二个元素 在 第一个元素 下边
             */
            if (secondRect.top > firstRect.bottom) {
                int x = secondRect.left + secondRect.width() / 2;
                drawLineWithText(canvas, x, firstRect.bottom, x, secondRect.top);
            }

            /**
             * 第一个元素 在 第二个元素 下边
             */
            if (firstRect.top > secondRect.bottom) {
                int x = secondRect.left + secondRect.width() / 2;
                drawLineWithText(canvas, x, secondRect.bottom, x, firstRect.top);
            }

            /**
             * 第二个元素 在 第一个元素 右边
             */
            if (secondRect.left > firstRect.right) {
                int y = secondRect.top + secondRect.height() / 2;
                drawLineWithText(canvas, secondRect.left, y, firstRect.right, y);
            }

            /**
             * 第一个元素 在 第二个元素 右边
             */
            if (firstRect.left > secondRect.right) {
                int y = secondRect.top + secondRect.height() / 2;
                drawLineWithText(canvas, secondRect.right, y, firstRect.left, y);
            }

            drawNestedAreaLine(canvas, firstRect, secondRect);
            drawNestedAreaLine(canvas, secondRect, firstRect);
        }
    }

    /**
     * 第二个元素 在 第一个元素 里边
     * 就是 第二个元素 是 第一个元素 的 child view
     */
    private void drawNestedAreaLine(Canvas canvas, Rect firstRect, Rect secondRect) {

        if (secondRect.left >= firstRect.left && secondRect.right <= firstRect.right && secondRect.top >= firstRect.top && secondRect.bottom <= firstRect.bottom) {

            drawLineWithText(canvas, secondRect.left, secondRect.top + secondRect.height() / 2,
                    firstRect.left, secondRect.top + secondRect.height() / 2);

            drawLineWithText(canvas, secondRect.right, secondRect.top + secondRect.height() / 2,
                    firstRect.right, secondRect.top + secondRect.height() / 2);

            drawLineWithText(canvas, secondRect.left + secondRect.width() / 2, secondRect.top,
                    secondRect.left + secondRect.width() / 2, firstRect.top);

            drawLineWithText(canvas, secondRect.left + secondRect.width() / 2, secondRect.bottom,
                    secondRect.left + secondRect.width() / 2, firstRect.bottom);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        relativeElements = null;
    }

    @Override
    protected void drawLineWithText(Canvas canvas, int startX, int startY, int endX, int endY) {
        drawLineWithText(canvas, startX, startY, endX, endY, dip2px(2));
    }
}
