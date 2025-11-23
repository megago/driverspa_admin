package com.driverspa.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Created by Yerzhan Tanatov on 06/07/16.
 */
public class MultipleSwipeRefreshLayout extends SwipeRefreshLayout {

    private GridView[] mSwipeableChildren;
    private int visibleView = 0;

    public MultipleSwipeRefreshLayout(Context context) {
        super(context);
        mSwipeableChildren = new GridView[3];
    }

    public MultipleSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSwipeableChildren = new GridView[3];
    }


    public void addGridView(GridView v, int i)
    {
        mSwipeableChildren[i] = v;
    }


    /**
     * This method controls when the swipe-to-refresh gesture is triggered. By returning false here
     * we are signifying that the view is in a state where a refresh gesture can start.
     *
     * <p>As {@link androidx.swiperefreshlayout.widget.SwipeRefreshLayout} only supports one direct child by
     * default, we need to manually iterate through our swipeable children to see if any are in a
     * state to trigger the gesture. If so we return false to start the gesture.
     */
    @Override
    public boolean canChildScrollUp() {
        if (mSwipeableChildren != null && mSwipeableChildren.length > 0) {
            for(int i = 0; i < 3; i++)
            {
                if((i == visibleView) && mSwipeableChildren[i] != null && !canViewScrollUp(mSwipeableChildren[i]))
                {
                    return false;
                }
            }
        }

        return true;
    }



    /**
     * Utility method to check whether a {@link View} can scroll up from it's current position.
     * Handles platform version differences, providing backwards compatible functionality where
     * needed.
     */
    private static boolean canViewScrollUp(GridView view)
    {
        return view.getChildCount() > 0 &&
                (view.getFirstVisiblePosition() > 0
                        || view.getChildAt(0).getTop() < view.getPaddingTop());
    }

    public void setVisibleView(int i)
    {
        visibleView = i;
    }

}