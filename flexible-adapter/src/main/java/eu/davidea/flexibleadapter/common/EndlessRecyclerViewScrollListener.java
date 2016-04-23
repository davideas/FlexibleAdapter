package eu.davidea.flexibleadapter.common;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews
 * https://guides.codepath.com/android/Endless-Scrolling-with-AdapterViews-and-RecyclerView
 * https://gist.github.com/ssinss/e06f12ef66c51252563e
 * http://stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview/26561717#26561717
 * https://github.com/dominicthomas/FlikrGridRecyclerView/blob/master/app/src/main/java/com/android/domji84/mcgridview/adapters/GridItemAdapter.java
 */
public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

	// The minimum amount of items to have below your current scroll position
	// before loading more.
	private int visibleThreshold = 5;
	// The current offset index of data you have loaded
	private int currentPage = 0;
	// The total number of items in the dataset after the last load
	private int previousTotalItemCount = 0;
	// True if we are still waiting for the last set of data to load.
	private boolean loading = true;

	RecyclerView.LayoutManager mLayoutManager;

	public EndlessRecyclerViewScrollListener(RecyclerView.LayoutManager layoutManager) {
		mLayoutManager = layoutManager;
		int spanCount = 1;
		if (mLayoutManager instanceof GridLayoutManager) {
			spanCount = ((GridLayoutManager) mLayoutManager).getSpanCount();
		} else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
			spanCount = ((StaggeredGridLayoutManager) mLayoutManager).getSpanCount();
		}
		visibleThreshold = visibleThreshold * spanCount;
	}

	public int getLastVisibleItem(int[] lastVisibleItemPositions) {
		int maxSize = 0;
		for (int i = 0; i < lastVisibleItemPositions.length; i++) {
			if (i == 0) {
				maxSize = lastVisibleItemPositions[i];
			} else if (lastVisibleItemPositions[i] > maxSize) {
				maxSize = lastVisibleItemPositions[i];
			}
		}
		return maxSize;
	}

	// This happens many times a second during a scroll, so be wary of the code you place here.
	// We are given a few useful parameters to help us work out if we need to load some more data,
	// but first we check if we are waiting for the previous load to finish.
	@Override
	public void onScrolled(RecyclerView view, int dx, int dy) {
		int lastVisibleItemPosition = 0;
		final int totalItemCount = mLayoutManager.getItemCount();

		if (mLayoutManager instanceof LinearLayoutManager) {
			lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
		} else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
			int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) mLayoutManager).findLastVisibleItemPositions(null);
			// Get maximum element within the list
			lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
		}

		// If the total item count is zero and the previous isn't, assume the
		// list is invalidated and should be reset back to initial state
		if (totalItemCount < previousTotalItemCount) {
			this.currentPage = 0;
			this.previousTotalItemCount = totalItemCount;
			if (totalItemCount == 0) {
				this.loading = true;
			}
		}
		// If it’s still loading, we check to see if the dataset count has
		// changed, if so we conclude it has finished loading and update the current page
		// number and total item count.
		if (loading && (totalItemCount > previousTotalItemCount)) {
			loading = false;
			previousTotalItemCount = totalItemCount;
		}

		// If it isn’t currently loading, we check to see if we have breached
		// the visibleThreshold and need to reload more data.
		// If we do need to reload some more data, we execute onLoadMore to fetch the data.
		// threshold should reflect how many total columns there are too
		if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
			currentPage++;
			loading = true;
			onLoadMore(currentPage, totalItemCount);
		}
	}

	public void resetPageCount() {
		previousTotalItemCount = 0;
		loading = true;
		currentPage = 0;
		onLoadMore(currentPage, mLayoutManager.getItemCount());
	}

	// Defines the process for actually loading more data based on page
	public abstract void onLoadMore(int page, int totalItemsCount);

}