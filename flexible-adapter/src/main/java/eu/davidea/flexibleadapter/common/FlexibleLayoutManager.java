/*
 * Copyright 2017 Davide Steduto
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

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Internal wrapper class for all conventional LayoutManagers.
 *
 * @author Davide Steduto
 * @since 22/04/2017
 */
public class FlexibleLayoutManager implements IFlexibleLayoutManager {

	protected RecyclerView.LayoutManager mLayoutManager;

	public FlexibleLayoutManager(RecyclerView.LayoutManager layoutManager) {
		this.mLayoutManager = layoutManager;
	}

	/**
	 * Finds the layout orientation of the RecyclerView, no matter which LayoutManager is in use.
	 *
	 * @return one of {@link OrientationHelper#HORIZONTAL}, {@link OrientationHelper#VERTICAL}
	 * @since 5.0.0-rc2
	 */
	public int getOrientation() {
		if (mLayoutManager instanceof LinearLayoutManager) {
			return ((LinearLayoutManager) mLayoutManager).getOrientation();
		} else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
			return ((StaggeredGridLayoutManager) mLayoutManager).getOrientation();
		}
		return OrientationHelper.VERTICAL;
	}

	/**
	 * Helper method to retrieve the number of the columns (span count) of the given LayoutManager.
	 * <p>All Layouts are supported.</p>
	 *
	 * @return the span count
	 * @since 5.0.0-b7
	 */
	public int getSpanCount() {
		if (mLayoutManager instanceof GridLayoutManager) {
			return ((GridLayoutManager) mLayoutManager).getSpanCount();
		} else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
			return ((StaggeredGridLayoutManager) mLayoutManager).getSpanCount();
		}
		return 1;
	}

	/**
	 * Helper method to find the adapter position of the <b>first completely</b> visible view
	 * [for each span], no matter which Layout is.
	 *
	 * @return the adapter position of the <b>first fully</b> visible item or {@code RecyclerView.NO_POSITION}
	 * if there aren't any visible items.
	 * @see #findFirstVisibleItemPosition()
	 * @since 5.0.0-b8
	 */
	public int findFirstCompletelyVisibleItemPosition() {
		if (mLayoutManager instanceof StaggeredGridLayoutManager) {
			return ((StaggeredGridLayoutManager) mLayoutManager).findFirstCompletelyVisibleItemPositions(null)[0];
		} else {
			return ((LinearLayoutManager) mLayoutManager).findFirstCompletelyVisibleItemPosition();
		}
	}

	/**
	 * Helper method to find the adapter position of the <b>first partially</b> visible view
	 * [for each span], no matter which Layout is.
	 *
	 * @return the adapter position of the <b>first partially</b> visible item or {@code RecyclerView.NO_POSITION}
	 * if there aren't any visible items.
	 * @see #findFirstCompletelyVisibleItemPosition()
	 * @since 5.0.0-rc1
	 */
	public int findFirstVisibleItemPosition() {
		if (mLayoutManager instanceof StaggeredGridLayoutManager) {
			return ((StaggeredGridLayoutManager) mLayoutManager).findFirstVisibleItemPositions(null)[0];
		} else {
			return ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
		}
	}

	/**
	 * Helper method to find the adapter position of the <b>last completely</b> visible view
	 * [for each span], no matter which Layout is.
	 *
	 * @return the adapter position of the <b>last fully</b> visible item or {@code RecyclerView.NO_POSITION}
	 * if there aren't any visible items.
	 * @see #findLastVisibleItemPosition()
	 * @since 5.0.0-b8
	 */
	public int findLastCompletelyVisibleItemPosition() {
		if (mLayoutManager instanceof StaggeredGridLayoutManager) {
			return ((StaggeredGridLayoutManager) mLayoutManager).findLastCompletelyVisibleItemPositions(null)[0];
		} else {
			return ((LinearLayoutManager) mLayoutManager).findLastCompletelyVisibleItemPosition();
		}
	}

	/**
	 * Helper method to find the adapter position of the <b>last partially</b> visible view
	 * [for each span], no matter which Layout is.
	 *
	 * @return the adapter position of the <b>last partially</b> visible item or {@code RecyclerView.NO_POSITION}
	 * if there aren't any visible items.
	 * @see #findLastCompletelyVisibleItemPosition()
	 * @since 5.0.0-rc1
	 */
	public int findLastVisibleItemPosition() {
		if (mLayoutManager instanceof StaggeredGridLayoutManager) {
			return ((StaggeredGridLayoutManager) mLayoutManager).findLastVisibleItemPositions(null)[0];
		} else {
			return ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
		}
	}

}