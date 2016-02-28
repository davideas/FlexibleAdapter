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
package eu.davidea.flexibleadapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import java.util.List;

import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.ExpandableViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This class provides a set of standard methods to handle changes on the data set such as
 * filtering, adding, removing, moving and animating an item.
 * <p><b>T</b> is your Model object containing the data, with version 5.0.0 it must implement
 * {@link IFlexible} interface.</p>
 * With version 5.0.0, this Adapter supports a set of standard methods for Headers/Sections to
 * expand and collapse an Expandable item, to Drag&Drop and Swipe any item.
 * <p><b>NOTE:</b>This Adapter supports Expandable of Expandable, but selection and restoration
 * don't work well in conjunction of multi level expansion. You should not enable functionalities
 * like: ActionMode, Undo, Drag and CollapseOnExpand in that case. Better to change approach in
 * favor of a better and clearer design/layout: Open the list of the subItem in a new Activity...
 * <br/>Instead, this extra level of expansion is useful in situations where those items are not
 * selectable nor draggable, and information in them are in read only mode or are action buttons.</p>
 *
 * @author Davide Steduto
 * @see FlexibleAnimatorAdapter
 * @see SelectableAdapter
 * @see IFlexible
 * @see FlexibleViewHolder
 * @see ExpandableViewHolder
 * @since 03/05/2015 Created
 * <br/>16/01/2016 Expandable items
 * <br/>24/01/2016 Drag&Drop, Swipe
 * <br/>30/01/2016 Class now extends {@link FlexibleAnimatorAdapter} that extends {@link SelectableAdapter}
 * <br/>02/02/2016 New code reorganization, new item interfaces and full refactoring
 * <br/>08/02/2016 Headers/Sections
 * <br/>10/02/2016 The class is not abstract anymore, it is ready to be used
 * <br/>20/02/2016 Sticky headers
 */
