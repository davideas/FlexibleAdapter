/*
 * Copyright 2016 Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import android.support.v7.widget.RecyclerView;
import android.view.View;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;

public class FlexibleItemDecoration extends RecyclerView.ItemDecoration {

	private Drawable mDivider;
	private int mSectionOffset;
	private boolean mDrawOver = false, withOffset = false;

	private static final int[] ATTRS = new int[]{
			android.R.attr.listDivider
	};

	/**
	 * Default Android divider will be used.
	 *
	 * @since 5.0.0-b4
	 */
	public FlexibleItemDecoration(Context context) {
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
	public FlexibleItemDecoration(@NonNull Context context, @DrawableRes int resId) {
		this(context, resId, 0);
	}

	/**
	 * Custom divider with gap between sections (in dpi).
	 * Passing a negative divider will only use
	 *
	 * @since 5.0.0-b6
	 */
	public FlexibleItemDecoration(@NonNull Context context, @DrawableRes int resId,
								  @IntRange(from = 0) int sectionOffset) {
		if (resId > 0) mDivider = ContextCompat.getDrawable(context, resId);
		mSectionOffset = (int) (context.getResources().getDisplayMetrics().density * sectionOffset);
	}

	/**
	 * Changes the mode to draw the divider.
	 * <p>- When {@code false}, any content will be drawn before the item views are drawn, and will
	 * thus appear <i>underneath</i> the views.
	 * <br/>- When {@code true}, any content will be drawn after the item views are drawn, and will
	 * thus  appear <i>over</i> the views.</p>
	 * Default value is false (drawn underneath).
	 *
	 * @param drawOver true to draw after the item has been added, false to draw underneath the item
	 * @return this Divider, so the call can be chained
	 * @since 5.0.0-b8
	 */
	public FlexibleItemDecoration withDrawOver(boolean drawOver) {
		this.mDrawOver = drawOver;
		return this;
	}

	/**
	 * @deprecated use {@link #withDrawOver(boolean)} instead.
	 */
	public FlexibleItemDecoration setDrawOver(boolean drawOver) {
		return withDrawOver(drawOver);
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
	 * @param gap width of the gap between sections, in pixel. Must be positive.
	 * @since 5.0.0-b6
	 * @deprecated Use Constructor instead.
	 */
	@Deprecated
	public void setSectionGapWidth(@IntRange(from = 0) int gap) {
		if (gap < 0) {
			throw new IllegalArgumentException("Invalid section gap width [<0]: " + gap);
		}
		mSectionOffset = gap;
	}

	/**
	 * Applies the physical offset between items, of the same size of the divider previously set.
	 *
	 * @param withOffset true to leave space between items, false divider will be drawn overlapping
	 *                   the items
	 * @return this FlexibleItemDecoration instance so the call can be chained
	 * @since 5.0.0-b8
	 */
	public FlexibleItemDecoration withOffset(boolean withOffset) {
		this.withOffset = withOffset;
		return this;
	}

	/**
	 * @since 5.0.0-b4
	 */
	@SuppressWarnings({"ConstantConditions", "unchecked", "SuspiciousNameCombination"})
	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView recyclerView, RecyclerView.State state) {
		int offset = (mDivider != null && withOffset ? mDivider.getIntrinsicHeight() : 0);
		if (mSectionOffset > 0 && recyclerView.getAdapter() instanceof FlexibleAdapter) {
			FlexibleAdapter flexibleAdapter = (FlexibleAdapter) recyclerView.getAdapter();
			int position = recyclerView.getChildAdapterPosition(view);

			//Only ISectionable items can finish with a gap and only if next item is a IHeader item
			if (flexibleAdapter.getItem(position) instanceof ISectionable &&
					(flexibleAdapter.isHeader(flexibleAdapter.getItem(position + 1)) ||
							position >= recyclerView.getAdapter().getItemCount() - 1)) {

				offset += mSectionOffset;
			}
		}
		if (FlexibleUtils.getOrientation(recyclerView.getLayoutManager()) == RecyclerView.VERTICAL) {
			outRect.set(0, 0, 0, offset);
		} else {
			outRect.set(0, 0, offset, 0);
		}
	}

}