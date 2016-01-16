package eu.davidea.flexibleadapter;

import android.os.Bundle;
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

	private ArrayList<Integer> selectedItems;
	private int mode;
	private FastScroller fastScroller;

	public SelectableAdapter() {
		this.selectedItems = new ArrayList<Integer>();
		this.mode = MODE_SINGLE;
	}

	/**
	 * Call this once to enable or disable DEBUG logs.<br/>
	 * DEBUG logs are disabled by default.
	 *
	 * @param enable true to show DEBUG logs, false to hide them.
	 */
	public static void enableLogs(boolean enable) {
		DEBUG = enable;
	}

	/* MAIN METHODS */

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
		return selectedItems.contains(Integer.valueOf(position));
	}

	/**
	 * Convenience method to never invalidate the Item.
	 *
	 * @param position Position of the item to toggle the selection status for.
	 * @see #toggleSelection(int, boolean)
	 */
	public void toggleSelection(int position) {
		toggleSelection(position, false);
	}

	/**
	 * Toggle the selection status of the item at a given position.<br/>
	 * The behaviour depends on the selection mode previously set with {@link #setMode}.
	 * <p/>
	 * Optionally the item can be invalidated.<br/>
	 * However it is preferable to set <i>false</i> and to handle the Activated/Selected State of
	 * the ItemView in the Click events of the ViewHolder after the selection is registered and
	 * up to date: Very Useful if the item has views with own animation to perform!
	 * <p/>
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
	 * @param position   Position of the item to toggle the selection status for.
	 * @param invalidate Boolean to indicate if the row must be invalidated and item rebound.
	 */
	public void toggleSelection(int position, boolean invalidate) {
		if (position < 0) return;
		if (mode == MODE_SINGLE) clearSelection();

		int index = selectedItems.indexOf(position);
		if (index != -1) {
			if (DEBUG) Log.v(TAG, "toggleSelection removing selection on position " + position);
			selectedItems.remove(index);
		} else {
			if (DEBUG) Log.v(TAG, "toggleSelection adding selection on position " + position);
			selectedItems.add(position);
		}
		if (invalidate) {
			if (DEBUG) Log.v(TAG, "toggleSelection notifyItemChanged on position " + position);
			notifyItemChanged(position);
		}
		if (DEBUG) Log.v(TAG, "toggleSelection current selection " + selectedItems);
	}

	/**
	 * Convenience method when there is no specific view to skip.
	 */
	public void selectAll() {
		selectAll(-1);
	}

	/**
	 * Add the selection status for all items.<br/>
	 * The selector container is sequentially filled with All items positions.
	 * <br/><b>Note:</b> All items are invalidated and rebound one by one!
	 *
	 * @param skipViewType ViewType for which we don't want selection
	 */
	public void selectAll(int skipViewType) {
		if (DEBUG) Log.v(TAG, "selectAll");
		selectedItems = new ArrayList<Integer>(getItemCount());
		for (int i = 0; i < getItemCount(); i++) {
			if (getItemViewType(i) == skipViewType) continue;
			selectedItems.add(i);
		}
		if (DEBUG)
			Log.v(TAG, "selectAll notifyItemRangeChanged from positionStart=0 itemCount=" + getItemCount());
		notifyItemRangeChanged(0, getItemCount());
	}

	/**
	 * Clear the selection status for all items one by one and it doesn't stop animations in the items.
	 * <p/>
	 * <b>Note 1:</b> Items are invalidated and rebound!<br/>
	 * <b>Note 2:</b> This method use java.util.Iterator to avoid java.util.ConcurrentModificationException.
	 */
	public void clearSelection() {
		Collections.sort(selectedItems, new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs) {
				return lhs - rhs;
			}
		});
		if (DEBUG) Log.v(TAG, "clearSelection current selection " + selectedItems);
		Iterator<Integer> iterator = selectedItems.iterator();
		int positionStart = 0, itemCount = 0;
		//The notification is done only on items that are currently selected.
		while (iterator.hasNext()) {
			int position = iterator.next();
			iterator.remove();
			//Optimization for ItemRangeChanged
			if (positionStart + itemCount == position) {
				itemCount++;
			} else {
				clearSelection(positionStart, itemCount);
				itemCount = 1;
				positionStart = position;
			}
		}
		//Notify remaining Items
		clearSelection(positionStart, itemCount);
	}

	private void clearSelection(int positionStart, int itemCount) {
		if (itemCount > 0) {
			if (DEBUG) Log.v(TAG, "clearSelection notifyItemRangeChanged from positionStart=" +
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
		return selectedItems.size();
	}

	/**
	 * Indicates the list of selected items.
	 *
	 * @return List of selected items ids
	 */
	public List<Integer> getSelectedItems() {
		return selectedItems;
	}

	/**
	 * Save the state of the current selection on the items.
	 *
	 * @param outState Current state
	 */
	public void onSaveInstanceState(Bundle outState) {
		outState.putIntegerArrayList(TAG, selectedItems);
	}

	/**
	 * Restore the previous state of the selection on the items.
	 *
	 * @param savedInstanceState Previous state
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		selectedItems = savedInstanceState.getIntegerArrayList(TAG);
	}

	/* FAST SCROLLER */

	public void setFastScroller(FastScroller fastScroller, RecyclerView recyclerView, int color) {
		this.fastScroller = fastScroller;
		fastScroller.setRecyclerView(recyclerView);
		fastScroller.setViewsToUse(R.layout.fast_scroller, R.id.fast_scroller_bubble, R.id.fast_scroller_handle, color);
	}

	@Override
	public String getTextToShowInBubble(int position) {
		return String.valueOf(position + 1);
	}

	@Override
	public void onFastScroll(boolean scrolling) {
		//nothing
	}

}