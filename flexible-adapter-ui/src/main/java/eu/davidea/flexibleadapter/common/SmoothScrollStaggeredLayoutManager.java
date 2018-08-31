/*
 * Copyright 2015-2016 Davide Steduto
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Optimized implementation of StaggeredGridLayoutManager to SmoothScroll to a Top position.
 *
 * @since 5.0.0-b8 Creation in main package
 * <br>17/12/2017 Moved into UI package
 */
public class SmoothScrollStaggeredLayoutManager extends StaggeredGridLayoutManager implements IFlexibleLayoutManager {

    private RecyclerView.SmoothScroller mSmoothScroller;

    public SmoothScrollStaggeredLayoutManager(Context context, int spanCount) {
        this(context, spanCount, VERTICAL);
    }

    public SmoothScrollStaggeredLayoutManager(Context context, int spanCount, int orientation) {
        super(spanCount, orientation);
        mSmoothScroller = new TopSnappedSmoothScroller(context, this);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        mSmoothScroller.setTargetPosition(position);
        startSmoothScroll(mSmoothScroller);
    }

    @Override
    public int findFirstCompletelyVisibleItemPosition() {
        int position = super.findFirstCompletelyVisibleItemPositions(null)[0];
        for (int i = 1; i < getSpanCount(); i++) {
            int nextPosition = super.findFirstCompletelyVisibleItemPositions(null)[i];
            if (nextPosition < position) position = nextPosition;
        }
        return position;
    }

    @Override
    public int findFirstVisibleItemPosition() {
        int position = super.findFirstVisibleItemPositions(null)[0];
        for (int i = 1; i < getSpanCount(); i++) {
            int nextPosition = super.findFirstVisibleItemPositions(null)[i];
            if (nextPosition < position) position = nextPosition;
        }
        return position;
    }

    @Override
    public int findLastCompletelyVisibleItemPosition() {
        int position = super.findLastCompletelyVisibleItemPositions(null)[0];
        for (int i = 1; i < getSpanCount(); i++) {
            int nextPosition = super.findLastCompletelyVisibleItemPositions(null)[i];
            if (nextPosition > position) position = nextPosition;
        }
        return position;
    }

    @Override
    public int findLastVisibleItemPosition() {
        int position = super.findLastVisibleItemPositions(null)[0];
        for (int i = 1; i < getSpanCount(); i++) {
            int nextPosition = super.findLastVisibleItemPositions(null)[i];
            if (nextPosition > position) position = nextPosition;
        }
        return position;
    }

}