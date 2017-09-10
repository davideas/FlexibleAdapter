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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.common.FlexibleLayoutManager;
import eu.davidea.flexibleadapter.common.IFlexibleLayoutManager;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;
import eu.davidea.flexibleadapter.utils.Log;
import eu.davidea.flexibleadapter.utils.Log.Level;
import eu.davidea.flexibleadapter.utils.Logger;
import eu.davidea.viewholders.FlexibleViewHolder;

import static eu.davidea.flexibleadapter.SelectableAdapter.Mode.IDLE;
import static eu.davidea.flexibleadapter.SelectableAdapter.Mode.MULTI;
import static eu.davidea.flexibleadapter.SelectableAdapter.Mode.SINGLE;

/**
 * This class provides a set of standard methods to handle the selection on the items of an Adapter.
 * <p>Also it manages the FastScroller.</p>
 * This class is extended by {@link AnimatorAdapter}.
 *
 * @author Davide Steduto
 * @see FlexibleAdapter
 * @see AnimatorAdapter
 * @since 03/05/2015 Created
 * <br>27/01/2016 Improved Selection, SelectAll, FastScroller
 * <br>29/05/2016 Use of TreeSet instead of ArrayList
 * <br>04/04/2017 Use of FastScrollerDelegate
 * <br>05/06/2017 Improved Log system
 */
