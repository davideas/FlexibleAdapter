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

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import eu.davidea.flexibleadapter.FlexibleAdapter.HeaderViewHolder;

/**
 * A sticky header decoration for RecyclerView, to use only with
 * {@link FlexibleAdapter}.
 */
public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {

    private RecyclerView mRecyclerView;
    private FrameLayout mStickyHolder;
    private FlexibleAdapter mAdapter;
    private HeaderViewHolder mHeader;

    /* --- Header state --- */
    private Long mHeaderId;
    // used to not have to call getHeaderId() all the time
    private Integer mHeaderPosition;
    
    public StickyHeaderDecoration(FlexibleAdapter adapter) {
        mAdapter = adapter;
        mAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
            public void onChanged() {
                updateHeader();
            }

            public void onItemRangeChanged(int positionStart, int itemCount) {
                updateHeader();
            }
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateHeader();
            }

            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateHeader();
            }

            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                updateHeader();
            }
        });
    }
    
    public void setParent(RecyclerView parent) {
        mRecyclerView = parent;
        if (mRecyclerView != null) {
            mRecyclerView.addOnScrollListener(new OnScrollListener() {
                public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                    updateHeader();
                }
           });
        }
    }
    
    private void updateHeader() {
        if (mStickyHolder == null || mRecyclerView == null || mRecyclerView.getChildCount() == 0) {
            return;
        }
        View nextItemView = mRecyclerView.getChildAt(0);
        int adapterPosHere = mRecyclerView.getChildAdapterPosition(nextItemView);
        updateOrClearHeader(mAdapter.getHeaderPosition(adapterPosHere));
    }
    
    public void setStickyHeadersHolder(FrameLayout view) {
        mStickyHolder = view;
        if (mStickyHolder != null) {
            if (mHeader != null) {
                ensureHeaderParent();
            }
        } else {
            clearHeader();
        }
    }
    
    private void updateOrClearHeader(int firstHeaderPosition) {
        final int adapterCount = mAdapter.getItemCount();
        if (adapterCount == 0) {
            return;
        }
        final boolean doesListHaveChildren = mRecyclerView.getChildCount() != 0;
        final boolean isHeaderPositionOutsideAdapterRange = firstHeaderPosition > adapterCount - 1
                || firstHeaderPosition < 0;
        if (!doesListHaveChildren || isHeaderPositionOutsideAdapterRange) {
            clearHeader();
            return;
        }

        updateHeader(Math.max(0, firstHeaderPosition));
    }
    
    private void ensureHeaderParent() {
        final View view = this.mHeader.realItemHolder.itemView;
        final ViewParent parent = view.getParent();
        if(parent != mStickyHolder) {
            ViewGroup.LayoutParams params = this.mHeader.layout.getLayoutParams();
            if (params != null) {
                params.width = view.getMeasuredWidth();
                params.height = view.getMeasuredHeight();
                this.mHeader.layout.setLayoutParams(params);
            }
            
            removeViewFromParent(view);
            
            mStickyHolder.addView(view, mAdapter.getStickyHeadersLayoutParams());
        }
    }

    private void updateHeader(int headerPosition) {

        // check if there is a new header should be sticky
        if (mHeaderPosition == null || mHeaderPosition != headerPosition) {
            mHeaderPosition = headerPosition;
            final long headerId = mAdapter.getHeaderId(headerPosition);
            if (mHeaderId == null || mHeaderId != headerId) {
                mHeaderId = headerId;
                final RecyclerView.ViewHolder holder = getHeader(mRecyclerView, headerPosition);
                //                final View header = mAdapter.getHeaderView(mHeaderPosition, mHeader, this);
                if ((holder== null || holder instanceof HeaderViewHolder) && mHeader != holder) {
                    swapHeader((HeaderViewHolder) holder);

                }
                else if(mHeader != null) {
                    ensureHeaderParent();
                }
            } else {
                //make sure if the real header (one created by the layoutManager
                //appear, we use it!
                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(headerPosition);
                if (holder instanceof HeaderViewHolder && holder != mHeader) {
                    swapHeader((HeaderViewHolder) holder);
                }
            }
        }
        if (mHeader != null) {
            final int orientation = getOrientation(mRecyclerView);
            int headerBottom = mHeader.itemView.getMeasuredHeight();
            int headerRight = mHeader.itemView.getMeasuredWidth();
            int headerOffsetX = 0;
            int headerOffsetY = 0;
            final int childCount = mRecyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = mRecyclerView.getChildAt(i);
                int adapterPos = mRecyclerView.getChildAdapterPosition(child);
                final Integer headerId = mAdapter.getHeaderId(adapterPos);
                //headerId == null means new section with no header!
                if (headerId == null || headerId.intValue() != mHeaderId) {
                    if (orientation == LinearLayoutManager.HORIZONTAL) {
                        if (child.getLeft() > 0) {
                            headerOffsetX = Math.min(child.getLeft() - headerRight, 0);
                            break;
                        }
                    } else {
                        if (child.getTop() > 0) {
                            headerOffsetY = Math.min(child.getTop() - headerBottom, 0);
                            break;
                        }
                    }
                }
                //old code for now
//                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(adapterPos);
//                final boolean doesChildHaveHeader = holder instanceof HeaderViewHolder && holder != mHeader;
//                if (!doesChildHaveHeader) {
//                    continue;
//                }
                
                
                
            }
            mHeader.realItemHolder.itemView.setTranslationX(headerOffsetX);
            mHeader.realItemHolder.itemView.setTranslationY(headerOffsetY);
        }

    }
    
    private void removeViewFromParent(final View view) {
        final ViewParent parent = view.getParent();
        if(parent != this) {
            if(parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(view);
            }
        }
    }
    
    private void resetHeader(HeaderViewHolder header) {
        final View view = header.realItemHolder.itemView;
        if (view.getParent() != header.layout) {
            removeViewFromParent(view);
          //reset transformation on removed header
            view.setTranslationX(0);
            view.setTranslationY(0);
            header.layout.addView(view);
            header.setIsRecyclable(true);
            ViewGroup.LayoutParams params = header.layout.getLayoutParams();
            if (params != null) {
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                header.layout.setLayoutParams(params);
            }
        }
    }

    private void swapHeader(HeaderViewHolder newHeader) {
        if (mHeader != null) {
            resetHeader(mHeader);
       }
        mHeader = newHeader;
        if (mHeader != null) {
            mHeader.setIsRecyclable(false);
            ensureHeaderParent();
        }
    }

    public int getOrientation(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView
                .getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getOrientation();
        }
        return LinearLayoutManager.HORIZONTAL;
    }
    
    private void clearHeader() {
        if (mHeader != null) {
            resetHeader(mHeader);
            mHeader = null;
            mHeaderId = null;
            mHeaderPosition = null;
        }
    }

