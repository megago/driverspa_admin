package com.driverspa.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.stream.IntStream;

import com.driverspa.R;
import com.driverspa.util.Functions;

/**
 * Created by Yerzhan Tanatov on 26/10/15.
 */
public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private Context context;
    public SimpleDividerItemDecoration(Context context) {
        this.context = context;
        this.mDivider = context.getResources().getDrawable(R.drawable.line_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = (int)Functions.dipToPixels(context,40);
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        IntStream.range(0, childCount).forEach(i -> {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        });
    }
}
