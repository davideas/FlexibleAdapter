/*
 * Copyright 2015-2017 Davide Steduto
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

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;
import eu.davidea.flexibleadapter.helpers.StickyHeaderHelper;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;
import eu.davidea.viewholders.ExpandableViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

import static eu.davidea.flexibleadapter.utils.FlexibleUtils.getClassName;

/**
 * This Adapter is backed by an ArrayList of arbitrary objects of class <b>T</b>, where <b>T</b>
 * is your adapter object containing the data of a single item. This Adapter simplifies the
 * development by providing a set of standard methods to handle changes on the data set such as:
 * <i>selecting, filtering, adding, removing, moving</i> and <i>animating</i> an item.
 * <p>
 * With version 5.0.0, <b>T</b> item must implement {@link IFlexible} item interface as base item
 * for all view types and new methods have been added in order to:
 * <ul>
 * <li>handle Headers/Sections with {@link IHeader} and {@link ISectionable} items;</li>
 * <li>expand and collapse {@link IExpandable} items;</li>
 * <li>drag&drop and swipe any items.</li>
 * </ul>
 *
 * @author Davide Steduto
 * @see AnimatorAdapter
 * @see SelectableAdapter
 * @see IFlexible
 * @see FlexibleViewHolder
 * @see ExpandableViewHolder
 * @since 03/05/2015 Created
 * <br>16/01/2016 Expandable items
 * <br>24/01/2016 Drag&Drop, Swipe-to-dismiss
 * <br>30/01/2016 Class now extends {@link AnimatorAdapter} that extends {@link SelectableAdapter}
 * <br>02/02/2016 New code reorganization, new item interfaces and full refactoring
 * <br>08/02/2016 Headers/Sections
 * <br>10/02/2016 The class is not abstract anymore, it is ready to be used
 * <br>20/02/2016 Sticky headers
 * <br>22/04/2016 Endless Scrolling
 * <br>13/07/2016 Update and Filter operations are executed asynchronously (high performance on big list)
 * <br>25/11/2016 Scrollable Headers and Footers; Reviewed EndlessScroll
 * <br>28/03/2017 Adapter now makes a copy of the provided list at startup, updateDataSet and filterItems
 * <br>10/04/2017 Endless Top Scrolling
 * <br>15/04/2017 Starting or resetting the Filter will empty the bin of the deletedItems
 * <br>23/04/2017 Wrapper class for any third type of LayoutManagers
 */
