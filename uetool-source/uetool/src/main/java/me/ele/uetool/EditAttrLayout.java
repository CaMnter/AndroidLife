package me.ele.uetool;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import me.ele.uetool.base.Element;

import static me.ele.uetool.base.DimenUtil.dip2px;
import static me.ele.uetool.base.DimenUtil.px2dip;

/**
 * 属性编辑 layout
 */
public class EditAttrLayout extends CollectViewsLayout {

    private final int moveUnit = dip2px(1);
    private final int lineBorderDistance = dip2px(5);

    private Paint areaPaint = new Paint() {
        {
            setAntiAlias(true);
            setColor(0x30000000);
        }
    };

    private Element targetElement;
    private AttrsDialog dialog;
    private IMode mode = new ShowMode();
    private float lastX, lastY;
    private OnDragListener onDragListener;


    public EditAttrLayout(Context context) {
        super(context);
    }


    public EditAttrLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public EditAttrLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 最后会调用 IMode # onDraw，绘制对应的效果
     * IMode 内会监听事件反复调用「invalidate」进而执行「onDraw」
     *
     * @param canvas canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (targetElement != null) {
            canvas.drawRect(targetElement.getRect(), areaPaint);
            mode.onDraw(canvas);
        }
    }


    /**
     * 事件处理，主要交给 IMode
     *
     * @param event event
     * @return boolean
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mode.triggerActionUp(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mode.triggerActionMove(event);
                break;
        }
        return true;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        targetElement = null;
        if (dialog != null) {
            dialog.dismiss();
        }
    }


    /**
     * 设置移动模式「MoveMode」的回调
     *
     * @param onDragListener
     */
    public void setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
    }


    /**
     * 移动模式「MoveMode」元素移动的时候，得进行绘制
     */
    class MoveMode implements IMode {

        /**
         * 先在原来位置，绘制一个红虚线矩形
         * 如果有父元素的话，绘制该元素各个边与父元素对应的各个边的相对距离是多少 dp
         *
         * @param canvas canvas
         */
        @Override
        public void onDraw(Canvas canvas) {
            Rect rect = targetElement.getRect();
            Rect originRect = targetElement.getOriginRect();
            canvas.drawRect(originRect, dashLinePaint);
            Element parentElement = targetElement.getParentElement();
            if (parentElement != null) {
                Rect parentRect = parentElement.getRect();
                int x = rect.left + rect.width() / 2;
                int y = rect.top + rect.height() / 2;
                drawLineWithText(canvas, rect.left, y, parentRect.left, y, dip2px(2));
                drawLineWithText(canvas, x, rect.top, x, parentRect.top, dip2px(2));
                drawLineWithText(canvas, rect.right, y, parentRect.right, y, dip2px(2));
                drawLineWithText(canvas, x, rect.bottom, x, parentRect.bottom, dip2px(2));
            }
            if (onDragListener != null) {
                onDragListener.showOffset(
                    "Offset:\n" + "x -> " + px2dip(rect.left - originRect.left, true) + " y -> " +
                        px2dip(rect.top - originRect.top, true));
            }
        }


        /**
         * move 时，记录 lastX 和 lastY
         * 反复调用「invalidate」进而执行「onDraw」
         * 然后进行 IMode 的 onDraw
         * 开始 show mode 的绘制移动时的元素
         *
         * @param event event
         */
        @Override
        public void triggerActionMove(MotionEvent event) {
            if (targetElement != null) {
                boolean changed = false;
                View view = targetElement.getView();
                float diffX = event.getX() - lastX;
                if (Math.abs(diffX) >= moveUnit) {
                    view.setTranslationX(view.getTranslationX() + diffX);
                    lastX = event.getX();
                    changed = true;
                }
                float diffY = event.getY() - lastY;
                if (Math.abs(diffY) >= moveUnit) {
                    view.setTranslationY(view.getTranslationY() + diffY);
                    lastY = event.getY();
                    changed = true;
                }
                if (changed) {
                    targetElement.reset();
                    invalidate();
                }
            }
        }


        @Override
        public void triggerActionUp(MotionEvent event) {

        }
    }


    /**
     * 显示模式
     * 选择元素时，得显示宽高多少 dp
     */
    @SuppressWarnings("DanglingJavadoc")
    class ShowMode implements IMode {

        /**
         * 绘制宽高多少 dp 和 红线
         *
         * @param canvas canvas
         */
        @Override
        public void onDraw(Canvas canvas) {
            Rect rect = targetElement.getRect();
            drawLineWithText(canvas, rect.left, rect.top - lineBorderDistance, rect.right,
                rect.top - lineBorderDistance);
            drawLineWithText(canvas, rect.right + lineBorderDistance, rect.top,
                rect.right + lineBorderDistance, rect.bottom);
        }


        @Override
        public void triggerActionMove(MotionEvent event) {

        }


        /**
         * 监听 up 事件
         * 基本上 up 时，「invalidate」调用「onDraw」
         * 然后进行 IMode 的 onDraw
         * 这里的话就，就是上面的绘制宽高多少 dp 和 红线
         *
         * 然后显示属性 Dialog
         *
         * @param event event
         */
        @Override
        public void triggerActionUp(final MotionEvent event) {
            final Element element = getTargetElement(event.getX(), event.getY());
            if (element != null) {
                targetElement = element;
                invalidate();
                if (dialog == null) {
                    dialog = new AttrsDialog(getContext());

                    /**
                     * 定义 属性 dialog callback
                     */
                    dialog.setAttrDialogCallback(new AttrsDialog.AttrDialogCallback() {

                        /**
                         * 这里 show mode 后允许 move mode
                         * 所以这里把 EditAttrLayout 的 mode field 设置为 move mode
                         */
                        @Override
                        public void enableMove() {
                            mode = new MoveMode();
                            dialog.dismiss();
                        }


                        /**
                         * 按了「ValidViews」开关后
                         * 通知 dialog 显示 view tree
                         *
                         * @param position position
                         * @param isChecked isChecked
                         */
                        @Override
                        public void showValidViews(int position, boolean isChecked) {
                            int positionStart = position + 1;
                            if (isChecked) {
                                dialog.notifyValidViewItemInserted(positionStart,
                                    getTargetElements(lastX, lastY), targetElement);
                            } else {
                                dialog.notifyItemRangeRemoved(positionStart);
                            }
                        }


                        /**
                         * 选择别的元素时，关掉当前 dialog
                         * 并打开 那个元素的 属性 dialog
                         *
                         * @param  element element
                         */
                        @Override
                        public void selectView(Element element) {
                            targetElement = element;
                            dialog.dismiss();
                            dialog.show(targetElement);
                        }
                    });
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (targetElement != null) {
                                targetElement.reset();
                                invalidate();
                            }
                        }
                    });
                }
                dialog.show(targetElement);
            }
        }
    }


    /**
     * IMode
     * 实现类有
     * 移动模式「MoveMode」元素移动的时候，得进行绘制
     * 显示模式「ShowMode」选择元素时，得显示宽高多少 dp
     */
    public interface IMode {

        /**
         * 绘制，基本在 onDraw 之后调用
         *
         * @param canvas canvas
         */
        void onDraw(Canvas canvas);

        /**
         * 代理 EditAttrLayout 的 move 事件
         *
         * @param event event
         */
        void triggerActionMove(MotionEvent event);

        /**
         * 代理 EditAttrLayout 的 up 事件
         *
         * @param event event
         */
        void triggerActionUp(MotionEvent event);
    }


    /**
     * 移动模式「MoveMode」的回调
     */
    public interface OnDragListener {
        void showOffset(String offsetContent);
    }
}
