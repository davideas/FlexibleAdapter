package eu.davidea.flexibleadapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

/**
 * This class provides a set of standard methods to handle changes on the data set
 * such as adding, removing, moving an item.
 * 
 * Remember to call {@link RecyclerView#scheduleLayoutAnimation()} after adding or
 * removing an item from the Adapter when the activity is paused.
 * 
 * <strong>VH</strong> is your implementation of {@link RecyclerView#ViewHolder}.
 * <strong>T</strong> is your domain object containing the data.
 * 
 * @author Davide Steduto
 */
public abstract class FlexibleAdapter<VH extends RecyclerView.ViewHolder, T> extends SelectableAdapter<VH> {
	
	private static final String TAG = FlexibleAdapter.class.getSimpleName();

	protected List<T> mItems;
	//TODO: Handling the undo functionality

	public FlexibleAdapter() {
		
	}
	
	/**
	 * This method will refresh the entire DataSet content.<br/>
	 * The parameter is useful to filter the DataSet. It can be removed
	 * or the type can be changed accordingly (String is the most used value).
	 * <br/>Pass null value in case not used.
	 * 
	 * @param param A custom parameter to filter the DataSet
	 */
	public abstract void updateDataSet(String param);
	
	public abstract T getItem(int position);
	
	public int getPositionForItem(T item) {
		if (mItems != null && mItems.size() > 0) return mItems.indexOf(item);
		return -1;
	}
	
	public boolean contains(T item) {
		if (mItems != null) return mItems.contains(item);
		return false;
	}
	
	@Override
	public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);
	
	@Override
	public abstract void onBindViewHolder(VH holder, final int position);

	@Override
	public int getItemCount() {
		return mItems.size();
	}
	
	public void addItem(int position, T item) {
		mItems.add(position, item);
		Log.d(TAG, "addItem notifyItemInserted on position "+position);
		notifyItemInserted(position);
	}
	
	private void removeItem(int position) {
		Log.d(TAG, "removeItem on position "+position);
		mItems.remove(position);
		Log.d(TAG, "removeItem notifyItemRemoved on position "+position);
		notifyItemRemoved(position);
	}
	
	public void removeItems(List<Integer> selectedPositions) {
		Log.d(TAG, "removeItems Sorting positions --------------");
		// Reverse-sort the list
		Collections.sort(selectedPositions, new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs) {
				return rhs - lhs;
			}
		});

		// Split the list in ranges
		while (!selectedPositions.isEmpty()) {
			if (selectedPositions.size() == 1) {
				removeItem(selectedPositions.get(0));
				//Align the selection list when removing the item
				selectedPositions.remove(0);
			} else {
				int count = 1;
				while (selectedPositions.size() > count && selectedPositions.get(count).equals(selectedPositions.get(count - 1) - 1)) {
					++count;
				}

				if (count == 1) {
					removeItem(selectedPositions.get(0));
				} else {
					removeRange(selectedPositions.get(count - 1), count);
				}

				for (int i = 0; i < count; ++i) {
					selectedPositions.remove(0);
				}
			}
			Log.d(TAG, "removeItems current selection " + getSelectedItems());
		}
	}

	private void removeRange(int positionStart, int itemCount) {
		Log.d(TAG, "removeRange positionStart="+positionStart+ " itemCount="+itemCount);
		for (int i = 0; i < itemCount; ++i) {
			mItems.remove(positionStart);
		}
		Log.d(TAG, "removeRange notifyItemRangeRemoved");
		notifyItemRangeRemoved(positionStart, itemCount);
	}
	
}