package eu.davidea.example;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
	private Drawable mDivider;
	
	private static final int[] ATTRS = new int[]{
		android.R.attr.listDivider
	};
	
	public SimpleDividerItemDecoration(Context context) {
		final TypedArray a = context.obtainStyledAttributes(ATTRS);
		mDivider = a.getDrawable(0);
		a.recycle();
	}
	
	public SimpleDividerItemDecoration(Drawable drawableDivider) {
		mDivider = drawableDivider;
	}
 
	@Override
	public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
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