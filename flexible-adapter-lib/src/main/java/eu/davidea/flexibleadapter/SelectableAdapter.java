package eu.davidea.flexibleadapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class provides a set of standard methods to handle the selection on the items of an Adapter.
 *
 * @author Davide Steduto
 */
public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

	private static final String TAG = SelectableAdapter.class.getSimpleName();
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

	public SelectableAdapter() {
		this.selectedItems = new ArrayList<Integer>();
		this.mode = MODE_SINGLE;
	}

	/**
	 * Set the mode of the selection, MODE_SINGLE is the default:
	 * <ul>
	 * <li>if {@link #MODE_SINGLE}, it will switch the selection position (previous selection is cleared automatically);
	 * <li>if {@link #MODE_MULTI}, it will add the position to the list of the items selected.
	 * </ul>
	 * <b>NOTE:</b> #mModeMultiJustFinished is set true when #MODE_MULTI is finished.
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
	 * Indicates if the item at position position is selected.
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
	 *
	 * <br/><br/>
	 * Optionally the item can be invalidated.<br/>
	 * However it is preferable to set <i>false</i> and to handle the Activated/Selected State of
	 * the ItemView in the Click events of the ViewHolder after the selection is registered and
	 * up to date: Very Useful if the item has views with own animation to perform!
	 *
	 * <br/><br/>
	 * <b>Usage:</b>
	 * <ul>
	 * <li>If you don't want any item to be selected/activated at all, just don't call this method.</li>
	 * <li>To have actually the item visually selected you need to add a custom <i>Selector Drawable</i> to your layout/view of the Item.
	 * It's preferable: <i>android:background="?attr/selectableItemBackground"</i> in your layout, pointing to a custom Drawable in the style.xml
	 * (note: prefix <i>?android:attr</i> seems to <u>not</u> work).</li>
	 * <li>In <i>onClick</i>, enable the Activated/Selected State of the ItemView of the ViewHolder <u>after</u> the listener consumed the event:
	 * <i>itemView.setActivated(mAdapter.isSelected(getAdapterPosition()));</i></li>
	 * <li>In <i>onBindViewHolder</i>, adjust the selection status: <i>holder.itemView.setActivated(isSelected(position));</i></li>
	 * <li>If <i>invalidate</i> is set true, {@link #notifyItemChanged} is called and {@link #onBindViewHolder} will be automatically called
	 * afterwards overriding any animation in the ItemView!</li>
	 *</ul>T
	 *
	 * @param position Position of the item to toggle the selection status for.
	 * @param invalidate Boolean to indicate if the row must be invalidated and item rebinded.
	 */
	public void toggleSelection(int position, boolean invalidate) {
		if (position < 0) return;
		if (mode == MODE_SINGLE) clearSelection();

		int index = selectedItems.indexOf(position);
		if (index != -1) {
			Log.d(TAG, "toggleSelection removing selection on position "+position);
			selectedItems.remove(index);
		} else {
			Log.d(TAG, "toggleSelection adding selection on position "+position);
			selectedItems.add(position);
		}
		if (invalidate) {
			Log.d(TAG, "toggleSelection notifyItemChanged on position "+position);
			notifyItemChanged(position);
		}
		Log.d(TAG, "toggleSelection current selection " + selectedItems);
	}

	/**
	 * Deprecated! Reasons:
	 * <br/>- Use {@link #toggleSelection} for normal situation instead.
	 * <br/>- Use {@link #getSelectedItems}.iterator() to avoid java.util.ConcurrentModificationException.
	 *
	 * <br/><br/>
	 * This method is used only after a removal of an Item <u>and</u> useful only in certain
	 * situations such when not all selected Items can be removed (business exceptions dependencies)
	 * while others still remains selected.
	 * <br/>For normal situations use {@link #toggleSelection} instead.
	 *
	 * <br/><br/>
	 * Remove the selection if at the specified
	 * position the item was previously selected.
	 *
	 * <br/><br/>
	 * <b>Note:</b> <i>notifyItemChanged</i> on the position is NOT called to avoid double call
	 * when removeItems!
	 *
	 * @param position
	 */
	@Deprecated
	protected void removeSelection(int position) {
		if (position < 0) return;
		Log.d(TAG, "removeSelection on position "+position);
		int index = selectedItems.indexOf(Integer.valueOf(position));
		if (index != -1) selectedItems.remove(index);
		//Avoid double notification:
		//Usually the notification is made in FlexibleAdapter.removeItem();
		//notifyItemChanged(position);
	}

	/**
	 * Convenience method when there is nothing to skip.
	 */
	public void selectAll() {
		selectAll(-1);
	}
	/**
	 * Add the selection status for all items.
	 * The selector container is sequentially filled with All items positions.
	 * <br/><b>Note:</b> All items are invalidated and rebinded!
	 *
	 * @param skipViewType ViewType for which we don't want selection
	 */
	public void selectAll(int skipViewType) {
		Log.d(TAG, "selectAll");
		selectedItems = new ArrayList<Integer>(getItemCount());
		for (int i = 0; i < getItemCount(); i++) {
			if (getItemViewType(i) == skipViewType) continue;
			selectedItems.add(i);
			Log.d(TAG, "selectAll notifyItemChanged on position "+i);
			notifyItemChanged(i);
		}
	}

	/**
	 * Clear the selection status for all items one by one to not kill animations in the items.
	 * <br/><br/>
	 * <b>Note 1:</b> Items are invalidated and rebinded!<br/>
	 * <b>Note 2:</b> This method use java.util.Iterator to avoid java.util.ConcurrentModificationException.
	 */
	public void clearSelection() {
		Iterator<Integer> iterator = selectedItems.iterator();
		while (iterator.hasNext()) {
			//The notification is done only on items that are currently selected.
			int i = iterator.next();
			iterator.remove();
			Log.d(TAG, "clearSelection notifyItemChanged on position "+i);
			notifyItemChanged(i);
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
	 * @param outState
	 */
	public void onSaveInstanceState(Bundle outState) {
		outState.putIntegerArrayList(TAG, selectedItems);
	}

	/**
	 * Restore the previous state of the selection on the items.
	 *
	 * @param savedInstanceState
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		selectedItems = savedInstanceState.getIntegerArrayList(TAG);
	}

}