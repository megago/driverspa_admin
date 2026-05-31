package com.driverspa.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple flow container: lays children left-to-right and wraps to the next
 * line when the current row runs out of width. The number of items per row is
 * therefore determined automatically by the available width — wider screens
 * fit more per row. GONE children are skipped (no gaps), and child margins are
 * honoured.
 */
public class FlowLayout extends ViewGroup {

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int maxLineWidth = widthSize - paddingLeft - paddingRight;

        int lineUsedWidth = 0;
        int contentHeight = 0;
        int lineHeight = 0;
        int maxContentWidth = 0;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (lineUsedWidth + childWidth > maxLineWidth && lineUsedWidth > 0) {
                // wrap to a new line
                contentHeight += lineHeight;
                maxContentWidth = Math.max(maxContentWidth, lineUsedWidth);
                lineUsedWidth = 0;
                lineHeight = 0;
            }
            lineUsedWidth += childWidth;
            lineHeight = Math.max(lineHeight, childHeight);
        }
        // last line
        contentHeight += lineHeight;
        maxContentWidth = Math.max(maxContentWidth, lineUsedWidth);

        int resolvedWidth = (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY)
                ? widthSize
                : maxContentWidth + paddingLeft + paddingRight;
        int resolvedHeight = (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY)
                ? MeasureSpec.getSize(heightMeasureSpec)
                : contentHeight + paddingTop + paddingBottom;

        setMeasuredDimension(resolvedWidth, resolvedHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int maxLineWidth = (right - left) - paddingLeft - getPaddingRight();

        int x = paddingLeft;
        int y = paddingTop;
        int lineHeight = 0;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (x + childWidth > paddingLeft + maxLineWidth && x > paddingLeft) {
                x = paddingLeft;
                y += lineHeight;
                lineHeight = 0;
            }

            int childLeft = x + lp.leftMargin;
            int childTop = y + lp.topMargin;
            child.layout(childLeft, childTop,
                    childLeft + child.getMeasuredWidth(),
                    childTop + child.getMeasuredHeight());

            x += childWidth;
            lineHeight = Math.max(lineHeight, childHeight);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }
}
