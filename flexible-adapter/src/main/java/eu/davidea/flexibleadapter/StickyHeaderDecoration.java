/*
 * Copyright 2014 Eduardo Barrenechea
 * Copyright 2016 Davide Steduto (Hyper-Optimized for FlexibleAdapter project)
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
package eu.davidea.flexibleadapter;

import android.graphics.Canvas;
import android.support.v4.util.LruCache;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;


/**
 * A sticky header decoration for RecyclerView, to use only with {@link FlexibleAdapter}.
 */
public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {

	private FlexibleAdapter mAdapter;
	private LruCache<Integer, View> mHeaderCache;

	/**
	 * @param adapter the sticky header adapter to use
	 */
	public StickyHeaderDecoration(FlexibleAdapter adapter, int maxCachedHeaders) {
		mAdapter = adapter;
		mHeaderCache = new LruCache<Integer, View>(maxCachedHeaders);
	}
	
	public void setMaxCachedHeaders(int maxCachedHeaders) {
        mHeaderCache.resize(maxCachedHeaders);
        mHeaderCache.evictAll();
	}
	
	
	public int getOrientation(RecyclerView recyclerView) {
	    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
	    if (layoutManager instanceof LinearLayoutManager) {
	        return ((LinearLayoutManager) layoutManager).getOrientation();
	    }
	    return LinearLayoutManager.HORIZONTAL;
	  } 
	/**
	 * Gets the header view for the associated position. If it doesn't exist yet, it will be
	 * created, measured, and laid out.
	 *
	 * @param recyclerView the RecyclerView
	 * @param position     the adapter position to get the header view for
	 * @return Header view or null if the associated position and previous has no header
	 */
	private View getHeader(RecyclerView recyclerView, int position) {
	    if (!mAdapter.areHeadersShown()) {
            return null;
	    }
	    final Integer key = mAdapter.getHeaderId(position);
	    if (key == null) {
//          Log.v("getHeader", "No Header");
            return null;
        } else {
            
            View header = mHeaderCache.get(key);
            if (header == null) {
                if (position < 0) return null;

                final RecyclerView.ViewHolder holder = mAdapter.createViewHolder(recyclerView, mAdapter.getItemViewType(position));
                if (holder == null) {
                    return null;
                }
                header = holder.itemView;
                if (header.getLayoutParams() == null) {
                    header.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                  }
                mAdapter.onBindHeaderViewHolder(holder, position);
                mHeaderCache.put(key, header);
            }
            
            
            //TODO: optimize by not measuring everytime
            // though we need new measurement for all headers when:
            // - orientation change
            // - recyclerView size change
            int widthSpec;
            int heightSpec;

            if (getOrientation(recyclerView) == LinearLayoutManager.VERTICAL) {
              widthSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY);
              heightSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), View.MeasureSpec.UNSPECIFIED);
            } else {
              widthSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.UNSPECIFIED);
              heightSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), View.MeasureSpec.EXACTLY);
            }
            int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                    recyclerView.getPaddingLeft() + recyclerView.getPaddingRight(), header.getLayoutParams().width);
            int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                    recyclerView.getPaddingTop() + recyclerView.getPaddingBottom(), header.getLayoutParams().height);

            header.measure(childWidth, childHeight);
            header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
            return header;
       }
	}
	
	private int currentStickyPos = -1;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
		//Get or create the headerView for top position (index = 0)
		if (recyclerView.getChildCount() < 0) return;
		View child = recyclerView.getChildAt(0);
		int adapterPos = recyclerView.getChildAdapterPosition(child);
		View headerView = getHeader(recyclerView, adapterPos);
		//A header is found?
		if (headerView != null) {
			int top = getHeaderTop(recyclerView, child, adapterPos);
			if (adapterPos != currentStickyPos && top == 0) {
			    currentStickyPos = adapterPos;
			    mAdapter.onStickyHeaderChange(currentStickyPos);
	            Log.v("onDrawOver", "new StickyHeader=" + currentStickyPos);
			}
			//Draw header!
			int left = child.getLeft();
			canvas.save();
			canvas.translate(left, top);
			headerView.draw(canvas);
			canvas.restore();
		}
	}

	/**
	 * Gets the new Header top and tries to compute the offscreen push if needed
	 *
	 * @param recyclerView the RecyclerView
	 * @param child        the current child item view
	 * @param adapterPos   the current Adapter position
	 * @return the new top (usually 0) of the header view, or the offset if next header pushes
	 * the previous offscreen
	 */
	private int getHeaderTop(RecyclerView recyclerView, View child, int adapterPos) {
		int top = Math.max(0, (int) child.getY());

		//Check item availability
		View current = getHeader(recyclerView, adapterPos);
		if (current == null || recyclerView.getChildCount() < 1)
			return top;

		//Get next(+1) view with header and compute the offscreen push if needed
		View nextItemView = recyclerView.getChildAt(1);
		int adapterPosHere = recyclerView.getChildAdapterPosition(nextItemView);
        View next = getHeader(recyclerView, adapterPosHere);

		if (next != null && !next.equals(current)) {

			int offset = (int) nextItemView.getY() - next.getHeight();
			if (offset < 0) {
				return offset;//The new translated top, when pushing up
			}
		}
		return top;
	}

	/**
	 * Clears the header view cache. Headers will be recreated and
	 * rebound on list scroll after this method has been called.
	 */
	public void clearHeadersCache() {
		mHeaderCache.evictAll();
	}
}