@SuppressWarnings({"unused", "unchecked", "ConstantConditions", "WeakerAccess"})
public abstract class SelectableAdapter extends RecyclerView.Adapter
        implements FastScroller.BubbleTextCreator, FastScroller.OnScrollStateChangeListener, FastScroller.AdapterInterface {

    private static final String TAG = SelectableAdapter.class.getSimpleName();
    Logger log;

    /**
     * Annotation interface for selection modes: {@link #IDLE}, {@link #SINGLE}, {@link #MULTI}
     */
    @SuppressLint("UniqueConstants")
    @IntDef({IDLE, SINGLE, MULTI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
        /**
         * - <b>IDLE:</b> Adapter will not keep track of selections.<br>
         * - <b>SINGLE:</b> Select only one per time.<br>
         * - <b>MULTI:</b> Multi selection will be activated.
         */
        int IDLE = 0, SINGLE = 1, MULTI = 2;
    }

    private final Set<Integer> mSelectedPositions;
    private final Set<FlexibleViewHolder> mBoundViewHolders;
    private int mMode;
    private IFlexibleLayoutManager mFlexibleLayoutManager;
    protected RecyclerView mRecyclerView;
    protected FastScroller.Delegate mFastScrollerDelegate;

    /**
     * Flag when fast scrolling is active.
     * <p>Used to know if user is fast scrolling.</p>
     */
    protected boolean isFastScroll = false;

    /**
     * ActionMode selection flag SelectAll.
     * <p>Used when user click on selectAll action button in ActionMode.</p>
     */
    protected boolean mSelectAll = false;

    /**
     * ActionMode selection flag LastItemInActionMode.
     * <p>Used when user returns to {@link Mode#IDLE} and no selection is active.</p>
     */
    protected boolean mLastItemInActionMode = false;

	/*--------------*/
    /* CONSTRUCTORS */
	/*--------------*/

    /**
     * @since 1.0.0
     */
    public SelectableAdapter() {
        if (Log.customTag == null) Log.useTag("FlexibleAdapter");
        log = new Logger(Log.customTag);
        log.i("Running version %s", BuildConfig.VERSION_NAME);
        mSelectedPositions = Collections.synchronizedSet(new TreeSet<Integer>());
        mBoundViewHolders = new HashSet<>();
        mMode = IDLE;

        mFastScrollerDelegate = new FastScroller.Delegate();
    }

	/*----------------*/
	/* STATIC METHODS */
	/*----------------*/

    /**
     * Call this once, to enable or disable internal logs with custom level.<br>
     * Logs are disabled by default.
     *
     * @param level One of {@link Level} value
     * @since 5.0.0-b1
     */
    public static void enableLogs(@Level int level) {
        Log.setLevel(level);
    }

    public static void useTag(String tag) {
        Log.useTag(tag);
    }

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

    /**
     * {@inheritDoc}
     * <p>Attaches the {@code FastScrollerDelegate} to the RecyclerView if necessary.</p>
     *
     * @since 5.0.0-b6
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (mFastScrollerDelegate != null) {
            mFastScrollerDelegate.onAttachedToRecyclerView(recyclerView);
        }
        mRecyclerView = recyclerView;
    }

    /**
     * {@inheritDoc}
     * <p>Detaches the {@code FastScrollerDelegate} from the RecyclerView if necessary.</p>
     *
     * @since 5.0.0-b6
     */
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (mFastScrollerDelegate != null) {
            mFastScrollerDelegate.onDetachedFromRecyclerView(recyclerView);
        }
        mRecyclerView = null;
        mFlexibleLayoutManager = null;
    }

    /**
     * @return the RecyclerView instance
     * @since 5.0.0-b6
     */
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * Current instance of the wrapper class for LayoutManager suitable for FlexibleAdapter.
     * LayoutManager must be already initialized in the RecyclerView.
     * <p>
     * return wrapper class for any non-conventional LayoutManagers or {@code null} if not initialized.
     *
     * @since 5.0.0-rc2
     */
    public IFlexibleLayoutManager getFlexibleLayoutManager() {
        if (mFlexibleLayoutManager == null) {
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager instanceof IFlexibleLayoutManager) {
                mFlexibleLayoutManager = (IFlexibleLayoutManager) layoutManager;
            } else if (layoutManager != null) {
                mFlexibleLayoutManager = new FlexibleLayoutManager(mRecyclerView);
            }
        }
        return this.mFlexibleLayoutManager;
    }

    /**
     * Allow to use a custom LayoutManager.
     *
     * @param flexibleLayoutManager the custom LayoutManager suitable for FlexibleAdapter
     * @since 5.0.0-rc2
     */
    public void setFlexibleLayoutManager(IFlexibleLayoutManager flexibleLayoutManager) {
        this.mFlexibleLayoutManager = flexibleLayoutManager;
    }

    /**
     * Sets the mode of the selection:
     * <ul>
     * <li>{@link Mode#IDLE} Default. Configures the adapter so that no item can be selected;
     * <li>{@link Mode#SINGLE} configures the adapter to react at the single tap over an item
     * (previous selection is cleared automatically);
     * <li>{@link Mode#MULTI} configures the adapter to save the position to the list of the
     * selected items.
     * </ul>
     *
     * @param mode one of {@link Mode#IDLE}, {@link Mode#SINGLE}, {@link Mode#MULTI}
     * @since 2.0.0
     */
    public void setMode(@Mode int mode) {
        log.i("Mode %s enabled", FlexibleUtils.getModeName(mode));
        if (mMode == SINGLE && mode == IDLE)
            clearSelection();
        this.mMode = mode;
        this.mLastItemInActionMode = (mode != MULTI);
    }

    /**
     * The current selection mode of the Adapter.
     *
     * @return current mode
     * @see Mode#IDLE
     * @see Mode#SINGLE
     * @see Mode#MULTI
     * @since 2.1.0
     */
    @Mode
    public int getMode() {
        return mMode;
    }

    /**
     * @return true if user clicks on SelectAll on action button in ActionMode.
     * @since 5.0.0-b1
     */
    public boolean isSelectAll() {
        // Reset the flags with delay
        resetActionModeFlags();
        return mSelectAll;
    }

    /**
     * @return true if user returns to {@link Mode#IDLE} or {@link Mode#SINGLE} and no
     * selection is active, false otherwise
     * @since 5.0.0-b1
     */
    public boolean isLastItemInActionMode() {
        // Reset the flags with delay
        resetActionModeFlags();
        return mLastItemInActionMode;
    }

    /**
     * Resets to false the ActionMode flags: {@code SelectAll} and {@code LastItemInActionMode}.
     *
     * @since 5.0.0-b1
     */
    private void resetActionModeFlags() {
        if (mSelectAll || mLastItemInActionMode) {
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSelectAll = false;
                    mLastItemInActionMode = false;
                }
            }, 200L);
        }
    }

    /**
     * Indicates if the item, at the provided position, is selected.
     *
     * @param position Position of the item to check.
     * @return true if the item is selected, false otherwise.
     * @since 1.0.0
     */
    public boolean isSelected(int position) {
        return mSelectedPositions.contains(position);
    }

    /**
     * Checks if the current item has the property {@code selectable = true}.
     *
     * @param position the current position of the item to check
     * @return true if the item property </i>selectable</i> is true, false otherwise
     * @since 5.0.0-b6
     */
    public abstract boolean isSelectable(int position);

    /**
     * Toggles the selection status of the item at a given position.
     * <p>The behaviour depends on the selection mode previously set with {@link #setMode(int)}.</p>
     * The Activated State of the ItemView is automatically set in
     * {@link FlexibleViewHolder#toggleActivation()} called in {@code onClick} event
     * <p><b>Usage:</b>
     * <ul>
     * <li>If you don't want any item to be selected/activated at all, just don't call this method.</li>
     * <li>To have actually the item visually selected you need to add a custom <i>Selector Drawable</i>
     * to the background of the View, via {@code DrawableUtils} or via layout's item:
     * <i>android:background="?attr/selectableItemBackground"</i>, pointing to a custom Drawable
     * in the style.xml (note: prefix <i>?android:attr</i> <u>doesn't</u> work).</li>
     * <li></li>
     * </ul></p>
     *
     * @param position Position of the item to toggle the selection status for.
     * @since 1.0.0
     */
    public void toggleSelection(int position) {
        if (position < 0) return;
        if (mMode == SINGLE)
            clearSelection();

        boolean contains = mSelectedPositions.contains(position);
        if (contains) {
            removeSelection(position);
        } else {
            addSelection(position);
        }
        log.v("toggleSelection %s on position %s, current %s",
                (contains ? "removed" : "added"), position, mSelectedPositions);
    }

    /**
     * Adds the selection status for the given position without notifying the change.
     *
     * @param position Position of the item to add the selection status for.
     * @return true if the set is modified, false otherwise or position is not currently selectable
     * @see #isSelectable(int)
     * @since 5.0.0-b7
     */
    public final boolean addSelection(int position) {
        return isSelectable(position) && mSelectedPositions.add(position);
    }

    /**
     * This method is used only internally to force adjust selection.
     *
     * @param position Position of the item to add the selection status for.
     * @return true if the set is modified, false otherwise
     * @since 5.0.0-rc1
     */
    final boolean addAdjustedSelection(int position) {
        return mSelectedPositions.add(position);
    }

    /**
     * Removes the selection status for the given position without notifying the change.
     *
     * @param position Position of the item to remove the selection status for.
     * @return true if the set is modified, false otherwise
     * @since 5.0.0-b7
     */
    public final boolean removeSelection(int position) {
        return mSelectedPositions.remove(position);
    }

    /**
     * Helper method to easily swap selection between 2 positions only if one of the positions
     * is <i>not</i> selected.
     *
     * @param fromPosition first position
     * @param toPosition   second position
     */
    protected void swapSelection(int fromPosition, int toPosition) {
        if (isSelected(fromPosition) && !isSelected(toPosition)) {
            removeSelection(fromPosition);
            addSelection(toPosition);
        } else if (!isSelected(fromPosition) && isSelected(toPosition)) {
            removeSelection(toPosition);
            addSelection(fromPosition);
        }
    }

    /**
     * Sets the selection status for all items which the ViewTypes are included in the specified array.
     *
     * @param viewTypes The ViewTypes for which we want the selection, pass nothing to select all
     * @since 1.0.0
     */
    public void selectAll(Integer... viewTypes) {
        mSelectAll = true;
        List<Integer> viewTypesToSelect = Arrays.asList(viewTypes);
        log.v("selectAll ViewTypes to include %s", viewTypesToSelect);
        int positionStart = 0, itemCount = 0;
        for (int i = 0; i < getItemCount(); i++) {
            if (isSelectable(i) &&
                    (viewTypesToSelect.isEmpty() || viewTypesToSelect.contains(getItemViewType(i)))) {
                mSelectedPositions.add(i);
                itemCount++;
            } else {
                // Optimization for ItemRangeChanged
                if (positionStart + itemCount == i) {
                    notifySelectionChanged(positionStart, itemCount);
                    itemCount = 0;
                    positionStart = i;
                }
            }
        }
        log.d("selectAll notifyItemRangeChanged from positionStart=%s itemCount=%s", positionStart, getItemCount());
        notifySelectionChanged(positionStart, getItemCount());
    }

    /**
     * Clears the selection status for all items one by one and it doesn't stop animations in the items.
     * <p>
     * <b>Note 1:</b> Items are not rebound, so an eventual animation is not stopped!<br>
     * <b>Note 2:</b> This method use {@code java.util.Iterator} on synchronized collection to
     * avoid {@code java.util.ConcurrentModificationException}.</p>
     *
     * @since 1.0.0
     */
    public void clearSelection() {
        // #373 - ConcurrentModificationException with Undo after multiple rapid swipe removals
        synchronized (mSelectedPositions) {
            log.d("clearSelection %s", mSelectedPositions);
            Iterator<Integer> iterator = mSelectedPositions.iterator();
            int positionStart = 0, itemCount = 0;
            // The notification is done only on items that are currently selected.
            while (iterator.hasNext()) {
                int position = iterator.next();
                iterator.remove();
                // Optimization for ItemRangeChanged
                if (positionStart + itemCount == position) {
                    itemCount++;
                } else {
                    // Notify previous items in range
                    notifySelectionChanged(positionStart, itemCount);
                    positionStart = position;
                    itemCount = 1;
                }
            }
            // Notify remaining items in range
            notifySelectionChanged(positionStart, itemCount);
        }
    }

    private void notifySelectionChanged(int positionStart, int itemCount) {
        if (itemCount > 0) {
            // Avoid to rebind the VH, direct call to the itemView activation
            for (FlexibleViewHolder flexHolder : mBoundViewHolders) {
                flexHolder.toggleActivation();
            }
            // Use classic notification, in case FlexibleViewHolder is not implemented
            if (mBoundViewHolders.isEmpty())
                notifyItemRangeChanged(positionStart, itemCount, Payload.SELECTION);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        // Bind the correct view elevation
        if (holder instanceof FlexibleViewHolder) {
            FlexibleViewHolder flexHolder = (FlexibleViewHolder) holder;
            flexHolder.getContentView().setActivated(isSelected(position));
            if (flexHolder.getContentView().isActivated() && flexHolder.getActivationElevation() > 0)
                ViewCompat.setElevation(flexHolder.getContentView(), flexHolder.getActivationElevation());
            else if (flexHolder.getActivationElevation() > 0) //Leave unaltered the default elevation
                ViewCompat.setElevation(flexHolder.getContentView(), 0);
            mBoundViewHolders.add(flexHolder);
        } else {
            // When user scrolls, this line binds the correct selection status
            holder.itemView.setActivated(isSelected(position));
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof FlexibleViewHolder)
            mBoundViewHolders.remove(holder);
    }

    /**
     * Usually {@code RecyclerView} binds 3 items more than the visible items.
     *
     * @return a Set with all bound FlexibleViewHolders
     * @since 5.0.0-rc1
     */
    public Set<FlexibleViewHolder> getAllBoundViewHolders() {
        return Collections.unmodifiableSet(mBoundViewHolders);
    }

    /**
     * Counts the selected items.
     *
     * @return Selected items count
     * @since 1.0.0
     */
    public int getSelectedItemCount() {
        return mSelectedPositions.size();
    }

    /**
     * Retrieves the list of selected items.
     * <p>The list is a copy and it's sorted.</p>
     *
     * @return A copied List of selected items ids from the Set
     * @since 5.0.0-b2
     */
    public List<Integer> getSelectedPositions() {
        return new ArrayList<>(mSelectedPositions);
    }

    /**
     * Retrieves the set of selected items.
     * <p>The set is sorted.</p>
     *
     * @return Set of selected items ids
     */
    public Set<Integer> getSelectedPositionsAsSet() {
        return mSelectedPositions;
    }

	/*----------------*/
	/* INSTANCE STATE */
	/*----------------*/

    /**
     * Saves the state of the current selection on the items.
     *
     * @param outState Current state
     * @since 1.0.0
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList(TAG, new ArrayList<>(mSelectedPositions));
        if (getSelectedItemCount() > 0) log.d("Saving selection %s", mSelectedPositions);
    }

    /**
     * Restores the previous state of the selection on the items.
     *
     * @param savedInstanceState Previous state
     * @since 1.0.0
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mSelectedPositions.addAll(savedInstanceState.getIntegerArrayList(TAG));
        if (getSelectedItemCount() > 0) log.d("Restore selection %s", mSelectedPositions);
    }

	/*---------------*/
	/* FAST SCROLLER */
	/*---------------*/

    /**
     * Displays or Hides the {@link FastScroller} if previously configured.
     * <br>The action is animated.
     *
     * @see #setFastScroller(FastScroller)
     * @since 5.0.0-b1
     */
    public void toggleFastScroller() {
        mFastScrollerDelegate.toggleFastScroller();
    }

    /**
     * @return true if {@link FastScroller} is configured and shown, false otherwise
     * @since 5.0.0-b1
     */
    public boolean isFastScrollerEnabled() {
        return mFastScrollerDelegate.isFastScrollerEnabled();
    }

    /**
     * @return the current instance of the {@link FastScroller} object
     * @since 5.0.0-b1
     */
    public FastScroller getFastScroller() {
        return mFastScrollerDelegate.getFastScroller();
    }

    /**
     * Sets up the {@link FastScroller} with automatic fetch of accent color.
     * <p><b>IMPORTANT:</b> Call this method after the adapter is added to the RecyclerView.</p>
     * <b>NOTE:</b> If the device has at least Lollipop, the Accent color is fetched, otherwise
     * for previous version, the default value is used.
     *
     * @param fastScroller instance of {@link FastScroller}
     * @since 5.0.0-b6
     */
    public void setFastScroller(@NonNull FastScroller fastScroller) {
        mFastScrollerDelegate.setFastScroller(fastScroller);
    }


    /**
     * @param position the position of the handle
     * @return the value of the item, default value is: position + 1
     * @since 5.0.0-b1
     */
    @Override
    public String onCreateBubbleText(int position) {
        return String.valueOf(position + 1);
    }

    /**
     * Triggered when FastScroller State is changed.
     *
     * @param scrolling true if the user is actively scrolling, false when idle
     * @since 5.0.0-b1
     */
    @Override
    public void onFastScrollerStateChange(boolean scrolling) {
        isFastScroll = scrolling;
    }

}