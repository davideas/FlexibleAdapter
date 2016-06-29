/*
 * Copyright 2016 Martin Guillon & Davide Steduto (Hyper-Optimized for FlexibleAdapter project)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.flexibleadapter.helpers;

import android.animation.Animator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.FlexibleAdapter.OnStickyHeaderChangeListener;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A sticky header helper, to use only with {@link FlexibleAdapter}.
 * <p>Header ViewHolders must be of type {@link FlexibleViewHolder}.</p>
 *
 * @since 25/03/2016 Created
 */
public class StickyHeaderHelper extends OnScrollListener {

	private static final String TAG = FlexibleAdapter.class.getSimpleName();

	private FlexibleAdapter mAdapter;
	private RecyclerView mRecyclerView;
	private ViewGroup mStickyHolderLayout;
	private FlexibleViewHolder mStickyHeaderViewHolder;
	private OnStickyHeaderChangeListener mStickyHeaderChangeListener;
	private int mHeaderPosition = RecyclerView.NO_POSITION;


	public StickyHeaderHelper(FlexibleAdapter adapter,
							  OnStickyHeaderChangeListener stickyHeaderChangeListener) {
		mAdapter = adapter;
		mStickyHeaderChangeListener = stickyHeaderChangeListener;
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		updateOrClearHeader(false);
	}

	public boolean isAttachedToRecyclerView() {
		return mRecyclerView != null;
	}

	public void attachToRecyclerView(RecyclerView parent) {
		if (mRecyclerView != null) {
			mRecyclerView.removeOnScrollListener(this);
			clearHeader();
		}
		mRecyclerView = parent;
		if (mRecyclerView != null) {
			mRecyclerView.addOnScrollListener(this);
			mRecyclerView.post(new Runnable() {
				@Override
				public void run() {
					initStickyHeadersHolder();
				}
			});
		}
	}

