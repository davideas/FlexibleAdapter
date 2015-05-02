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

	private ArrayList<Integer> selectedItems;

	public SelectableAdapter() {
		selectedItems = new ArrayList<Integer>();
	}

	/**
	 * Indicates if the item at position position is selected
	 * @param position Position of the item to check
	 * @return true if the item is selected, false otherwise
	 */
	protected boolean isSelected(int position) {
		return selectedItems.contains(Integer.valueOf(position));
	}

	/**
	 * Toggle the selection status of the item at a given position
	 * @param position Position of the item to toggle the selection status for
	 */
	public void toggleSelection(int position) {
		Integer positionTapped = Integer.valueOf(position);
		int index = selectedItems.indexOf(positionTapped);
		if (index != -1) {
			Log.d(TAG, "toggleSelection removing selection on position "+position);
			selectedItems.remove(index);
		} else {
			Log.d(TAG, "toggleSelection adding selection on position "+position);
			selectedItems.add(positionTapped);
		}
		Log.d(TAG, "toggleSelection notifyItemChanged on position "+position);
		notifyItemChanged(position);
		Log.d(TAG, "toggleSelection current selection "+selectedItems);
	}
	
	/**
	 * This method remove the selection if at the specified
	 * position the item was previously selected.
	 * Note: notifyItemChanged on the position is NOT called!
	 *  This is useful when an item is mainly removed from the
	 *  implementation of the Adapter 
	 * @param position
	 */
	protected void removeSelection(int position) {
		Log.d(TAG, "removeSelection on position "+position);
		int index = selectedItems.indexOf(Integer.valueOf(position));
		if (index != -1) selectedItems.remove(index);
		//The notification is made outside this method
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
			selectedItems.add(Integer.valueOf(i));
			Log.d(TAG, "selectAll notifyItemChanged on position "+i);
			notifyItemChanged(i);
		}
		//TODO: Not sure about this call notifyDataSetChanged() when ALL items needs to be refreshed.
		//notifyDataSetChanged();
	}

	/**
	 * Clear the selection status for all items.
	 * If Adapter size match with the size of the selection list,
	 * then fast clear is performed.
	 */
	public void clearSelection() {
		if (selectedItems.size() == getItemCount()) {
			selectedItems.clear();
			//TODO: Not sure about this call notifyDataSetChanged() when ALL items needs to be refreshed.
			Log.d(TAG, "clearSelection notifyDataSetChanged on all position");
			notifyDataSetChanged();
		} else {
			Iterator<Integer> iterator = selectedItems.iterator();
			while (iterator.hasNext()) {
				//The notification is done only on items that are currently selected.
				//Clearing all items on selectedItems, we loose the position to notify.
				//TODO: To verify: Clearing all after the notification actually it does any effect because
				// the position called rely on the selectionItems that still contains that position
				int i = iterator.next();
				iterator.remove();
				Log.d(TAG, "clearSelection notifyItemChanged on position "+i);
				notifyItemChanged(i);
			}
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