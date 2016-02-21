package eu.davidea.flexibleadapter.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
	private Drawable mDivider;
	
	private static final int[] ATTRS = new int[]{
		android.R.attr.listDivider
	};

	/**
	 * Default divider will be used
	 */
	public DividerItemDecoration(Context context) {
		final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
		mDivider = styledAttributes.getDrawable(0);
		styledAttributes.recycle();
	}

	/**
	 * Custom divider will be used
	 */
	public DividerItemDecoration(Context context, int resId) {
		mDivider = ContextCompat.getDrawable(context, resId);
	}

 
	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		int left = parent.getPaddingLeft();
		int right = parent.getWidth() - parent.getPaddingRight();
 
		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = parent.getChildAt(i);
 
			RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
 
			int top = child.getBottom() + params.bottomMargin +
					Math.round(ViewCompat.getTranslationY(child));
			int bottom = top + mDivider.getIntrinsicHeight();
 
			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(c);
		}
	}

}