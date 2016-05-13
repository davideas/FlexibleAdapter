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

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.FlexibleAdapter.OnStickyHeaderChangeListener;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A sticky header helper, to use only with {@link FlexibleAdapter}.
 *
 * @since 25/03/2016 Created
 */
public class StickyHeaderHelper extends OnScrollListener {

	private static final String TAG = FlexibleAdapter.class.getSimpleName();

	private FlexibleAdapter mAdapter;
	private RecyclerView mRecyclerView;
	private ViewGroup mStickyHolderLayout;
	private RecyclerView.ViewHolder mStickyHeaderViewHolder;
	private OnStickyHeaderChangeListener mStickyHeaderChangeListener;

	/* Header state, used to not call getSectionHeader() all the time */
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
			clearHeader();
			mRecyclerView = null;
		}
	}

	public boolean hasStickyHeaderTranslated(int position) {
		RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForAdapterPosition(position);
		return vh != null && (vh.itemView.getX() < 0 || vh.itemView.getY() < 0);
	}

	private void initStickyHeadersHolder() {
		//Initialize Holder Layout and show sticky header if exists already
		mStickyHolderLayout = mAdapter.getStickySectionHeadersHolder();
		if (mStickyHolderLayout != null) {
			if (mStickyHolderLayout.getLayoutParams() == null) {
				mStickyHolderLayout.setLayoutParams(getDefaultLayoutParams());
				//TODO: Animate Layout change when attach and detach
			}
			updateOrClearHeader(false);
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

	private void ensureHeaderParent() {
		final View view = mStickyHeaderViewHolder.itemView;
		ViewGroup.LayoutParams params = mStickyHolderLayout.getLayoutParams();
		params.width = view.getMeasuredWidth();
		params.height = view.getMeasuredHeight();
		removeViewFromParent(view);
		mStickyHolderLayout.addView(view);
	}

	private void updateHeader(int headerPosition, boolean updateHeaderContent) {
		// Check if there is a new header should be sticky
		if (mHeaderPosition != headerPosition) {
			mHeaderPosition = headerPosition;
			RecyclerView.ViewHolder holder = getHeaderViewHolder(headerPosition);
			if (mStickyHeaderViewHolder != holder) {
				if (FlexibleAdapter.DEBUG) Log.v(TAG, "swapHeader newPosition=" + mHeaderPosition);
				swapHeader(holder);
			}
		} else if (updateHeaderContent && mStickyHeaderViewHolder != null) {
			mAdapter.onBindViewHolder(mStickyHeaderViewHolder, mHeaderPosition);
			ensureHeaderParent();
		}
		translateHeader();
	}

	private void translateHeader() {
		if (mStickyHeaderViewHolder == null) return;

		int headerOffsetX = 0, headerOffsetY = 0;

		//If linear Layout, next potential header is always at position 1
		int nextChildHeaderPosition = 1;
		if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
			nextChildHeaderPosition = ((GridLayoutManager) mRecyclerView.getLayoutManager()).getSpanCount();
		}
		//Search for the position where the next header item is found and take the new offset
		for (int i = nextChildHeaderPosition; i > 0; i--) {
			final View nextChild = mRecyclerView.getChildAt(i);
			if (nextChild != null) {
				int adapterPos = mRecyclerView.getChildAdapterPosition(nextChild);
				int nextHeaderPosition = getHeaderPosition(adapterPos);
				if (mHeaderPosition != nextHeaderPosition) {
					if (getOrientation(mRecyclerView) == LinearLayoutManager.HORIZONTAL) {
						if (nextChild.getLeft() > 0) {
							int headerWidth = mStickyHeaderViewHolder.itemView.getMeasuredWidth();
							headerOffsetX = Math.min(nextChild.getLeft() - headerWidth, 0);
							if (headerOffsetX < 0) break;
						}
					} else {
						if (nextChild.getTop() > 0) {
							int headerHeight = mStickyHeaderViewHolder.itemView.getMeasuredHeight();
							headerOffsetY = Math.min(nextChild.getTop() - headerHeight, 0);
							if (headerOffsetY < 0) break;
						}
					}
				}
			}
		}
		//Fix to remove unnecessary shadow
		if (Utils.hasLollipop())
			mStickyHeaderViewHolder.itemView.setElevation(0f);
		//Apply translation
		mStickyHeaderViewHolder.itemView.setTranslationX(headerOffsetX);
		mStickyHeaderViewHolder.itemView.setTranslationY(headerOffsetY);
	}

	private void swapHeader(final RecyclerView.ViewHolder newHeader) {
		mRecyclerView.post(new Runnable() {
			@Override
			public void run() {
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
		});
	}

	public void clearHeader() {
		if (mStickyHeaderViewHolder != null) {
			if (FlexibleAdapter.DEBUG) Log.v(TAG, "clearHeader");
			resetHeader(mStickyHeaderViewHolder);
			mStickyHeaderViewHolder = null;
			mHeaderPosition = RecyclerView.NO_POSITION;
			onStickyHeaderChange(mHeaderPosition);
		}
	}

	@SuppressWarnings({"unchecked", "ConstantConditions"})
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
	 * Gets the header view for the associated position. If it doesn't exist
	 * yet, it will be created, measured, and laid out.
	 *
	 * @param position the adapter position to get the header view for
	 * @return Header view or null if the associated position and previous has
	 * no header
	 */
	@SuppressWarnings("unchecked")
	private RecyclerView.ViewHolder getHeaderViewHolder(int position) {
		//Find existing ViewHolder
		RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(position);
		if (holder == null) {
			//Create and binds a new ViewHolder
			holder = mAdapter.createViewHolder(mRecyclerView, mAdapter.getItemViewType(position));
			mAdapter.bindViewHolder(holder, position);

			//Restore the Adapter position
			if (holder instanceof FlexibleViewHolder)
				((FlexibleViewHolder) holder).setBackupPosition(position);

			//Calculate width and height
			int widthSpec;
			int heightSpec;
			if (getOrientation(mRecyclerView) == LinearLayoutManager.VERTICAL) {
				widthSpec = View.MeasureSpec.makeMeasureSpec(mRecyclerView.getWidth(), View.MeasureSpec.EXACTLY);
				heightSpec = View.MeasureSpec.makeMeasureSpec(mRecyclerView.getHeight(), View.MeasureSpec.UNSPECIFIED);
			} else {
				widthSpec = View.MeasureSpec.makeMeasureSpec(mRecyclerView.getWidth(), View.MeasureSpec.UNSPECIFIED);
				heightSpec = View.MeasureSpec.makeMeasureSpec(mRecyclerView.getHeight(), View.MeasureSpec.EXACTLY);
			}

			//Measure and Layout the itemView
			final View headerView = holder.itemView;
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

	private static void resetHeader(RecyclerView.ViewHolder header) {
		final View view = header.itemView;
		removeViewFromParent(view);
		//Reset transformation on removed header
		view.setTranslationX(0);
		view.setTranslationY(0);
		//Restore LayoutParams
		view.setLayoutParams(new RecyclerView.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		header.setIsRecyclable(true);
	}

	private static void removeViewFromParent(final View view) {
		final ViewParent parent = view.getParent();
		if (parent instanceof ViewGroup) {
			((ViewGroup) parent).removeView(view);
		}
	}

	private static ViewGroup.LayoutParams getDefaultLayoutParams() {
		return new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	private static int getOrientation(RecyclerView recyclerView) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		if (layoutManager instanceof LinearLayoutManager) {
			return ((LinearLayoutManager) layoutManager).getOrientation();
		}
		return LinearLayoutManager.HORIZONTAL;
	}

}