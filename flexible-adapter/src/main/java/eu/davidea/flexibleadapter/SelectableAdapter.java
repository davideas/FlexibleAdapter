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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This class provides a set of standard methods to handle the selection on the items of an Adapter.
 * <p>Also it manages the FastScroller.</p>
 * This class is extended by {@link FlexibleAnimatorAdapter}.
 *
 * @author Davide Steduto
 * @see FlexibleAdapter
 * @see FlexibleAnimatorAdapter
 * @since 03/05/2015 Created
 * <br/>27/01/2016 Improved Selection, SelectAll, FastScroller
 */
@SuppressWarnings({"unused", "Convert2Diamond", "unchecked"})
public abstract class SelectableAdapter extends RecyclerView.Adapter
		implements FastScroller.BubbleTextCreator, FastScroller.ScrollStateChangeListener {

	private static final String TAG = SelectableAdapter.class.getSimpleName();
	public static boolean DEBUG = false;

	//TODO: Change MODE from int to Enum and EnumSet??
//	public enum Mode {//beta test
//		IDLE, SINGLE, MULTI, DRAG, SWIPE
//	}
	/**
	 * Contains type of animators already added
	 */
//	private EnumSet<Mode> mode = EnumSet.of(Mode.IDLE);

	/**
	 * Adapter will not keep track of selections
	 */
	public static final int MODE_IDLE = 0;
	/**
	 * Default mode for selection
	 */
	public static final int MODE_SINGLE = 1;
	/**
	 * Multi selection will be activated
	 */
	public static final int MODE_MULTI = 2;
	/**
	 * Mode to use when dragging or swiping
	 */
	//public static final int MODE_DRAG_SWIPE = 4;

	//TODO: Evaluate TreeSet instead of ArrayList for mSelectedPositions, TreeSet is a sortedList
	private ArrayList<Integer> mSelectedPositions;
	private int mMode;
	protected RecyclerView mRecyclerView;
	protected FastScroller mFastScroller;

	/**
	 * ActionMode selection flag SelectAll.
	 * <p>Used when user click on selectAll action button in ActionMode.</p>
	 */
	protected boolean mSelectAll = false;

	/**
	 * ActionMode selection flag LastItemInActionMode.
	 * <p>Used when user returns to {@link #MODE_IDLE} and no selection is active.</p>
	 */
	protected boolean mLastItemInActionMode = false;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	public SelectableAdapter() {
		mSelectedPositions = new ArrayList<Integer>();
		mMode = MODE_IDLE;
	}

	/*----------------*/
	/* STATIC METHODS */
	/*----------------*/

	/**
	 * Call this once, to enable or disable DEBUG logs.<br/>
	 * DEBUG logs are disabled by default.
	 *
	 * @param enable true to show DEBUG logs in verbose mode, false to hide them.
	 */
	public static void enableLogs(boolean enable) {
		DEBUG = enable;
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		mRecyclerView = recyclerView;
	}
	
	   @Override
	    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
	        super.onDetachedFromRecyclerView(recyclerView);
	        mRecyclerView = null;
	    }

	/**
	 * Sets the mode of the selection, MODE_SINGLE is the default:
	 * <ul>
	 * <li>{@link #MODE_SINGLE} configures the adapter to react at the single tap over an item
	 * (previous selection is cleared automatically);
	 * <li>{@link #MODE_MULTI} configures the adapter to save the position to the list of the
	 * selected items.
	 * </ul>
	 *
	 * @param mode MODE_SINGLE or MODE_MULTI
	 */
	public void setMode(int mode) {
		this.mMode = mode;
		mLastItemInActionMode = (mode == MODE_IDLE);
	}

	/**
	 * The current selection mode of the Adapter.
	 *
	 * @return current mode
	 * @see #MODE_SINGLE
	 * @see #MODE_MULTI
	 */
	public int getMode() {
		return mMode;
	}

	/**
	 * @return true if user clicks on SelectAll on action button in ActionMode.
	 */
	public boolean isSelectAll() {
		return mSelectAll;
	}

	/**
	 * @return true if user returns to {@link #MODE_IDLE} and no selection is active, false otherwise
	 */
	public boolean isLastItemInActionMode() {
		return mLastItemInActionMode;
	}

	/**
	 * Reset to false the ActionMode flags: {@code SelectAll} and {@code LastItemInActionMode}.
	 * <p><b>IMPORTANT:</b> To be called with <u>delay</u> in {@code holder.itemView.postDelayed()}.</p>
	 */
	public void resetActionModeFlags() {
		this.mSelectAll = false;
		this.mLastItemInActionMode = false;
	}

	/**
	 * Indicates if the item, at the provided position, is selected.
	 *
	 * @param position Position of the item to check.
	 * @return true if the item is selected, false otherwise.
	 */
	public boolean isSelected(int position) {
		return mSelectedPositions.contains(Integer.valueOf(position));
	}

	public abstract boolean isSelectable(int position);

	/**
	 * Toggles the selection status of the item at a given position.<br/>
	 * The behaviour depends on the selection mode previously set with {@link #setMode(int)}.
	 *
	 * <p>The Activated State of the ItemView is automatically set in
	 * {@link FlexibleViewHolder#toggleActivation()} called in {@code onClick} event</p>
	 *
	 * <b>Usage:</b>
	 * <ul>
	 * <li>If you don't want any item to be selected/activated at all, just don't call this method.</li>
	 * <li>To have actually the item visually selected you need to add a custom <i>Selector Drawable</i>
	 * to your layout/view of the Item. It's preferable to set in your layout:
	 * <i>android:background="?attr/selectableItemBackground"</i>, pointing to a custom Drawable
	 * in the style.xml (note: prefix <i>?android:attr</i> <u>doesn't</u> work).</li>
	 * <li>In <i>bindViewHolder</i>, adjust the selection status:
	 * <i>holder.itemView.setActivated(isSelected(position));</i></li>
	 * </ul>
	 *
	 * @param position Position of the item to toggle the selection status for.
	 */
	public void toggleSelection(int position) {
		if (position < 0) return;
		if (mMode == MODE_SINGLE)
			clearSelection();

		int index = mSelectedPositions.indexOf(position);
		if (index != -1) {
			mSelectedPositions.remove(index);
		} else {
			mSelectedPositions.add(position);
		}
		if (DEBUG) Log.v(TAG, "toggleSelection " + (index != -1 ? "removed" : "added") +
				" selection on position " + position +
				", current selection " + mSelectedPositions);
	}

	/**
	 * Sets the selection status for all items which the ViewTypes are included in the specified array.
	 * <p><b>Note:</b> All items are invalidated and rebound!</p>
	 *
	 * @param viewTypes The ViewTypes for which we want the selection, pass nothing to select all
	 */
	public void selectAll(Integer... viewTypes) {
		mSelectAll = true;
		List<Integer> viewTypesToSelect = Arrays.asList(viewTypes);
		if (DEBUG) Log.v(TAG, "selectAll ViewTypes to include " + viewTypesToSelect);
		mSelectedPositions = new ArrayList<Integer>(getItemCount());
		int positionStart = 0, itemCount = 0;
		for (int i = 0; i < getItemCount(); i++) {
			if (isSelectable(i) &&
					(viewTypesToSelect.size() == 0 || viewTypesToSelect.contains(getItemViewType(i)))) {
				mSelectedPositions.add(i);
				itemCount++;
			} else {
				//Optimization for ItemRangeChanged
				if (positionStart + itemCount == i) {
					handleSelection(positionStart, itemCount);
					itemCount = 0;
					positionStart = i;
				}
			}
		}
		if (DEBUG)
			Log.v(TAG, "selectAll notifyItemRangeChanged from positionStart=" + positionStart + " itemCount=" + getItemCount());
		handleSelection(positionStart, getItemCount());
	}

	/**
	 * Clears the selection status for all items one by one and it doesn't stop animations in the items.
	 * <br/><br/>
	 * <b>Note 1:</b> Items are invalidated and rebound!<br/>
	 * <b>Note 2:</b> This method use java.util.Iterator to avoid java.util.ConcurrentModificationException.
	 */
	public void clearSelection() {
		Collections.sort(mSelectedPositions, new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs) {
				return lhs - rhs;
			}
		});
		if (DEBUG) Log.v(TAG, "clearSelection " + mSelectedPositions);
		Iterator<Integer> iterator = mSelectedPositions.iterator();
		int positionStart = 0, itemCount = 0;
		//The notification is done only on items that are currently selected.
		while (iterator.hasNext()) {
			int position = iterator.next();
			iterator.remove();
			//Optimization for ItemRangeChanged
			if (positionStart + itemCount == position) {
				itemCount++;
			} else {
				//Notify previous items in range
				handleSelection(positionStart, itemCount);
				positionStart = position;
				itemCount = 1;
			}
		}
		//Notify remaining items in range
		handleSelection(positionStart, itemCount);
	}

	private void handleSelection(int positionStart, int itemCount) {
		if (itemCount > 0) notifyItemRangeChanged(positionStart, itemCount);
	}

	/**
	 * Counts the selected items.
	 *
	 * @return Selected items count
	 */
	public int getSelectedItemCount() {
		return mSelectedPositions.size();
	}

	/**
	 * Indicates the list of selected items.
	 *
	 * @return List of selected items ids
	 */
	public List<Integer> getSelectedPositions() {
		return mSelectedPositions;
	}

	/*----------------*/
	/* INSTANCE STATE */
	/*----------------*/

	/**
	 * Saves the state of the current selection on the items.
	 *
	 * @param outState Current state
	 */
	public void onSaveInstanceState(Bundle outState) {
		outState.putIntegerArrayList(TAG, mSelectedPositions);
	}

	/**
	 * Restores the previous state of the selection on the items.
	 *
	 * @param savedInstanceState Previous state
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		mSelectedPositions = savedInstanceState.getIntegerArrayList(TAG);
	}

	/*---------------*/
	/* FAST SCROLLER */
	/*---------------*/

	/**
	 * Displays or Hides the {@link FastScroller} if previously configured.
	 *
	 * @see #setFastScroller(FastScroller, int)
	 */
	public void toggleFastScroller() {
		if (mFastScroller != null) {
			if (mFastScroller.getVisibility() != View.VISIBLE)
				mFastScroller.setVisibility(View.VISIBLE);
			else mFastScroller.setVisibility(View.GONE);
		}
	}

	/**
	 * @return true if {@link FastScroller} is configured and shown, false otherwise
	 */
	public boolean isFastScrollerEnabled() {
		return mFastScroller != null && mFastScroller.getVisibility() == View.VISIBLE;
	}

	public FastScroller getFastScroller() {
		return mFastScroller;
	}

	/**
	 * Sets up the {@link FastScroller} with automatic fetch of accent color.
	 * <p><b>IMPORTANT:</b> Call this method after the adapter is added to the RecyclerView.</p>
	 * <b>NOTE:</b> If the device has at least Lollipop, the Accent color is fetched, otherwise
	 * for previous version, the default value is used.
	 *
	 * @param fastScroller instance of {@link FastScroller}
	 * @param accentColor  the default value color if the accentColor cannot be fetched
	 */
	public void setFastScroller(@NonNull FastScroller fastScroller, int accentColor) {
		if (mRecyclerView == null) {
			throw new IllegalStateException("RecyclerView cannot be null. Setup FastScroller after the Adapter is added to the RecyclerView.");
		}
		mFastScroller = fastScroller;
		mFastScroller.setRecyclerView(mRecyclerView);
		accentColor = Utils.fetchAccentColor(fastScroller.getContext(), accentColor);
		mFastScroller.setViewsToUse(R.layout.fast_scroller, R.id.fast_scroller_bubble, R.id.fast_scroller_handle, accentColor);
	}

	@Override
	public String onCreateBubbleText(int position) {
		return String.valueOf(position + 1);
	}

	@Override
	public void onFastScrollerStateChange(boolean scrolling) {
		//nothing
	}

}