	public void detachFromRecyclerView(RecyclerView parent) {
		if (mRecyclerView == parent) {
			mRecyclerView.removeOnScrollListener(this);
			mRecyclerView = null;
			mStickyHolderLayout.animate().setListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					clearHeader();
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}
			});
			mStickyHolderLayout.animate().alpha(0).start();
		}
	}

	private void initStickyHeadersHolder() {
		//Initialize Holder Layout and show sticky header if exists already
		mStickyHolderLayout = mAdapter.getStickySectionHeadersHolder();
		if (mStickyHolderLayout != null) {
			if (mStickyHolderLayout.getLayoutParams() == null) {
				throw new IllegalStateException("The ViewGroup provided, doesn't have LayoutParams correctly set, please initialize the ViewGroup accordingly");
			}
			mStickyHolderLayout.setClipToPadding(false);
			mStickyHolderLayout.setAlpha(0);
			updateOrClearHeader(false);
			mStickyHolderLayout.animate().alpha(1).start();
		} else {
			Log.w(TAG, "WARNING! ViewGroup for Sticky Headers unspecified! You must include @layout/sticky_header_layout or implement FlexibleAdapter.getStickySectionHeadersHolder() method");
		}
	}

	private void onStickyHeaderChange(int sectionIndex) {
		if (mStickyHeaderChangeListener != null) {
			mStickyHeaderChangeListener.onStickyHeaderChange(sectionIndex);
		}
	}

	public void updateOrClearHeader(boolean updateHeaderContent) {
		if (mStickyHolderLayout == null || mAdapter.hasSearchText() ||
				mRecyclerView == null || mRecyclerView.getChildCount() == 0) {
			clearHeader();
			return;
		}
		int firstHeaderPosition = getHeaderPosition(RecyclerView.NO_POSITION);
		if (firstHeaderPosition >= 0 && firstHeaderPosition < mAdapter.getItemCount()) {
			updateHeader(firstHeaderPosition, updateHeaderContent);
		} else {
			clearHeader();
		}
	}

	private void updateHeader(int headerPosition, boolean updateHeaderContent) {
		// Check if there is a new header should be sticky
		if (mHeaderPosition != headerPosition) {
			mHeaderPosition = headerPosition;
			FlexibleViewHolder holder = getHeaderViewHolder(headerPosition);
			if (FlexibleAdapter.DEBUG) Log.v(TAG, "swapHeader newHeaderPosition=" + mHeaderPosition);
			swapHeader(holder);
		} else if (updateHeaderContent && mStickyHeaderViewHolder != null) {
			mAdapter.onBindViewHolder(mStickyHeaderViewHolder, mHeaderPosition);
			ensureHeaderParent();
		}
		translateHeader();
	}

	private void translateHeader() {
		if (mStickyHeaderViewHolder == null) return;

		int headerOffsetX = 0, headerOffsetY = 0;

		//Search for the position where the next header item is found and take the new offset
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            final View nextChild = mRecyclerView.getChildAt(i);
			if (nextChild != null) {
				int adapterPos = mRecyclerView.getChildAdapterPosition(nextChild);
				int nextHeaderPosition = getHeaderPosition(adapterPos);
				if (mHeaderPosition != nextHeaderPosition) {
					if (getOrientation(mRecyclerView) == OrientationHelper.HORIZONTAL) {
						if (nextChild.getLeft() > 0) {
							int headerWidth = mStickyHolderLayout.getMeasuredWidth();
							headerOffsetX = Math.min(nextChild.getLeft() - headerWidth, 0);
							if (headerOffsetX < 0) break;
						}
					} else {
						if (nextChild.getTop() > 0) {
							int headerHeight = mStickyHolderLayout.getMeasuredHeight();
							headerOffsetY = Math.min(nextChild.getTop() - headerHeight, 0);
							if (headerOffsetY < 0) break;
						}
					}
				}
			}
		}
		//Fix to remove unnecessary shadow
		//ViewCompat.setElevation(mStickyHeaderViewHolder.getContentView(), 0f);
		//Apply translation
		mStickyHolderLayout.setTranslationX(headerOffsetX);
		mStickyHolderLayout.setTranslationY(headerOffsetY);
		//Log.v(TAG, "TranslationX=" + headerOffsetX + " TranslationY=" + headerOffsetY);
	}

	private void swapHeader(FlexibleViewHolder newHeader) {
		if (mStickyHeaderViewHolder != null) {
			resetHeader(mStickyHeaderViewHolder);
		}
		mStickyHeaderViewHolder = newHeader;
		if (mStickyHeaderViewHolder != null) {
			mStickyHeaderViewHolder.setIsRecyclable(false);
			ensureHeaderParent();
		}
		onStickyHeaderChange(mHeaderPosition);
	}

	private void ensureHeaderParent() {
		final View view = mStickyHeaderViewHolder.getContentView();
		//Make sure the item params are kept if WRAP_CONTENT has been set for the Header View
		mStickyHeaderViewHolder.itemView.getLayoutParams().width = view.getMeasuredWidth();
		mStickyHeaderViewHolder.itemView.getLayoutParams().height = view.getMeasuredHeight();
		//Now make sure the params are transferred to the StickyHolderLayout
		ViewGroup.LayoutParams params = mStickyHolderLayout.getLayoutParams();
		params.width = view.getMeasuredWidth();
		params.height = view.getMeasuredHeight();
		removeViewFromParent(view);
		mStickyHolderLayout.setClipToPadding(false);
		mStickyHolderLayout.addView(view);
	}

	public void clearHeader() {
		if (mStickyHeaderViewHolder != null) {
			if (FlexibleAdapter.DEBUG) Log.v(TAG, "clearHeader");
			resetHeader(mStickyHeaderViewHolder);
			mStickyHolderLayout.setAlpha(1);
			mStickyHeaderViewHolder = null;
			mHeaderPosition = RecyclerView.NO_POSITION;
			onStickyHeaderChange(mHeaderPosition);
		}
	}

	private void resetHeader(FlexibleViewHolder header) {
		final View view = header.getContentView();
		removeViewFromParent(view);
		//Reset transformation on removed header
		view.setTranslationX(0);
		view.setTranslationY(0);
		mStickyHeaderViewHolder.itemView.setVisibility(View.VISIBLE);
		if (!header.itemView.equals(view))
			((ViewGroup) header.itemView).addView(view);
		header.setIsRecyclable(true);
	}

	private static void removeViewFromParent(final View view) {
		final ViewParent parent = view.getParent();
		if (parent instanceof ViewGroup) {
			((ViewGroup) parent).removeView(view);
		}
	}

	@SuppressWarnings("unchecked")
	private int getHeaderPosition(int adapterPosHere) {
		if (adapterPosHere == RecyclerView.NO_POSITION) {
			View firstChild = mRecyclerView.getChildAt(0);
			adapterPosHere = mRecyclerView.getChildAdapterPosition(firstChild);
		}
		IHeader header = mAdapter.getSectionHeader(adapterPosHere);
		//Header cannot be sticky if it's also an Expandable in collapsed status, RV will raise an exception
		if (header == null || mAdapter.isExpandable(header) && !mAdapter.isExpanded(header)) {
			return RecyclerView.NO_POSITION;
		}
		return mAdapter.getGlobalPositionOf(header);
	}

	/**
	 * Gets the header view for the associated header position. If it doesn't exist yet, it will
	 * be created, measured, and laid out.
	 *
	 * @param position the adapter position to get the header view
	 * @return ViewHolder of type FlexibleViewHolder of the associated header position
	 */
	@SuppressWarnings("unchecked")
	private FlexibleViewHolder getHeaderViewHolder(int position) {
		//Find existing ViewHolder
		FlexibleViewHolder holder = (FlexibleViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
		if (holder == null) {
			//Create and binds a new ViewHolder
			holder = (FlexibleViewHolder) mAdapter.createViewHolder(mRecyclerView, mAdapter.getItemViewType(position));
			mAdapter.bindViewHolder(holder, position);

			//Restore the Adapter position
			holder.setBackupPosition(position);

			//Calculate width and height
			int widthSpec;
			int heightSpec;
			if (getOrientation(mRecyclerView) == OrientationHelper.VERTICAL) {
				widthSpec = View.MeasureSpec.makeMeasureSpec(mRecyclerView.getWidth(), View.MeasureSpec.EXACTLY);
				heightSpec = View.MeasureSpec.makeMeasureSpec(mRecyclerView.getHeight(), View.MeasureSpec.UNSPECIFIED);
			} else {
				widthSpec = View.MeasureSpec.makeMeasureSpec(mRecyclerView.getWidth(), View.MeasureSpec.UNSPECIFIED);
				heightSpec = View.MeasureSpec.makeMeasureSpec(mRecyclerView.getHeight(), View.MeasureSpec.EXACTLY);
			}

			//Measure and Layout the stickyView
			final View headerView = holder.getContentView();
			int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
					mRecyclerView.getPaddingLeft() + mRecyclerView.getPaddingRight(),
					headerView.getLayoutParams().width);
			int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
					mRecyclerView.getPaddingTop() + mRecyclerView.getPaddingBottom(),
					headerView.getLayoutParams().height);

			headerView.measure(childWidth, childHeight);
			headerView.layout(0, 0, headerView.getMeasuredWidth(), headerView.getMeasuredHeight());
		}
		return holder;
	}

	private static int getOrientation(RecyclerView recyclerView) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		if (layoutManager instanceof LinearLayoutManager) {
			return ((LinearLayoutManager) layoutManager).getOrientation();
		} else if (layoutManager instanceof StaggeredGridLayoutManager) {
			return ((StaggeredGridLayoutManager) layoutManager).getOrientation();
		}
		return OrientationHelper.HORIZONTAL;
	}

}