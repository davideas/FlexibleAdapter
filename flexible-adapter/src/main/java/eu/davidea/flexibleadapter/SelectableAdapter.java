package eu.davidea.flexibleadapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import eu.davidea.fastscroller.FastScroller;

/**
 * This class provides a set of standard methods to handle the selection on the items of an Adapter.
 *
 * @author Davide Steduto
 */
public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
		implements FastScroller.ScrollerListener {

	private static final String TAG = SelectableAdapter.class.getSimpleName();
	protected static boolean DEBUG = false;

	/**
	 * Default mode for selection
	 */
	public static final int MODE_SINGLE = 1;
	/**
	 * Multi selection will be activated
	 */
	public static final int MODE_MULTI = 2;

	//TODO: Evaluate TreeSet instead of ArrayList for selectedPositions, TreeSet is a sortedList
	private ArrayList<Integer> selectedPositions;
	private int mode;
	protected RecyclerView mRecyclerView;
	protected FastScroller mFastScroller;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	public SelectableAdapter() {
		selectedPositions = new ArrayList<Integer>();
		mode = MODE_SINGLE;
	}

	/*----------------*/
	/* STATIC METHODS */
	/*----------------*/

	/**
	 * Call this once to enable or disable DEBUG logs.<br/>
	 * DEBUG logs are disabled by default.
	 *
	 * @param enable true to show DEBUG logs, false to hide them.
	 */
	public static void enableLogs(boolean enable) {
		DEBUG = enable;
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	/**
	 * Set the mode of the selection, MODE_SINGLE is the default:
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
		this.mode = mode;
	}

	/**
	 * The current selection mode of the Adapter.
	 *
	 * @return current mode
	 * @see #MODE_SINGLE
	 * @see #MODE_MULTI
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Indicates if the item, at the provided position, is selected.
	 *
	 * @param position Position of the item to check.
	 * @return true if the item is selected, false otherwise.
	 */
	public boolean isSelected(int position) {
		return selectedPositions.contains(Integer.valueOf(position));
	}

	/**
	 * Toggle the selection status of the item at a given position.<br/>
	 * The behaviour depends on the selection mode previously set with {@link #setMode}.
	 * <br/><br/>
	 * Optionally the item can be invalidated.<br/>
	 * However it is preferable to set <i>false</i> and to handle the Activated/Selected State of
	 * the ItemView in the Click events of the ViewHolder after the selection is registered and
	 * up to date: Very Useful if the item has views with own animation to perform!
	 * <br/><br/>
	 * <b>Usage:</b>
	 * <ul>
	 * <li>If you don't want any item to be selected/activated at all, just don't call this method.</li>
	 * <li>To have actually the item visually selected you need to add a custom <i>Selector Drawable</i>
	 * to your layout/view of the Item. It's preferable to set in your layout:
	 * <i>android:background="?attr/selectableItemBackground"</i>, pointing to a custom Drawable
	 * in the style.xml (note: prefix <i>?android:attr</i> <u>doesn't</u> work).</li>
	 * <li>In <i>onClick</i> event, enable the Activated/Selected State of the ItemView of the
	 * ViewHolder <u>after</u> the listener consumed the event:
	 * <i>itemView.setActivated(mAdapter.isSelected(getAdapterPosition()));</i></li>
	 * <li>In <i>onBindViewHolder</i>, adjust the selection status:
	 * <i>holder.itemView.setActivated(isSelected(position));</i></li>
	 * <li>If <i>invalidate</i> is set true, {@link #notifyItemChanged} is called and
	 * {@link #onBindViewHolder} will be automatically called afterwards overriding any animation
	 * inside the ItemView!</li>
	 * </ul>
	 *
	 * @param position Position of the item to toggle the selection status for.
	 */
	public void toggleSelection(int position) {
		if (position < 0) return;
		if (mode == MODE_SINGLE) clearSelection();

		int index = selectedPositions.indexOf(position);
		if (index != -1) {
			if (DEBUG) Log.v(TAG, "toggleSelection removing selection on position " + position);
			selectedPositions.remove(index);
		} else {
			if (DEBUG) Log.v(TAG, "toggleSelection adding selection on position " + position);
			selectedPositions.add(position);
		}
		if (DEBUG) Log.v(TAG, "toggleSelection current selection " + selectedPositions);
	}

	/**
	 * Convenience method when there is no specific view to skip.
	 */
	public void selectAll() {
		selectAll(-1000);
	}

	/**
	 * Set the selection status for all items which the ViewType is lower than specified param.
	 * <br/><b>Note:</b> All items are invalidated and rebound!
	 *
	 * @param skipViewTypes All ViewTypes for which we don't want selection
	 */
	public void selectAll(int skipViewTypes) {
		if (DEBUG) Log.v(TAG, "selectAll");
		selectedPositions = new ArrayList<Integer>(getItemCount());
		int positionStart = 0, itemCount = 0;
		for (int i = 0; i < getItemCount(); i++) {
			Log.v(TAG, "selectAll ViewType=" + getItemViewType(i) + " position=" + i);
			if (getItemViewType(i) >= skipViewTypes) {
				//Optimization for ItemRangeChanged
				if (positionStart + itemCount == i) {
					handleSelection(positionStart, itemCount);
					itemCount = 0;
					positionStart = i;
				}
				continue;
			}
			itemCount++;
			selectedPositions.add(i);
		}
		if (DEBUG)
			Log.v(TAG, "selectAll notifyItemRangeChanged from positionStart=" + positionStart + " itemCount=" + getItemCount());
		notifyItemRangeChanged(positionStart, getItemCount());
	}

	/**
	 * Clear the selection status for all items one by one and it doesn't stop animations in the items.
	 * <br/><br/>
	 * <b>Note 1:</b> Items are invalidated and rebound!<br/>
	 * <b>Note 2:</b> This method use java.util.Iterator to avoid java.util.ConcurrentModificationException.
	 */
	public void clearSelection() {
		Collections.sort(selectedPositions, new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs) {
				return lhs - rhs;
			}
		});
		if (DEBUG) Log.v(TAG, "clearSelection current selection " + selectedPositions);
		Iterator<Integer> iterator = selectedPositions.iterator();
		int positionStart = 0, itemCount = 0;
		//The notification is done only on items that are currently selected.
		while (iterator.hasNext()) {
			int position = iterator.next();
			iterator.remove();
			//Optimization for ItemRangeChanged
			if (positionStart + itemCount == position) {
				itemCount++;
			} else {
				//Notify previous range
				handleSelection(positionStart, itemCount);
				positionStart = position;
				itemCount = 1;
			}
		}
		//Notify remaining range
		handleSelection(positionStart, itemCount);
	}

	private void handleSelection(int positionStart, int itemCount) {
		if (itemCount > 0) {
			if (DEBUG) Log.v(TAG, "handleSelection notifyItemRangeChanged from positionStart=" +
					positionStart + " itemCount=" + itemCount);
			notifyItemRangeChanged(positionStart, itemCount);
		}
	}

	/**
	 * Count the selected items.
	 *
	 * @return Selected items count
	 */
	public int getSelectedItemCount() {
		return selectedPositions.size();
	}

	/**
	 * Indicates the list of selected items.
	 *
	 * @return List of selected items ids
	 */
	public List<Integer> getSelectedPositions() {
		return selectedPositions;
	}

	/*----------------*/
	/* INSTANCE STATE */
	/*----------------*/

	/**
	 * Save the state of the current selection on the items.
	 *
	 * @param outState Current state
	 */
	public void onSaveInstanceState(Bundle outState) {
		outState.putIntegerArrayList(TAG, selectedPositions);
	}

	/**
	 * Restore the previous state of the selection on the items.
	 *
	 * @param savedInstanceState Previous state
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		selectedPositions = savedInstanceState.getIntegerArrayList(TAG);
	}

	/*---------------*/
	/* FAST SCROLLER */
	/*---------------*/

	public FastScroller getFastScroller() {
		return mFastScroller;
	}

	/**
	 * Method under development. It will be public when ready.
	 *
	 * @param fastScroller Instance of {@link FastScroller}
	 */
	private void setFastScroller(@NonNull FastScroller fastScroller) {
		//TODO: Fetch accent color automatically if at least Android is Lollipop
		setFastScroller(fastScroller, 0);
	}

	/**
	 * <b>IMPORTANT:</b> Call this method after the adapter is added to the RecyclerView.
	 *
	 * @param fastScroller Instance of {@link FastScroller}
	 * @param accentColor  The color when the fast scroller is touched
	 */
	public void setFastScroller(@NonNull FastScroller fastScroller, int accentColor) {
		if (mRecyclerView == null) {
			throw new IllegalStateException("RecyclerView cannot be null. Call this method after the adapter is added to the RecyclerView.");
		}
		mFastScroller = fastScroller;
		mFastScroller.setRecyclerView(mRecyclerView);
		mFastScroller.setViewsToUse(R.layout.fast_scroller, R.id.fast_scroller_bubble, R.id.fast_scroller_handle, accentColor);
	}

	@Override
	public String getTextToShowInBubble(int position) {
		return String.valueOf(position + 1);
	}

	@Override
	public void onFastScroll(boolean scrolling) {
		//nothing
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		mRecyclerView = recyclerView;
	}

}