@SuppressWarnings({"unchecked"})
public abstract class FlexibleAdapter extends FlexibleAnimatorAdapter
		implements ItemTouchHelperCallback.AdapterCallback {

	private static final String TAG = FlexibleAdapter.class.getSimpleName();
	private static final String EXTRA_HEADERS = TAG + "_headersShown";
	private LayoutManager mLayoutManager;

	/**
	 * Header/Section items
	 */
	private boolean headersShown = false, headersSticky = false;
    private GridLayoutManager.SpanSizeLookup gridSpanSizeLookup;
    private GridLayoutManager.SpanSizeLookup externalSpanSizeLookup;

	/**
	 * Used to save deleted items and to recover them (Undo).
	 */
    private StickyHeaderDecoration stickyHeaderDecoration;
    private boolean stickyHeaderDecorationAttached = false;
	private boolean restoreSelection = false;

	/* Drag&Drop and Swipe helpers */
	private boolean longPressDragEnabled = false, handleDragEnabled = true, swipeEnabled = false;
	private ItemTouchHelperCallback mItemTouchHelperCallback;
	private ItemTouchHelper mItemTouchHelper;

	/* Listeners */
	public OnItemClickListener mItemClickListener;
	public OnItemLongClickListener mItemLongClickListener;
	protected OnItemMoveListener mItemMoveListener;
    protected OnItemSwipeListener mItemSwipeListener;
    protected OnStickyHeaderChangeListener mStickyHeaderChangeListener;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	/**
	 * Main Constructor with all managed listeners for ViewHolder and the Adapter itself.
	 * <p>The listener must be a single instance of a class, usually <i>Activity</i> or <i>Fragment</i>,
	 * where you can implement how to handle the different events.</p>
	 * Any write operation performed on the items list is <u>synchronized</u>.
	 * <p><b>PASS ALWAYS A <u>COPY</u> OF THE ORIGINAL LIST</b>: <i>new ArrayList&lt;T&gt;(originalList);</i></p>
	 *
	 * @param items     items to display
	 * @param listeners can be an instance of:
	 *                  <br/>- {@link OnUpdateListener}
	 *                  <br/>- {@link OnItemClickListener}
	 *                  <br/>- {@link OnItemLongClickListener}
	 *                  <br/>- {@link OnItemMoveListener}
	 *                  <br/>- {@link OnItemSwipeListener}
	 */
	public FlexibleAdapter(@Nullable Object listeners) {
	    
		if (listeners instanceof OnItemClickListener)
			mItemClickListener = (OnItemClickListener) listeners;
		if (listeners instanceof OnItemLongClickListener)
			mItemLongClickListener = (OnItemLongClickListener) listeners;
		if (listeners instanceof OnItemMoveListener)
			mItemMoveListener = (OnItemMoveListener) listeners;
		if (listeners instanceof OnItemSwipeListener)
			mItemSwipeListener = (OnItemSwipeListener) listeners;
		if (listeners instanceof OnStickyHeaderChangeListener)
            mStickyHeaderChangeListener = (OnStickyHeaderChangeListener) listeners;

	}
	
	public void setLayoutManager(LayoutManager layoutManager) {
	    mLayoutManager = layoutManager;
        if (layoutManager instanceof GridLayoutManager) {
            gridSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (isHeader(position)) {
                        return ((GridLayoutManager) mLayoutManager).getSpanCount();
                    }
                    if (externalSpanSizeLookup != null) {
                        return externalSpanSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            };
            ((GridLayoutManager) mLayoutManager).setSpanSizeLookup(gridSpanSizeLookup);
       } else {
            gridSpanSizeLookup = null;
        }
	}
	
	public void setSpanSizeLookup(GridLayoutManager.SpanSizeLookup spanSizeLookup) { 
	    externalSpanSizeLookup = spanSizeLookup;
	}

	/*------------------------------*/
	/* SELECTION METHODS OVERRIDDEN */
	/*------------------------------*/

	// to be overriden
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public void toggleSelection(@IntRange(from = 0) int position) {
		if (isSelectable(position)) {
			super.toggleSelection(position);
		}
	}

	/**
	 * Helper to automatically select all the items of the viewType equal to the viewType of
	 * the first selected item.
	 * <p>Examples:
	 * <br/>- if user initially selects an expandable of type A, then only expandable items of
	 * type A will be selected.
	 * <br/>- if user initially selects a non expandable of type B, then only items of Type B
	 * will be selected.
	 * <br/>- The developer can override this behaviour by passing a list of viewTypes for which
	 * he wants to force the selection.</p>
	 *
	 * @param viewTypes All the desired viewTypes to be selected, pass nothing to automatically
	 *                  select all the viewTypes of the first item user selected
	 */
	@Override
	public void selectAll(Integer... viewTypes) {
		if (getSelectedItemCount() > 0 && viewTypes.length == 0) {
			super.selectAll(getItemViewType(getSelectedPositions().get(0)));//Priority on the first item
		} else {
			super.selectAll(viewTypes);//Force the selection for the viewTypes passed
		}
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * You can override this method to define your own concept of "Empty". This method is never
	 * called internally.
	 * <p>Default value is the result of {@link #getItemCount()}.</p>
	 *
	 * @return true if the list is empty, false otherwise
	 * @see #getItemCount()
	 * @see #getItemCountOfTypes(Integer...)
	 */
	public boolean isEmpty() {
		return getItemCount() == 0;
	}

	/*--------------------------*/
	/* HEADERS/SECTIONS METHODS */
	/*--------------------------*/

	/**
	 * @return true if all headers are currently displayed, false otherwise
	 */
	public boolean areHeadersShown() {
		return headersShown;
	}

	/**
	 * Sets if all headers should be shown at startup. To call before setting the headers!
	 * <p>Default value is false.</p>
	 *
	 * @param displayHeaders true to display them, false to keep them hidden
	 * @return this adapter so the call can be chained
	 */
	public FlexibleAdapter setDisplayHeaders(boolean displayHeaders) {
		headersShown = displayHeaders;
		return this;
	}

	/**
	 * Returns if Adapter will display sticky headers on the top.
	 *
	 * @return true if headers can be sticky, false if headers are scrolled together with all items
	 */
	public boolean areHeadersSticky() {
		return headersSticky;
	}

	/**
	 * Enables the sticky header functionality. Adds {@link StickyHeaderDecoration} to the
	 * RecyclerView.
	 * <p>Headers can be sticky only if they are shown. Command is otherwise ignored!</p>
	 * <b>NOTE:</b> Sticky headers cannot be dragged, swiped, moved nor deleted. They, instead,
	 * are automatically re-linked if the linked Sectionable item is different.
	 *
	 * @param maxCachedHeaders the max view instances to keep in the cache. This number depends by
	 *                         how many headers are normally displayed in the RecyclerView. It
	 *                         depends by the specific use case.
	 */
	public void enableStickyHeaders() {
		setStickyHeaders(true);
	}

	/**
	 * Disables the sticky header functionality. Clears the cache and removes the
	 * {@link StickyHeaderDecoration} from the RecyclerView.
	 */
	public void disableStickyHeaders() {
		setStickyHeaders(false);
	}

	public void setStickyHeaders(boolean headersSticky) {
		//Add or Remove the sticky headers decoration
		if (headersSticky) {
			this.headersSticky = true;
			if (stickyHeaderDecoration == null) {
	            stickyHeaderDecoration = new StickyHeaderDecoration(this);
			}
			if (!stickyHeaderDecorationAttached && headersShown && mRecyclerView != null) {
			    stickyHeaderDecorationAttached = true;
                stickyHeaderDecoration.setParent(mRecyclerView);
			    mRecyclerView.addItemDecoration(stickyHeaderDecoration);
                stickyHeaderDecoration.setStickyHeadersHolder(getStickyHeadersHolder());
            }
		} else if (stickyHeaderDecoration != null) {
			this.headersSticky = false;
			
			if (stickyHeaderDecoration != null) {
	            if (mRecyclerView != null) {
	                mRecyclerView.removeItemDecoration(stickyHeaderDecoration);
	                stickyHeaderDecoration.setParent(null);
	            }
	            stickyHeaderDecoration.setStickyHeadersHolder(null);
	            stickyHeaderDecoration = null;
                stickyHeaderDecorationAttached = false;
            }
			
		}
	}
	
	@Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (stickyHeaderDecoration != null) {
            stickyHeaderDecoration.setParent(mRecyclerView);
            stickyHeaderDecoration.setStickyHeadersHolder(getStickyHeadersHolder());
            if (!stickyHeaderDecorationAttached && headersShown) {
                stickyHeaderDecorationAttached = true;
                mRecyclerView.addItemDecoration(stickyHeaderDecoration);
            }
        }
    }
	
	/**
     * Returns the view sticky headers will be attached to
     *
     * @return FrameLayout the view
     */
	public abstract FrameLayout getStickyHeadersHolder();

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (stickyHeaderDecoration != null) {
            stickyHeaderDecoration.setParent(null);
            stickyHeaderDecoration.setStickyHeadersHolder(null);
            if (stickyHeaderDecorationAttached) {
                stickyHeaderDecorationAttached = false;
                recyclerView.removeItemDecoration(stickyHeaderDecoration);
            }
        }
        super.onDetachedFromRecyclerView(recyclerView);
    }

	/**
	 * Shows all headers in the RecyclerView at their linked position.
	 * <p>Headers can be shown or hidden all together.</p>
	 *
	 * @see #hideAllHeaders()
	 */
	public void showAllHeaders() {
		headersShown = true;
		notifyDataSetChanged();
	}

	/**
	 * Hides all headers from the RecyclerView.
	 * <p>Headers can be shown or hidden all together.</p>
	 *
	 * @see #showAllHeaders()
	 */
	public void hideAllHeaders() {
		headersShown = false;
        notifyDataSetChanged();
	}
	
//	private class HeaderFrameLayout extends FrameLayout {
//
//        private final View shapeView;
//        public HeaderFrameLayout(View view) {
//            super(view.getContext());
//            this.shapeView = view;
//        }
////        @Override
////        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
////            measureChildWithMargins(shapeView, widthMeasureSpec, 0, heightMeasureSpec, 0);
////            setMeasuredDimension(shapeView.getMeasuredWidth(), shapeView.getMeasuredHeight());
////        }
//    }
	
	public class HeaderViewHolder extends RecyclerView.ViewHolder {
	    public RecyclerView.ViewHolder realItemHolder;
	    public FrameLayout layout;
	    
	    
        public HeaderViewHolder(RecyclerView.ViewHolder itemHolder) {
            super(new FrameLayout(itemHolder.itemView.getContext()));
            this.layout = (FrameLayout) this.itemView;
            this.layout.setClipChildren(false);
            this.realItemHolder = itemHolder;
            this.layout.addView(this.realItemHolder.itemView);
        }
	}

	/*---------------------*/
	/* VIEW HOLDER METHODS */
	/*---------------------*/
	

	private static int HEADER_TYPE_FLAG = 0x80000000;
	
	/**
     * Returns the view type for the header
     *
     * @param position position of the headerView
     */
	public abstract int getHeaderViewType(int position);
	
	/**
     * Returns the view type for a normal view
     *
     * @param position position of the view
     */
    public abstract int getNormalViewType(int position);
    
    /**
     * Returns the ViewHolder for a header
     *
     * @param parent the RecyclerView
     * @param viewType the ViewType
     */
    public abstract RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType);
    
    /**
     * Returns the ViewHolder for a normal view
     *
     * @param parent the RecyclerView
     * @param viewType the ViewType
     */
    public abstract RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup parent, int viewType);
   
    /**
     * Bind a header view
     *
     * @param holder the ViewHolder
     * @param position position of the view
     */
    public abstract void onBindHeaderViewHolder(ViewHolder holder, int position);
    
    /**
     * Bind a view
     *
     * @param holder the ViewHolder
     * @param position position of the view
     */
    public abstract void onBindNormalViewHolder(ViewHolder holder, int position, boolean selected);
    
    /**
     * return the header id at one position. This is a unique identifier for your section
     *
     * @return id the id for the section corresponding to position
     */
    public abstract Integer getHeaderId(int position);
    
    /**
     * return the position of the header of the item at position
     *
      * @param position position of the item for which we are searching for
    * @return id the id for the section corresponding to position
     */
    public abstract int getHeaderPosition(int position);
    
	@Override
    public final int getItemViewType(int position) {
	    if (isHeader(position)) {
            return getHeaderViewType(position) | HEADER_TYPE_FLAG;
        } else {
            return getNormalViewType(position);
        }
    }
	
    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
            int viewType) {
        if ((viewType & HEADER_TYPE_FLAG) != 0) {
            return new HeaderViewHolder(onCreateHeaderViewHolder(parent, viewType & ~HEADER_TYPE_FLAG));
        }
        return onCreateNormalViewHolder(parent, viewType);
    }

	@Override
	public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
	    if (holder instanceof HeaderViewHolder) {
	        this.onBindHeaderViewHolder(((HeaderViewHolder) holder).realItemHolder, position);
	    } else {
	        this.onBindNormalViewHolder(holder, position, isSelected(position));
	    }
	}

	public boolean isHeader(int position) {
        return headersShown && getHeaderPosition(position) == position;
    }
	   
    /**
     * @since 26/01/2016
     */
    public interface OnStickyHeaderChangeListener {
        /**
         * Called when the current sticky header changed
         *
         * @param position  the position of header
         */
        void onStickyHeaderChange(int position);
    }


    public void onStickyHeaderChange(int position) {
        
        if (mStickyHeaderChangeListener != null) {
            mStickyHeaderChangeListener.onStickyHeaderChange(position);
        }
    }
    
	/**
	 * Returns the current configuration to restore selections on Undo.
	 *
	 * @return true if selection will be restored, false otherwise
	 */
	public boolean isRestoreWithSelection() {
		return restoreSelection;
	}

	/**
	 * Gives the possibility to restore the selection on Undo, when {@link #restoreDeletedItems()}
	 * is called.
	 * <p>To use in combination with {@code ActionMode} in order to not disable it.</p>
	 * Default value is false.
	 *
	 * @param restoreSelection true to have restored items still selected, false to empty selections.
	 */
	public void setRestoreSelectionOnUndo(boolean restoreSelection) {
		this.restoreSelection = restoreSelection;
	}

	/*---------------*/
	/* TOUCH METHODS */
	/*---------------*/

	/**
	 * Used by {@link FlexibleViewHolder#onTouch(View, MotionEvent)}
	 * to start Drag when HandleView is touched.
	 *
	 * @return the ItemTouchHelper instance already initialized.
	 */
	public final ItemTouchHelper getItemTouchHelper() {
		initializeItemTouchHelper();
		return mItemTouchHelper;
	}

	/**
	 * Returns whether ItemTouchHelper should start a drag and drop operation if an item is
	 * long pressed.<p>
	 * Default value returns false.
	 *
	 * @return true if ItemTouchHelper should start dragging an item when it is long pressed,
	 * false otherwise. Default value is false.
	 */
	public boolean isLongPressDragEnabled() {
		return mItemTouchHelperCallback.isLongPressDragEnabled();
	}

	/**
	 * Enable the Drag on LongPress on the entire ViewHolder.
	 * <p><b>NOTE:</b> This will skip LongClick on the view in order to handle the LongPress,
	 * however the LongClick listener will be called if necessary in the new
	 * {@link FlexibleViewHolder#onActionStateChanged(int, int)}.</p>
	 * Default value is false.
	 *
	 * @param longPressDragEnabled true to activate, false otherwise
	 */
	public final void setLongPressDragEnabled(boolean longPressDragEnabled) {
		initializeItemTouchHelper();
		this.longPressDragEnabled = longPressDragEnabled;
		mItemTouchHelperCallback.setLongPressDragEnabled(longPressDragEnabled);
	}

	/**
	 * Enabled by default.
	 * <p>To use, it is sufficient to set the HandleView by calling
	 * {@link FlexibleViewHolder#setDragHandleView(View)}.</p>
	 *
	 * @return true if active, false otherwise
	 */
	public boolean isHandleDragEnabled() {
		return handleDragEnabled;
	}

	/**
	 * Enable/Disable the drag with handle.
	 * <p>Default value is true.</p>
	 *
	 * @param handleDragEnabled true to activate, false otherwise
	 */
	public void setHandleDragEnabled(boolean handleDragEnabled) {
		this.handleDragEnabled = handleDragEnabled;
	}

	/**
	 * Returns whether ItemTouchHelper should start a swipe operation if a pointer is swiped
	 * over the View.
	 * <p>Default value returns false.</p>
	 *
	 * @return true if ItemTouchHelper should start swiping an item when user swipes a pointer
	 * over the View, false otherwise. Default value is false.
	 */
	public final boolean isSwipeEnabled() {
		return mItemTouchHelperCallback.isItemViewSwipeEnabled();
	}

	/**
	 * Enable the Swipe of the items
	 * <p>Default value is false.</p>
	 *
	 * @param swipeEnabled true to activate, false otherwise
	 */
	public final void setSwipeEnabled(boolean swipeEnabled) {
		initializeItemTouchHelper();
		this.swipeEnabled = swipeEnabled;
		mItemTouchHelperCallback.setSwipeEnabled(swipeEnabled);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean shouldMove(int fromPosition, int toPosition) {
//		if (mItemMoveListener != null) {
//			return mItemMoveListener.shouldMoveItem(fromPosition, toPosition);
//		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@CallSuper
	public boolean onItemMove(int fromPosition, int toPosition) {
//		moveItem(fromPosition, toPosition);
		//After the swap, delegate further actions to the user
		if (mItemMoveListener != null) {
			mItemMoveListener.onItemMove(fromPosition, toPosition);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@CallSuper
	public void onItemSwiped(int position, int direction) {
		//Delegate actions to the user
		if (mItemSwipeListener != null) {
			mItemSwipeListener.onItemSwipe(position, direction);
		}
	}

	private void initializeItemTouchHelper() {
		if (mItemTouchHelper == null) {
			if (mRecyclerView == null) {
				throw new IllegalStateException("RecyclerView cannot be null. Enabling LongPressDrag or Swipe must be done after the Adapter is added to the RecyclerView.");
			}
			mItemTouchHelperCallback = new ItemTouchHelperCallback(this);
			mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
			mItemTouchHelper.attachToRecyclerView(mRecyclerView);
		}
	}

	/*----------------*/
	/* INSTANCE STATE */
	/*----------------*/

	/**
	 * Save the state of the current expanded items.
	 *
	 * @param outState Current state
	 */
	public void onSaveInstanceState(Bundle outState) {
		if (outState != null) {
			super.onSaveInstanceState(outState);
			outState.putBoolean(EXTRA_HEADERS, headersShown);
		}
	}

	/**
	 * Restore the previous state of the expanded items.
	 *
	 * @param savedInstanceState Previous state
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			super.onRestoreInstanceState(savedInstanceState);
			headersShown = savedInstanceState.getBoolean(EXTRA_HEADERS);
			showAllHeaders();
		}
	}

	/*---------------*/
	/* INNER CLASSES */
	/*---------------*/

	/**
	 * @since 03/01/2016
	 */
	public interface OnUpdateListener {
		/**
		 * Called at startup and every time an item is inserted, removed or filtered.
		 *
		 * @param size the current number of items in the adapter, result of {@link #getItemCount()}
		 */
		void onUpdateEmptyView(int size);
	}

	/**
	 * @since 29/11/2015
	 */
	public interface OnDeleteCompleteListener {
		/**
		 * Called when Undo timeout is over and removal must be committed in the user Database.
		 * <p>Due to Java Generic, it's too complicated and not
		 * well manageable if we pass the List&lt;T&gt; object.<br/>
		 * To get deleted items, use {@link #getDeletedItems()} from the
		 * implementation of this method.</p>
		 */
		void onDeleteConfirmed();
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemClickListener {
		/**
		 * Called when single tap occurs.
		 * <p>Delegates the click event to the listener and checks if selection MODE if
		 * SINGLE or MULTI is enabled in order to activate the ItemView.</p>
		 * For Expandable Views it will toggle the Expansion if configured so.
		 *
		 * @param position the adapter position of the item clicked
		 * @return true if the click should activate the ItemView, false for no change.
		 */
		boolean onItemClick(int position);
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemLongClickListener {
		/**
		 * Called when long tap occurs.
		 * <p>This method always calls
		 * {@link FlexibleViewHolder#toggleActivation}
		 * after listener event is consumed in order to activate the ItemView.</p>
		 * For Expandable Views it will collapse the View if configured so.
		 *
		 * @param position the adapter position of the item clicked
		 */
		void onItemLongClick(int position);
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemMoveListener {
		/**
		 * Called when the item would like to be swapped.
		 * <p>Delegate this permission to the developer.</p>
		 *
		 * @param fromPosition the potential start position of the dragged item
		 * @param toPosition   the potential resolved position of the swapped item
		 * @return return true if the items can swap ({@code onItemMove()} will be called),
		 * false otherwise (nothing happens)
		 * @see #onItemMove(int, int)
		 */
//		boolean shouldMoveItem(int fromPosition, int toPosition);

		/**
		 * Called when an item has been dragged far enough to trigger a move. <b>This is called
		 * every time an item is shifted</b>, and <strong>not</strong> at the end of a "drop" event.
		 * <p>The end of the "drop" event is instead handled by
		 * {@link FlexibleViewHolder#onItemReleased(int)}</p>.
		 *
		 * @param fromPosition the start position of the moved item
		 * @param toPosition   the resolved position of the moved item
		 *                     //		 * @see #shouldMoveItem(int, int)
		 */
		void onItemMove(int fromPosition, int toPosition);
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemSwipeListener {
		/**
		 * Called when swiping ended its animation and Item is not visible anymore.
		 *
		 * @param position  the position of the item swiped
		 * @param direction the direction to which the ViewHolder is swiped
		 */
		void onItemSwipe(int position, int direction);
	}


    public LayoutParams getStickyHeadersLayoutParams() {
        FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        newParams.gravity = Gravity.TOP | Gravity.LEFT;
        return newParams;
    }
}