//
//    /**
//     * Gets the header view for the associated position. If it doesn't exist
//     * yet, it will be created, measured, and laid out.
//     *
//     * @param recyclerView
//     *            the RecyclerView
//     * @param position
//     *            the adapter position to get the header view for
//     * @return Header view or null if the associated position and previous has
//     *         no header
//     */
    private RecyclerView.ViewHolder getHeader(RecyclerView recyclerView, int position) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(mAdapter.getHeaderPosition(position));
            if (holder != null) {
                return holder;
            }
            holder = mAdapter.onCreateViewHolder(recyclerView, mAdapter.getItemViewType(position));
            
            final View header = holder.itemView;
          if (header.getLayoutParams() == null) {
              header.setLayoutParams(new ViewGroup.LayoutParams(
                      ViewGroup.LayoutParams.WRAP_CONTENT,
                      ViewGroup.LayoutParams.WRAP_CONTENT));
          }
          
          mAdapter.onBindViewHolder(holder, position);
          int widthSpec;
          int heightSpec;

          if (getOrientation(recyclerView) == LinearLayoutManager.VERTICAL) {
              widthSpec = View.MeasureSpec.makeMeasureSpec(
                      recyclerView.getWidth(), View.MeasureSpec.EXACTLY);
              heightSpec = View.MeasureSpec.makeMeasureSpec(
                      recyclerView.getHeight(), View.MeasureSpec.UNSPECIFIED);
          } else {
              widthSpec = View.MeasureSpec.makeMeasureSpec(
                      recyclerView.getWidth(), View.MeasureSpec.UNSPECIFIED);
              heightSpec = View.MeasureSpec.makeMeasureSpec(
                      recyclerView.getHeight(), View.MeasureSpec.EXACTLY);
          }
          int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                  recyclerView.getPaddingLeft()
                          + recyclerView.getPaddingRight(),
                  header.getLayoutParams().width);
          int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                  recyclerView.getPaddingTop()
                          + recyclerView.getPaddingBottom(),
                  header.getLayoutParams().height);

          header.measure(childWidth, childHeight);
          header.layout(0, 0, header.getMeasuredWidth(),
                  header.getMeasuredHeight());
            return holder;
    }


}