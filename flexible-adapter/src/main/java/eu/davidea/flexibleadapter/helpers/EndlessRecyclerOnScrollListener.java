package eu.davidea.flexibleadapter.helpers;

import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * This class is not used. Adopted different approach.
 *
 * https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews
 * https://guides.codepath.com/android/Endless-Scrolling-with-AdapterViews-and-RecyclerView
 * https://gist.github.com/ssinss/e06f12ef66c51252563e
 * http://stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview/26561717#26561717
 * https://github.com/dominicthomas/FlikrGridRecyclerView/blob/master/app/src/main/java/com/android/domji84/mcgridview/adapters/GridItemAdapter.java
 */
abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

	private int mPreviousTotal = 0;
	private boolean mLoading = true;
	private int mVisibleThreshold = -1;
	private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;

	private boolean mIsOrientationHelperVertical;
	private OrientationHelper mOrientationHelper;

	private int mCurrentPage = 1;

	private FlexibleAdapter mAdapter;

	private RecyclerView.LayoutManager mLayoutManager;

	public EndlessRecyclerOnScrollListener() {
	}

	public EndlessRecyclerOnScrollListener(FlexibleAdapter adapter) {
		this.mAdapter = adapter;
	}

	public EndlessRecyclerOnScrollListener(int visibleThreshold) {
		this.mVisibleThreshold = visibleThreshold;
	}

	private int findFirstVisibleItemPosition(RecyclerView recyclerView) {
		final View child = findOneVisibleChild(0, mLayoutManager.getChildCount(), false, true);
		return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
	}

	private int findLastVisibleItemPosition(RecyclerView recyclerView) {
		final View child = findOneVisibleChild(recyclerView.getChildCount() - 1, -1, false, true);
		return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
	}

	private View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
									 boolean acceptPartiallyVisible) {
		if (mLayoutManager.canScrollVertically() != mIsOrientationHelperVertical
				|| mOrientationHelper == null) {
			mIsOrientationHelperVertical = mLayoutManager.canScrollVertically();
			mOrientationHelper = mIsOrientationHelperVertical
					? OrientationHelper.createVerticalHelper(mLayoutManager)
					: OrientationHelper.createHorizontalHelper(mLayoutManager);
		}

		final int start = mOrientationHelper.getStartAfterPadding();
		final int end = mOrientationHelper.getEndAfterPadding();
		final int next = toIndex > fromIndex ? 1 : -1;
		View partiallyVisible = null;
		for (int i = fromIndex; i != toIndex; i += next) {
			final View child = mLayoutManager.getChildAt(i);
			if (child != null) {
				final int childStart = mOrientationHelper.getDecoratedStart(child);
				final int childEnd = mOrientationHelper.getDecoratedEnd(child);
				if (childStart < end && childEnd > start) {
					if (completelyVisible) {
						if (childStart >= start && childEnd <= end) {
							return child;
						} else if (acceptPartiallyVisible && partiallyVisible == null) {
							partiallyVisible = child;
						}
					} else {
						return child;
					}
				}
			}
		}
		return partiallyVisible;
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);
		if (mLayoutManager == null)
			mLayoutManager = recyclerView.getLayoutManager();

		int footerItemCount = mAdapter != null ? mAdapter.getItemCount() : 0;

		if (mVisibleThreshold == -1)
			mVisibleThreshold = findLastVisibleItemPosition(recyclerView) - findFirstVisibleItemPosition(recyclerView) - footerItemCount;

		mVisibleItemCount = recyclerView.getChildCount() - footerItemCount;
		mTotalItemCount = mLayoutManager.getItemCount() - footerItemCount;
		mFirstVisibleItem = findFirstVisibleItemPosition(recyclerView);

		if (mLoading) {
			if (mTotalItemCount > mPreviousTotal) {
				mLoading = false;
				mPreviousTotal = mTotalItemCount;
			}
		}
		if (!mLoading && (mTotalItemCount - mVisibleItemCount)
				<= (mFirstVisibleItem + mVisibleThreshold)) {

			mCurrentPage++;

			onLoadMore(mCurrentPage);

			mLoading = true;
		}
	}

	public void resetPageCount(int page) {
		mPreviousTotal = 0;
		mLoading = true;
		mCurrentPage = page;
		onLoadMore(mCurrentPage);
	}

	public void resetPageCount() {
		resetPageCount(0);
	}

	public int getTotalItemCount() {
		return mTotalItemCount;
	}

	public int getFirstVisibleItem() {
		return mFirstVisibleItem;
	}

	public int getVisibleItemCount() {
		return mVisibleItemCount;
	}

	public int getCurrentPage() {
		return mCurrentPage;
	}

	public abstract void onLoadMore(int currentPage);

}