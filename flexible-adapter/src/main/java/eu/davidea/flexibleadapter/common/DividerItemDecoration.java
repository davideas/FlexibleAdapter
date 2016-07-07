package eu.davidea.flexibleadapter.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.ISectionable;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

	private Drawable mDivider;
	private int mGapWidth;
	private boolean mDrawOver = false;

	private static final int[] ATTRS = new int[]{
			android.R.attr.listDivider
	};

	/**
	 * Default Android divider will be used.
	 *
	 * @since 5.0.0-b4
	 */
	public DividerItemDecoration(Context context) {
		final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
		mDivider = styledAttributes.getDrawable(0);
		styledAttributes.recycle();
	}

	/**
	 * Custom divider will be used.
	 * <p>By default, divider will be drawn underneath the item.</p>
	 *
	 * @since 5.0.0-b4
	 */
	public DividerItemDecoration(Context context, int resId) {
		this(context, resId, 0);
	}

	/**
	 * Custom divider with gap between sections (in dpi).
	 *
	 * @since 5.0.0-b6
	 */
	public DividerItemDecoration(@NonNull Context context, @DrawableRes int resId,
								 @IntRange(from = 0) int gapWidth) {
		if (resId > 0) mDivider = ContextCompat.getDrawable(context, resId);
		setSectionGapWidth((int) (context.getResources().getDisplayMetrics().density * gapWidth));
	}

	/**
	 * Changes the mode to draw the divider.
	 * <p>- When {@code false}, any content will be drawn before the item views are drawn, and will
	 * thus appear <i>underneath</i> the views.
	 * <br/>- When {@code true}, any content will be drawn after the item views are drawn, and will
	 * thus  appear <i>over</i> the views.</p>
	 * Default value is false (drawn underneath).
	 *
	 * @param mDrawOver true to draw after the item has been added, false to draw underneath the item
	 * @return this Divider, so the call can be chained
	 * @since 5.0.0-b7
	 */
	public DividerItemDecoration setDrawOver(boolean mDrawOver) {
		this.mDrawOver = mDrawOver;
		return this;
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		if (mDivider != null && !mDrawOver) {
			draw(c, parent);
		}
	}

	@Override
	public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
		if (mDivider != null && mDrawOver) {
			draw(c, parent);
		}
	}

	private void draw(Canvas c, RecyclerView parent) {
		int left = parent.getPaddingLeft();
		int right = parent.getWidth() - parent.getPaddingRight();

		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = parent.getChildAt(i);

			RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

			int top = child.getBottom() + params.bottomMargin +
					Math.round(ViewCompat.getTranslationY(child));
			int bottom = top + mDivider.getIntrinsicHeight() + 1;

			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(c);
		}
	}

	/**
	 * @param gapWidth width of the gap between sections, in pixel. Must be positive.
	 * @since 5.0.0-b6
	 */
	public void setSectionGapWidth(@IntRange(from = 0) int gapWidth) {
		if (gapWidth < 0) {
			throw new IllegalArgumentException("Invalid section gap width [<0]: " + gapWidth);
		}
		mGapWidth = gapWidth;
	}

	/**
	 * @since 5.0.0-b4
	 */
	@SuppressWarnings({"ConstantConditions", "unchecked", "SuspiciousNameCombination"})
	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		if (mGapWidth > 0 && parent.getAdapter() instanceof FlexibleAdapter) {
			FlexibleAdapter flexibleAdapter = (FlexibleAdapter) parent.getAdapter();
			int position = parent.getChildAdapterPosition(view);

			//Only ISectionable items can finish with a gap and only if next item is a IHeader item
			if (flexibleAdapter.getItem(position) instanceof ISectionable &&
					(flexibleAdapter.isHeader(flexibleAdapter.getItem(position + 1)) ||
							position >= parent.getAdapter().getItemCount() - 1)) {

				int orientation;
				RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
				if (layoutManager instanceof LinearLayoutManager) {
					orientation = ((LinearLayoutManager) layoutManager).getOrientation();
				} else {
					orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
				}
				if (orientation == RecyclerView.VERTICAL) {
					outRect.set(0, 0, 0, mGapWidth);
				} else {
					outRect.set(0, 0, mGapWidth, 0);
				}
			}
		}
	}

}