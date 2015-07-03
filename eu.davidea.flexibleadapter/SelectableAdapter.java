package eu.davidea.flexibleadapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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
	 * @param mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	/**
	 * The current selection mode of the Adapter
	 * @return current mode
	 * @see #MODE_SINGLE
	 * @see #MODE_MULTI
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Indicates if the item at position position is selected.
	 * @param position Position of the item to check.
	 * @return true if the item is selected, false otherwise.
	 */
	public boolean isSelected(int position) {
		return selectedItems.contains(Integer.valueOf(position));
	}

	/**
	 * Toggle the selection status of the item at a given position.<br/>
	 * The behaviour depends on the selection mode previously set with {@link #setMode}.
	 * 
	 * <br/><br/>
	 * <b>Note:</b>
	 * <ul>
	 * <li>If you don't want any item to be selected/activated at all, just don't call this method.</li>
	 * <li>To have actually the item visually selected you need to add a custom <i>Selector Drawable</i> to your layout/view of the Item.
	 * or to add <i>android:background="?attr/selectableItemBackground"</i> in your layout pointing to a custom Drawable in the style.xml</li>
	 * <li>{@link #notifyItemChanged} is called and {@link #onBindViewHolder} will be automatically called afterwards.</li>
	 *</ul>
	 * @param position Position of the item to toggle the selection status for.
	 */
	public void toggleSelection(int position) {
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
		Log.d(TAG, "toggleSelection notifyItemChanged on position "+position);
		notifyItemChanged(position);
		Log.d(TAG, "toggleSelection current selection "+selectedItems);
	}
	
	/**
	 * Deprecated. This method is actually never used. Use {@link #toggleSelection} instead.
	 * <br/><br/>
	 * Remove the selection if at the specified
	 * position the item was previously selected.<br/><br/>
	 * <b>Note:</b> <i>notifyItemChanged</i> on the position is NOT called!
	 *  This is useful when an item is mainly removed from the
	 *  implementation of the Adapter.
	 *  
	 * @param position
	 */
	@Deprecated
	protected void removeSelection(int position) {
		Log.d(TAG, "removeSelection on position "+position);
		int index = selectedItems.indexOf(Integer.valueOf(position));
		if (index != -1) selectedItems.remove(index);
		//Usually the notification is made in the caller of this method
		//notifyItemChanged(position);
	}
	
	/**
	 * Add the selection status for all items.
	 * The selector container is sequentially filled with All items positions.
	 */
	public void selectAll() {
		Log.d(TAG, "selectAll");
		selectedItems = new ArrayList<Integer>(getItemCount());
		for (int i = 0; i < getItemCount(); i++) {
			selectedItems.add(i);
			Log.d(TAG, "selectAll notifyItemChanged on position "+i);
			notifyItemChanged(i);
		}
	}

	/**
	 * Clear the selection status for all items one by one to not kill animations in the items
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
	 * Count the selected items
	 * @return Selected items count
	 */
	public int getSelectedItemCount() {
		return selectedItems.size();
	}

	/**
	 * Indicates the list of selected items
	 * @return List of selected items ids
	 */
	public List<Integer> getSelectedItems() {
		return selectedItems;
	}
	
	/**
	 * Save the state of the current selection on the items.
	 * @param outState
	 */
	public void onSaveInstanceState(Bundle outState) {
		outState.putIntegerArrayList(TAG, selectedItems);
	}
	
	/**
	 * Restore the previous state of the selection on the items.
	 * @param savedInstanceState
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		selectedItems = savedInstanceState.getIntegerArrayList(TAG);
	}
	
}
