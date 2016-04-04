package com.camnter.newlife.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.camnter.newlife.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Description：EasyFlowLayout
 * Created by：CaMnter
 * Time：2015-12-24 15:23
 */
public class EasyFlowLayout extends ViewGroup {

    private static final int LEFT = -1;
    private static final int CENTER = 0;
    private static final int RIGHT = 1;

    private int gravity;

    /**
     * 记录所有的子View，按行记录
     * Records of all child View, press the row
     */
    private List<List<View>> allViews = new ArrayList<>();
    /**
     * 记录每一行的最大高度
     * Record the maximum height of every line
     */
    private List<Integer> allHeight = new ArrayList<>();

    /**
     * 记录每一行的宽度
     * Record the width of each line
     */
    private List<Integer> lineWidths = new ArrayList<>();

    /**
     * 临时存储当前行的childView
     * Temporarily store the current row childView
     */
    private List<View> currentLineViews = new ArrayList<>();


    public EasyFlowLayout(Context context) {
        super(context);
        this.initStyleable(context, null);
    }


    public EasyFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initStyleable(context, attrs);
    }


    public EasyFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initStyleable(context, attrs);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EasyFlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initStyleable(context, attrs);
    }


    private void initStyleable(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EasyFlowLayout);
        this.gravity = typedArray.getInt(R.styleable.EasyFlowLayout_gravity, LEFT);
        typedArray.recycle();
    }


    /**
     * <p>
     * Measure the view and its content to determine the measured width and the
     * measured height. This method is invoked by {@link #measure(int, int)} and
     * should be overridden by subclasses to provide accurate and efficient
     * measurement of their contents.
     * </p>
     * <p/>
     * <p>
     * <strong>CONTRACT:</strong> When overriding this method, you
     * <em>must</em> call {@link #setMeasuredDimension(int, int)} to store the
     * measured width and height of this view. Failure to do so will trigger an
     * <code>IllegalStateException</code>, thrown by
     * {@link #measure(int, int)}. Calling the superclass'
     * {@link #onMeasure(int, int)} is a valid use.
     * </p>
     * <p/>
     * <p>
     * The base class implementation of measure defaults to the background size,
     * unless a larger size is allowed by the MeasureSpec. Subclasses should
     * override {@link #onMeasure(int, int)} to provide better measurements of
     * their content.
     * </p>
     * <p/>
     * <p>
     * If this method is overridden, it is the subclass's responsibility to make
     * sure the measured height and width are at least the view's minimum height
     * and width ({@link #getSuggestedMinimumHeight()} and
     * {@link #getSuggestedMinimumWidth()}).
     * </p>
     *
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent.
     * The requirements are encoded with
     * {@link MeasureSpec}.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     * The requirements are encoded with
     * {@link MeasureSpec}.
     * @see #getMeasuredWidth()
     * @see #getMeasuredHeight()
     * @see #setMeasuredDimension(int, int)
     * @see #getSuggestedMinimumHeight()
     * @see #getSuggestedMinimumWidth()
     * @see MeasureSpec#getMode(int)
     * @see MeasureSpec#getSize(int)
     */
    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int layoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        int layoutHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        /**
         * 动态记录warp_content时的宽高
         * High dynamic recording warp_content wide
         */
        int wrapWidth = 0;
        int wrapHeight = 0;

        /**
         * 记录每一行的宽度，wrapWidth不断取最大宽度
         * Record the width of each line, wrapWidth constantly get maximum width
         */
        int lineWidth = 0;
        /**
         * 记录每一行的高度，累加至wrapHeight
         * Record the height of each row, accumulate to wrapHeight
         */
        int lineHeight = 0;
        int childCount = this.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = this.getChildAt(i);

            if (child.getVisibility() == View.VISIBLE) {
                this.measureChild(child, widthMeasureSpec, heightMeasureSpec);
            } else {
                continue;
            }
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) child.getLayoutParams();

            /**
             * 子View的实际宽度
             * The actual width of the child View
             */
            int childWidth = child.getMeasuredWidth() +
                    marginLayoutParams.leftMargin +
                    marginLayoutParams.rightMargin;
            /**
             * 子View的实际高度
             * The actual width of the child View
             */
            int childHeight = child.getMeasuredHeight() +
                    marginLayoutParams.topMargin +
                    marginLayoutParams.bottomMargin;

            /**
             * 如果在加入childView的时候，超出的最大宽度
             * If at the time of joining childView, beyond the maximum width
             */
            if (lineWidth + childWidth > layoutWidth) {
                /*
                 * 最大的为如果是warp_content情况下时的宽度
                 * Biggest as if is warp_content cases when the width
                 */
                wrapWidth = Math.max(lineWidth, childWidth);
                /*
                 * 换行
                 * newline
                 */
                lineWidth = childWidth;
                /*
                 * 换行，要加高度
                 * Newline, to add height
                 */
                wrapHeight += lineHeight;
                lineHeight = childHeight;
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }

            if (i == childCount - 1) {
                wrapWidth = Math.max(wrapWidth, lineWidth);
                wrapHeight += lineHeight;
            }
        }

        /**
         * 如果是warp_content 则设置记录好的wrapWidth和wrapHeight
         * 否则 设置 layoutWidth 和 layoutHeight
         * If it is warp_content wrapWidth and wrapHeight is set records
         * Otherwise set layoutWidth and layoutHeight
         */
        this.setMeasuredDimension((modeWidth == MeasureSpec.EXACTLY) ? layoutWidth : wrapWidth,
                (modeHeight == MeasureSpec.EXACTLY) ? layoutHeight : wrapHeight);
    }


    /**
     * {@inheritDoc}
     *
     * @param changed changed
     * @param l l
     * @param t t
     * @param r r
     * @param b b
     */
    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.allViews.clear();
        this.allHeight.clear();
        this.currentLineViews.clear();

        int layoutWidth = this.getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        int childCount = this.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = this.getChildAt(i);
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            /**
             * 如果在加入childView的时候，超出的最大宽度
             * If at the time of joining childView, beyond the maximum width
             */
            if (childWidth + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin +
                    lineWidth > layoutWidth) {
                this.allViews.add(this.currentLineViews);
                this.allHeight.add(lineHeight);

                this.lineWidths.add(lineWidth);
                lineWidth = 0;
                this.currentLineViews = new ArrayList<>();
            }
            /**
             * 不执行换行，继续叠加
             * Does not perform a newline, continue to stack
             */
            lineWidth += childWidth + marginLayoutParams.leftMargin +
                    marginLayoutParams.rightMargin;
            lineHeight = Math.max(lineHeight,
                    childHeight + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin);
            this.currentLineViews.add(child);
        }

        /**
         * 记录最后一行
         * Record the last line
         */
        this.allHeight.add(lineHeight);
        this.allViews.add(this.currentLineViews);
        this.lineWidths.add(lineWidth);

        int left = 0;
        int top = 0;
        int lineCount = this.allViews.size();
        for (int i = 0; i < lineCount; i++) {
            /*
             * 每一行的所有的views
             * All the views of each line
             */
            this.currentLineViews = this.allViews.get(i);
            /*
             * 当前行的最大高度
             * The current row maximum height
             */
            lineHeight = this.allHeight.get(i);

            /*
             * 拿到当前行的宽度
             * Get the current line width
             */
            int currentLineWidth = this.lineWidths.get(i);

            /**
             * 设置 gravity
             * Set the gravity
             */
            switch (this.gravity) {
                case LEFT:
                    left = 0;
                    break;
                case CENTER:
                    left = (layoutWidth - currentLineWidth) / 2;
                    break;
                case RIGHT:
                    left = layoutWidth - currentLineWidth;
                    break;
            }

            /**
             * 遍历当前行
             * Traverse the current line
             */
            for (int j = 0; j < this.currentLineViews.size(); j++) {
                View child = this.currentLineViews.get(j);
                if (child.getVisibility() == View.GONE) continue;

                MarginLayoutParams marginLayoutParams
                        = (MarginLayoutParams) child.getLayoutParams();
                /**
                 * childView的 左上点坐标 和 右下点坐标
                 * The upper left point coordinates and lower point coordinates childView
                 */
                int childLeft = left + marginLayoutParams.leftMargin;
                int childTop = top + marginLayoutParams.topMargin;
                int childRight = childLeft + child.getMeasuredWidth();
                int childBottom = childTop + child.getMeasuredHeight();

                child.layout(childLeft, childTop, childRight, childBottom);
                left += child.getMeasuredWidth() + marginLayoutParams.leftMargin +
                        marginLayoutParams.rightMargin;
            }
            top += lineHeight;
        }
    }


    /**
     * Returns a new set of layout parameters based on the supplied attributes set.
     *
     * @param attrs the attributes to build the layout parameters from
     * @return an instance of {@link LayoutParams} or one
     * of its descendants
     */
    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(this.getContext(), attrs);
    }


    /**
     * Returns a safe set of layout parameters based on the supplied layout params.
     * When a ViewGroup is passed a View whose layout params do not pass the test of
     * {@link #checkLayoutParams(LayoutParams)}, this method
     * is invoked. This method should return a new set of layout params suitable for
     * this ViewGroup, possibly by copying the appropriate attributes from the
     * specified set of layout params.
     *
     * @param p The layout parameters to convert into a suitable set of layout parameters
     * for this ViewGroup.
     * @return an instance of {@link LayoutParams} or one
     * of its descendants
     */
    @Override protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }


    /**
     * Returns a set of default layout parameters. These parameters are requested
     * when the View passed to {@link #addView(View)} has no layout parameters
     * already set. If null is returned, an exception is thrown from addView.
     *
     * @return a set of default layout parameters or null
     */
    @Override protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }


    public List<List<View>> getAllViews() {
        return allViews;
    }


    public List<Integer> getAllHeight() {
        return allHeight;
    }
}
