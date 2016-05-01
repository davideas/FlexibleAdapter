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
import android.view.View;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.ISectionable;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

	private Drawable mDivider;
	private int mGapWidth;

	private static final int[] ATTRS = new int[]{
			android.R.attr.listDivider
	};

	/**
	 * Default Android divider will be used.
	 */
	public DividerItemDecoration(Context context) {
		final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
		mDivider = styledAttributes.getDrawable(0);
		styledAttributes.recycle();
	}

	/**
	 * Custom divider will be used.
	 */
	public DividerItemDecoration(Context context, int resId) {
		this(context, resId, 0);
	}

	/**
	 * Custom divider with gap between sections (in dpi).
	 */
	public DividerItemDecoration(@NonNull Context context, @DrawableRes int resId,
								 @IntRange(from = 0) int gapWidth) {
		if (resId > 0) mDivider = ContextCompat.getDrawable(context, resId);
		setSectionGapWidth((int) (context.getResources().getDisplayMetrics().density * gapWidth));
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		if (mDivider != null) {
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
	}

	/**
	 * @param gapWidth width of the gap between sections, in pixel. Must be positive.
	 */
	public void setSectionGapWidth(@IntRange(from = 0) int gapWidth) {
		if (gapWidth < 0) {
			throw new IllegalArgumentException("invalid width");
		}
		mGapWidth = gapWidth;
	}

	@SuppressWarnings({"ConstantConditions", "unchecked", "SuspiciousNameCombination"})
	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		if (mGapWidth > 0 && parent.getAdapter() instanceof FlexibleAdapter) {
			FlexibleAdapter flexibleAdapter = (FlexibleAdapter) parent.getAdapter();
			int position = parent.getChildAdapterPosition(view);

			//Only ISectionable items can finish with a gap and only if next item is a IHeader item
			if (flexibleAdapter.getItem(position) instanceof ISectionable &&
					(flexibleAdapter.isHeader(flexibleAdapter.getItem(position + 1)) ||
							position >= parent.getAdapter().getItemCount() - 1) ) {

				int orientation = ((LinearLayoutManager) parent.getLayoutManager()).getOrientation();
				if (orientation == RecyclerView.VERTICAL) {
					outRect.set(0, 0, 0, mGapWidth);
				} else {
					outRect.set(0, 0, mGapWidth, 0);
				}
			}
		}
	}

}