@SuppressWarnings({"Range", "unused", "unchecked", "ConstantConditions", "SuspiciousMethodCalls", "WeakerAccess"})
public class FlexibleAdapter<T extends IFlexible>
        extends AnimatorAdapter
        implements ItemTouchHelperCallback.AdapterCallback {

    private static final String TAG = FlexibleAdapter.class.getSimpleName();
    private static final String EXTRA_PARENT = TAG + "_parentSelected";
    private static final String EXTRA_CHILD = TAG + "_childSelected";
    private static final String EXTRA_HEADERS = TAG + "_headersShown";
    private static final String EXTRA_STICKY = TAG + "_stickyHeaders";
    private static final String EXTRA_LEVEL = TAG + "_selectedLevel";
    private static final String EXTRA_SEARCH = TAG + "_searchText";

    /* The main container for ALL items */
    private List<T> mItems, mTempItems, mOriginalList;

    /* HashSet, AsyncTask and DiffUtil objects, will increase performance in big list */
    private Set<T> mHashItems;
    private List<Notification> mNotifications;
    private FilterAsyncTask mFilterAsyncTask;
    private long start, time;

    /* Handler for delayed actions */
    protected final int UPDATE = 1, FILTER = 2, LOAD_MORE_COMPLETE = 8;
    protected Handler mHandler = new Handler(Looper.getMainLooper(), new HandlerCallback());

    /* Deleted items and RestoreList (Undo) */
    private List<RestoreInfo> mRestoreList;
    private boolean restoreSelection = false, multiRange = false, unlinkOnRemoveHeader = false,
            permanentDelete = true, adjustSelected = true;

    /* Scrollable Headers/Footers items */
    private List<T> mScrollableHeaders, mScrollableFooters;

    /* Section items (with sticky headers) */
    private boolean headersShown = false, recursive = false;
    private int mStickyElevation;
    private StickyHeaderHelper mStickyHeaderHelper;
    private ViewGroup mStickyContainer;

    /* ViewTypes */
    protected LayoutInflater mInflater;
    @SuppressLint("UseSparseArrays")//We can usually count Type instances on the fingers of a hand
    private HashMap<Integer, T> mTypeInstances = new HashMap<>();
    private boolean autoMap = false;

    /* Filter */
    private String mSearchText = "", mOldSearchText = "";
    private Set<IExpandable> mExpandedFilterFlags;
    private boolean notifyChangeOfUnfilteredItems = true, filtering = false,
            notifyMoveOfFilteredItems = false;
    private static int ANIMATE_TO_LIMIT = 1000;
    private int mAnimateToLimit = ANIMATE_TO_LIMIT;

    /* Expandable flags */
    private int mMinCollapsibleLevel = 0, mSelectedLevel = -1;
    private boolean scrollOnExpand = false, collapseOnExpand = false, collapseSubLevels = false,
            childSelected = false, parentSelected = false;

    /* Drag&Drop and Swipe helpers */
    private ItemTouchHelperCallback mItemTouchHelperCallback;
    private ItemTouchHelper mItemTouchHelper;

    /* EndlessScroll */
    private int mEndlessScrollThreshold = 1, mEndlessTargetCount = 0, mEndlessPageSize = 0;
    private boolean endlessLoading = false, endlessScrollEnabled = false, mTopEndless = false;
    private T mProgressItem;

    /* Listeners */
    public OnItemClickListener mItemClickListener;
    public OnItemLongClickListener mItemLongClickListener;
    protected OnUpdateListener mUpdateListener;
    protected OnItemMoveListener mItemMoveListener;
    protected OnItemSwipeListener mItemSwipeListener;
    protected EndlessScrollListener mEndlessScrollListener;
    protected OnDeleteCompleteListener mDeleteCompleteListener;
    protected OnStickyHeaderChangeListener mStickyHeaderChangeListener;

	/*--------------*/
    /* CONSTRUCTORS */
    /*--------------*/

    /**
     * Simple Constructor with NO listeners!
     *
     * @param items items to display.
     * @see #FlexibleAdapter(List, Object)
     * @see #FlexibleAdapter(List, Object, boolean)
     * @since 4.2.0 Created
     * <br>5.0.0-rc2 Copy of the Original List is done internally
     */
    public FlexibleAdapter(@Nullable List<T> items) {
        this(items, null);
    }

    /**
     * Main Constructor with all managed listeners for ViewHolder and the Adapter itself.
     * <p>The listener must be a single instance of a class, usually <i>Activity</i> or
     * <i>Fragment</i>, where you can implement how to handle the different events.</p>
     * <p><b>THE ADAPTER WORKS WITH A <u>COPY</u> OF THE ORIGINAL LIST</b>:
     * {@code new ArrayList<T>(originalList);}</i></p>
     *
     * @param items     items to display
     * @param listeners can be an instance of:
     *                  <ul>
     *                  <li>{@link OnItemClickListener}
     *                  <li>{@link OnItemLongClickListener}
     *                  <li>{@link OnItemMoveListener}
     *                  <li>{@link OnItemSwipeListener}
     *                  <li>{@link OnStickyHeaderChangeListener}
     *                  <li>{@link OnUpdateListener}
     *                  </ul>
     * @see #FlexibleAdapter(List)
     * @see #FlexibleAdapter(List, Object, boolean)
     * @see #addListener(Object)
     * @since 5.0.0-b1 Created
     * <br>5.0.0-rc2 Copy of the Original List is done internally
     */
    public FlexibleAdapter(@Nullable List<T> items, @Nullable Object listeners) {
        this(items, listeners, false);
    }

    /**
     * Same as {@link #FlexibleAdapter(List, Object)} with possibility to set stableIds.
     * <p><b>Tip:</b> Setting {@code true} allows the RecyclerView to rebind only items really
     * changed after a refresh with {@link #notifyDataSetChanged()} or after swapping Adapter.
     * This increases performance.<br>
     * Set {@code true} only if items implement {@link Object#hashCode()} and have unique ids.
     * The method {@link #setHasStableIds(boolean)} will be called.</p>
     *
     * @param stableIds set {@code true} if item implements {@code hashcode()} and have unique ids.
     * @see #FlexibleAdapter(List)
     * @see #FlexibleAdapter(List, Object)
     * @see #addListener(Object)
     * @since 5.0.0-b8 Created
     * <br>5.0.0-rc2 Copy of the Original List is done internally
     */
    public FlexibleAdapter(@Nullable List<T> items, @Nullable Object listeners, boolean stableIds) {
        super(stableIds);
        // Copy of the original list
        if (items == null) mItems = new ArrayList<>();
        else mItems = new ArrayList<>(items);
        // Initialize internal lists
        mScrollableHeaders = new ArrayList<>();
        mScrollableFooters = new ArrayList<>();
        mRestoreList = new ArrayList<>();

        // Create listeners instances
        addListener(listeners);

        // Get notified when items are inserted or removed (it adjusts selected positions)
        registerAdapterDataObserver(new AdapterDataObserver());
    }

    /**
     * Initializes the listener(s) of this Adapter.
     * <p>This method is automatically called from the Constructor.</p>
     *
     * @param listener the object(s) instance(s) of any listener
     * @return this Adapter, so the call can be chained
     * @see #removeListener(Class[])
     * @since 5.0.0-b6
     */
    @CallSuper
    public FlexibleAdapter<T> addListener(@Nullable Object listener) {
        if (listener != null) {
            log.i("Setting listener class %s as:", getClassName(listener));
        }
        if (listener instanceof OnItemClickListener) {
            log.i("- OnItemClickListener");
            mItemClickListener = (OnItemClickListener) listener;
            for (FlexibleViewHolder holder : getAllBoundViewHolders()) {
                holder.getContentView().setOnClickListener(holder);
            }
        }
        if (listener instanceof OnItemLongClickListener) {
            log.i("- OnItemLongClickListener");
            mItemLongClickListener = (OnItemLongClickListener) listener;
            // Restore the event
            for (FlexibleViewHolder holder : getAllBoundViewHolders()) {
                holder.getContentView().setOnLongClickListener(holder);
            }
        }
        if (listener instanceof OnItemMoveListener) {
            log.i("- OnItemMoveListener");
            mItemMoveListener = (OnItemMoveListener) listener;
        }
        if (listener instanceof OnItemSwipeListener) {
            log.i("- OnItemSwipeListener");
            mItemSwipeListener = (OnItemSwipeListener) listener;
        }
        if (listener instanceof OnDeleteCompleteListener) {
            log.i("- OnDeleteCompleteListener");
            mDeleteCompleteListener = (OnDeleteCompleteListener) listener;
        }
        if (listener instanceof OnStickyHeaderChangeListener) {
            log.i("- OnStickyHeaderChangeListener");
            mStickyHeaderChangeListener = (OnStickyHeaderChangeListener) listener;
        }
        if (listener instanceof OnUpdateListener) {
            log.i("- OnUpdateListener");
            mUpdateListener = (OnUpdateListener) listener;
            mUpdateListener.onUpdateEmptyView(getMainItemCount());
        }
        return this;
    }

    /**
     * Removes one or more listeners from this Adapter.
     * <p><b>Warning:</b>
     * <ul><li>In case of <i>Click</i> and <i>LongClick</i> events, it will remove also the callback
     * from all bound ViewHolders too. To restore these 2 events on the current bound ViewHolders
     * call {@link #addListener(Object)} providing the instance of desired listener.</li>
     * <li>To remove a specific listener you have to provide the Class of the listener,
     * example:
     * <pre>removeListener(FlexibleAdapter.OnUpdateListener.class,
     *     FlexibleAdapter.OnItemLongClickListener.class);</pre></li></ul></p>
     *
     * @param listeners the listeners type Classes to remove from the this Adapter and/or from all bound ViewHolders
     * @return this Adapter, so the call can be chained
     * @see #addListener(Object)
     * @since 5.0.0-rc3
     */
    public final FlexibleAdapter<T> removeListener(@NonNull Class... listeners) {
        if (listeners == null || listeners.length == 0) {
            log.e("No listener class to remove!");
            return this;
        }
        for (Class listener : listeners) {
            if (listener == OnItemClickListener.class) {
                mItemClickListener = null;
                log.i("Removed OnItemClickListener");
                for (FlexibleViewHolder holder : getAllBoundViewHolders()) {
                    holder.getContentView().setOnClickListener(null);
                }
            }
            if (listener == OnItemLongClickListener.class) {
                mItemLongClickListener = null;
                log.i("Removed OnItemLongClickListener");
                for (FlexibleViewHolder holder : getAllBoundViewHolders()) {
                    holder.getContentView().setOnLongClickListener(null);
                }
            }
            if (listener == OnItemMoveListener.class) {
                mItemMoveListener = null;
                log.i("Removed OnItemMoveListener");
            }
            if (listener == OnItemSwipeListener.class) {
                mItemSwipeListener = null;
                log.i("Removed OnItemSwipeListener");
            }
            if (listener == OnDeleteCompleteListener.class) {
                mDeleteCompleteListener = null;
                log.i("Removed OnDeleteCompleteListener");
            }
            if (listener == OnStickyHeaderChangeListener.class) {
                mStickyHeaderChangeListener = null;
                log.i("Removed OnStickyHeaderChangeListener");
            }
            if (listener == OnUpdateListener.class) {
                mUpdateListener = null;
                log.i("Removed OnUpdateListener");
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>Attaches the {@code StickyHeaderHelper} to the RecyclerView if necessary.</p>
     *
     * @since 5.0.0-b6
     */
    @CallSuper
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        log.v("Attached Adapter to RecyclerView");
        if (headersShown && areHeadersSticky()) {
            mStickyHeaderHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    /**
     * {@inheritDoc}
     * <p>Detaches the {@code StickyHeaderHelper} from the RecyclerView if necessary.</p>
     *
     * @since 5.0.0-b6
     */
    @CallSuper
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (areHeadersSticky()) {
            mStickyHeaderHelper.detachFromRecyclerView();
            mStickyHeaderHelper = null;
        }
        super.onDetachedFromRecyclerView(recyclerView);
        log.v("Detached Adapter from RecyclerView");
    }

    /**
     * Maps and expands items that are initially configured to be shown as expanded.
     * <p>This method should be called during the creation of the Activity/Fragment, useful also
     * after a screen rotation.</p>
     *
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b6
     */
    public FlexibleAdapter<T> expandItemsAtStartUp() {
        int position = 0;
        setScrollAnimate(true);
        multiRange = true;
        while (position < getItemCount()) {
            T item = getItem(position);
            if (!headersShown && isHeader(item) && !item.isHidden()) {
                headersShown = true;
            }
            if (isExpanded(item)) {
                expand(position, false, true, false);
            }
            position++; //+1 Check also subItems with expanded = true
        }
        multiRange = false;
        setScrollAnimate(false);
        return this;
    }

	/*------------------------------*/
    /* SELECTION METHODS OVERRIDDEN */
	/*------------------------------*/

    /**
     * Checks if the current item has the property {@code enabled = true}.
     * <p>When an item is disabled, user cannot interact with it.</p>
     *
     * @param position the current position of the item to check
     * @return true if the item property <i>enabled</i> is set true, false otherwise
     * @since 5.0.0-b6
     */
    public boolean isEnabled(int position) {
        T item = getItem(position);
        return item != null && item.isEnabled();
    }

    /**
     * {@inheritDoc}
     *
     * @since 5.0.0-b6
     */
    @Override
    public boolean isSelectable(int position) {
        T item = getItem(position);
        return item != null && item.isSelectable();
    }

    /**
     * {@inheritDoc}
     *
     * @param position position of the item to toggle the selection status for.
     * @since 5.0.0-b1
     */
    //TODO: Review the logic of selection coherence
    @Override
    public void toggleSelection(@IntRange(from = 0) int position) {
        T item = getItem(position);
        // Allow selection only for selectable items
        if (item != null && item.isSelectable()) {
            IExpandable parent = getExpandableOf(item);
            boolean hasParent = parent != null;
            if ((isExpandable(item) || !hasParent) && !childSelected) {
                // Allow selection of Parent if no Child has been previously selected
                parentSelected = true;
                if (hasParent) mSelectedLevel = parent.getExpansionLevel();
                super.toggleSelection(position);
            } else if (hasParent && (mSelectedLevel == -1 || !parentSelected && parent.getExpansionLevel() + 1 == mSelectedLevel)) {
                // Allow selection of Child of same level and if no Parent has been previously selected
                childSelected = true;
                mSelectedLevel = parent.getExpansionLevel() + 1;
                super.toggleSelection(position);
            }
        }
        // Reset flags if necessary, just to be sure
        if (super.getSelectedItemCount() == 0) {
            mSelectedLevel = -1;
            parentSelected = childSelected = false;
        }
    }

    /**
     * Helper to automatically select all the items of the viewType equal to the viewType of
     * the first selected item.
     * <p>Examples:
     * <ul><li>if user initially selects an expandable of type A, then only expandable items of
     * type A can be selected.</li>
     * <li>if user initially selects a non-expandable of type B, then only items of type B
     * can be selected.</li>
     * <li>The developer can override this behaviour by passing a list of viewTypes for which
     * he wants to force the selection.</li></ul></p>
     *
     * @param viewTypes All the desired viewTypes to be selected, providing no view types, will
     *                  automatically select all the viewTypes of the first item user has selected
     * @since 5.0.0-b1
     */
    @Override
    public void selectAll(Integer... viewTypes) {
        if (getSelectedItemCount() > 0 && viewTypes.length == 0) {
            super.selectAll(getItemViewType(getSelectedPositions().get(0))); //Priority on the first item
        } else {
            super.selectAll(viewTypes); //Force the selection for the viewTypes passed
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 5.0.0-b1
     */
    @Override
    @CallSuper
    public void clearSelection() {
        parentSelected = childSelected = false;
        super.clearSelection();
    }

    /**
     * @return true if a parent is selected
     * @since 5.0.0-b1
     */
    public boolean isAnyParentSelected() {
        return parentSelected;
    }

    /**
     * @return true if any child of any parent is selected, false otherwise
     * @since 5.0.0-b1
     */
    public boolean isAnyChildSelected() {
        return childSelected;
    }

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

    /**
     * Convenience method of {@link #updateDataSet(List, boolean)} (You should read the comments
     * of this method).
     * <p>In this call, changes will NOT be animated: <b>Warning!</b>
     * {@link #notifyDataSetChanged()} will be invoked.</p>
     *
     * @param items the new data set
     * @see #updateDataSet(List, boolean)
     * @since 5.0.0-b1 Created
     * <br>5.0.0-rc2 Copy of the Original List done internally
     */
    @CallSuper
    public void updateDataSet(@Nullable List<T> items) {
        updateDataSet(items, false);
    }

    /**
     * This method will refresh the entire data set content. Optionally, all changes can be
     * animated, limited by the value previously set with {@link #setAnimateToLimit(int)}
     * to improve performance on very big list. Should provide {@code animate=false} to
     * directly invoke {@link #notifyDataSetChanged()} without any animations, if {@code stableIds}
     * is not set!
     * <p><b>Note:</b>
     * <ul><li>Scrollable Headers and Footers (if existent) will be restored in this call.</li>
     * <li>I strongly recommend to implement {@link Object#hashCode()} to all adapter items
     * along with {@link Object#equals(Object)}: This Adapter is making use of HashSet to
     * improve performance.</li>
     * </ul></p>
     * <b>Note:</b> The following methods will be also called at the end of the operation:
     * <ol><li>{@link #expandItemsAtStartUp()}</li>
     * <li>{@link #showAllHeaders()} if headers are shown</li>
     * <li>{@link #onPostUpdate()}</li>
     * <li>{@link OnUpdateListener#onUpdateEmptyView(int)} if the listener is set</li></ol>
     *
     * @param items   the new data set
     * @param animate true to animate the changes, false for an instant refresh
     * @see #updateDataSet(List)
     * @see #setAnimateToLimit(int)
     * @see #onPostUpdate()
     * @since 5.0.0-b7 Created
     * <br>5.0.0-b8 Synchronization animations limit
     * <br>5.0.0-rc2 Copy of the Original List done internally
     */
    @CallSuper
    public void updateDataSet(@Nullable List<T> items, boolean animate) {
        mOriginalList = null; // Reset original list from filter
        if (items == null) items = new ArrayList<>();
        if (animate) {
            mHandler.removeMessages(UPDATE);
            mHandler.sendMessage(Message.obtain(mHandler, UPDATE, items));
        } else {
            // Copy of the original list
            List<T> newItems = new ArrayList<>(items);
            prepareItemsForUpdate(newItems);
            mItems = newItems;
            // Execute instant reset on init
            log.w("updateDataSet with notifyDataSetChanged!");
            notifyDataSetChanged();
            onPostUpdate();
        }
    }

    /**
     * Returns the object of type <b>T</b>.
     * <p>This method cannot be overridden since the entire library relies on it.</p>
     *
     * @param position the position of the item in the list
     * @return The <b>T</b> object for the position provided or null if item not found
     * @since 1.0.0
     */
    @Nullable
    public T getItem(int position) {
        if (position < 0 || position >= getItemCount()) return null;
        return mItems.get(position);
    }

    /**
     * This method is mostly used by the adapter if items have stableIds.
     *
     * @param position the position of the current item
     * @return Hashcode of the item at the specific position
     * @since 5.0.0-b1
     */
    @Override
    public long getItemId(int position) {
        T item = getItem(position);
        return item != null ? item.hashCode() : RecyclerView.NO_ID;
    }

    /**
     * Returns the total number of items in the data set held by the adapter (headers and footers
     * INCLUDED). Use {@link #getMainItemCount()} with {@code false} as parameter to retrieve
     * only real items excluding headers and footers.
     * <p><b>Note:</b> This method cannot be overridden since the selection and the internal
     * methods rely on it.</p>
     *
     * @return the total number of items (headers and footers INCLUDED) held by the adapter
     * @see #getMainItemCount()
     * @see #getItemCountOfTypes(Integer...)
     * @see #isEmpty()
     * @since 1.0.0
     */
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * <ul>
     * <li>Provide {@code true} (default behavior) to count all items, same result of {@link #getItemCount()}.</li>
     * <li>Provide {@code false} to count only main items (headers and footers are EXCLUDED).</li>
     * </ul>
     * <b>Note:</b> This method cannot be overridden since internal methods rely on it.
     *
     * @return the total number of items held by the adapter, with or without headers and footers,
     * depending by the provided parameter
     * @see #getItemCount()
     * @see #getItemCountOfTypes(Integer...)
     * @since 5.0.0-rc1
     */
    public final int getMainItemCount() {
        return hasSearchText() ? getItemCount() : getItemCount() - mScrollableHeaders.size() - mScrollableFooters.size();
    }

    /**
     * Provides the number of items currently displayed of one or more certain types.
     *
     * @param viewTypes the viewTypes to count
     * @return number of the viewTypes counted
     * @see #getItemCount()
     * @see #getMainItemCount()
     * @see #isEmpty()
     * @since 5.0.0-b1
     */
    public final int getItemCountOfTypes(Integer... viewTypes) {
        List<Integer> viewTypeList = Arrays.asList(viewTypes);
        int count = 0;
        for (int i = 0; i < getItemCount(); i++) {
            if (viewTypeList.contains(getItemViewType(i)))
                count++;
        }
        return count;
    }

    /**
     * Gets an unmodifiable view of the internal list of items.
     *
     * @return an unmodifiable view of the current adapter list
     * @since 5.0.0-rc2
     */
    @NonNull
    public final List<T> getCurrentItems() {
        return Collections.unmodifiableList(mItems);
    }

    /**
     * You can override this method to define your own concept of "Empty". This method is never
     * called internally.
     *
     * @return true if the list is empty, false otherwise
     * @see #getItemCount()
     * @see #getItemCountOfTypes(Integer...)
     * @since 4.2.0
     */
    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    /**
     * Retrieves the global position of the item in the Adapter list.
     * If no scrollable Headers are added, the global position coincides with the cardinal position.
     * <p>This method cannot be overridden since the entire library relies on it.</p>
     *
     * @param item the item for which the position needs to be found
     * @return the global position in the Adapter if found, -1 otherwise
     * @see #getSameTypePositionOf(IFlexible)
     * @since 5.0.0-b1
     */
    public final int getGlobalPositionOf(IFlexible item) {
        return item != null ? mItems.indexOf(item) : -1;
    }

    /**
     * Retrieves the position of the Main item in the Adapter list excluding the scrollable Headers.
     * If no scrollable Headers are added, the cardinal position coincides with the global position.
     * <p><b>Note:</b>
     * <br>- This method is NOT suitable to call when managing items: ALL insert, remove, move and
     * swap operations, should done with global position {@link #getGlobalPositionOf(IFlexible)}.
     * <br>- This method cannot be overridden.</p>
     *
     * @param item the item for which the position needs to be found
     * @return the position in the Adapter excluding the Scrollable Headers, -1 otherwise
     * @see #getSameTypePositionOf(IFlexible)
     * @since 5.0.0-rc1
     */
    public final int getCardinalPositionOf(@NonNull IFlexible item) {
        int position = getGlobalPositionOf(item);
        if (position > mScrollableHeaders.size()) position -= mScrollableHeaders.size();
        return position;
    }

    /**
     * Retrieves the position of any item in the Adapter <u>counting</u> only the items of the
     * same view type of the provided item and <u>excluding</u> all the others view types.
     * <p><b>Tip:</b> You can identify the number of the section (you need to add +1) of any
     * headers OR to retrieve the position of an item as it were the only view type visible in
     * the Adapter.</p>
     *
     * @param item the item for which the position needs to be found
     * @return the position in the Adapter counting only the items of the same type, -1 otherwise
     * @see #getSubPositionOf(IFlexible)
     * @since 5.0.0-rc3
     */
    public final int getSameTypePositionOf(@NonNull IFlexible item) {
        int position = -1;
        for (T current : mItems) {
            if (current.getItemViewType() == item.getItemViewType()) {
                position++;
                if (current.equals(item)) break;
            }
        }
        return position;
    }

    /**
     * This method is never called internally.
     *
     * @param item the item to find
     * @return true if the provided item is currently displayed, false otherwise
     * @since 2.0.0
     */
    public boolean contains(@Nullable T item) {
        return item != null && mItems.contains(item);
    }

    /**
     * New method to extract the new position where the item should lay.
     * <p><b>Note: </b>The {@code Comparator} object should be customized to support <u>all</u>
     * types of items this Adapter is managing or a {@code ClassCastException} will be raised.</p>
     * If the {@code Comparator} is {@code null} the returned position is 0 (first position).
     *
     * @param item       the item to evaluate the insertion
     * @param comparator the Comparator object with the logic to sort the list
     * @return the position resulted from sorting with the provided Comparator
     * @since 5.0.0-b7
     */
    public int calculatePositionFor(@NonNull Object item, @Nullable Comparator<IFlexible> comparator) {
        // There's nothing to compare
        if (comparator == null) return 0;

        // Header is visible
        if (item instanceof ISectionable) {
            ISectionable sectionable = (ISectionable) item;
            IHeader header = sectionable.getHeader();
            if (header != null && !header.isHidden()) {
                List<ISectionable> sortedList = getSectionItems(header);
                sortedList.add(sectionable);
                Collections.sort(sortedList, comparator);
                int itemPosition = getGlobalPositionOf(sectionable);
                int headerPosition = getGlobalPositionOf(header);
                // #143 - calculatePositionFor() missing a +1 when addItem (fixed by condition: itemPosition != -1)
                // fix represents the situation when item is before the target position (used in moveItem)
                int fix = itemPosition != -1 && itemPosition < headerPosition ? 0 : 1;
                int result = headerPosition + sortedList.indexOf(item) + fix;
                log.v("Calculated finalPosition=%s sectionPosition=%s relativePosition=%s fix=%s",
                        result, headerPosition, sortedList.indexOf(item), fix);
                return result;
            }
        }
        // All other cases
        List sortedList = new ArrayList<>(mItems);
        if (!sortedList.contains(item)) sortedList.add(item);
        Collections.sort(sortedList, comparator);
        log.v("Calculated position %s for item=%s", Math.max(0, sortedList.indexOf(item)), item);
        return Math.max(0, sortedList.indexOf(item));
    }

	/*------------------------------------*/
	/* SCROLLABLE HEADERS/FOOTERS METHODS */
	/*------------------------------------*/

    /**
     * @return unmodifiable list of Scrollable Headers currently held by the Adapter
     * @see #addScrollableHeader(IFlexible)
     * @see #addScrollableHeaderWithDelay(IFlexible, long, boolean)
     * @since 5.0.0-rc1
     */
    public final List<T> getScrollableHeaders() {
        return Collections.unmodifiableList(mScrollableHeaders);
    }

    /**
     * @return unmodifiable list of Scrollable Footers currently held by the Adapter
     * @see #addScrollableFooter(IFlexible)
     * @see #addScrollableFooterWithDelay(IFlexible, long, boolean)
     * @since 5.0.0-rc1
     */
    public final List<T> getScrollableFooters() {
        return Collections.unmodifiableList(mScrollableFooters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isScrollableHeaderOrFooter(int position) {
        T item = getItem(position);
        return isScrollableHeaderOrFooter(item);
    }

    /**
     * Checks if at the provided item is a Header or Footer.
     *
     * @param item the item to check
     * @return true if it's a scrollable item
     * @since 5.0.0-rc2
     */
    public final boolean isScrollableHeaderOrFooter(T item) {
        return item != null && mScrollableHeaders.contains(item) || mScrollableFooters.contains(item);
    }

    /**
     * Adds a Scrollable Header.
     * <p><b>Scrollable Headers</b> have the following characteristic:
     * <ul>
     * <li>lay always before any main item.</li>
     * <li>cannot be selectable nor draggable.</li>
     * <li>cannot be inserted twice, but many can be inserted.</li>
     * <li>any new header will be inserted before the existent.</li>
     * <li>can be of any type so they can be bound at runtime with any data inside.</li>
     * <li>won't be filtered because they won't be part of the main list, but added separately
     * at the initialization phase</li>
     * <li>can be added and removed with certain delay.</li>
     * </ul></p>
     *
     * @param headerItem the header item to be added
     * @return true if the header has been successfully added, false if the header already exists
     * @see #getScrollableHeaders()
     * @see #addScrollableHeaderWithDelay(IFlexible, long, boolean)
     * @since 5.0.0-rc1
     */
    public final boolean addScrollableHeader(@NonNull T headerItem) {
        log.d("Add scrollable header %s", getClassName(headerItem));
        if (!mScrollableHeaders.contains(headerItem)) {
            headerItem.setSelectable(false);
            headerItem.setDraggable(false);
            int progressFix = (headerItem == mProgressItem) ? mScrollableHeaders.size() : 0;
            mScrollableHeaders.add(headerItem);
            setScrollAnimate(true); //Headers will scroll animate
            performInsert(progressFix, Collections.singletonList(headerItem), true);
            setScrollAnimate(false);
            return true;
        } else {
            log.w("Scrollable header %s already exists", getClassName(headerItem));
            return false;
        }
    }

    /**
     * Adds a Scrollable Footer.
     * <p><b>Scrollable Footers</b> have the following characteristic:
     * <ul>
     * <li>lay always after any main item.</li>
     * <li>cannot be selectable nor draggable.</li>
     * <li>cannot be inserted twice, but many can be inserted.</li>
     * <li>cannot scroll animate, when inserted for the first time.</li>
     * <li>any new footer will be inserted after the existent.</li>
     * <li>can be of any type so they can be bound at runtime with any data inside.</li>
     * <li>won't be filtered because they won't be part of the main list, but added separately
     * at the initialization phase</li>
     * <li>can be added and removed with certain delay.</li>
     * <li>endless {@code progressItem} is handled as a Scrollable Footer, but it will be always
     * displayed between the main items and the others footers.</li>
     * </ul></p>
     *
     * @param footerItem the footer item to be added
     * @return true if the footer has been successfully added, false if the footer already exists
     * @see #getScrollableFooters()
     * @see #addScrollableFooterWithDelay(IFlexible, long, boolean)
     * @since 5.0.0-rc1
     */
    public final boolean addScrollableFooter(@NonNull T footerItem) {
        if (!mScrollableFooters.contains(footerItem)) {
            log.d("Add scrollable footer %s", getClassName(footerItem));
            footerItem.setSelectable(false);
            footerItem.setDraggable(false);
            int progressFix = (footerItem == mProgressItem) ? mScrollableFooters.size() : 0;
            //Prevent wrong position after a possible updateDataSet
            if (progressFix > 0 && mScrollableFooters.size() > 0) {
                mScrollableFooters.add(0, footerItem);
            } else {
                mScrollableFooters.add(footerItem);
            }
            performInsert(getItemCount() - progressFix, Collections.singletonList(footerItem), true);
            return true;
        } else {
            log.w("Scrollable footer %s already exists", getClassName(footerItem));
            return false;
        }
    }

    /**
     * Removes the provided Scrollable Header.
     *
     * @param headerItem the header to remove
     * @see #removeScrollableHeaderWithDelay(IFlexible, long)
     * @see #removeAllScrollableHeaders()
     * @since 5.0.0-rc1
     */
    public final void removeScrollableHeader(@NonNull T headerItem) {
        if (mScrollableHeaders.remove(headerItem)) {
            log.d("Remove scrollable header %s", getClassName(headerItem));
            performRemove(headerItem, true);
        }
    }

    /**
     * Removes the provided Scrollable Footer.
     *
     * @param footerItem the footer to remove
     * @see #removeScrollableFooterWithDelay(IFlexible, long)
     * @see #removeAllScrollableFooters()
     * @since 5.0.0-rc1
     */
    public final void removeScrollableFooter(@NonNull T footerItem) {
        if (mScrollableFooters.remove(footerItem)) {
            log.d("Remove scrollable footer %s", getClassName(footerItem));
            performRemove(footerItem, true);
        }
    }

    /**
     * Removes all Scrollable Headers at once.
     *
     * @see #removeScrollableHeader(IFlexible)
     * @see #removeScrollableHeaderWithDelay(IFlexible, long)
     * @since 5.0.0-rc1
     */
    public final void removeAllScrollableHeaders() {
        if (mScrollableHeaders.size() > 0) {
            log.d("Remove all scrollable headers");
            mItems.removeAll(mScrollableHeaders);
            notifyItemRangeRemoved(0, mScrollableHeaders.size());
            mScrollableHeaders.clear();
        }
    }

    /**
     * Removes all Scrollable Footers at once.
     *
     * @see #removeScrollableFooter(IFlexible)
     * @see #removeScrollableFooterWithDelay(IFlexible, long)
     * @since 5.0.0-rc1
     */
    public final void removeAllScrollableFooters() {
        if (mScrollableFooters.size() > 0) {
            log.d("Remove all scrollable footers");
            mItems.removeAll(mScrollableFooters);
            notifyItemRangeRemoved(getItemCount() - mScrollableFooters.size(), mScrollableFooters.size());
            mScrollableFooters.clear();
        }
    }

    /**
     * Same as {@link #addScrollableHeader(IFlexible)} but with a delay and the possibility to
     * scroll to it.
     *
     * @param headerItem       the header item to be added
     * @param delay            the delay in ms
     * @param scrollToPosition true to scroll to the header item position once it has been added
     * @see #addScrollableHeader(IFlexible)
     * @since 5.0.0-rc1
     */
    public final void addScrollableHeaderWithDelay(@NonNull final T headerItem, @IntRange(from = 0) long delay,
                                                   final boolean scrollToPosition) {
        log.d("Enqueued adding scrollable header (%sms) %s", delay, getClassName(headerItem));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (addScrollableHeader(headerItem) && scrollToPosition)
                    performScroll(getGlobalPositionOf(headerItem));
            }
        }, delay);
    }

    /**
     * Same as {@link #addScrollableFooter(IFlexible)} but with a delay and the possibility to
     * scroll to it.
     *
     * @param footerItem       the footer item to be added
     * @param delay            the delay in ms
     * @param scrollToPosition true to scroll to the footer item position once it has been added
     * @see #addScrollableFooter(IFlexible)
     * @since 5.0.0-rc1
     */
    public final void addScrollableFooterWithDelay(@NonNull final T footerItem, @IntRange(from = 0) long delay,
                                                   final boolean scrollToPosition) {
        log.d("Enqueued adding scrollable footer (%sms) %s", delay, getClassName(footerItem));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (addScrollableFooter(footerItem) && scrollToPosition)
                    performScroll(getGlobalPositionOf(footerItem));
            }
        }, delay);
    }

    /**
     * Same as {@link #removeScrollableHeader(IFlexible)} but with a delay.
     *
     * @param headerItem the header item to be removed
     * @param delay      the delay in ms
     * @see #removeScrollableHeader(IFlexible)
     * @see #removeAllScrollableHeaders()
     * @since 5.0.0-rc1
     */
    public final void removeScrollableHeaderWithDelay(@NonNull final T headerItem, @IntRange(from = 0) long delay) {
        log.d("Enqueued removing scrollable header (%sms) %s", delay, getClassName(headerItem));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeScrollableHeader(headerItem);
            }
        }, delay);
    }

    /**
     * Same as {@link #removeScrollableFooter(IFlexible)} but with a delay.
     *
     * @param footerItem the footer item to be removed
     * @param delay      the delay in ms
     * @see #removeScrollableFooter(IFlexible)
     * @see #removeAllScrollableFooters()
     * @since 5.0.0-rc1
     */
    public final void removeScrollableFooterWithDelay(@NonNull final T footerItem, @IntRange(from = 0) long delay) {
        log.d("Enqueued removing scrollable footer (%sms) %s", delay, getClassName(footerItem));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeScrollableFooter(footerItem);
            }
        }, delay);
    }

    /**
     * Helper method to restore the scrollable headers/footers along with the main items.
     * After the update and the filter operations.
     */
    private void restoreScrollableHeadersAndFooters(List<T> items) {
        for (T item : mScrollableHeaders)
            if (items.size() > 0) items.add(0, item);
            else items.add(item);
        for (T item : mScrollableFooters)
            items.add(item);
    }

	/*--------------------------*/
	/* HEADERS/SECTIONS METHODS */
	/*--------------------------*/

    /**
     * Setting to automatically unlink the deleted header from items having that header linked.
     * <p>Default value is {@code false}.</p>
     *
     * @param unlinkOnRemoveHeader true to unlink the deleted header from items having that header
     *                             linked, false otherwise
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b6
     */
    public FlexibleAdapter<T> setUnlinkAllItemsOnRemoveHeaders(boolean unlinkOnRemoveHeader) {
        log.i("Set unlinkOnRemoveHeader=%s", unlinkOnRemoveHeader);
        this.unlinkOnRemoveHeader = unlinkOnRemoveHeader;
        return this;
    }

    /**
     * Retrieves all the header items.
     *
     * @return non-null list with all the header items
     * @since 5.0.0-b6
     */
    @NonNull
    public List<IHeader> getHeaderItems() {
        List<IHeader> headers = new ArrayList<>();
        for (T item : mItems) {
            if (isHeader(item))
                headers.add((IHeader) item);
        }
        return headers;
    }

    /**
     * @param item the item to check
     * @return true if the item is an instance of {@link IHeader} interface, false otherwise
     * @since 5.0.0-b6
     */
    public boolean isHeader(T item) {
        return item != null && item instanceof IHeader;
    }

    /**
     * Helper method to check if an item holds a header.
     *
     * @param item the identified item
     * @return true if the item holds a header, false otherwise
     * @since 5.0.0-b6
     */
    public boolean hasHeader(T item) {
        return getHeaderOf(item) != null;
    }

    /**
     * Checks if the item has a header and that header is the same of the provided one.
     *
     * @param item   the item supposing having the header
     * @param header the header to compare
     * @return true if the item has a header and it is the same of the provided one, false otherwise
     * @since 5.0.0-b6
     */
    public boolean hasSameHeader(T item, IHeader header) {
        IHeader current = getHeaderOf(item);
        return current != null && header != null && current.equals(header);
    }

    /**
     * Retrieves the header of the provided {@code ISectionable} item.
     *
     * @param item the ISectionable item holding a header
     * @return the header of the passed Sectionable, null otherwise
     * @since 5.0.0-b6
     */
    public IHeader getHeaderOf(T item) {
        if (item != null && item instanceof ISectionable) {
            return ((ISectionable) item).getHeader();
        }
        return null;
    }

    /**
     * Retrieves the {@link IHeader} item of any specified position.
     *
     * @param position the item position
     * @return the IHeader item linked to the specified item position
     * @since 5.0.0-b6
     */
    public IHeader getSectionHeader(@IntRange(from = 0) int position) {
        // Headers are not visible nor sticky
        if (!headersShown) return null;
        // When headers are visible and sticky, get the previous header
        for (int i = position; i >= 0; i--) {
            T item = getItem(i);
            if (isHeader(item)) return (IHeader) item;
        }
        return null;
    }

    /**
     * Provides all the items that belongs to the section represented by the provided header.
     *
     * @param header the {@code IHeader} item that represents the section
     * @return NonNull list of all items in the provided section
     * @since 5.0.0-b6
     */
    @NonNull
    public List<ISectionable> getSectionItems(@NonNull IHeader header) {
        List<ISectionable> sectionItems = new ArrayList<>();
        int startPosition = getGlobalPositionOf(header);
        T item = getItem(++startPosition);
        while (hasSameHeader(item, header)) {
            sectionItems.add((ISectionable) item);
            item = getItem(++startPosition);
        }
        return sectionItems;
    }

    /**
     * Provides all the item positions that belongs to the section represented by the provided
     * header.
     *
     * @param header the {@code IHeader} item that represents the section
     * @return NonNull list of all item positions in the provided section
     * @since 5.0.0-b8
     */
    @NonNull
    public List<Integer> getSectionItemPositions(@NonNull IHeader header) {
        List<Integer> sectionItemPositions = new ArrayList<>();
        int position = getGlobalPositionOf(header);
        T item = getItem(++position);
        while (hasSameHeader(item, header)) {
            sectionItemPositions.add(position);
            item = getItem(++position);
        }
        return sectionItemPositions;
    }

    /**
     * Evaluates if Adapter has headers shown.
     *
     * @return true if all headers are currently displayed, false otherwise
     * @since 5.0.0-b6
     */
    public boolean areHeadersShown() {
        return headersShown;
    }

    /**
     * Evaluates if Adapter can actually display sticky headers on the top.
     *
     * @return true if headers can be sticky, false if headers are scrolled together with all items
     * @since 5.0.0-b6
     */
    public boolean areHeadersSticky() {
        return mStickyHeaderHelper != null;
    }

    /**
     * Returns the current position of the sticky header.
     *
     * @return the current sticky header position, -1 if no header is sticky
     * @since 5.0.0-rc2
     */
    public final int getStickyPosition() {
        return areHeadersSticky() ? mStickyHeaderHelper.getStickyPosition() : -1;
    }

    /**
     * Ensures the current sticky header view is correctly displayed in the sticky layout.
     *
     * @since 5.0.0-rc2
     */
    public final void ensureHeaderParent() {
        if (areHeadersSticky()) mStickyHeaderHelper.ensureHeaderParent();
    }

    /**
     * Enables/Disables the sticky header feature with <u>default</u> sticky layout container.
     * <p><b>Note:</b>
     * <ul>
     * <li>You should consider to display headers with {@link #setDisplayHeadersAtStartUp(boolean)}:
     * Feature can enabled/disabled freely, but if headers are hidden nothing will happen.</li>
     * <li>Only in case of "Sticky Header" items you must <u>provide</u> {@code true} to the
     * constructor: {@link FlexibleViewHolder#FlexibleViewHolder(View, FlexibleAdapter, boolean)}.</li>
     * <li>Optionally, you can set a custom sticky layout container that must be <u>already
     * inflated</u>.
     * <br>Check {@link #setStickyHeaders(boolean, ViewGroup)}.</li>
     * <li>Optionally, you can set a layout <u>elevation</u>: Header item elevation is used first,
     * if not set, default elevation of {@code 21f} pixel is used.
     * <li>Sticky headers are clickable as any views, but cannot be dragged nor swiped.</li>
     * <li>Content and linkage are automatically updated.</li>
     * <li>Sticky layout container is <i>fade-in</i> and <i>fade-out</i> animated when feature
     * is respectively enabled and disabled.</li>
     * <li>Sticky container can be elevated only if the header item layout has elevation (Do not
     * exaggerate with elevation).</li>
     * </ul>
     * </p>
     * <b>Important!</b> In order to display the Refresh circle AND the FastScroller on the Top of
     * the sticky container, the RecyclerView must be wrapped with a {@code FrameLayout} as following:
     * <pre>
     * &lt;FrameLayout
     *     android:layout_width="match_parent"
     *     android:layout_height="match_parent"&gt;
     *
     *     &lt;android.support.v7.widget.RecyclerView
     *         android:id="@+id/recycler_view"
     *         android:layout_width="match_parent"
     *         android:layout_height="match_parent"/&gt;
     *
     * &lt;/FrameLayout&gt;
     * </pre>
     *
     * @param sticky true to initialize sticky headers with default container, false to disable them
     * @return this Adapter, so the call can be chained
     * @throws IllegalStateException if this Adapter was not attached to the RecyclerView
     * @see #setStickyHeaders(boolean, ViewGroup)
     * @see #setDisplayHeadersAtStartUp(boolean)
     * @see #setStickyHeaderElevation(int)
     * @since 5.0.0-rc1
     */
    public FlexibleAdapter<T> setStickyHeaders(boolean sticky) {
        return setStickyHeaders(sticky, mStickyContainer);
    }

    /**
     * Enables/Disables the sticky header feature with a <u>custom</u> sticky layout container.
     * <p><b>Important:</b> Read the javaDoc of the overloaded method {@link #setStickyHeaders(boolean)}.</p>
     *
     * @param sticky          true to initialize sticky headers, false to disable them
     * @param stickyContainer user defined and already <u>inflated</u> sticky layout that will
     *                        hold the sticky header itemViews
     * @return this Adapter, so the call can be chained
     * @throws IllegalStateException if this Adapter was not attached to the RecyclerView
     * @see #setStickyHeaders(boolean)
     * @see #setDisplayHeadersAtStartUp(boolean)
     * @see #setStickyHeaderElevation(int)
     * @since 5.0.0-rc1
     */
    public FlexibleAdapter<T> setStickyHeaders(final boolean sticky, @Nullable ViewGroup stickyContainer) {
        log.i("Set stickyHeaders=%s (in Post!)%s", sticky, (stickyContainer != null ? " with user defined Sticky Container" : ""));

        // With user defined container
        mStickyContainer = stickyContainer;

        // Run in post to be sure about the RecyclerView initialization
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Enable or Disable the sticky headers layout
                if (sticky) {
                    if (mStickyHeaderHelper == null) {
                        mStickyHeaderHelper = new StickyHeaderHelper(FlexibleAdapter.this,
                                mStickyHeaderChangeListener, mStickyContainer);
                        mStickyHeaderHelper.attachToRecyclerView(mRecyclerView);
                        log.i("Sticky headers enabled");
                    }
                } else if (areHeadersSticky()) {
                    mStickyHeaderHelper.detachFromRecyclerView();
                    mStickyHeaderHelper = null;
                    log.i("Sticky headers disabled");
                }
            }
        });
        return this;
    }

    /**
     * Gets the layout in dpi elevation for sticky header.
     * <p><b>Note:</b> This setting is ignored if the header item has already an elevation. The
     * header elevation overrides this setting.</p>
     *
     * @return the elevation in pixel
     * @see #setStickyHeaderElevation(int)
     * @since 5.0.0-rc1
     */
    public int getStickyHeaderElevation() {
        return mStickyElevation;
    }

    /**
     * Sets the elevation in dpi for the sticky header layout.
     * <p><b>Note:</b> This setting is ignored if the header item has already an elevation. The
     * header elevation overrides this setting.</p>
     * Default value is 0 dpi.
     *
     * @param stickyElevation the elevation in dpi
     * @return this Adapter, so the call can be chained
     * @see #getStickyHeaderElevation()
     * @since 5.0.0-rc1
     */
    public FlexibleAdapter<T> setStickyHeaderElevation(@IntRange(from = 0) int stickyElevation) {
        mStickyElevation = stickyElevation;
        return this;
    }

    /**
     * Sets if all headers should be shown at startup.
     * <p>If called, this method won't trigger {@code notifyItemInserted()} and scrolling
     * animations are instead performed if the header item was configured with animation:
     * <u>Headers will be loaded/bound along with others items.</u></p>
     * <b>Note:</b> Headers can only be shown or hidden all together.
     * <p>Default value is {@code false} (headers are <u>not</u> shown at startup).</p>
     *
     * @param displayHeaders true to display headers, false to keep them hidden
     * @return this Adapter, so the call can be chained
     * @see #showAllHeaders()
     * @see #setAnimationOnScrolling(boolean)
     * @since 5.0.0-b6
     */
    public FlexibleAdapter<T> setDisplayHeadersAtStartUp(boolean displayHeaders) {
        if (!headersShown && displayHeaders) {
            showAllHeaders(true);
        }
        return this;
    }

    /**
     * Shows all headers in the RecyclerView at their linked position.
     * <p>If called at startup, this method will trigger {@code notifyItemInserted} for a
     * different loading effect: <u>Headers will be inserted after the items!</u></p>
     * <b>Note:</b> Headers can only be shown or hidden all together.
     *
     * @return this Adapter, so the call can be chained
     * @see #hideAllHeaders()
     * @see #setDisplayHeadersAtStartUp(boolean)
     * @since 5.0.0-b1
     */
    public FlexibleAdapter<T> showAllHeaders() {
        showAllHeaders(false);
        return this;
    }

    /**
     * @param init true to skip {@code notifyItemInserted}, false to make the call in Post
     *             and notify single insertion
     */
    private void showAllHeaders(boolean init) {
        if (init) {
            log.i("showAllHeaders at startup");
            // No notifyItemInserted!
            showAllHeadersWithReset(true);
        } else {
            log.i("showAllHeaders with insert notification (in Post!)");
            // In post, let's notifyItemInserted!
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // #144 - Check if headers are already shown, discard the call to not duplicate headers
                    if (headersShown) {
                        log.w("Double call detected! Headers already shown OR the method showAllHeaders() was already called!");
                        return;
                    }
                    showAllHeadersWithReset(false);
                    // #142 - At startup, when insert notifications are performed to show headers
                    // for the first time. Header item is not visible at position 0: it has to be
                    // displayed by scrolling to it. This resolves the first item below sticky
                    // header when enabled as well.
                    if (mRecyclerView != null) {
                        int firstVisibleItem = getFlexibleLayoutManager().findFirstCompletelyVisibleItemPosition();
                        if (firstVisibleItem == 0 && isHeader(getItem(0)) && !isHeader(getItem(1))) {
                            mRecyclerView.scrollToPosition(0);
                        }
                    }
                }
            });
        }
    }

    /**
     * @param init true to skip the call to notifyItemInserted, false otherwise
     */
    private void showAllHeadersWithReset(boolean init) {
        int position = 0;
        IHeader sameHeader = null;
        while (position < getItemCount() - mScrollableFooters.size()) {
            T item = getItem(position);
            // Reset hidden status! Necessary after the filter and the update
            IHeader header = getHeaderOf(item);
            if (header != null && !header.equals(sameHeader) && !isExpandable((T) header)) {
                sameHeader = header;
                header.setHidden(true);
            }
            if (showHeaderOf(position, item, init))
                position++; //It's the same element, skip it
            position++;
        }
        headersShown = true;
    }

    /**
     * Internal method to show/add a header in the internal list.
     *
     * @param position the position where the header will be displayed
     * @param item     the item that holds the header
     * @param init     for silent initialization: skip notifyItemInserted
     * @since 5.0.0-b1
     */
    private boolean showHeaderOf(int position, T item, boolean init) {
        // Take the header
        IHeader header = getHeaderOf(item);
        // Check header existence
        if (header == null || getPendingRemovedItem(item) != null) return false;
        if (header.isHidden()) {
            log.v("Showing header position=%s header=%s", position, header);
            header.setHidden(false);
            // Insert header, but skip notifyItemInserted when init=true!
            performInsert(position, Collections.singletonList((T) header), !init);
            return true;
        }
        return false;
    }

    /**
     * Hides all headers from the RecyclerView.
     * <p>Headers can be shown or hidden all together.</p>
     *
     * @see #showAllHeaders()
     * @see #setDisplayHeadersAtStartUp(boolean)
     * @since 5.0.0-b1
     */
    public void hideAllHeaders() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                multiRange = true;
                // Hide linked headers between Scrollable Headers and Footers
                int position = getItemCount() - mScrollableFooters.size() - 1;
                while (position >= Math.max(0, mScrollableHeaders.size() - 1)) {
                    T item = getItem(position);
                    if (isHeader(item))
                        hideHeader(position, (IHeader) item);
                    position--;
                }
                headersShown = false;
                // Clear the header currently sticky
                if (areHeadersSticky()) {
                    mStickyHeaderHelper.clearHeaderWithAnimation();
                }
                // setStickyHeaders(false);
                multiRange = false;
            }
        });
    }

    /**
     * Internal method to hide/remove a header from the internal list.
     *
     * @param item the item that holds the header
     * @since 5.0.0-b1
     */
    private boolean hideHeaderOf(T item) {
        // Take the header
        IHeader header = getHeaderOf(item);
        // Check header existence
        return header != null && !header.isHidden() && hideHeader(getGlobalPositionOf(header), header);
    }

    private boolean hideHeader(int position, IHeader header) {
        if (position >= 0) {
            log.v("Hiding header position=%s header=$s", position, header);
            header.setHidden(true);
            // Remove and notify removals
            mItems.remove(position);
            notifyItemRemoved(position);
            return true;
        }
        return false;
    }

    /**
     * Internal method to link the header to the new item.
     * <p>Used by the Adapter during the Remove/Restore/Move operations.</p>
     * The new item looses the previous header, and if the old header is not shared,
     * old header is added to the orphan list.
     *
     * @param item    the item that holds the header
     * @param header  the header item
     * @param payload any non-null user object to notify the header and the item (the payload
     *                will be therefore passed to the bind method of the items ViewHolder),
     *                pass null to <u>not</u> notify the header and item
     * @since 5.0.0-b6
     */
    private boolean linkHeaderTo(T item, IHeader header, @Nullable Object payload) {
        boolean linked = false;
        if (item != null && item instanceof ISectionable) {
            ISectionable sectionable = (ISectionable) item;
            // Unlink header only if different
            if (sectionable.getHeader() != null && !sectionable.getHeader().equals(header)) {
                unlinkHeaderFrom((T) sectionable, Payload.UNLINK);
            }
            if (sectionable.getHeader() == null && header != null) {
                log.v("Link header %s to %s", header, sectionable);
                //TODO: try-catch for when sectionable item has a different header class signature, if so, they just can't accept that header!
                sectionable.setHeader(header);
                linked = true;
                // Notify items
                if (payload != null) {
                    if (!header.isHidden()) notifyItemChanged(getGlobalPositionOf(header), payload);
                    if (!item.isHidden()) notifyItemChanged(getGlobalPositionOf(item), payload);
                }
            }
        } else {
            notifyItemChanged(getGlobalPositionOf(header), payload);
        }
        return linked;
    }

    /**
     * Internal method to unlink the header from the specified item.
     * <p>Used by the Adapter during the Remove/Restore/Move operations.</p>
     *
     * @param item    the item that holds the header
     * @param payload any non-null user object to notify the header and the item (the payload
     *                will be therefore passed to the bind method of the items ViewHolder),
     *                pass null to <u>not</u> notify the header and item
     * @since 5.0.0-b6
     */
    private IHeader unlinkHeaderFrom(T item, @Nullable Object payload) {
        if (hasHeader(item)) {
            ISectionable sectionable = (ISectionable) item;
            IHeader header = sectionable.getHeader();
            log.v("Unlink header %s from %s", header, sectionable);
            sectionable.setHeader(null);
            // Notify items
            if (payload != null) {
                if (!header.isHidden()) notifyItemChanged(getGlobalPositionOf(header), payload);
                if (!item.isHidden()) notifyItemChanged(getGlobalPositionOf(item), payload);
            }
            return header;
        }
        return null;
    }

	/*--------------------------------------------*/
	/* VIEW HOLDER METHODS ARE DELEGATED TO ITEMS */
	/*--------------------------------------------*/

    /**
     * <p>You <b>CANNOT</b> override this method: {@code FlexibleAdapter} delegates the ViewType
     * definition via {@code IFlexible.getLayoutRes()} so ViewTypes are automatically mapped
     * (AutoMap).</p>
     * {@inheritDoc}
     *
     * @param position position for which ViewType is requested
     * @return layout resource defined in {@code IFlexible#getLayoutRes()}
     * @see IFlexible#getItemViewType()
     * @since 5.0.0-b1
     */
    @Override
    public int getItemViewType(int position) {
        T item = getItem(position);
        if (item == null) {
            return 0;
        }
        // Map the view type if not done yet
        mapViewTypeFrom(item);
        autoMap = true;
        return item.getItemViewType();
    }

    /**
     * You <b>CANNOT</b> override this method to create the ViewHolder, {@code FlexibleAdapter}
     * delegates the creation via {@code IFlexible.createViewHolder()}.
     * <p><b>HELP:</b> To know how to implement AutoMap for ViewTypes please refer to the
     * FlexibleAdapter <a href="https://github.com/davideas/FlexibleAdapter/wiki">Wiki Page</a>
     * on GitHub.</p>
     * {@inheritDoc}
     *
     * @return a new ViewHolder that holds a View of the given view type
     * @see IFlexible#createViewHolder(View, FlexibleAdapter)
     * @since 5.0.0-b1
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        T item = getViewTypeInstance(viewType);
        if (item == null || !autoMap) {
            // If everything has been set properly, this should never happen ;-)
            throw new IllegalStateException(
                    String.format("ViewType instance not found for viewType %s. You should implement the AutoMap properly.", viewType));
        }
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        return item.createViewHolder(mInflater.inflate(item.getLayoutRes(), parent, false), this);
    }

    /**
     * You <b>CANNOT</b> override this method to bind the items. {@code FlexibleAdapter} delegates
     * the binding to the corresponding item via {@code IFlexible.bindViewHolder()}.
     * <p><b>HELP:</b> To know how to implement AutoMap for ViewTypes please refer to the
     * FlexibleAdapter <a href="https://github.com/davideas/FlexibleAdapter/wiki">Wiki Page</a>
     * on GitHub.</p>
     * {@inheritDoc}
     *
     * @see IFlexible#bindViewHolder(FlexibleAdapter, RecyclerView.ViewHolder, int, List)
     * @since 5.0.0-b1
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        this.onBindViewHolder(holder, position, Collections.unmodifiableList(new ArrayList<>()));
    }

    /**
     * Same concept of {@code #onBindViewHolder()} but with Payload.
     * <p/>{@inheritDoc}
     *
     * @see IFlexible#bindViewHolder(FlexibleAdapter, RecyclerView.ViewHolder, int, List)
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     * @since 5.0.0-b1
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position, List payloads) {
        log.v("onViewBound    Holder=%s position=%s itemId=%s", getClassName(holder), position, holder.getItemId());
        if (!autoMap) {
            // If everything has been set properly, this should never happen ;-)
            throw new IllegalStateException("AutoMap is not active, this method cannot be called. You should implement the AutoMap properly.");
        }
        // Bind view activation with current selection
        super.onBindViewHolder(holder, position, payloads);
        // Bind the item
        T item = getItem(position);
        if (item != null) {
            holder.itemView.setEnabled(item.isEnabled());
            item.bindViewHolder(this, holder, position, payloads);
            // Avoid to show the double background in case header has transparency
            // The visibility will be restored when header is reset in StickyHeaderHelper
            if (areHeadersSticky() && !isFastScroll && mStickyHeaderHelper.getStickyPosition() >= 0 && payloads.isEmpty()) {
                int headerPos = getFlexibleLayoutManager().findFirstVisibleItemPosition() - 1;
                if (headerPos == position && isHeader(item))
                    holder.itemView.setVisibility(View.INVISIBLE);
            }
        }
        // Endless Scroll
        onLoadMore(position);
        // Scroll Animation
        animateView(holder, position);
    }

    @CallSuper
    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (areHeadersSticky()) {
            // #297 - Empty (Invisible) Header Item when Using Sticky Headers
            holder.itemView.setVisibility(View.VISIBLE);
        }
        int position = holder.getAdapterPosition();
        T item = getItem(position);
        if (item != null) item.unbindViewHolder(this, holder, position);
    }

	/*------------------------*/
	/* ENDLESS SCROLL METHODS */
	/*------------------------*/

    /**
     * @return true if loading more will add items to the top, false to the bottom.
     * @since 5.0.0-rc2
     */
    public boolean isTopEndless() {
        return mTopEndless;
    }

    /**
     * Sets endless scrolling from the top. All item will be added to the top of the list.
     * Default value is {@code false} (bottom endless scrolling).
     *
     * @param topEndless true to enable endless scrolling from the top, false from the bottom
     * @since 5.0.0-rc2
     */
    public void setTopEndless(boolean topEndless) {
        mTopEndless = topEndless;
    }

    /**
     * Evaluates if the Adapter is in Endless Scroll mode. When no more load, this method will
     * return {@code false}. To enable again the progress item you MUST be set again.
     *
     * @return true if the progress item is set, false otherwise
     * @see #setEndlessProgressItem(IFlexible)
     * @since 5.0.0-rc1
     */
    public boolean isEndlessScrollEnabled() {
        return endlessScrollEnabled;
    }

    /**
     * Provides the current endless page if the page size limit is set, if not set the returned
     * value is always 1.
     *
     * @return the current endless page
     * @see #getEndlessPageSize()
     * @see #setEndlessPageSize(int)
     * @since 5.0.0-rc1
     */
    public int getEndlessCurrentPage() {
        return Math.max(1, mEndlessPageSize > 0 ? getMainItemCount() / mEndlessPageSize : 0);
    }

    /**
     * The current setting for the endless page size limit.
     * <p><b>Tip:</b> This limit is ignored if value is 0.</p>
     *
     * @return the page size limit, if the limit is not set, 0 is returned.
     * @see #getEndlessCurrentPage()
     * @see #setEndlessPageSize(int)
     * @since 5.0.0-rc1
     */
    public int getEndlessPageSize() {
        return mEndlessPageSize;
    }

    /**
     * Sets the limit to automatically disable the endless feature when coming items size is less
     * than the <i>page size</i>.
     * <p>When endless feature is disabled a {@link #notifyItemChanged(int, Object)} with payload
     * {@link Payload#NO_MORE_LOAD} will be triggered on the progressItem, so you can display a
     * message or change the views in this item.</p>
     * Default value is 0 (limit is ignored).
     *
     * @param endlessPageSize the size limit for each page
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-rc1
     */
    public FlexibleAdapter<T> setEndlessPageSize(@IntRange(from = 0) int endlessPageSize) {
        log.i("Set endlessPageSize=%s", endlessPageSize);
        mEndlessPageSize = endlessPageSize;
        return this;
    }

    /**
     * The current setting for the endless target item count limit.
     * <p><b>Tip:</b> This limit is ignored if value is 0.</p>
     *
     * @return the target items count limit, if the limit is not set, 0 is returned.
     * @see #setEndlessTargetCount(int)
     * @since 5.0.0-rc1
     */
    public int getEndlessTargetCount() {
        return mEndlessTargetCount;
    }

    /**
     * Sets the limit to automatically disable the endless feature when the total items count in
     * the Adapter is equals or bigger than the <i>target count</i>.
     * <p>When endless feature is disabled a {@link #notifyItemChanged(int, Object)} with payload
     * {@link Payload#NO_MORE_LOAD} will be triggered on the progressItem, so you can display a
     * message or change the views in this item.</p>
     * Default value is 0 (limit is ignored).
     *
     * @param endlessTargetCount the total items count limit
     * @return this Adapter, so the call can be chained
     * @see #getEndlessTargetCount()
     * @since 5.0.0-rc1
     */
    public FlexibleAdapter<T> setEndlessTargetCount(@IntRange(from = 0) int endlessTargetCount) {
        log.i("Set endlessTargetCount=%s", endlessTargetCount);
        mEndlessTargetCount = endlessTargetCount;
        return this;
    }

    /**
     * Sets if Endless / Loading More should be triggered at start-up, especially when the
     * list is empty.
     * <p>Default value is {@code false}.</p>
     *
     * @param enable true to trigger a loading at start up, false to trigger loading with binding
     * @return this Adapter, so the call can be chained
     */
    public FlexibleAdapter<T> setLoadingMoreAtStartUp(boolean enable) {
        log.i("Set loadingAtStartup=%s", enable);
        if (enable) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onLoadMore(0);
                }
            });
        }
        return this;
    }

    /**
     * Sets the progressItem to be displayed at the end of the list and activate the Loading More
     * feature.
     * <p>Using this method, the {@link EndlessScrollListener} won't be called so that you can
     * handle a click event to load more items upon a user request.</p>
     * To correctly implement "Load more upon a user request" check the Wiki page of this library.
     *
     * @param progressItem the item representing the progress bar
     * @return this Adapter, so the call can be chained
     * @see #isEndlessScrollEnabled()
     * @see #setEndlessScrollListener(EndlessScrollListener, IFlexible)
     * @since 5.0.0-b8
     */
    public FlexibleAdapter<T> setEndlessProgressItem(@Nullable T progressItem) {
        endlessScrollEnabled = progressItem != null;
        if (progressItem != null) {
            setEndlessScrollThreshold(mEndlessScrollThreshold);
            mProgressItem = progressItem;
            log.i("Set progressItem=%s", getClassName(progressItem));
            log.i("Enabled EndlessScrolling");
        } else {
            log.i("Disabled EndlessScrolling");
        }
        return this;
    }

    /**
     * Sets the progressItem to be displayed at the end of the list and Sets the callback to
     * automatically load more items asynchronously(your duty) (no further user action is needed
     * but the scroll).
     *
     * @param endlessScrollListener the callback to invoke the asynchronous loading
     * @param progressItem          the item representing the progress bar
     * @return this Adapter, so the call can be chained
     * @see #isEndlessScrollEnabled()
     * @see #setEndlessProgressItem(IFlexible)
     * @since 5.0.0-b6
     */
    public FlexibleAdapter<T> setEndlessScrollListener(@Nullable EndlessScrollListener endlessScrollListener,
                                                       @NonNull T progressItem) {
        log.i("Set endlessScrollListener=%s", getClassName(endlessScrollListener));
        mEndlessScrollListener = endlessScrollListener;
        return setEndlessProgressItem(progressItem);
    }

    /**
     * Sets the minimum number of items still to bind to start the automatic loading.
     * <p>Default value is 1.</p>
     *
     * @param thresholdItems minimum number of unbound items to start loading more items
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b6
     */
    public FlexibleAdapter<T> setEndlessScrollThreshold(@IntRange(from = 1) int thresholdItems) {
        // Increase visible threshold based on number of columns
        if (mRecyclerView != null) {
            int spanCount = getFlexibleLayoutManager().getSpanCount();
            thresholdItems = thresholdItems * spanCount;
        }
        mEndlessScrollThreshold = thresholdItems;
        log.i("Set endlessScrollThreshold=%s", mEndlessScrollThreshold);
        return this;
    }

    /**
     * This method is called automatically when an item is bound.
     *
     * @param position the current binding position
     * @since 5.0.0-b6
     * <br>5.0.0-rc1 Added limits check and progressItem with Scrollable Footers
     */
    protected void onLoadMore(int position) {
        // Skip everything when loading more is unused OR currently loading
        if (!isEndlessScrollEnabled() || endlessLoading || getItem(position) == mProgressItem)
            return;

        // Check next loading threshold
        int threshold = mTopEndless ?
                mEndlessScrollThreshold - (hasSearchText() ? 0 : mScrollableHeaders.size())
                : getItemCount() - mEndlessScrollThreshold - (hasSearchText() ? 0 : mScrollableFooters.size());
        if ((!mTopEndless && (position == getGlobalPositionOf(mProgressItem) || position < threshold)) ||
                (mTopEndless && position > 0 && position > threshold)) {
            return;
        } else {
            log.v("onLoadMore     topEndless=%s, loading=%s, position=%s, itemCount=%s threshold=%s, currentThreshold=%s",
                    mTopEndless, endlessLoading, position, getItemCount(), mEndlessScrollThreshold, threshold);
        }
        // Load more if not loading and inside the threshold
        endlessLoading = true;
        // Insertion is in post, as suggested by Android because: java.lang.IllegalStateException:
        // Cannot call notifyItemInserted while RecyclerView is computing a layout or scrolling
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Clear previous delayed message
                mHandler.removeMessages(LOAD_MORE_COMPLETE);
                // Add progressItem if not already shown
                boolean added = mTopEndless ? addScrollableHeader(mProgressItem) : addScrollableFooter(mProgressItem);
                // When the listener is not set, loading more is called upon a user request
                if (added && mEndlessScrollListener != null) {
                    log.d("onLoadMore     invoked!");
                    mEndlessScrollListener.onLoadMore(getMainItemCount(), getEndlessCurrentPage());
                } else if (!added) {
                    endlessLoading = false;
                }
            }
        });
    }

    /**
     * To call when more items are successfully loaded.
     * <p>When noMoreLoad OR onError OR onCancel, pass empty list or null to hide the
     * progressItem.</p>
     * In this case the ProgressItem is removed immediately.
     *
     * @param newItems the list of the new items, can be empty or null
     * @since 5.0.0-b6
     */
    public void onLoadMoreComplete(@Nullable List<T> newItems) {
        onLoadMoreComplete(newItems, 0L);
    }

    /**
     * Call this method to complete the action of the Loading more items.
     * <p>When noMoreLoad OR onError OR onCancel, pass empty list or null to hide the
     * progressItem. When limits are set, endless feature will be <u>disabled</u>. To enable
     * again call {@link #setEndlessProgressItem(IFlexible)}.</p>
     * Optionally you can pass a delay time to still display the item with the latest information
     * inside. The message has to be handled inside the {@code bindViewHolder} of the item.
     * <p>A {@link #notifyItemChanged(int, Object)} with payload {@link Payload#NO_MORE_LOAD}
     * will be triggered on the progressItem, so you can display a message or change the views in
     * this item.</p>
     *
     * @param newItems the list of the new items, can be empty or null
     * @param delay    the delay used to remove the progress item or -1 to disable the
     *                 loading forever and to keep the progress item visible.
     * @since 5.0.0-b8
     * <br>5.0.0-rc1 Added limits check and changed progressItem to Scrollable Footer
     * <br>5.0.0-rc2 Added Top Endless
     */
    public void onLoadMoreComplete(@Nullable List<T> newItems, @IntRange(from = -1) long delay) {
        // Calculate new items count
        int newItemsSize = newItems == null ? 0 : newItems.size();
        int totalItemCount = newItemsSize + getMainItemCount();
        // Add any new items
        if (newItemsSize > 0) {
            log.v("onLoadMore     performing adding %s new items on page=%s", newItemsSize, getEndlessCurrentPage());
            int position = mTopEndless ? mScrollableHeaders.size() : getGlobalPositionOf(mProgressItem);
            addItems(position, newItems);
        }
        // Check if features are enabled and the limits have been reached
        if (mEndlessPageSize > 0 && newItemsSize < mEndlessPageSize || // Is feature enabled and Not enough items?
                mEndlessTargetCount > 0 && totalItemCount >= mEndlessTargetCount) { // Is feature enabled and Max limit has been reached?
            // Disable the EndlessScroll feature
            setEndlessProgressItem(null);
        }
        // Remove the progressItem if needed
        if (delay > 0 && (newItemsSize == 0 || !isEndlessScrollEnabled())) {
            log.v("onLoadMore     enqueued removing progressItem (%sms)", delay);
            mHandler.sendEmptyMessageDelayed(LOAD_MORE_COMPLETE, delay);
        } else {
            hideProgressItem();
        }
        // Reset the loading status
        endlessLoading = false;
        // Eventually notify noMoreLoad
        if (newItemsSize == 0 || !isEndlessScrollEnabled()) {
            noMoreLoad(newItemsSize);
        }
    }

    /**
     * Called when loading more should continue.
     */
    private void hideProgressItem() {
        int positionToNotify = getGlobalPositionOf(mProgressItem);
        if (positionToNotify >= 0) {
            log.v("onLoadMore     remove progressItem");
            if (mTopEndless) {
                removeScrollableHeader(mProgressItem);
            } else {
                removeScrollableFooter(mProgressItem);
            }
        }
    }

    /**
     * Called when no more items are loaded.
     */
    private void noMoreLoad(int newItemsSize) {
        log.i("noMoreLoad!");
        int positionToNotify = getGlobalPositionOf(mProgressItem);
        if (positionToNotify >= 0)
            notifyItemChanged(positionToNotify, Payload.NO_MORE_LOAD);
        if (mEndlessScrollListener != null) {
            mEndlessScrollListener.noMoreLoad(newItemsSize);
        }
    }

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

    /**
     * @return true if {@code collapseOnExpand} is enabled, false otherwise
     * @since 5.0.0-b8
     */
    public boolean isAutoCollapseOnExpand() {
        return collapseOnExpand;
    }

    /**
     * Automatically collapse all previous expanded parents before expand the new clicked parent.
     * <p>Default value is {@code false} (disabled).</p>
     * <b>Tip:</b> This parameter works in collaboration with {@link #setMinCollapsibleLevel(int)}.
     *
     * @param collapseOnExpand true to collapse others items, false to just expand the current
     * @return this Adapter, so the call can be chained
     * @see #setMinCollapsibleLevel(int)
     * @since 5.0.0-b1
     */
    public FlexibleAdapter<T> setAutoCollapseOnExpand(boolean collapseOnExpand) {
        log.i("Set autoCollapseOnExpand=%s", collapseOnExpand);
        this.collapseOnExpand = collapseOnExpand;
        return this;
    }

    /**
     * @return true if {@code collapseSubLevels} is enabled, false otherwise
     * @since 5.0.0-rc3
     */
    public boolean isRecursiveCollapse() {
        return collapseSubLevels;
    }

    /**
     * Automatically collapse all inner sub expandable when higher parent is collapsed.<br>
     * By keeping this parameter {@code false}, their expanded status remains {@code expanded=true}
     * so when the higher parent is expanded again, the sub expandables will appear again expanded.
     * <p>Default value is {@code false} (keep expanded status).</p>
     * <b>Tip:</b> This parameter works in collaboration with {@link #setMinCollapsibleLevel(int)}.
     *
     * @param collapseSubLevels true to allow inner sub expandable to collapse, false to keep the expanded status
     * @return this Adapter, so the call can be chained
     * @see #setMinCollapsibleLevel(int)
     * @since 5.0.0-rc3
     */
    public FlexibleAdapter<T> setRecursiveCollapse(boolean collapseSubLevels) {
        log.i("Set setAutoCollapseSubLevels=%s", collapseSubLevels);
        this.collapseSubLevels = collapseSubLevels;
        return this;
    }

    /**
     * @return true if {@code scrollOnExpand} is enabled, false otherwise
     * @since 5.0.0-b8
     */
    public boolean isAutoScrollOnExpand() {
        return scrollOnExpand;
    }

    /**
     * Automatically scroll the clicked expandable item to the first visible position.<br>
     * <p>Default value is {@code false} (disabled).</p>
     * <b>Note:</b> This works ONLY in combination with {@link SmoothScrollLinearLayoutManager}
     * or with {@link SmoothScrollGridLayoutManager}.
     *
     * @param scrollOnExpand true to enable automatic scroll, false to disable
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b1
     */
    public FlexibleAdapter<T> setAutoScrollOnExpand(boolean scrollOnExpand) {
        log.i("Set setAutoScrollOnExpand=%s", scrollOnExpand);
        this.scrollOnExpand = scrollOnExpand;
        return this;
    }

    /**
     * @param position the position of the item to check
     * @return true if the item implements {@link IExpandable} interface and its property has
     * {@code expanded = true}
     * @since 5.0.0-b1
     */
    public boolean isExpanded(@IntRange(from = 0) int position) {
        return isExpanded(getItem(position));
    }

    /**
     * Checks if the provided item is an {@link IExpandable} instance and is expanded.
     *
     * @param item the item to check
     * @return true if the item implements {@link IExpandable} interface and its property has
     * {@code expanded = true}
     * @since 5.0.0-b1
     */
    public boolean isExpanded(@Nullable T item) {
        return isExpandable(item) && ((IExpandable) item).isExpanded();
    }

    /**
     * Checks if the provided item is an {@link IExpandable} instance.
     *
     * @param item the item to check
     * @return true if the item implements {@link IExpandable} interface, false otherwise
     * @since 5.0.0-b1
     */
    public boolean isExpandable(@Nullable T item) {
        return item instanceof IExpandable;
    }

    /**
     * @return the level of the minimum collapsible level used in MultiLevel expandable
     * @since 5.0.0-b6
     */
    public int getMinCollapsibleLevel() {
        return mMinCollapsibleLevel;
    }

    /**
     * Sets the minimum level which all sub expandable items will be collapsed too.
     * <p>Default value is {@link #mMinCollapsibleLevel} (All levels including 0).</p>
     * <b>Tip:</b> This parameter works in collaboration with {@link #setRecursiveCollapse(boolean)}.
     *
     * @param minCollapsibleLevel the minimum level to auto-collapse sub expandable items
     * @return this Adapter, so the call can be chained
     * @see #setRecursiveCollapse(boolean)
     * @since 5.0.0-b6
     */
    public FlexibleAdapter<T> setMinCollapsibleLevel(int minCollapsibleLevel) {
        log.i("Set minCollapsibleLevel=%s", minCollapsibleLevel);
        this.mMinCollapsibleLevel = minCollapsibleLevel;
        return this;
    }

    /**
     * Utility method to check if the expandable item has sub items.
     *
     * @param expandable the {@link IExpandable} object
     * @return true if the expandable has subItems, false otherwise
     * @since 5.0.0-b1
     */
    public boolean hasSubItems(IExpandable expandable) {
        return expandable != null && expandable.getSubItems() != null &&
                expandable.getSubItems().size() > 0;
    }

    /**
     * Retrieves the parent of a child for the provided position.
     * <p>Only for a real child of an expanded parent.</p>
     *
     * @param position the position of the child item
     * @return the parent of this child item or null if item has no parent
     * @since 5.0.0-b1
     */
    @Nullable
    public IExpandable getExpandableOf(int position) {
        return getExpandableOf(getItem(position));
    }

    /**
     * Retrieves the parent of a child.
     * <p>Only for a real child of an expanded parent.</p>
     *
     * @param child the child item
     * @return the parent of this child item or null if item has no parent
     * @see #getExpandablePositionOf(IFlexible)
     * @see #getSubPositionOf(IFlexible)
     * @since 5.0.0-b1
     */
    @Nullable
    public IExpandable getExpandableOf(T child) {
        for (T parent : mItems) {
            if (isExpandable(parent)) {
                IExpandable expandable = (IExpandable) parent;
                if (expandable.isExpanded() && hasSubItems(expandable)) {
                    List<T> list = expandable.getSubItems();
                    for (T subItem : list) {
                        //Pick up only no-hidden items
                        if (!subItem.isHidden() && subItem.equals(child))
                            return expandable;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the parent position of a child.
     * <p>Only for a real child of an expanded parent.</p>
     *
     * @param child the child item
     * @return the parent position of this child item or -1 if not found
     * @see #getExpandableOf(IFlexible)
     * @see #getSubPositionOf(IFlexible)
     * @since 5.0.0-b1
     */
    public int getExpandablePositionOf(@NonNull T child) {
        return getGlobalPositionOf(getExpandableOf(child));
    }

    /**
     * Retrieves the sub position of any sub item in the section where it lays. First position
     * corresponds to {@code 0}.
     * <p>Works for items under header and under expandable too.</p>
     *
     * @param child any sub item of any section
     * @return the position in the parent or -1 if the child is a parent/header itself or not found
     * @see #getSameTypePositionOf(IFlexible)
     * @see #getExpandableOf(IFlexible)
     * @see #getExpandablePositionOf(IFlexible)
     * @since 5.0.0-b1
     */
    public int getSubPositionOf(@NonNull T child) {
        // If a sectionable has header, we take the global position of both
        // and calculate the difference. Expandable will have precedence.
        if (child instanceof ISectionable && hasHeader(child)) {
            IHeader header = getHeaderOf(child);
            if (!(header instanceof IExpandable)) {
                return getGlobalPositionOf(child) - getGlobalPositionOf(header) - 1;
            }
        }
        return getSiblingsOf(child).indexOf(child);
    }

    /**
     * Provides the full sub list where the child currently lays.
     *
     * @param child the child item
     * @return the list of the child element, or an empty list if the child item has no parent
     * @see #getExpandableOf(IFlexible)
     * @see #getExpandablePositionOf(IFlexible)
     * @see #getSubPositionOf(IFlexible)
     * @see #getExpandedItems()
     * @since 5.0.0-b1
     */
    @NonNull
    public List<T> getSiblingsOf(@NonNull T child) {
        IExpandable expandable = getExpandableOf(child);
        return expandable != null ? expandable.getSubItems() : new ArrayList<>();
    }

    /**
     * Provides a list of all expandable items that are currently expanded.
     *
     * @return a list with all expanded items
     * @see #getSiblingsOf(IFlexible)
     * @see #getExpandedPositions()
     * @since 5.0.0-b1
     */
    @NonNull
    public List<T> getExpandedItems() {
        List<T> expandedItems = new ArrayList<>();
        for (T item : mItems) {
            if (isExpanded(item))
                expandedItems.add(item);
        }
        return expandedItems;
    }

    /**
     * Provides a list of all expandable positions that are currently expanded.
     *
     * @return a list with the global positions of all expanded items
     * @see #getSiblingsOf(IFlexible)
     * @see #getExpandedItems()
     * @since 5.0.0-b1
     */
    @NonNull
    public List<Integer> getExpandedPositions() {
        List<Integer> expandedPositions = new ArrayList<>();
        int startPosition = Math.max(0, mScrollableHeaders.size() - 1);
        int endPosition = getItemCount() - mScrollableFooters.size() - 1;
        for (int i = startPosition; i < endPosition; i++) {
            if (isExpanded(getItem(i))) expandedPositions.add(i);
        }
        return expandedPositions;
    }

    /**
     * Recursively determine the total number of items between a range of expandable subItems.
     *
     * @return item count, including recursive expansions, to the first level sub-position item
     * @since 5.0.0-rc3
     */
    private int getRecursiveSubItemCount(@NonNull IExpandable parent, int subPosition) {
        int count = 0;
        // Get the subItems
        List<T> subItems = parent.getSubItems();
        // Iterate through subItems
        for (int index = 0; index < subPosition; index++) {
            T subItem = subItems.get(index);
            // Check whether item is also expandable and expanded
            if (isExpanded(subItem)) {
                IExpandable subExpandable = (IExpandable) subItem;
                int size = subExpandable.getSubItems() != null ? subExpandable.getSubItems().size() : 0;
                count += getRecursiveSubItemCount(subExpandable, size);
            }
            count++;
        }
        return count;
    }

    /**
     * Expands an item that is {@code IExpandable} type, not yet expanded <u>and</u> if has subItems.
     * <p>If configured, automatic smooth scroll will be performed when necessary.</p>
     * Parent won't be notified.
     *
     * @param position the position of the item to expand
     * @return the number of subItems expanded
     * @see #expand(int, boolean)
     * @see #expand(IFlexible)
     * @see #expand(IFlexible, boolean)
     * @see #expandAll()
     * @since 5.0.0-b1
     */
    public int expand(@IntRange(from = 0) int position) {
        return expand(position, false);
    }

    /**
     * Expands an item that is {@code IExpandable} type, not yet expanded and if has subItems.
     * <p>If configured, automatic smooth scroll will be performed when necessary.</p>
     *
     * @param position     the position of the item to expand
     * @param notifyParent true to notify the parent with {@link Payload#EXPANDED}
     * @return the number of subItems expanded
     * @see #expand(int)
     * @see #expand(IFlexible)
     * @see #expand(IFlexible, boolean)
     * @see #expandAll()
     * @since 5.0.0-b1
     */
    public int expand(@IntRange(from = 0) int position, boolean notifyParent) {
        return expand(position, false, false, notifyParent);
    }

    /**
     * Convenience method to expand a single item. Parent will be notified.
     * <p>Expands an item that is Expandable, not yet expanded, that has subItems and
     * no child is selected.</p>
     * If configured, automatic smooth scroll will be performed.
     *
     * @param item the item to expand, must be an Expandable and present in the list
     * @return the number of subItems expanded
     * @see #expand(int)
     * @see #expand(int, boolean)
     * @see #expand(IFlexible, boolean)
     * @see #expandAll()
     * @since 5.0.0-b6
     */
    public int expand(T item) {
        return expand(getGlobalPositionOf(item), false, false, true);
    }

    /**
     * Convenience method to initially expand a single item. Parent won't be notified.
     * <p><b>Note:</b> Must be used in combination with adding new items that require to be
     * initially expanded.</p>
     * <b>WARNING!</b>
     * <br>Expanded status is ignored if {@code init = true}: it will always attempt to expand
     * the item: If subItems are already visible <u>and</u> the new item has status expanded, the
     * subItems will appear duplicated(!) and the automatic smooth scroll will be skipped!
     *
     * @param item the item to expand, must be an Expandable and present in the list
     * @param init true to initially expand item
     * @return the number of subItems expanded
     * @see #expand(int)
     * @see #expand(int, boolean)
     * @see #expand(IFlexible)
     * @see #expandAll()
     * @since 5.0.0-b7
     */
    public int expand(T item, boolean init) {
        return expand(getGlobalPositionOf(item), false, init, false);
    }

    /**
     * Convenience method to initially expand a single item.
     * <p><b>Note:</b> Must be used in combination with adding new items that require to be
     * initially expanded.</p>
     * <b>WARNING!</b>
     * <br>Expanded status is ignored if {@code init = true}: it will always attempt to expand
     * the item: If subItems are already visible <u>and</u> the new item has status expanded, the
     * subItems will appear duplicated(!) and the automatic smooth scroll will be skipped!
     *
     * @param position     the position of the item to expand
     * @param init         true to initially expand item
     * @param notifyParent true to notify the parent with {@link Payload#EXPANDED}
     * @return the number of subItems expanded
     * @since 5.0.0-rc2
     */
    private int expand(int position, boolean expandAll, boolean init, boolean notifyParent) {
        T item = getItem(position);
        if (!isExpandable(item)) return 0;

        IExpandable expandable = (IExpandable) item;
        if (!hasSubItems(expandable)) {
            expandable.setExpanded(false);//clear the expanded flag
            log.w("No subItems to Expand on position %s expanded %s", position, expandable.isExpanded());
            return 0;
        }
        if (!init && !expandAll) {
            log.v("Request to Expand on position=%s expanded=%s anyParentSelected=%s",
                    position, expandable.isExpanded(), parentSelected);
        }
        int subItemsCount = 0;
        if (init || !expandable.isExpanded() &&
                (!parentSelected || expandable.getExpansionLevel() <= mSelectedLevel)) {

            // Collapse others expandable if configured so Skip when expanding all is requested
            // Fetch again the new position after collapsing all!!
            if (collapseOnExpand && !expandAll && collapseAll(mMinCollapsibleLevel) > 0) {
                position = getGlobalPositionOf(item);
            }

            // Every time an expansion is requested, subItems must be taken from the
            // original Object and without the subItems marked hidden (removed)
            List<T> subItems = getExpandableList(expandable, true);
            mItems.addAll(position + 1, subItems);
            subItemsCount = subItems.size();
            // Save expanded state
            expandable.setExpanded(true);

            // Automatically smooth scroll the current expandable item to show as much
            // children as possible
            if (!init && scrollOnExpand && !expandAll) {
                autoScrollWithDelay(position, subItemsCount, 150L);
            }

            // Expand!
            if (notifyParent) notifyItemChanged(position, Payload.EXPANDED);
            notifyItemRangeInserted(position + 1, subItemsCount);

            // Show also the headers of the subItems
            if (!init && headersShown) {
                int count = 0;
                for (T subItem : subItems) {
                    if (showHeaderOf(position + (++count), subItem, false)) count++;
                }
            }

            // Expandable as a Scrollable Header/Footer
            if (!expandSHF(mScrollableHeaders, expandable))
                expandSHF(mScrollableFooters, expandable);

            log.v("%s %s subItems on position=%s", (init ? "Initially expanded" : "Expanded"), subItemsCount, position);
        }
        return subItemsCount;
    }

    private boolean expandSHF(List<T> scrollables, IExpandable expandable) {
        int index = scrollables.indexOf(expandable);
        if (index >= 0) {
            if (index + 1 < scrollables.size()) {
                return scrollables.addAll(index + 1, expandable.getSubItems());
            } else {
                return scrollables.addAll(expandable.getSubItems());
            }
        }
        return false;
    }

    /**
     * Expands all {@code IExpandable} items with minimum of level {@link #mMinCollapsibleLevel}.
     *
     * @return the number of parent successfully expanded
     * @see #expandAll(int)
     * @see #setMinCollapsibleLevel(int)
     * @since 5.0.0-b1
     */
    public int expandAll() {
        return expandAll(mMinCollapsibleLevel);
    }

    /**
     * Expands all {@code IExpandable} items with at least the specified level.
     * <p>Parents will be notified.</p>
     *
     * @param level the minimum level to expand the sub expandable items
     * @return the number of parent successfully expanded
     * @see #expandAll()
     * @see #setMinCollapsibleLevel(int)
     * @since 5.0.0-b6
     */
    public int expandAll(int level) {
        int expanded = 0;
        // More efficient if we expand from First expandable position
        int startPosition = Math.max(0, mScrollableHeaders.size() - 1);
        for (int i = startPosition; i < (getItemCount() - mScrollableFooters.size()); i++) {
            T item = getItem(i);
            if (isExpandable(item)) {
                IExpandable expandable = (IExpandable) item;
                if (expandable.getExpansionLevel() <= level && expand(i, true, false, true) > 0) {
                    i += expandable.getSubItems().size();
                    expanded++;
                }
            }
        }
        return expanded;
    }

    /**
     * Collapses an {@code IExpandable} item that is already expanded <u>and</u> if no subItem
     * is selected.
     * <p>Multilevel option behaviours:
     * <ul>
     * <li>{@code IExpandable} subItems, that are expanded, can be recursively collapsed,
     * see {@link #setRecursiveCollapse(boolean)}.</li>
     * <li>You can set the minimum level to auto-collapse siblings,
     * see {@link #setMinCollapsibleLevel(int)}.</li>
     * </ul></p>
     * Parent won't be notified.
     *
     * @param position the position of the item to collapse
     * @return the number of subItems collapsed
     * @see #collapseAll()
     * @since 5.0.0-b1
     */
    public int collapse(@IntRange(from = 0) int position) {
        return collapse(position, false);
    }

    /**
     * Same behaviors as {@link #collapse(int)} with possibility to notify/update the parent.
     *
     * @param position     the position of the item to collapse
     * @param notifyParent notify the parent with {@link Payload#COLLAPSED}
     * @return the number of subItems collapsed
     * @see #collapseAll()
     * @since 5.0.0-b1
     */
    public int collapse(@IntRange(from = 0) int position, boolean notifyParent) {
        T item = getItem(position);
        if (!isExpandable(item)) return 0;

        IExpandable expandable = (IExpandable) item;
        // Take the current subList (will improve the performance when collapseAll)
        List<T> subItems = getExpandableList(expandable, true);
        int subItemsCount = subItems.size();

        log.v("Request to Collapse on position=%s expanded=%s hasSubItemsSelected=%s",
                position, expandable.isExpanded(), hasSubItemsSelected(position, subItems));

        if (expandable.isExpanded() && subItemsCount > 0 &&
                (!hasSubItemsSelected(position, subItems) || getPendingRemovedItem(item) != null)) {

            // Recursive collapse of all sub expandable
            if (collapseSubLevels) {
                recursiveCollapse(position + 1, subItems, expandable.getExpansionLevel());
            }
            mItems.removeAll(subItems);
            subItemsCount = subItems.size();
            // Save expanded state
            expandable.setExpanded(false);

            // Collapse!
            if (notifyParent) notifyItemChanged(position, Payload.COLLAPSED);
            notifyItemRangeRemoved(position + 1, subItemsCount);

            // Hide also the headers of the subItems
            if (headersShown && !isHeader(item)) {
                for (T subItem : subItems) {
                    hideHeaderOf(subItem);
                }
            }

            // Expandable as a Scrollable Header/Footer
            if (!collapseSHF(mScrollableHeaders, expandable))
                collapseSHF(mScrollableFooters, expandable);

            log.v("Collapsed %s subItems on position %s", subItemsCount, position);
        }
        return subItemsCount;
    }

    private boolean collapseSHF(List<T> scrollables, IExpandable expandable) {
        return scrollables.contains(expandable) && scrollables.removeAll(expandable.getSubItems());
    }

    private int recursiveCollapse(int startPosition, List<T> subItems, int level) {
        int collapsed = 0;
        for (int i = subItems.size() - 1; i >= 0; i--) {
            T subItem = subItems.get(i);
            if (isExpanded(subItem)) {
                IExpandable expandable = (IExpandable) subItem;
                if (expandable.getExpansionLevel() >= level &&
                        collapse(startPosition + i, true) > 0) {
                    collapsed++;
                }
            }
        }
        return collapsed;
    }

    /**
     * Collapses all expandable items with the minimum level of {@link #mMinCollapsibleLevel}.
     *
     * @return the number of parent successfully collapsed
     * @see #collapse(int, boolean)
     * @see #collapseAll(int)
     * @see #setMinCollapsibleLevel(int)
     * @since 5.0.0-b1
     */
    public int collapseAll() {
        return collapseAll(mMinCollapsibleLevel);
    }

    /**
     * Collapses all expandable items with the level equals-higher than the specified level.
     * <p>Parents will be notified.</p>
     *
     * @param level the level to start collapse sub expandable items
     * @return the number of parent successfully collapsed
     * @see #collapseAll()
     * @since 5.0.0-b6
     */
    public int collapseAll(int level) {
        return recursiveCollapse(0, mItems, level);
    }

	/*----------------*/
	/* UPDATE METHODS */
	/*----------------*/

    /**
     * Updates/Rebounds the itemView corresponding to the current position of the
     * provided item with the new content.
     *
     * @param item the item with the new content
     * @see #updateItem(IFlexible, Object)
     * @see #updateItem(int, IFlexible, Object)
     * @since 2.1.0
     */
    public void updateItem(@NonNull T item) {
        updateItem(item, null);
    }

    /**
     * Updates/Rebounds the itemView corresponding to the current position of the
     * provided item with the new content.
     *
     * @param item    the item with the new content
     * @param payload any non-null user object to notify the current item (the payload will be
     *                therefore passed to the bind method of the item ViewHolder to optimize the
     *                content to update); pass null to rebind all fields of this item.
     * @see #updateItem(IFlexible)
     * @see #updateItem(int, IFlexible, Object)
     * @since 2.1.0
     */
    public void updateItem(@NonNull T item, @Nullable Object payload) {
        updateItem(getGlobalPositionOf(item), item, payload);
    }

    /**
     * Updates/Rebounds the itemView corresponding to the current position of the
     * provided item with the new content. Use {@link #updateItem(IFlexible, Object)} if the
     * new content should be bound on the same position.
     *
     * @param position the position where the new content should be updated and rebound
     * @param item     the item with the new content
     * @param payload  any non-null user object to notify the current item (the payload will be
     *                 therefore passed to the bind method of the item ViewHolder to optimize the
     *                 content to update); pass null to rebind all fields of this item.
     * @see #updateItem(IFlexible)
     * @see #updateItem(IFlexible, Object)
     * @since 5.0.0-b1
     */
    public void updateItem(@IntRange(from = 0) int position, @NonNull T item, @Nullable Object payload) {
        if (item == null) {
            log.e("updateItem No Item to update!");
            return;
        }
        int itemCount = getItemCount();
        if (position < 0 || position >= itemCount) {
            log.e("Cannot updateItem on position out of OutOfBounds!");
            return;
        }
        mItems.set(position, item);
        log.d("updateItem notifyItemChanged on position " + position);
        notifyItemChanged(position, payload);
    }

	/*----------------*/
	/* ADDING METHODS */
	/*----------------*/

    /**
     * Inserts the given item at desired position or Add item at last position with a delay
     * and auto-scroll to the position.
     * <p>Scrolling animation is automatically preserved, meaning that, notification for animation
     * is ignored.</p>
     * Useful at startup, when there's an item to add after Adapter Animations is completed.
     *
     * @param position         position of the item to add
     * @param item             the item to add
     * @param delay            a non-negative delay
     * @param scrollToPosition true if RecyclerView should scroll after item has been added,
     *                         false otherwise
     * @see #addItem(int, IFlexible)
     * @see #addItems(int, List)
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)
     * @see #removeItemWithDelay(IFlexible, long, boolean)
     * @since 5.0.0-b1
     */
    public void addItemWithDelay(@IntRange(from = 0) final int position, @NonNull final T item,
                                 @IntRange(from = 0) long delay, final boolean scrollToPosition) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (addItem(position, item) && scrollToPosition) performScroll(position);
            }
        }, delay);
    }

    /**
     * Simply append the provided item to the end of the list.
     * <p>Convenience method of {@link #addItem(int, IFlexible)} with
     * {@code position = getMainItemCount()}.</p>
     *
     * @param item the item to add
     * @return true if the internal list was successfully modified, false otherwise
     */
    public boolean addItem(@NonNull T item) {
        return addItem(getItemCount(), item);
    }

    /**
     * Inserts the given item at the specified position or Adds the item to the end of the list
     * (no matters if the new position is out of bounds!).
     *
     * @param position position inside the list, if negative, items will be added to the end
     * @param item     the item to add
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addItem(IFlexible)
     * @see #addItems(int, List)
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)
     * @see #addItemWithDelay(int, IFlexible, long, boolean)
     * @since 1.0.0
     */
    public boolean addItem(@IntRange(from = 0) int position, @NonNull T item) {
        if (item == null) {
            log.e("addItem No item to add!");
            return false;
        }
        log.v("addItem delegates addition to addItems!");
        return addItems(position, Collections.singletonList(item));
    }

    /**
     * Inserts a set of items at specified position or Adds the items to the end of the list
     * (no matters if the new position is out of bounds!).
     * <p><b>Note:</b>
     * <br>- When all headers are shown, if exists, the header of this item will be shown as well,
     * unless it's already shown, so it won't be shown twice.
     * <br>- Items will be always added between any scrollable header and footers.</p>
     *
     * @param position position inside the list, if negative, items will be added to the end
     * @param items    the set of items to add
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addItem(IFlexible)
     * @see #addItem(int, IFlexible)
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)
     * @since 5.0.0-b1
     */
    public boolean addItems(@IntRange(from = 0) int position, @NonNull List<T> items) {
        if (items == null || items.isEmpty()) {
            log.e("addItems No items to add!");
            return false;
        }
        int initialCount = getMainItemCount();//Count only main items!
        if (position < 0) {
            log.w("addItems Position is negative! adding items to the end");
            position = initialCount;
        }
        // Insert the item properly
        performInsert(position, items, true);

        // Show the headers of these items if all headers are already visible
        if (headersShown && !recursive) {
            recursive = true;
            for (T item : items)
                showHeaderOf(getGlobalPositionOf(item), item, false);//We have to find the correct position!
            recursive = false;
        }
        // Call listener to update EmptyView
        if (!recursive && mUpdateListener != null && !multiRange && initialCount == 0 && getItemCount() > 0)
            mUpdateListener.onUpdateEmptyView(getMainItemCount());
        return true;
    }

    private void performInsert(int position, List<T> items, boolean notify) {
        int itemCount = getItemCount();
        if (position < itemCount) {
            mItems.addAll(position, items);
        } else {
            mItems.addAll(items);
            position = itemCount;
        }
        // Notify range addition
        if (notify) {
            log.d("addItems on position=%s itemCount=%s", position, items.size());
            notifyItemRangeInserted(position, items.size());
        }
    }

    /**
     * Convenience method of {@link #addSubItem(int, int, IFlexible, boolean, Object)}.
     * <br>In this case parent item will never be expanded if it is collapsed.
     *
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)
     * @since 5.0.0-b1
     */
    public boolean addSubItem(@IntRange(from = 0) int parentPosition,
                              @IntRange(from = 0) int subPosition, @NonNull T item) {
        return this.addSubItem(parentPosition, subPosition, item, false, Payload.CHANGE);
    }

    /**
     * Convenience method of {@link #addSubItems(int, int, IExpandable, List, boolean, Object)}.
     * <br>Optionally you can pass any payload to notify the parent about the change and optimize
     * the view binding.
     *
     * @param parentPosition position of the expandable item that shall contain the subItem
     * @param subPosition    the position of the subItem in the expandable list
     * @param item           the subItem to add in the expandable list
     * @param expandParent   true to initially expand the parent (if needed) and after to add
     *                       the subItem, false to simply add the subItem to the parent
     * @param payload        any non-null user object to notify the parent (the payload will be
     *                       therefore passed to the bind method of the parent ViewHolder),
     *                       pass null to <u>not</u> notify the parent
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)
     * @since 5.0.0-b1
     */
    public boolean addSubItem(@IntRange(from = 0) int parentPosition,
                              @IntRange(from = 0) int subPosition,
                              @NonNull T item, boolean expandParent, @Nullable Object payload) {
        if (item == null) {
            log.e("addSubItem No items to add!");
            return false;
        }
        // Build a new list with 1 item to chain the methods of addSubItems
        return addSubItems(parentPosition, subPosition, Collections.singletonList(item), expandParent, payload);
    }

    /**
     * Convenience method of {@link #addSubItems(int, int, List, boolean, Object)}.
     * <br>In this case parent item will never be expanded if it is collapsed.
     *
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)
     * @since 5.0.0-rc3
     */
    public boolean addSubItems(@IntRange(from = 0) int parentPosition,
                               @IntRange(from = 0) int subPosition, @NonNull List<T> items) {
        return this.addSubItems(parentPosition, subPosition, items, false, Payload.CHANGE);
    }

    /**
     * Convenience method of {@link #addSubItems(int, int, IExpandable, List, boolean, Object)}.
     * <br>Optionally you can pass any payload to notify the parent about the change and optimize
     * the view binding.
     *
     * @param parentPosition position of the expandable item that shall contain the subItems
     * @param subPosition    the start position in the parent where the new items shall be inserted
     * @param items          the list of the subItems to add
     * @param expandParent   true to initially expand the parent (if needed) and after to add
     *                       the subItems, false to simply add the subItems to the parent
     * @param payload        any non-null user object to notify the parent (the payload will be
     *                       therefore passed to the bind method of the parent ViewHolder),
     *                       pass null to <u>not</u> notify the parent
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addSubItems(int, int, IExpandable, List, boolean, Object)
     * @since 5.0.0-b1
     */
    public boolean addSubItems(@IntRange(from = 0) int parentPosition,
                               @IntRange(from = 0) int subPosition,
                               @NonNull List<T> items, boolean expandParent, @Nullable Object payload) {
        T parent = getItem(parentPosition);
        if (isExpandable(parent)) {
            IExpandable expandable = (IExpandable) parent;
            return addSubItems(parentPosition, subPosition, expandable, items, expandParent, payload);
        }
        log.e("addSubItems Provided parentPosition doesn't belong to an Expandable item!");
        return false;
    }

    /**
     * Adds new subItems on the specified parent item, to the internal list.
     * <p><b>In order to add subItems</b>, the following condition must be satisfied:
     * <br>- The item resulting from the parent position is actually an {@link IExpandable}.</p>
     * Optionally, the parent can be expanded and subItems displayed.
     * <br>Optionally, you can pass any payload to notify the parent about the change and
     * optimize the view binding.
     *
     * @param parentPosition position of the expandable item that shall contain the subItems
     * @param subPosition    the start position in the parent where the new items shall be inserted
     * @param parent         the expandable item which shall contain the new subItem
     * @param subItems       the list of the subItems to add
     * @param expandParent   true to initially expand the parent (if needed) and after to add
     *                       the subItems, false to simply add the subItems to the parent
     * @param payload        any non-null user object to notify the parent (the payload will be
     *                       therefore passed to the bind method of the parent ViewHolder),
     *                       pass null to <u>not</u> notify the parent
     * @return true if the internal list was successfully modified, false otherwise
     * @see #addItems(int, List)
     * @since 5.0.0-b1
     */
    private boolean addSubItems(@IntRange(from = 0) int parentPosition,
                                @IntRange(from = 0) int subPosition,
                                @NonNull IExpandable parent,
                                @NonNull List<T> subItems, boolean expandParent, @Nullable Object payload) {
        boolean added = false;
        // Expand parent if requested and not already expanded
        if (expandParent && !parent.isExpanded()) {
            expand(parentPosition);
        }
        // Notify the adapter of the new addition to display it and animate it.
        // If parent is collapsed there's no need to add sub items.
        if (parent.isExpanded()) {
            added = addItems(parentPosition + 1 + getRecursiveSubItemCount(parent, subPosition), subItems);
        }
        // Notify the parent about the change if requested
        if (payload != null) notifyItemChanged(parentPosition, payload);
        return added;
    }

    /**
     * Adds and shows an empty section to the top (position = 0).
     *
     * @return the calculated position for the new item
     * @see #addSection(IHeader, Comparator)
     * @since 5.0.0-b6
     */
    public int addSection(@NonNull IHeader header) {
        return addSection(header, null);
    }

    /**
     * Adds and shows an empty section. The new section is a {@link IHeader} item and the
     * position is calculated after sorting the data set.
     * <p>- To add Sections to the <b>top</b>, set null the Comparator object or simply call
     * {@link #addSection(IHeader)};
     * <br>- To add Sections to the <b>bottom</b> or in the <b>middle</b>, implement a Comparator
     * object able to support <u>all</u> the item types this Adapter is displaying or a
     * ClassCastException will be raised.</p>
     *
     * @param header     the section header item to add
     * @param comparator the criteria to sort the Data Set used to extract the correct position
     *                   of the new header
     * @return the calculated position for the new item
     * @since 5.0.0-b7
     */
    public int addSection(@NonNull IHeader header, @Nullable Comparator<IFlexible> comparator) {
        int position = calculatePositionFor(header, comparator);
        addItem(position, (T) header);
        return position;
    }

    /**
     * Adds a new item in a section when the relative position is <b>unknown</b>.
     * <p>The header can be a {@code IExpandable} type or {@code IHeader} type.</p>
     * The Comparator object must support <u>all</u> the item types this Adapter is displaying or
     * a ClassCastException will be raised.
     *
     * @param sectionable the item to add
     * @param header      the section receiving the new item
     * @param comparator  the criteria to sort the sectionItems used to extract the correct position
     *                    of the new item in the section
     * @return the calculated final position for the new item
     * @see #addItemToSection(ISectionable, IHeader, int)
     * @since 5.0.0-b6
     */
    public int addItemToSection(@NonNull ISectionable sectionable, @Nullable IHeader header,
                                @Nullable Comparator<IFlexible> comparator) {
        int index;
        if (header != null && !header.isHidden()) {
            List<ISectionable> sectionItems = getSectionItems(header);
            sectionItems.add(sectionable);
            //Sort the list for new position
            Collections.sort(sectionItems, comparator);
            //Get the new position
            index = sectionItems.indexOf(sectionable);
        } else {
            index = calculatePositionFor(sectionable, comparator);
        }
        return addItemToSection(sectionable, header, index);
    }

    /**
     * Adds a new item in a section when the relative position is <b>known</b>.
     * <p>The header can be a {@code IExpandable} type or {@code IHeader} type.</p>
     *
     * @param sectionable the item to add
     * @param header      the section receiving the new item
     * @param index       the known relative position where to add the new item into the section
     * @return the calculated final position for the new item
     * @see #addItemToSection(ISectionable, IHeader, Comparator)
     * @since 5.0.0-b6
     */
    public int addItemToSection(@NonNull ISectionable sectionable, @Nullable IHeader header,
                                @IntRange(from = 0) int index) {
        log.d("addItemToSection relativePosition=%s", index);
        int headerPosition = getGlobalPositionOf(header);
        if (index >= 0) {
            sectionable.setHeader(header);
            if (headerPosition >= 0 && isExpandable((T) header))
                addSubItem(headerPosition, index, (T) sectionable, false, Payload.ADD_SUB_ITEM);
            else
                addItem(headerPosition + 1 + index, (T) sectionable);
        }
        return getGlobalPositionOf(sectionable);
    }

	/*----------------------*/
	/* DELETE ITEMS METHODS */
	/*----------------------*/

    /**
     * This method clears <b>everything</b>: main items, Scrollable Headers and Footers and
     * everything else is displayed.
     *
     * @see #clearAllBut(Integer...)
     * @see #removeRange(int, int)
     * @see #removeItemsOfType(Integer...)
     * @since 5.0.0-rc1
     */
    public void clear() {
        log.d("clearAll views");
        removeAllScrollableHeaders();
        removeAllScrollableFooters();
        removeRange(0, getItemCount(), null);
    }

    /**
     * Clears the Adapter list retaining Scrollable Headers and Footers and all items of the
     * type provided as parameter.
     * <p><b>Tip:</b>- This method is opposite of {@link #removeItemsOfType(Integer...)}.
     *
     * @param viewTypes the viewTypes to retain
     * @see #clear()
     * @see #removeItems(List)
     * @see #removeItemsOfType(Integer...)
     * @since 5.0.0-rc1
     */
    public void clearAllBut(Integer... viewTypes) {
        List<Integer> viewTypeList = Arrays.asList(viewTypes);
        log.d("clearAll retaining views %s", viewTypeList);
        List<Integer> positionsToRemove = new ArrayList<>();
        int startPosition = Math.max(0, mScrollableHeaders.size());
        int endPosition = getItemCount() - mScrollableFooters.size();
        for (int i = startPosition; i < endPosition; i++) {
            if (!viewTypeList.contains(getItemViewType(i)))
                positionsToRemove.add(i);
        }
        // Remove items by ranges
        removeItems(positionsToRemove);
    }

    /**
     * Removes the given item after the given delay.
     * <p>Scrolling animation is automatically preserved, meaning that notification for animation
     * is ignored.</p>
     *
     * @param item      the item to add
     * @param delay     a non-negative delay
     * @param permanent true to permanently delete the item (no undo), false otherwise
     * @see #removeItem(int)
     * @see #removeItems(List)
     * @see #removeItemsOfType(Integer...)
     * @see #removeRange(int, int)
     * @see #removeAllSelectedItems()
     * @see #addItemWithDelay(int, IFlexible, long, boolean)
     * @since 5.0.0-b7
     */
    public void removeItemWithDelay(@NonNull final T item, @IntRange(from = 0) long delay,
                                    final boolean permanent) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performRemove(item, permanent);
            }
        }, delay);
    }

    private void performRemove(T item, boolean permanent) {
        boolean tempPermanent = permanentDelete;
        if (permanent) permanentDelete = true;
        removeItem(getGlobalPositionOf(item));
        permanentDelete = tempPermanent;
    }

    /**
     * Convenience method of {@link #removeItem(int, Object)} providing {@link Payload#CHANGE}
     * as payload for the parent item.
     *
     * @param position the position of item to remove
     * @see #removeItems(List)
     * @see #removeItemsOfType(Integer...)
     * @see #removeRange(int, int)
     * @see #removeAllSelectedItems()
     * @see #removeItemWithDelay(IFlexible, long, boolean)
     * @see #removeItem(int, Object)
     * @since 1.0.0
     */
    public void removeItem(@IntRange(from = 0) int position) {
        this.removeItem(position, Payload.CHANGE);
    }

    /**
     * Removes an item from the internal list and notify the change.
     * <p>The item is retained for an eventual Undo.</p>
     * This method delegates the removal to removeRange.
     *
     * @param position The position of item to remove
     * @param payload  any non-null user object to notify the parent (the payload will be
     *                 therefore passed to the bind method of the parent ViewHolder),
     *                 pass null to <u>not</u> notify the parent
     * @see #removeItems(List, Object)
     * @see #removeRange(int, int, Object)
     * @see #removeAllSelectedItems(Object)
     * @see #removeItem(int)
     * @since 5.0.0-b1
     */
    public void removeItem(@IntRange(from = 0) int position, @Nullable Object payload) {
        // Request to collapse after the notification of remove range
        collapse(position);
        log.v("removeItem delegates removal to removeRange");
        removeRange(position, 1, payload);
    }

    /**
     * Convenience method of {@link #removeItems(List, Object)} providing the default
     * {@link Payload#CHANGE} for the parent items.
     *
     * @see #removeItem(int)
     * @see #removeItemsOfType(Integer...)
     * @see #removeRange(int, int)
     * @see #removeAllSelectedItems()
     * @see #removeItems(List, Object)
     * @since 1.0.0
     */
    public void removeItems(@NonNull List<Integer> selectedPositions) {
        this.removeItems(selectedPositions, Payload.REM_SUB_ITEM);
    }

    /**
     * Removes items by <b>ranges</b> and notify the change.
     * <p>Every item is retained for an eventual Undo.</p>
     * Optionally you can pass any payload to notify the parent items about the change to
     * optimize the view binding.
     * <p>This method delegates the removal to removeRange.</p>
     *
     * @param selectedPositions list with item positions to remove
     * @param payload           any non-null user object to notify the parent (the payload will be
     *                          therefore passed to the bind method of the parent ViewHolder),
     *                          pass null to <u>not</u> notify the parent
     * @see #removeItem(int, Object)
     * @see #removeRange(int, int, Object)
     * @see #removeAllSelectedItems(Object)
     * @see #removeItems(List)
     * @since 5.0.0-b1
     */
    public void removeItems(@Nullable List<Integer> selectedPositions, @Nullable Object payload) {
        log.v("removeItems selectedPositions=%s payload=%s", selectedPositions, payload);
        // Check if list is empty
        if (selectedPositions == null || selectedPositions.isEmpty()) return;
        // Reverse-sort the list, start from last position for efficiency
        Collections.sort(selectedPositions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });
        log.v("removeItems after reverse sort selectedPositions=%s", selectedPositions);
        // Split the list in ranges
        int positionStart = 0, itemCount = 0;
        int lastPosition = selectedPositions.get(0);
        multiRange = true;
        for (Integer position : selectedPositions) {//10 9 8 //5 4 //1
            if (lastPosition - itemCount == position) {//10-0==10  10-1==9  10-2==8  10-3==5 NO  //5-1=4  5-2==1 NO
                itemCount++;             // 1  2  3  //2
                positionStart = position;//10  9  8  //4
            } else {
                // Remove range
                if (itemCount > 0)
                    removeRange(positionStart, itemCount, payload);//8,3  //4,2
                positionStart = lastPosition = position;//5  //1
                itemCount = 1;
            }
            // Request to collapse after the notification of remove range
            collapse(position);
        }
        multiRange = false;
        // Remove last range
        if (itemCount > 0) {
            removeRange(positionStart, itemCount, payload);//1,1
        }
    }

    /**
     * Selectively removes all items of the type provided as parameter.
     * <p><b>Tips:</b>
     * <br>- This method is opposite of {@link #clearAllBut(Integer...)}.
     * <br>- View types of Scrollable Headers and Footers are ignored!</p>
     *
     * @param viewTypes the viewTypes to remove
     * @see #clear()
     * @see #clearAllBut(Integer...)
     * @see #removeItem(int, Object)
     * @see #removeItems(List)
     * @see #removeAllSelectedItems()
     * @since 5.0.0-b5
     */
    public void removeItemsOfType(Integer... viewTypes) {
        List<Integer> viewTypeList = Arrays.asList(viewTypes);
        List<Integer> itemsToRemove = new ArrayList<>();
        int startPosition = Math.max(0, mScrollableHeaders.size() - 1);
        int endPosition = getItemCount() - mScrollableFooters.size() - 1;
        for (int i = endPosition; i >= startPosition; i--) {
            if (viewTypeList.contains(getItemViewType(i)))
                itemsToRemove.add(i);
        }
        this.removeItems(itemsToRemove);
    }

    /**
     * Same as {@link #removeRange(int, int, Object)}, but in this case the parent will not be
     * notified about the change, if children are removed.
     *
     * @see #clear()
     * @see #clearAllBut(Integer...)
     * @see #removeItem(int, Object)
     * @see #removeItems(List)
     * @see #removeItemsOfType(Integer...)
     * @see #removeAllSelectedItems()
     * @see #removeRange(int, int, Object)
     * @since 5.0.0-b1
     */
    public void removeRange(@IntRange(from = 0) int positionStart,
                            @IntRange(from = 0) int itemCount) {
        this.removeRange(positionStart, itemCount, Payload.REM_SUB_ITEM);
    }

    /**
     * Removes a list of consecutive items and notify the change.
     * <p>If the item, resulting from the provided position:</p>
     * - is <u>not expandable</u> with <u>no</u> parent, it is removed as usual.<br>
     * - is <u>not expandable</u> with a parent, it is removed only if the parent is expanded.<br>
     * - is <u>expandable</u> implementing {@link IExpandable}, it is removed as usual, but
     * it will be collapsed if expanded.<br>
     * - has a {@link IHeader} item, the header will be automatically linked to the first item
     * after the range or can remain orphan.
     * <p>Optionally you can pass any payload to notify the <u>parent</u> or the <u>header</u>
     * about the change and optimize the view binding.</p>
     *
     * @param positionStart the start position of the first item
     * @param itemCount     how many items should be removed
     * @param payload       any non-null user object to notify the parent (the payload will be
     *                      therefore passed to the bind method of the parent ViewHolder),
     *                      pass null to <u>not</u> notify the parent
     * @see #clear()
     * @see #clearAllBut(Integer...)
     * @see #removeItem(int, Object)
     * @see #removeItems(List, Object)
     * @see #removeRange(int, int)
     * @see #removeAllSelectedItems(Object)
     * @see #setPermanentDelete(boolean)
     * @see #setRestoreSelectionOnUndo(boolean)
     * @see #getDeletedItems()
     * @see #getDeletedChildren(IExpandable)
     * @see #restoreDeletedItems()
     * @see #emptyBin()
     * @since 5.0.0-b1
     */
    public void removeRange(@IntRange(from = 0) int positionStart,
                            @IntRange(from = 0) int itemCount, @Nullable Object payload) {
        int initialCount = getItemCount();
        log.d("removeRange positionStart=%s itemCount=%s", positionStart, itemCount);
        if (positionStart < 0 || (positionStart + itemCount) > initialCount) {
            log.e("Cannot removeRange with positionStart OutOfBounds!");
            return;
        } else if (itemCount == 0 || initialCount == 0) {
            log.w("removeRange Nothing to delete!");
            return;
        }

        T item = null;
        IExpandable parent = null;
        for (int position = positionStart; position < positionStart + itemCount; position++) {
            item = getItem(positionStart); // We remove always at positionStart!
            if (item == null) continue;

            if (!permanentDelete) {
                // When removing a range of children, parent is always the same :-)
                if (parent == null) parent = getExpandableOf(item);
                // Differentiate: (Expandable & NonExpandable with No parent) from (NonExpandable with a parent)
                if (parent == null) {
                    createRestoreItemInfo(positionStart, item);
                } else {
                    createRestoreSubItemInfo(parent, item);
                }
            }
            // Mark hidden the deleted item
            item.setHidden(true);
            // Unlink items that belongs to the removed header
            if (unlinkOnRemoveHeader && isHeader(item)) {
                IHeader header = (IHeader) item;
                // If item is a Header, remove linkage from ALL Sectionable items if exist
                List<ISectionable> sectionableList = getSectionItems(header);
                for (ISectionable sectionable : sectionableList) {
                    sectionable.setHeader(null);
                    if (payload != null)
                        notifyItemChanged(getGlobalPositionOf(sectionable), Payload.UNLINK);
                }
            }
            // Remove item from internal list
            mItems.remove(positionStart);
            removeSelection(position);
        }

        // Notify range removal
        notifyItemRangeRemoved(positionStart, itemCount);

        // Update content of the header linked to first item of the range
        IHeader header = item != null ? getHeaderOf(item) : null;
        int headerPosition = header != null ? getGlobalPositionOf(header) : -1;
        if (payload != null && header != null && headerPosition >= 0) {
            // The header does not represents a group anymore, add it to the Orphan list
            notifyItemChanged(headerPosition, payload);
        }
        // Notify the Parent about the change if requested
        int parentPosition = getGlobalPositionOf(parent);
        if (payload != null && parentPosition >= 0 && parentPosition != headerPosition) {
            notifyItemChanged(parentPosition, payload);
        }

        // Update empty view
        if (mUpdateListener != null && !multiRange && initialCount > 0 && getItemCount() == 0)
            mUpdateListener.onUpdateEmptyView(getMainItemCount());
    }

    /**
     * Convenience method to remove all items that are currently selected.
     * <p>Parent will <u>not</u> be notified about the change if a child is removed.</p>
     *
     * @see #clear()
     * @see #clearAllBut(Integer...)
     * @see #removeItem(int)
     * @see #removeItems(List)
     * @see #removeRange(int, int)
     * @see #removeItemsOfType(Integer...)
     * @see #removeAllSelectedItems(Object)
     * @since 5.0.0-b1
     */
    public void removeAllSelectedItems() {
        removeAllSelectedItems(null);
    }

    /**
     * Convenience method to remove all items that are currently selected.
     * <p>Optionally, the parent item can be notified about the change if a child is removed,
     * by providing any non-null payload.</p>
     *
     * @param payload any non-null user object to notify the parent (the payload will be
     *                therefore passed to the bind method of the parent ViewHolder),
     *                pass null to <u>not</u> notify the parent
     * @see #clear()
     * @see #clearAllBut(Integer...)
     * @see #removeItem(int, Object)
     * @see #removeItems(List, Object)
     * @see #removeRange(int, int, Object)
     * @see #removeAllSelectedItems()
     * @since 5.0.0-b1
     */
    public void removeAllSelectedItems(@Nullable Object payload) {
        this.removeItems(getSelectedPositions(), payload);
    }

	/*----------------------*/
	/* UNDO/RESTORE METHODS */
	/*----------------------*/

    /**
     * Returns if items will be deleted immediately when deletion is requested.
     * <p>Default value is {@code true} (Undo mechanism is disabled).</p>
     *
     * @return true if the items are deleted immediately, false if items are retained for an
     * eventual restoration
     * @since 5.0.0-b6
     */
    public boolean isPermanentDelete() {
        return permanentDelete;
    }

    /**
     * Sets if the deleted items should be deleted immediately or if Adapter should cache them to
     * restore them when requested by the user.
     * <p>Default value is {@code true} (Undo mechanism is disabled).</p>
     *
     * @param permanentDelete true to delete items forever, false to use the cache for Undo feature
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b6
     */
    public FlexibleAdapter<T> setPermanentDelete(boolean permanentDelete) {
        log.i("Set permanentDelete=%s", permanentDelete);
        this.permanentDelete = permanentDelete;
        return this;
    }

    /**
     * Returns the current configuration to restore selections on Undo.
     * <p>Default value is {@code false} (selection is NOT restored).</p>
     *
     * @return true if selection will be restored, false otherwise
     * @see #setRestoreSelectionOnUndo(boolean)
     * @since 5.0.0-b1
     */
    public boolean isRestoreWithSelection() {
        return restoreSelection;
    }

    /**
     * Gives the possibility to restore the selection on Undo, when {@link #restoreDeletedItems()}
     * is called.
     * <p>To use in combination with {@code ActionMode} in order to not disable it.</p>
     * Default value is {@code false} (selection is NOT restored).
     *
     * @param restoreSelection true to have restored items still selected, false to empty selections
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b1
     */
    public FlexibleAdapter<T> setRestoreSelectionOnUndo(boolean restoreSelection) {
        log.i("Set restoreSelectionOnUndo=%s", restoreSelection);
        this.restoreSelection = restoreSelection;
        return this;
    }

    /**
     * Restore items just removed.
     * <p><b>Tip:</b> If filter is active, only items that match that filter will be shown(restored).</p>
     *
     * @see #setRestoreSelectionOnUndo(boolean)
     * @since 3.0.0
     */
    @SuppressWarnings("ResourceType")
    public void restoreDeletedItems() {
        multiRange = true;
        int initialCount = getItemCount();
        // Selection coherence: start from a clean situation
        if (getSelectedItemCount() > 0) clearSelection();
        // Start from latest item deleted, since others could rely on it
        for (int i = mRestoreList.size() - 1; i >= 0; i--) {
            adjustSelected = false;
            RestoreInfo restoreInfo = mRestoreList.get(i);

            if (restoreInfo.relativePosition >= 0) {
                // Restore child
                log.d("Restore SubItem %s", restoreInfo);
                addSubItem(restoreInfo.getRestorePosition(true), restoreInfo.relativePosition,
                        restoreInfo.item, false, Payload.UNDO);
            } else {
                // Restore parent or simple item
                log.d("Restore Item %s", restoreInfo);
                addItem(restoreInfo.getRestorePosition(false), restoreInfo.item);
            }
            // Item is again visible
            restoreInfo.item.setHidden(false);
            // Notify header if exists
            IHeader header = getHeaderOf(restoreInfo.item);
            if (header != null) {
                notifyItemChanged(getGlobalPositionOf(header), Payload.UNDO);
            }
            // Restore header linkage
            if (unlinkOnRemoveHeader && isHeader(restoreInfo.item)) {
                header = (IHeader) restoreInfo.item;
                List<ISectionable> items = getSectionItems(header);
                for (ISectionable sectionable : items) {
                    linkHeaderTo((T) sectionable, header, Payload.LINK);
                }
            }
        }
        // Restore selection if requested, before emptyBin
        if (restoreSelection && !mRestoreList.isEmpty()) {
            if (isExpandable(mRestoreList.get(0).item) || getExpandableOf(mRestoreList.get(0).item) == null) {
                parentSelected = true;
            } else {
                childSelected = true;
            }
            for (RestoreInfo restoreInfo : mRestoreList) {
                if (restoreInfo.item.isSelectable()) {
                    addSelection(getGlobalPositionOf(restoreInfo.item));
                }
            }
            log.d("Selected positions after restore %s", getSelectedPositions());
        }
        // Call listener to update EmptyView
        multiRange = false;
        if (mUpdateListener != null && initialCount == 0 && getItemCount() > 0)
            mUpdateListener.onUpdateEmptyView(getMainItemCount());

        emptyBin();
    }

    /**
     * Cleans memory from items just removed.
     * <p><b>Note:</b> This method is automatically called after timer is over and after a
     * restoration.</p>
     *
     * @since 3.0.0
     */
    public synchronized void emptyBin() {
        log.d("emptyBin!");
        mRestoreList.clear();
    }

    /**
     * @return true if the restore list is not empty, false otherwise
     * @since 4.0.0
     */
    public final boolean isRestoreInTime() {
        return mRestoreList != null && !mRestoreList.isEmpty();
    }

    /**
     * @return the list of deleted items
     * @since 4.0.0
     */
    @NonNull
    public List<T> getDeletedItems() {
        List<T> deletedItems = new ArrayList<>();
        for (RestoreInfo restoreInfo : mRestoreList) {
            deletedItems.add(restoreInfo.item);
        }
        return deletedItems;
    }

    /**
     * Retrieves the expandable of the deleted child.
     *
     * @param child the deleted child
     * @return the expandable(parent) of this child, or null if no parent found.
     * @since 5.0.0-b1
     */
    public final IExpandable getExpandableOfDeletedChild(@NonNull T child) {
        for (RestoreInfo restoreInfo : mRestoreList) {
            if (restoreInfo.item.equals(child) && isExpandable(restoreInfo.refItem))
                return (IExpandable) restoreInfo.refItem;
        }
        return null;
    }

    /**
     * Retrieves only the deleted children of the specified parent.
     *
     * @param expandable the parent item
     * @return the list of deleted children
     * @since 5.0.0-b1
     */
    @NonNull
    public final List<T> getDeletedChildren(IExpandable expandable) {
        List<T> deletedChild = new ArrayList<>();
        for (RestoreInfo restoreInfo : mRestoreList) {
            if (restoreInfo.refItem != null && restoreInfo.refItem.equals(expandable) && restoreInfo.relativePosition >= 0)
                deletedChild.add(restoreInfo.item);
        }
        return deletedChild;
    }

    /**
     * Retrieves all the original children of the specified parent, filtering out all the
     * deleted children if any.
     *
     * @param expandable the parent item
     * @return a non-null list of the original children minus the deleted children if some are
     * pending removal.
     * @since 5.0.0-b1
     */
    @NonNull
    public final List<T> getCurrentChildren(@Nullable IExpandable expandable) {
        // Check item and subItems existence
        if (expandable == null || !hasSubItems(expandable))
            return new ArrayList<>();

        // Take a copy of the subItems list
        List<T> subItems = new ArrayList<>(expandable.getSubItems());
        // Remove all children pending removal
        if (!mRestoreList.isEmpty()) {
            subItems.removeAll(getDeletedChildren(expandable));
        }
        return subItems;
    }

	/*----------------*/
	/* FILTER METHODS */
	/*----------------*/

    /**
     * @return true if the current search text is not empty or null
     * @since 3.1.0
     */
    public boolean hasSearchText() {
        return mSearchText != null && !mSearchText.isEmpty();
    }

    /**
     * Checks if the searchText is changed.
     *
     * @param newText the new searchText
     * @return true if the old search text is different than the newText, false otherwise
     * @since 5.0.0-b5
     */
    public boolean hasNewSearchText(String newText) {
        return !mOldSearchText.equalsIgnoreCase(newText);
    }

    /**
     * @return the current search text
     * @since 3.1.0
     */
    public String getSearchText() {
        return mSearchText;
    }

    /**
     * Sets the new search text.
     * <p><b>Note:</b> Text is always <b>trimmed</b> and <b>lowercase</b>.</p>
     * <p><b>Tip:</b> You can highlight filtered Text or Words using:
     * <ul><li>{@link FlexibleUtils#highlightText(TextView, String, String)}</li>
     * <li>{@link FlexibleUtils#highlightWords(TextView, String, String)}</li></ul></p>
     *
     * @param searchText the new text to filter the items
     * @since 3.1.0
     */
    public void setSearchText(String searchText) {
        mSearchText = FlexibleUtils.toLowerCase(searchText.trim());
    }

    /**
     * With this setting, we can skip the check of the implemented method
     * {@link IFlexible#shouldNotifyChange(IFlexible)}.
     * <p>By setting false <u>all</u> items will be skipped by an update.</p>
     * Default value is {@code true} (items will always be notified of a change).
     *
     * @param notifyChange true to trigger {@link #notifyItemChanged(int)},
     *                     false to not update the items' content.
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b1
     */
    public final FlexibleAdapter setNotifyChangeOfUnfilteredItems(boolean notifyChange) {
        log.i("Set notifyChangeOfUnfilteredItems=%s", notifyChange);
        this.notifyChangeOfUnfilteredItems = notifyChange;
        return this;
    }

    /**
     * This method performs a further step to nicely animate the moved items. When false, the
     * items are not moved but removed, to be added at the correct position.
     * <p>The process is very slow on big list of the order of ~3-5000 items and higher,
     * due to the calculation of the correct position for each item to be shifted.
     * Use with caution!</p>
     * The slowness is higher when the searchText is cleared out.
     * <p>Default value is {@code false}.</p>
     *
     * @param notifyMove true to animate move changes after filtering or update data set,
     *                   false otherwise
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b8
     */
    public final FlexibleAdapter setNotifyMoveOfFilteredItems(boolean notifyMove) {
        log.i("Set notifyMoveOfFilteredItems=%s", notifyMove);
        this.notifyMoveOfFilteredItems = notifyMove;
        return this;
    }

    /**
     * Filters the current list with the searchText previously set with
     * {@link #setSearchText(String)}.
     *
     * @see #filterItems(long)
     * @see #filterItems(List)
     * @see #onPostFilter()
     * @see #setAnimateToLimit(int)
     * @since 5.0.0-rc2
     */
    public void filterItems() {
        if (mOriginalList == null) mOriginalList = mItems;
        filterItems(mOriginalList);
    }

    /**
     * Same as {@link #filterItems()} but with a delay in the execution, useful to grab
     * more characters from user before starting the search.
     *
     * @param delay any non-negative delay
     * @see #filterItems()
     * @see #filterItems(List, long)
     * @see #onPostFilter()
     * @see #setAnimateToLimit(int)
     * @since 5.0.0-rc2
     */
    public void filterItems(@IntRange(from = 0) long delay) {
        if (mOriginalList == null) mOriginalList = mItems;
        filterItems(mOriginalList, delay);
    }

    /**
     * <b>WATCH OUT! ADAPTER ALREADY CREATES A <u>COPY</u> OF THE PROVIDED LIST</b>: due to internal
     * mechanism, items are removed and/or added in order to animate items in the final list.
     * <p>Same as {@link #filterItems(List)}, but with a delay in the execution, useful to grab
     * more characters from user before starting the search.</p>
     *
     * @param unfilteredItems the list to filter
     * @param delay           any non-negative delay
     * @see #filterItems(long)
     * @see #filterItems(List)
     * @see #onPostFilter()
     * @see #setAnimateToLimit(int)
     * @since 5.0.0-b1
     */
    public void filterItems(@NonNull List<T> unfilteredItems, @IntRange(from = 0) long delay) {
        //Make longer the timer for new coming deleted items
        mHandler.removeMessages(FILTER);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, FILTER, unfilteredItems), delay > 0 ? delay : 0);
    }

    /**
     * <b>WATCH OUT! ADAPTER ALREADY CREATES A <u>COPY</u> OF THE PROVIDED LIST</b>: due to internal
     * mechanism, items are removed and/or added in order to animate items in the final list.
     * <p>This method filters the provided list with the searchText previously set with
     * {@link #setSearchText(String)}.</p>
     * <b>Important notes:</b>
     * <ol>
     * <li>The Filter is <u>always</u> executed in background, asynchronously.
     * The method {@link #onPostFilter()} is called after the filter task is completed.</li>
     * <li>This method calls {@link #filterObject(IFlexible, String)} for each filterable item.</li>
     * <li>Any pending deleted items are always deleted before filter is performed:
     * {@link OnDeleteCompleteListener#onDeleteConfirmed(int)} is therefore invoked
     * (Implemented in {@code UndoHelper}).</li>
     * <li>Expandable items are picked up and displayed if at least a child is collected by
     * the current filter.</li>
     * <li>Filter is skipped while endless feature is active (loading).</li>
     * <li>If searchText is empty or {@code null}, the provided list is the new list plus any
     * Scrollable Headers and Footers if existent.</li>
     * <li>Items are animated thanks to {@link #animateTo(List, Payload)} BUT a limit of
     * {@value #ANIMATE_TO_LIMIT} (default) items is set. <b>Tip:</b> Above this limit,
     * {@link #notifyDataSetChanged()} will be called to improve performance. You can change
     * this limit by calling {@link #setAnimateToLimit(int)}.</li>
     * </ol>
     *
     * @param unfilteredItems the list to filter
     * @see #filterItems()
     * @see #filterItems(List, long)
     * @see #onPostFilter()
     * @see #setAnimateToLimit(int)
     * @since 4.1.0 Created
     * <br>5.0.0-b1 Expandable + Child filtering
     * <br>5.0.0-b8 Synchronization animations limit + AsyncFilter
     * <br>5.0.0-rc1 Scrollable Headers and Footers adaptation
     * <br>5.0.0-rc2 Copy of the Original List is done internally
     * <br>5.0.0-rc2 Removal of Undo in combination with Filter
     */
    public void filterItems(@NonNull List<T> unfilteredItems) {
        mHandler.removeMessages(FILTER);
        mHandler.sendMessage(Message.obtain(mHandler, FILTER, unfilteredItems));
    }

    private synchronized void filterItemsAsync(@NonNull List<T> unfilteredItems) {
        log.d("filterItems with searchText=\"%s\"", mSearchText);
        List<T> filteredItems = new ArrayList<>();
        filtering = true; //Enable flag

        if (hasSearchText() && hasNewSearchText(mSearchText)) { //skip when text is unchanged
            for (T item : unfilteredItems) {
                if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) return;
                // Filter normal AND expandable objects
                filterObject(item, filteredItems);
            }
        } else if (hasNewSearchText(mSearchText)) { //this is better than checking emptiness
            filteredItems = unfilteredItems; //original items with no filter
            resetFilterFlags(filteredItems); //recursive reset
            mExpandedFilterFlags = null;
            if (mOriginalList == null)
                restoreScrollableHeadersAndFooters(filteredItems);
            mOriginalList = null;
        }

        // Animate search results only in case of new SearchText
        if (hasNewSearchText(mSearchText)) {
            mOldSearchText = mSearchText;
            animateTo(filteredItems, Payload.FILTER);
        }

        // Reset flag
        filtering = false;
    }

    /**
     * @return true if the filter is currently running, false otherwise.
     */
    public boolean isFiltering() {
        return filtering;
    }

    /**
     * This method is a wrapper filter for expandable items.<br>
     * It performs filtering on the subItems returning true, if the any child should be in the
     * filtered collection.
     * <p>If the provided item is not an expandable it will be filtered as usual by
     * {@link #filterObject(T, String)}.</p>
     *
     * @param item the object with subItems to be inspected
     * @return true, if the object should be in the filteredResult, false otherwise
     * @since 5.0.0-b1
     */
    private boolean filterObject(T item, List<T> values) {
        // Stop filter task if cancelled
        if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) return false;
        // Skip already filtered items (it happens when internal originalList)
        if (mOriginalList != null && (isScrollableHeaderOrFooter(item) || values.contains(item))) {
            return false;
        }
        // Start to compose the filteredItems to maintain the order of addition
        // It will be discarded if no subItem will be filtered
        List<T> filteredItems = new ArrayList<>();
        filteredItems.add(item);
        // Filter subItems
        boolean filtered = filterExpandableObject(item, filteredItems);
        // If no subItem was filtered, fallback to Normal filter
        if (!filtered) {
            filtered = filterObject(item, getSearchText());
        }
        if (filtered) {
            // Check if header has to be added too
            IHeader header = getHeaderOf(item);
            if (headersShown && hasHeader(item) && !values.contains(header)) {
                header.setHidden(false);
                values.add((T) header);
            }
            values.addAll(filteredItems);
        }
        item.setHidden(!filtered);
        return filtered;
    }

    private boolean filterExpandableObject(T item, List<T> filteredItems) {
        boolean filtered = false;
        // Is item an expandable?
        if (isExpandable(item)) {
            IExpandable expandable = (IExpandable) item;
            // Save which expandable was originally expanded before filtering it out
            if (expandable.isExpanded()) {
                if (mExpandedFilterFlags == null)
                    mExpandedFilterFlags = new HashSet<>();
                mExpandedFilterFlags.add(expandable);
            }
            // SubItems scan filter
            for (T subItem : getCurrentChildren(expandable)) {
                // Recursive filter for subExpandable
                if (subItem instanceof IExpandable && filterObject(subItem, filteredItems)) {
                    filtered = true;
                } else {
                    // Use normal filter for normal subItem
                    subItem.setHidden(!filterObject(subItem, getSearchText()));
                    if (!subItem.isHidden()) {
                        filtered = true;
                        filteredItems.add(subItem);
                    }
                }
            }
            // Expand if filter found text in subItems
            expandable.setExpanded(filtered);
        }
        return filtered;
    }

    /**
     * This method checks if the provided object is a type of {@link IFilterable} interface,
     * if yes, performs the filter on the implemented method {@link IFilterable#filter(String)}.
     * <p><b>Note:</b>
     * <br>- The item will be collected if the implemented method returns true.
     * <br>- {@code IExpandable} items are automatically picked up and displayed if at least a
     * child is collected by the current filter. You DON'T NEED to implement the scan for the
     * children: this is already done :-)
     * <br>- If you don't want to implement the {@code IFilterable} interface on the items, then
     * you can override this method to have another filter logic!
     *
     * @param item       the object to be inspected
     * @param constraint constraint, that the object has to fulfil
     * @return true, if the object returns true as well, and so if it should be in the
     * filteredResult, false otherwise
     * @since 3.1.0 Created
     * <br>5.0.0-b1 Expandable + Child filtering
     */
    protected boolean filterObject(T item, String constraint) {
        return item instanceof IFilterable && ((IFilterable) item).filter(constraint);
    }

    /**
     * Clears flags after searchText is cleared out for Expandable items and sub items.
     * Also restore headers visibility.
     */
    private void resetFilterFlags(List<T> items) {
        if (items == null) return;
        IHeader sameHeader = null;
        // Reset flags for all items!
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            item.setHidden(false);
            if (isExpandable(item)) {
                IExpandable expandable = (IExpandable) item;
                // Reset expanded flag
                if (mExpandedFilterFlags != null)
                    expandable.setExpanded(mExpandedFilterFlags.contains(expandable));
                if (hasSubItems(expandable)) {
                    List<T> subItems = expandable.getSubItems();
                    // Reset subItem hidden flag
                    for (T subItem : subItems) {
                        subItem.setHidden(false);
                        if (subItem instanceof IExpandable) {
                            IExpandable subExpandable = (IExpandable) subItem;
                            subExpandable.setExpanded(false);
                            resetFilterFlags(subExpandable.getSubItems());
                        }
                    }
                    // Show subItems for expanded items
                    if (expandable.isExpanded() && mOriginalList == null) {
                        if (i < items.size()) items.addAll(i + 1, subItems);
                        else items.addAll(subItems);
                        i += subItems.size();
                    }
                }
            }
            // Restore headers visibility
            if (headersShown && mOriginalList == null) {
                IHeader header = getHeaderOf(item);
                if (header != null && !header.equals(sameHeader) && !isExpandable((T) header)) {
                    header.setHidden(false);
                    sameHeader = header;
                    items.add(i, (T) header);
                    i++;
                }
            }
        }
    }

    /**
     * Tunes the limit after the which the synchronization animations, occurred during
     * updateDataSet and filter operations, are skipped and {@link #notifyDataSetChanged()}
     * will be called instead.
     * <p>Default value is {@value #ANIMATE_TO_LIMIT} items, number new items.</p>
     *
     * @param limit the number of new items that, when reached, will skip synchronization animations
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b8
     */
    public FlexibleAdapter<T> setAnimateToLimit(int limit) {
        log.i("Set animateToLimit=%s", limit);
        mAnimateToLimit = limit;
        return this;
    }

	/*-------------------------*/
	/* ANIMATE CHANGES METHODS */
	/*-------------------------*/

    /**
     * Animate the synchronization between the old list and the new list.
     * <p>Used by filter and updateDataSet.</p>
     * <b>Note:</b> The animations are skipped in favor of {@link #notifyDataSetChanged()}
     * when the number of items reaches the limit. See {@link #setAnimateToLimit(int)}.
     * <p><b>Note:</b> In case the animations are performed, unchanged items will be notified if
     * {@code notifyChangeOfUnfilteredItems} is set true, a CHANGE payload will be set.</p>
     *
     * @param newItems the new list containing the new items
     * @see #setNotifyChangeOfUnfilteredItems(boolean)
     * @see #setNotifyMoveOfFilteredItems(boolean)
     * @see #setAnimateToLimit(int)
     * @since 5.0.0-b1 Created
     * <br>5.0.0-b8 Synchronization animation limit
     */
    private synchronized void animateTo(@Nullable List<T> newItems, Payload payloadChange) {
        mNotifications = new ArrayList<>();
        if (newItems != null && newItems.size() <= mAnimateToLimit) {
            log.d("Animate changes! oldSize=%s newSize=%s limit=%s", getItemCount(), newItems.size(), mAnimateToLimit);
            mTempItems = new ArrayList<>(mItems);
            applyAndAnimateRemovals(mTempItems, newItems);
            applyAndAnimateAdditions(mTempItems, newItems);
            if (notifyMoveOfFilteredItems)
                applyAndAnimateMovedItems(mTempItems, newItems);
        } else {
            log.d("NotifyDataSetChanged! oldSize=%s newSize=%s limit=%s", getItemCount(), (newItems != null ? newItems.size() : "0"), mAnimateToLimit);
            mTempItems = newItems;
            mNotifications.add(new Notification(-1, 0));
        }
        //Execute All notifications if filter was Synchronous!
        if (mFilterAsyncTask == null) executeNotifications(payloadChange);
    }

    /**
     * Calculates the modifications for items to rebound.
     *
     * @return A Map with the unfilteredItems items to rebound and their index
     * @since 5.0.0-rc1
     */
    @Nullable
    private Map<T, Integer> applyModifications(List<T> from, List<T> newItems) {
        if (notifyChangeOfUnfilteredItems) {
            // Using Hash for performance
            mHashItems = new HashSet<>(from);
            Map<T, Integer> unfilteredItems = new HashMap<>();
            for (int i = 0; i < newItems.size(); i++) {
                if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) break;
                final T item = newItems.get(i);
                // Save the index of this new item
                if (mHashItems.contains(item)) unfilteredItems.put(item, i);
            }
            return unfilteredItems;
        }
        return null;
    }

    /**
     * Find out all removed items and animate them, also update existent positions with newItems.
     *
     * @since 5.0.0-b1
     */
    private void applyAndAnimateRemovals(List<T> from, List<T> newItems) {
        // This avoids the call indexOf() later on: newItems.get(unfilteredItems.indexOf(item)));
        Map<T, Integer> unfilteredItems = applyModifications(from, newItems);

        // Using Hash for performance
        mHashItems = new HashSet<>(newItems);
        int out = 0, mod = 0;
        for (int i = from.size() - 1; i >= 0; i--) {
            if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) return;
            final T item = from.get(i);
            if (!mHashItems.contains(item)) {
                log.v("calculateRemovals remove position=%s item=%s", i, item);
                from.remove(i);
                mNotifications.add(new Notification(i, Notification.REMOVE));
                out++;
            } else if (notifyChangeOfUnfilteredItems) {
                assert unfilteredItems != null;
                T newItem = newItems.get(unfilteredItems.get(item));
                // Check whether the old content should be updated with the new one
                // Always true in case filter is active
                if (isFiltering() || item.shouldNotifyChange(newItem)) {
                    from.set(i, newItem);
                    mNotifications.add(new Notification(i, Notification.CHANGE));
                    mod++;
                }
            }
        }
        mHashItems = null;
        log.d("calculateModifications total mod=%s", mod);
        log.d("calculateRemovals total out=%s", out);
    }

    /**
     * Find out all added items and animate them.
     *
     * @since 5.0.0-b1
     */
    private void applyAndAnimateAdditions(List<T> from, List<T> newItems) {
        // Using Hash for performance
        mHashItems = new HashSet<>(from);
        int in = 0;
        for (int position = 0; position < newItems.size(); position++) {
            if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) return;
            final T item = newItems.get(position);
            if (!mHashItems.contains(item)) {
                log.v("calculateAdditions add position=%s item=%s", position, item);
                if (notifyMoveOfFilteredItems) {
                    // We add always at the end to animate moved items at the missing position
                    from.add(item);
                    mNotifications.add(new Notification(from.size(), Notification.ADD));
                } else {
                    // #328 - Filtering issue during delete search query (make sure position is in bounds)
                    if (position < from.size()) from.add(position, item);
                    else from.add(item);
                    mNotifications.add(new Notification(position, Notification.ADD));
                }
                in++;
            }
        }
        mHashItems = null;
        log.d("calculateAdditions total new=%s", in);
    }

    /**
     * Find out all moved items and animate them.
     * <p>This method is very slow on list bigger than ~3000 items. Use with caution!</p>
     *
     * @since 5.0.0-b7
     */
    private void applyAndAnimateMovedItems(List<T> from, List<T> newItems) {
        int move = 0;
        for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
            if (mFilterAsyncTask != null && mFilterAsyncTask.isCancelled()) return;
            final T item = newItems.get(toPosition);
            final int fromPosition = from.indexOf(item);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                log.v("calculateMovedItems fromPosition=%s toPosition=%s", fromPosition, toPosition);
                T movedItem = from.remove(fromPosition);
                if (toPosition < from.size()) from.add(toPosition, movedItem);
                else from.add(movedItem);
                mNotifications.add(new Notification(fromPosition, toPosition, Notification.MOVE));
                move++;
            }
        }
        log.v("calculateMovedItems total move=%s", move);
    }

    private synchronized void executeNotifications(Payload payloadChange) {
        log.i("Performing %s notifications", mNotifications.size());
        mItems = mTempItems; //Update mItems in the UI Thread
        setScrollAnimate(false); //Disable scroll animation
        for (Notification notification : mNotifications) {
            switch (notification.operation) {
                case Notification.ADD:
                    notifyItemInserted(notification.position);
                    break;
                case Notification.CHANGE:
                    notifyItemChanged(notification.position, payloadChange);
                    break;
                case Notification.REMOVE:
                    notifyItemRemoved(notification.position);
                    break;
                case Notification.MOVE:
                    notifyItemMoved(notification.fromPosition, notification.position);
                    break;
                default:
                    log.w("notifyDataSetChanged!");
                    notifyDataSetChanged();
                    break;
            }
        }
        mTempItems = null;
        mNotifications = null;

        time = System.currentTimeMillis();
        time = time - start;
        log.i("Animate changes DONE in %sms", time);
    }

    /**
     * @return the time (in ms) of the last update or filter operation.
     */
    public long getTime() {
        return time;
    }

	/*---------------*/
	/* TOUCH METHODS */
	/*---------------*/

    private void initializeItemTouchHelper() {
        if (mItemTouchHelper == null) {
            if (mRecyclerView == null) {
                throw new IllegalStateException("RecyclerView cannot be null. Enabling LongPressDrag or Swipe must be done after the Adapter is added to the RecyclerView.");
            }
            if (mItemTouchHelperCallback == null) {
                mItemTouchHelperCallback = new ItemTouchHelperCallback(this);
                log.i("Initialized default ItemTouchHelperCallback");
            }
            mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    /**
     * Used by {@link FlexibleViewHolder#onTouch(View, MotionEvent)}
     * to start Drag or Swipe when HandleView is touched.
     *
     * @return the ItemTouchHelper instance already initialized.
     * @since 5.0.0-b1
     */
    public final ItemTouchHelper getItemTouchHelper() {
        initializeItemTouchHelper();
        return mItemTouchHelper;
    }

    /**
     * Returns the customization of the ItemTouchHelperCallback or the default if it wasn't set
     * before.
     *
     * @return the ItemTouchHelperCallback instance already initialized
     * @see #setItemTouchHelperCallback(ItemTouchHelperCallback)
     * @since 5.0.0-b7
     */
    public final ItemTouchHelperCallback getItemTouchHelperCallback() {
        initializeItemTouchHelper();
        return mItemTouchHelperCallback;
    }

    /**
     * Sets a custom callback implementation for item touch.
     * <p>If called, Helper will be reinitialized.</p>
     * If not called, the default Helper will be used.
     *
     * @param itemTouchHelperCallback the custom callback implementation for item touch
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-rc1
     */
    public final FlexibleAdapter setItemTouchHelperCallback(ItemTouchHelperCallback itemTouchHelperCallback) {
        mItemTouchHelperCallback = itemTouchHelperCallback;
        mItemTouchHelper = null;
        initializeItemTouchHelper();
        log.i("Initialized custom ItemTouchHelperCallback");
        return this;
    }

    /**
     * Returns whether ItemTouchHelper should start a drag and drop operation if an item is
     * long pressed.<p>
     * Default value is {@code false}.
     *
     * @return true if ItemTouchHelper should start dragging an item when it is long pressed,
     * false otherwise. Default value is {@code false}.
     * @see #setLongPressDragEnabled(boolean)
     * @since 5.0.0-b1
     */
    public final boolean isLongPressDragEnabled() {
        return mItemTouchHelperCallback != null && mItemTouchHelperCallback.isLongPressDragEnabled();
    }

    /**
     * Enable / Disable the Drag on LongPress on the entire ViewHolder.
     * <p><b>Note:</b> This will skip LongClick on the view in order to handle the LongPress,
     * however the LongClick listener will be called if necessary in the new
     * {@link FlexibleViewHolder#onActionStateChanged(int, int)}.</p>
     * Default value is {@code false}.
     *
     * @param longPressDragEnabled true to activate, false otherwise
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b1
     */
    public final FlexibleAdapter setLongPressDragEnabled(boolean longPressDragEnabled) {
        initializeItemTouchHelper();
        log.i("Set longPressDragEnabled=%s", longPressDragEnabled);
        mItemTouchHelperCallback.setLongPressDragEnabled(longPressDragEnabled);
        return this;
    }

    /**
     * Returns whether ItemTouchHelper should start a drag and drop operation by touching its
     * handle.
     * <p>Default value is {@code false}.</p>
     * To use, it is sufficient to set the HandleView by calling
     * {@link FlexibleViewHolder#setDragHandleView(View)}.
     *
     * @return true if active, false otherwise
     * @see #setHandleDragEnabled(boolean)
     * @since 5.0.0-b1
     */
    public final boolean isHandleDragEnabled() {
        return mItemTouchHelperCallback != null && mItemTouchHelperCallback.isHandleDragEnabled();
    }

    /**
     * Enable / Disable the drag of the itemView with a handle view.
     * <p>Default value is {@code false}.</p>
     *
     * @param handleDragEnabled true to activate, false otherwise
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b1
     */
    public final FlexibleAdapter setHandleDragEnabled(boolean handleDragEnabled) {
        initializeItemTouchHelper();
        log.i("Set handleDragEnabled=%s", handleDragEnabled);
        this.mItemTouchHelperCallback.setHandleDragEnabled(handleDragEnabled);
        return this;
    }

    /**
     * Returns whether ItemTouchHelper should start a swipe operation if a pointer is swiped
     * over the View.
     * <p>Default value is {@code false}.</p>
     *
     * @return true if ItemTouchHelper should start swiping an item when user swipes a pointer
     * over the View, false otherwise. Default value is {@code false}.
     * @see #setSwipeEnabled(boolean)
     * @since 5.0.0-b1
     */
    public final boolean isSwipeEnabled() {
        return mItemTouchHelperCallback != null && mItemTouchHelperCallback.isItemViewSwipeEnabled();
    }

    /**
     * Enable the Full Swipe of the items.
     * <p>Default value is {@code false}.</p>
     *
     * @param swipeEnabled true to activate, false otherwise
     * @return this Adapter, so the call can be chained
     * @since 5.0.0-b1
     */
    public final FlexibleAdapter setSwipeEnabled(boolean swipeEnabled) {
        log.i("Set swipeEnabled=%s", swipeEnabled);
        initializeItemTouchHelper();
        mItemTouchHelperCallback.setSwipeEnabled(swipeEnabled);
        return this;
    }

    /**
     * Moves the item placed at position {@code fromPosition} to the position
     * {@code toPosition}.
     * <br>- Selection of moved element is preserved.
     * <br>- If item is an expandable, it is collapsed and then expanded at the new position.
     *
     * @param fromPosition previous position of the item
     * @param toPosition   new position of the item
     * @see #moveItem(int, int, Object)
     * @since 5.0.0-b7
     */
    public void moveItem(int fromPosition, int toPosition) {
        moveItem(fromPosition, toPosition, Payload.MOVE);
    }

    /**
     * Moves the item placed at position {@code fromPosition} to the position
     * {@code toPosition}.
     * <br>- Selection of moved element is preserved.
     * <br>- If item is an expandable, it is collapsed and then expanded at the new position.
     *
     * @param fromPosition previous position of the item
     * @param toPosition   new position of the item
     * @param payload      allows to update the content of the item just moved
     * @since 5.0.0-b7
     */
    public void moveItem(int fromPosition, int toPosition, @Nullable Object payload) {
        log.v("moveItem fromPosition=%s toPosition=%s", fromPosition, toPosition);
        // Preserve selection
        if ((isSelected(fromPosition))) {
            removeSelection(fromPosition);
            addSelection(toPosition);
        }
        T item = getItem(fromPosition);
        // Preserve expanded status and Collapse expandable
        boolean expanded = isExpanded(item);
        if (expanded) collapse(toPosition);
        // Move item!
        mItems.remove(fromPosition);
        performInsert(toPosition, Collections.singletonList(item), false);
        notifyItemMoved(fromPosition, toPosition);
        if (payload != null) notifyItemChanged(toPosition, payload);
        // Eventually display the new Header
        if (headersShown) {
            showHeaderOf(toPosition, item, false);
        }
        // Restore original expanded status
        if (expanded) expand(toPosition);
    }

    /**
     * Swaps the elements of list at indices fromPosition and toPosition and notify the change.
     * <p>Selection of swiped elements is automatically updated.</p>
     *
     * @param fromPosition previous position of the item.
     * @param toPosition   new position of the item.
     * @since 5.0.0-b7
     */
    public void swapItems(List<T> list, int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= getItemCount() ||
                toPosition < 0 || toPosition >= getItemCount()) {
            return;
        }
        log.v("swapItems from=%s [selected? %s] to=%s [selected? %s]",
                fromPosition, isSelected(fromPosition), toPosition, isSelected(toPosition));

        // Collapse expandable before swapping (otherwise items are mixed badly)
        if (fromPosition < toPosition && isExpandable(getItem(fromPosition)) && isExpanded(toPosition)) {
            collapse(toPosition);
        }

        // Perform item swap (for all LayoutManagers)
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                log.v("swapItems from=%s to=%s", i, (i + 1));
                Collections.swap(list, i, i + 1);
                swapSelection(i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                log.v("swapItems from=%s to=%s", i, (i - 1));
                Collections.swap(list, i, i - 1);
                swapSelection(i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

        // Header swap linkage
        if (headersShown) {
            // Situation AFTER items have been swapped, items are inverted!
            T fromItem = getItem(toPosition);
            T toItem = getItem(fromPosition);
            int oldPosition, newPosition;
            if (toItem instanceof IHeader && fromItem instanceof IHeader) {
                if (fromPosition < toPosition) {
                    // Dragging down fromHeader
                    // Auto-linkage all section-items with new header
                    IHeader header = (IHeader) fromItem;
                    List<ISectionable> items = getSectionItems(header);
                    for (ISectionable sectionable : items) {
                        linkHeaderTo((T) sectionable, header, Payload.LINK);
                    }
                } else {
                    // Dragging up fromHeader
                    // Auto-linkage all section-items with new header
                    IHeader header = (IHeader) toItem;
                    List<ISectionable> items = getSectionItems(header);
                    for (ISectionable sectionable : items) {
                        linkHeaderTo((T) sectionable, header, Payload.LINK);
                    }
                }
            } else if (toItem instanceof IHeader) {
                // A Header is being swapped up
                // Else a Header is being swapped down
                oldPosition = fromPosition < toPosition ? toPosition + 1 : toPosition;
                newPosition = fromPosition < toPosition ? toPosition : fromPosition + 1;
                // Swap header linkage
                linkHeaderTo(getItem(oldPosition), getSectionHeader(oldPosition), Payload.LINK);
                linkHeaderTo(getItem(newPosition), (IHeader) toItem, Payload.LINK);
            } else if (fromItem instanceof IHeader) {
                // A Header is being dragged down
                // Else a Header is being dragged up
                oldPosition = fromPosition < toPosition ? fromPosition : fromPosition + 1;
                newPosition = fromPosition < toPosition ? toPosition + 1 : fromPosition;
                // Swap header linkage
                linkHeaderTo(getItem(oldPosition), getSectionHeader(oldPosition), Payload.LINK);
                linkHeaderTo(getItem(newPosition), (IHeader) fromItem, Payload.LINK);
            } else {
                // A Header receives the toItem
                // Else a Header receives the fromItem
                oldPosition = fromPosition < toPosition ? toPosition : fromPosition;
                newPosition = fromPosition < toPosition ? fromPosition : toPosition;
                // Swap header linkage
                T oldItem = getItem(oldPosition);
                IHeader header = getHeaderOf(oldItem);
                if (header != null) {
                    IHeader oldHeader = getSectionHeader(oldPosition);
                    if (oldHeader != null && !oldHeader.equals(header)) {
                        linkHeaderTo(oldItem, oldHeader, Payload.LINK);
                    }
                    linkHeaderTo(getItem(newPosition), header, Payload.LINK);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 5.0.0-b7
     */
    @Override
    public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (mItemMoveListener != null)
            mItemMoveListener.onActionStateChanged(viewHolder, actionState);
        else if (mItemSwipeListener != null) {
            mItemSwipeListener.onActionStateChanged(viewHolder, actionState);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 5.0.0-b1
     */
    @Override
    public boolean shouldMove(int fromPosition, int toPosition) {
        T toItem = getItem(toPosition);
        return !(mScrollableHeaders.contains(toItem) || mScrollableFooters.contains(toItem)) &&
                (mItemMoveListener == null || mItemMoveListener.shouldMoveItem(fromPosition, toPosition));
    }

    /**
     * {@inheritDoc}
     *
     * @since 5.0.0-b1
     */
    @Override
    @CallSuper
    public boolean onItemMove(int fromPosition, int toPosition) {
        swapItems(mItems, fromPosition, toPosition);
        // After the swap, delegate further actions to the user
        if (mItemMoveListener != null) {
            mItemMoveListener.onItemMove(fromPosition, toPosition);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @since 5.0.0-b1
     */
    @Override
    @CallSuper
    public void onItemSwiped(int position, int direction) {
        // Delegate actions to the user
        if (mItemSwipeListener != null) {
            mItemSwipeListener.onItemSwipe(position, direction);
        }
    }

	/*------------------------*/
	/* OTHERS PRIVATE METHODS */
	/*------------------------*/

    /**
     * Internal mapper to remember and add all view types for the items.
     *
     * @param item the item to map
     * @since 5.0.0-b1
     */
    private void mapViewTypeFrom(T item) {
        if (item != null && !mTypeInstances.containsKey(item.getItemViewType())) {
            mTypeInstances.put(item.getItemViewType(), item);
            log.i("Mapped viewType %s from %s", item.getItemViewType(), getClassName(item));
        }
    }

    /**
     * Retrieves the type instance remembered within the FlexibleAdapter for an item.
     *
     * @param viewType the view type of the item (layout resourceId)
     * @return the IFlexible instance, creator of the view type
     * @since 5.0.0-b1
     */
    private T getViewTypeInstance(int viewType) {
        return mTypeInstances.get(viewType);
    }

    /**
     * @param item the item to compare
     * @return the removed item if found, null otherwise
     */
    private RestoreInfo getPendingRemovedItem(T item) {
        for (RestoreInfo restoreInfo : mRestoreList) {
            // refPosition >= 0 means that position has been calculated and restore is ongoing
            if (restoreInfo.item.equals(item) && restoreInfo.refPosition < 0) return restoreInfo;
        }
        return null;
    }

    /**
     * @param expandable the expandable, parent of this sub item
     * @param item       the deleted item
     * @since 5.0.0-b1
     */
    private void createRestoreSubItemInfo(IExpandable expandable, T item) {
        List<T> siblings = getExpandableList(expandable, false);
        int childPosition = siblings.indexOf(item);
        mRestoreList.add(new RestoreInfo((T) expandable, item, childPosition));
        log.v("Recycled SubItem %s with Parent position=%s",
                mRestoreList.get(mRestoreList.size() - 1), getGlobalPositionOf(expandable));
    }

    /**
     * @param position the position of the item to retain.
     * @param item     the deleted item
     * @since 5.0.0-b1
     */
    private void createRestoreItemInfo(int position, T item) {
        // Collapse Parent before removal if it is expanded!
        if (isExpanded(item)) collapse(position);
        // Get the reference of the previous item (getItem returns null if outOfBounds)
        // If null, it will be restored at position = 0
        T refItem = getItem(position - 1);
        if (refItem != null) {
            // Check if the refItem is a child of an Expanded parent, take the parent!
            IExpandable expandable = getExpandableOf(refItem);
            if (expandable != null) refItem = (T) expandable;
        }
        mRestoreList.add(new RestoreInfo(refItem, item));
        log.v("Recycled Item %s on position=%s", mRestoreList.get(mRestoreList.size() - 1), position);
    }

    /**
     * @param expandable the parent item
     * @return the list of the subItems not hidden
     * @since 5.0.0-b1
     */
    @NonNull
    private List<T> getExpandableList(IExpandable expandable, boolean isRecursive) {
        List<T> subItems = new ArrayList<>();
        if (expandable != null && hasSubItems(expandable)) {
            List<T> allSubItems = expandable.getSubItems();
            for (T subItem : allSubItems) {
                // Pick up only no hidden items (doesn't get into account the filtered items)
                if (!subItem.isHidden()) {
                    // Add the current subitem
                    subItems.add(subItem);
                    // If expandable, expanded, and of non-zero size, recursively add sub-subItems
                    if (isRecursive && isExpanded(subItem) &&
                            ((IExpandable) subItem).getSubItems().size() > 0) {
                        subItems.addAll(getExpandableList((IExpandable) subItem, true));
                    }
                }
            }
        }
        return subItems;
    }

    /**
     * Allows or disallows the request to collapse the Expandable item.
     *
     * @param startPosition helps to improve performance, so we can avoid a new search for position
     * @param subItems      the list of sub items to check
     * @return true if at least 1 subItem is currently selected, false if no subItems are selected
     * search is non-recursive
     * @since 5.0.0-b1
     */
    private boolean hasSubItemsSelected(int startPosition, List<T> subItems) {
        for (T subItem : subItems) {
            if (isSelected(++startPosition) ||
                    (isExpanded(subItem) && hasSubItemsSelected(startPosition,
                            getExpandableList((IExpandable) subItem, false))))
                return true;
        }
        return false;
    }

    private void performScroll(final int position) {
        if (mRecyclerView != null) {
            mRecyclerView.smoothScrollToPosition(Math.min(Math.max(0, position), getItemCount() - 1));
        }
    }

    private void autoScrollWithDelay(final int position, final int subItemsCount, final long delay) {
        // Must be delayed to give time at RecyclerView to recalculate positions after an automatic collapse
        new Handler(Looper.getMainLooper(), new Handler.Callback() {
            public boolean handleMessage(Message message) {
                int firstVisibleItem = getFlexibleLayoutManager().findFirstCompletelyVisibleItemPosition();
                int lastVisibleItem = getFlexibleLayoutManager().findLastCompletelyVisibleItemPosition();
                int itemsToShow = position + subItemsCount - lastVisibleItem;
//				log.v("autoScroll itemsToShow=%s firstVisibleItem=%s lastVisibleItem=%s RvChildCount=%s", itemsToShow, firstVisibleItem, lastVisibleItem, mRecyclerView.getChildCount());
                if (itemsToShow > 0) {
                    int scrollMax = position - firstVisibleItem;
                    int scrollMin = Math.max(0, position + subItemsCount - lastVisibleItem);
                    int scrollBy = Math.min(scrollMax, scrollMin);
                    int spanCount = getFlexibleLayoutManager().getSpanCount();
                    if (spanCount > 1) {
                        scrollBy = scrollBy % spanCount + spanCount;
                    }
                    int scrollTo = firstVisibleItem + scrollBy;
//					log.v("autoScroll scrollMin=%s scrollMax=%s scrollBy=%s scrollTo=%s", scrollMin, scrollMax, scrollBy, scrollTo);
                    performScroll(scrollTo);
                } else if (position < firstVisibleItem) {
                    performScroll(position);
                }
                return true;
            }
        }).sendMessageDelayed(Message.obtain(mHandler), delay);
    }

    private void adjustSelected(int startPosition, int itemCount) {
        List<Integer> selectedPositions = getSelectedPositions();
        boolean adjusted = false;
        String diff = "";
        if (itemCount > 0) {
            // Reverse sorting is necessary because using Set removes duplicates
            // during adjusting, so we scan backward.
            Collections.sort(selectedPositions, new Comparator<Integer>() {
                @Override
                public int compare(Integer lhs, Integer rhs) {
                    return rhs - lhs;
                }
            });
            diff = "+";
        }
        for (Integer position : selectedPositions) {
            if (position >= startPosition) {
//				log.v("Adjust Selected position %s to %s", position, Math.max(position + itemCount, startPosition));
                removeSelection(position);
                addAdjustedSelection(Math.max(position + itemCount, startPosition));
                adjusted = true;
            }
        }
        if (adjusted)
            log.v("AdjustedSelected(%s)=%s", (diff + itemCount), getSelectedPositions());
    }

    /**
     * Helper method to post invalidate the item decorations after the provided delay.
     * <p>The delay will give time to the LayoutManagers to complete the layout of the child views.</p>
     * <b>Tip:</b> A delay of {@code 100ms} should be enough, anyway it can be customized.
     *
     * @param delay delay to invalidate the decorations
     */
    public final void invalidateItemDecorations(@IntRange(from = 0) long delay) {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView != null) mRecyclerView.invalidateItemDecorations();
            }
        }, delay);
    }

	/*----------------*/
	/* INSTANCE STATE */
	/*----------------*/

    /**
     * Save the state of the current expanded items.
     *
     * @param outState Current state
     * @since 5.0.0-b1
     */
    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            // Save selection state
            if (mScrollableHeaders.size() > 0) {
                // We need to rollback the added item positions if headers were added lately
                adjustSelected(0, -mScrollableHeaders.size());
            }
            super.onSaveInstanceState(outState);
            // Save selection coherence
            outState.putBoolean(EXTRA_CHILD, this.childSelected);
            outState.putBoolean(EXTRA_PARENT, this.parentSelected);
            outState.putInt(EXTRA_LEVEL, this.mSelectedLevel);
            // Current filter. Old text is not saved otherwise animateTo() cannot be called
            outState.putString(EXTRA_SEARCH, this.mSearchText);
            // Save headers shown status
            outState.putBoolean(EXTRA_HEADERS, this.headersShown);
            outState.putBoolean(EXTRA_STICKY, areHeadersSticky());
        }
    }

    /**
     * Restore the previous state of the expanded items.
     *
     * @param savedInstanceState Previous state
     * @since 5.0.0-b1
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore headers shown status
            boolean headersShown = savedInstanceState.getBoolean(EXTRA_HEADERS);
            if (!headersShown) {
                hideAllHeaders();
            } else if (!this.headersShown) {
                showAllHeadersWithReset(true);
            }
            this.headersShown = headersShown;
            if (savedInstanceState.getBoolean(EXTRA_STICKY) && !areHeadersSticky()) {
                setStickyHeaders(true);
            }
            // Restore selection state
            super.onRestoreInstanceState(savedInstanceState);
            if (mScrollableHeaders.size() > 0) {
                // We need to restore the added item positions if headers were added early
                adjustSelected(0, mScrollableHeaders.size());
            }
            // Restore selection coherence
            this.parentSelected = savedInstanceState.getBoolean(EXTRA_PARENT);
            this.childSelected = savedInstanceState.getBoolean(EXTRA_CHILD);
            this.mSelectedLevel = savedInstanceState.getInt(EXTRA_LEVEL);
            // Current filter (old text must not be saved)
            this.mSearchText = savedInstanceState.getString(EXTRA_SEARCH);
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
         * Called at startup and every time a main item is inserted, removed or filtered.
         * <p><b>Note:</b> Having any Scrollable Headers/Footers visible, the {@code size}
         * will represents only the <b>main</b> items.</p>
         *
         * @param size the current number of <b>main</b> items in the adapter, result of
         *             {@link FlexibleAdapter#getMainItemCount()}
         * @since 5.0.0-b1
         */
        void onUpdateEmptyView(int size);
    }

    /**
     * @since 29/11/2015
     */
    public interface OnDeleteCompleteListener {
        /**
         * Called when UndoTime out is over or when Filter is started or reset in order
         * to commit deletion in the user Repository.
         * <p><b>Note:</b> Must be called on user Main Thread!</p>
         *
         * @param event One of the event of {@code Snackbar.Callback}
         * @since 5.0.0-b1
         */
        void onDeleteConfirmed(int event);
    }

    /**
     * @since 26/01/2016
     */
    public interface OnItemClickListener {
        /**
         * Called when single tap occurs.
         * <p>This method receives the click event generated from the itemView to check if one
         * of the selection mode ({@code SINGLE or MULTI}) is enabled in order to activate the
         * itemView.</p>
         * For Expandable Views it will toggle the Expansion if configured so.
         *
         * @param position the adapter position of the item clicked
         * @return true if the click should activate the itemView according to the selection mode,
         * false for no change to the itemView.
         * @since 5.0.0-b1
         */
        boolean onItemClick(int position);
    }

    /**
     * @since 26/01/2016
     */
    public interface OnItemLongClickListener {
        /**
         * Called when long tap occurs.
         * <p>This method always calls {@link FlexibleViewHolder#toggleActivation()}
         * <u>after</u> the listener event is consumed in order to activate the itemView.</p>
         * For Expandable Views it will collapse the View if configured so.
         *
         * @param position the adapter position of the item clicked
         * @since 5.0.0-b1
         */
        void onItemLongClick(int position);
    }

    /**
     * @since 06/06/2016
     */
    public interface OnActionStateListener {
        /**
         * Called when the {@link ItemTouchHelper} first registers an item as being moved
         * or swiped or when has been released.
         * <p>Override this method to receive touch events with its state.</p>
         *
         * @param viewHolder  the viewHolder touched
         * @param actionState one of {@link ItemTouchHelper#ACTION_STATE_SWIPE} or
         *                    {@link ItemTouchHelper#ACTION_STATE_DRAG} or
         *                    {@link ItemTouchHelper#ACTION_STATE_IDLE}.
         * @since 5.0.0-b7
         */
        void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState);
    }

    /**
     * @since 26/01/2016
     */
    public interface OnItemMoveListener extends OnActionStateListener {
        /**
         * Called when the item would like to be swapped.
         * <p>Delegate this permission to the user.</p>
         *
         * @param fromPosition the potential start position of the dragged item
         * @param toPosition   the potential resolved position of the swapped item
         * @return return true if the items can swap ({@code onItemMove()} will be called),
         * false otherwise (nothing happens)
         * @see FlexibleAdapter#onItemMove(int, int)
         * @since 5.0.0-b8
         */
        boolean shouldMoveItem(int fromPosition, int toPosition);

        /**
         * Called when an item has been dragged far enough to trigger a move. <b>This is called
         * every time an item is shifted</b>, and <strong>not</strong> at the end of a "drop" event.
         * <p>The end of the "drop" event is instead handled by
         * {@link FlexibleViewHolder#onItemReleased(int)}.</p>
         * <b>Tip:</b> Here, you should call {@link #invalidateItemDecorations(long)} to recalculate
         * item offset if any item decoration has been set.
         *
         * @param fromPosition the start position of the moved item
         * @param toPosition   the resolved position of the moved item
         * @see FlexibleAdapter#shouldMove(int, int)
         * @since 5.0.0-b1
         */
        void onItemMove(int fromPosition, int toPosition);
    }

    /**
     * @since 26/01/2016
     */
    public interface OnItemSwipeListener extends OnActionStateListener {
        /**
         * Called when swiping ended its animation and item is not visible anymore.
         *
         * @param position  the position of the item swiped
         * @param direction the direction to which the ViewHolder is swiped, one of:
         *                  {@link ItemTouchHelper#LEFT},
         *                  {@link ItemTouchHelper#RIGHT},
         *                  {@link ItemTouchHelper#UP},
         *                  {@link ItemTouchHelper#DOWN},
         * @since 5.0.0-b1
         */
        void onItemSwipe(int position, int direction);
    }

    /**
     * @since 05/03/2016 created
     * <br>5.0.0-rc3 providing the old position
     */
    public interface OnStickyHeaderChangeListener {
        /**
         * Called when the current sticky header changed.
         *
         * @param newPosition the position of NEW sticky header, -1 if no header is sticky
         * @param oldPosition the position of OLD sticky header, -1 if no header was sticky
         * @since 5.0.0-b1
         */
        void onStickyHeaderChange(int newPosition, int oldPosition);
    }

    /**
     * @since 22/04/2016
     */
    public interface EndlessScrollListener {

        /**
         * No more data to load.
         * <p>This method is called if any limit is reached (<b>targetCount</b> or <b>pageSize</b>
         * must be set) AND if new data is <u>temporary</u> unavailable (ex. no connection or no
         * new updates remotely). If no new data, a {@link FlexibleAdapter#notifyItemChanged(int, Object)}
         * with a payload {@link Payload#NO_MORE_LOAD} is triggered on the <i>progressItem</i>.</p>
         *
         * @param newItemsSize the last size of the new items loaded
         * @see FlexibleAdapter#setEndlessTargetCount(int)
         * @see FlexibleAdapter#setEndlessPageSize(int)
         * @since 5.0.0-rc1
         */
        void noMoreLoad(int newItemsSize);

        /**
         * Loads more data.
         * <p>Use {@code lastPosition} and {@code currentPage} to know what to load next.</p>
         * {@code lastPosition} is the count of the main items without Scrollable Headers.
         *
         * @param lastPosition the position of the last main item in the adapter
         * @param currentPage  the current page
         * @since 5.0.0-b6
         * <br>5.0.0-rc1 added {@code lastPosition} and {@code currentPage} as parameters
         */
        void onLoadMore(int lastPosition, int currentPage);
    }

    /**
     * Observer Class responsible to recalculate Selection and Expanded positions.
     */
    private class AdapterDataObserver extends RecyclerView.AdapterDataObserver {

        private int lastHeaderUpdate = RecyclerView.NO_POSITION;

        private void adjustPositions(int positionStart, int itemCount) {
            if (adjustSelected) //Don't, if remove range / restore
                adjustSelected(positionStart, itemCount);
            adjustSelected = true;
        }

        private void updateOrClearHeader() {
            if (areHeadersSticky() && (!multiRange || !mRestoreList.isEmpty())) {
                // This check avoids to bind multiple times the same header
                if (lastHeaderUpdate != mStickyHeaderHelper.getStickyPosition()) {
                    lastHeaderUpdate = mStickyHeaderHelper.getStickyPosition();
                    // #320 - To include adapter changes just notified we need a new layout pass:
                    // We must give time to LayoutManager otherwise the findFirstVisibleItemPosition()
                    // will return wrong position!
                    mRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (areHeadersSticky()) mStickyHeaderHelper.updateOrClearHeader(true);
                        }
                    }, 100L);
                }
            }
        }

        /* Triggered by notifyDataSetChanged() */
        @Override
        public void onChanged() {
            updateOrClearHeader();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            adjustPositions(positionStart, itemCount);
            updateOrClearHeader();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            adjustPositions(positionStart, -itemCount);
            updateOrClearHeader();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            updateOrClearHeader();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            updateOrClearHeader();
        }
    }

    private class RestoreInfo {
        // Positions
        int refPosition = -1, relativePosition = -1;
        // The item to which the deleted item is referring to
        T refItem = null;
        // The deleted item
        T item = null;

        /**
         * Constructor for simple, header or parent items
         */
        public RestoreInfo(T refItem, T item) {
            this(refItem, item, -1);
        }

        /**
         * Constructor for sub-items
         */
        public RestoreInfo(T refItem, T item, int relativePosition) {
            this.refItem = refItem; //This can be an Expandable or a Header (constructor overload)
            this.item = item;
            this.relativePosition = relativePosition;
        }

        /**
         * @return the position where the deleted item should be restored
         */
        public int getRestorePosition(boolean isChild) {
            if (refPosition < 0) {
                refPosition = getGlobalPositionOf(refItem);
            }
            T item = getItem(refPosition);
            if (isChild && isExpandable(item)) {
                // Assert the expandable children are collapsed
                recursiveCollapse(refPosition, getCurrentChildren((IExpandable) item), 0);
            } else if (isExpanded(item) && !isChild) {
                refPosition += getExpandableList((IExpandable) item, true).size() + 1;
            } else {
                refPosition++;
            }
            return refPosition;
        }

        @Override
        public String toString() {
            return "RestoreInfo[item=" + item + ", refItem=" + refItem + "]";
        }
    }

    /**
     * Class necessary to notify the changes when using AsyncTask.
     */
    private static class Notification {

        public static final int ADD = 1, CHANGE = 2, REMOVE = 3, MOVE = 4;
        int fromPosition, position, operation;

        public Notification(int position, int operation) {
            this.position = position;
            this.operation = operation;
        }

        public Notification(int fromPosition, int toPosition, int operation) {
            this(toPosition, operation);
            this.fromPosition = fromPosition;
        }

        @Override
        public String toString() {
            return "Notification{" +
                    "operation=" + operation +
                    (operation == MOVE ? ", fromPosition=" + fromPosition : "") +
                    ", position=" + position +
                    '}';
        }
    }

    private class FilterAsyncTask extends AsyncTask<Void, Void, Void> {

        private final List<T> newItems;
        private final int what;

        FilterAsyncTask(int what, @Nullable List<T> newItems) {
            this.what = what;
            // Copy of the original list if not null
            this.newItems = newItems == null ? new ArrayList<T>() : new ArrayList<>(newItems);
        }

        @Override
        protected void onPreExecute() {
            if (endlessLoading) {
                log.w("Cannot filter while endlessLoading");
                this.cancel(true);
            }
            // Note: In case some items are in pending deletion (Undo started),
            // we commit the deletion before starting or resetting the filter.
            if (isRestoreInTime() && mDeleteCompleteListener != null) {
                log.d("Removing all deleted items before filtering/updating");
                newItems.removeAll(getDeletedItems());
                if (mOriginalList != null) mOriginalList.removeAll(getDeletedItems());
                mDeleteCompleteListener.onDeleteConfirmed(3); // Snackbar.Callback.DISMISS_EVENT_MANUAL = 3
            }
        }

        @Override
        protected void onCancelled() {
            log.i("FilterAsyncTask cancelled!");
        }

        @Override
        protected Void doInBackground(Void... params) {
            start = System.currentTimeMillis();
            switch (what) {
                case UPDATE:
                    log.d("doInBackground - started UPDATE");
                    prepareItemsForUpdate(newItems);
                    animateTo(newItems, Payload.CHANGE);
                    log.d("doInBackground - ended UPDATE");
                    break;
                case FILTER:
                    log.d("doInBackground - started FILTER");
                    filterItemsAsync(newItems);
                    log.d("doInBackground - ended FILTER");
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mNotifications != null) {
                //Execute post data
                switch (what) {
                    case UPDATE:
                        // Notify all the changes
                        executeNotifications(Payload.CHANGE);
                        onPostUpdate();
                        break;
                    case FILTER:
                        // Notify all the changes
                        executeNotifications(Payload.FILTER);
                        onPostFilter();
                        break;
                }
            }
            mFilterAsyncTask = null;
        }
    }

    private void prepareItemsForUpdate(List<T> newItems) {
        // Display Scrollable Headers and Footers
        restoreScrollableHeadersAndFooters(newItems);

        int position = 0;
        IHeader sameHeader = null;
        // We use 1 cycle for expanding And display headers
        // to optimize the operations of adding hidden items/subItems
        while (position < newItems.size()) {
            T item = newItems.get(position);
            // Expand Expandable
            if (isExpanded(item)) {
                IExpandable expandable = (IExpandable) item;
                expandable.setExpanded(true);
                List<T> subItems = getExpandableList(expandable, false);
                int itemCount = newItems.size();
                if (position < itemCount) {
                    newItems.addAll(position + 1, subItems);
                } else {
                    newItems.addAll(subItems);
                }
            }
            // Display headers too
            if (!headersShown && isHeader(item) && !item.isHidden()) {
                headersShown = true;
            }
            IHeader header = getHeaderOf(item);
            if (header != null && !header.equals(sameHeader) && !isExpandable((T) header)) {
                header.setHidden(false);
                sameHeader = header;
                newItems.add(position, (T) header);
                position++;
            }
            position++;
        }
    }

    /**
     * This method is called after the execution of Async Update, it calls the
     * implementation of the {@link OnUpdateListener} for the emptyView.
     *
     * @see #updateDataSet(List, boolean)
     */
    @CallSuper
    protected void onPostUpdate() {
        // Call listener to update EmptyView, assuming the update always made a change
        if (mUpdateListener != null)
            mUpdateListener.onUpdateEmptyView(getMainItemCount());
    }

    /**
     * This method is called after the execution of Async Filter, it calls the
     * implementation of the {@link OnUpdateListener} for the emptyView.
     *
     * @see #filterItems(List)
     */
    @CallSuper
    protected void onPostFilter() {
        // Call listener to update EmptyView, assuming the filter always made a change
        if (mUpdateListener != null)
            mUpdateListener.onUpdateEmptyView(getMainItemCount());
    }

    /**
     * Handler callback for delayed actions.
     * <p>You can use and override this Callback, current values used by the Adapter:</p>
     * 1 = async call for updateDataSet.
     * <br>2 = async call for filterItems, optionally delayed.
     * <br>8 = hide the progress item from the list, optionally delayed.
     * <p><b>Note:</b> numbers 0-9 are reserved for the Adapter, use others.</p>
     *
     * @since 5.0.0-rc1
     */
    public class HandlerCallback implements Handler.Callback {
        @CallSuper
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case UPDATE: //updateDataSet OR
                case FILTER: //filterItems
                    if (mFilterAsyncTask != null) mFilterAsyncTask.cancel(true);
                    mFilterAsyncTask = new FilterAsyncTask(message.what, (List<T>) message.obj);
                    mFilterAsyncTask.execute();
                    return true;
                case LOAD_MORE_COMPLETE: //hide progress item
                    hideProgressItem();
                    return true;
            }
            return false;
        }
    }

}