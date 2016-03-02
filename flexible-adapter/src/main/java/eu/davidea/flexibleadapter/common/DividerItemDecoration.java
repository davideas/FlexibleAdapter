package eu.davidea.flexibleadapter.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import eu.davidea.flexibleadapter.section.HeaderViewHolder;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
	private Drawable mDivider;
	private int mDividerHeight;
	
	private static final int[] ATTRS = new int[]{
		android.R.attr.listDivider
	};

	/**
	 * Default divider will be used
	 */
	public DividerItemDecoration(Context context) {
		final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
		mDivider = styledAttributes.getDrawable(0);
		mDividerHeight = mDivider.getIntrinsicHeight();
		styledAttributes.recycle();
	}

	/**
	 * Custom divider will be used
	 */
	public DividerItemDecoration(Context context, int resId) {
		mDivider = ContextCompat.getDrawable(context, resId);
        mDividerHeight = mDivider.getIntrinsicHeight();
	}
	
	public Drawable getDivider() {
	    return mDivider;
	}
	
	public void setDividerHeight(int height) {
        mDividerHeight = height;
    }
	
	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
	    RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
	    if (!(holder instanceof HeaderViewHolder)) {
	        outRect.bottom = mDividerHeight;
	    }
	}
	
	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		int left = parent.getPaddingLeft();
		int right = parent.getWidth() - parent.getPaddingRight();
 
		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = parent.getChildAt(i);
			RecyclerView.ViewHolder holder = parent.getChildViewHolder(child);
	        if (holder instanceof HeaderViewHolder) {
	            continue;
	        }
			RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
 
			int top = child.getBottom() + params.bottomMargin +
					Math.round(ViewCompat.getTranslationY(child));
			int bottom = top + mDividerHeight;
 
			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(c);
		}
	}

}