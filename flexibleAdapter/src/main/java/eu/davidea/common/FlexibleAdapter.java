package eu.davidea.common;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
 * <strong>VH</strong> is your implementation of {@link RecyclerView.ViewHolder}.
 * <strong>T</strong> is your domain object containing the data.
 * 
 * @author Davide Steduto
 */
public abstract class FlexibleAdapter<VH extends RecyclerView.ViewHolder, T>
		extends SelectableAdapter<VH>
		implements Filterable {

	private static final String TAG = FlexibleAdapter.class.getSimpleName();
	public static final long UNDO_TIMEOUT = 5000L;

	/**
	 * Lock used to modify the content of {@link #mItems}. Any write operation performed on the array should be
	 * synchronized on this lock. This lock is also used by the filter (see {@link #getFilter()} to make a synchronized
	 * copy of the original array of data.
	 */
	private final Object mLock = new Object();

	protected List<T> mItems;
	protected List<T> mDeletedItems;
	protected List<Integer> mOriginalPosition;
	private Timer mUndoTimer;
	//Searchable fields
	protected static String mSearchText; //Static: It can exist only 1 searchText
	private FlexibleFilter mFilter;
	//A copy of the original mObjects array, initialized from and then used instead as soon as
	// the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
	private ArrayList<T> mOriginalValues;


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
	
	/**
	 * Returns the custom object "Item".
	 * @param position
	 * @return the custom "Item" object
	 */
	public T getItem(int position) {
		return mItems.get(position);
	}
	
	/**
	 * Retrieve the position of the Item in the Adapter
	 *
	 * @param item
	 * @return the position in the Adapter if found, -1 otherwise
	 */
	public int getPositionForItem(T item) {
		return mItems != null && mItems.size() > 0 ? mItems.indexOf(item) : -1;
	}


	private int getOriginalPositionForItem(T item) {
		return mOriginalValues != null && mOriginalValues.size() > 0 ? mOriginalValues.indexOf(item) : -1;
	}

	public boolean contains(T item) {
		if (mOriginalValues != null) {
			return mOriginalValues.contains(item);
		} else {
			return mItems != null && mItems.contains(item);
		}
	}
	
	@Override
	public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);
	
	@Override
	public abstract void onBindViewHolder(VH holder, final int position);

	@Override
	public int getItemCount() {
		return mItems.size();
	}
	
	public void updateItem(int position, T item) {
		if (position < 0) return;
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.set(getOriginalPositionForItem(item), item);
			}
			mItems.set(position, item);
		}
		Log.d(TAG, "updateItem notifyItemChanged on position "+position);
		notifyItemChanged(position);
	}

	/**
	 * Insert given Item at position or Add Item at last position.
	 *
	 * @param position
	 * @param item
	 */
	public void addItem(int position, T item) {
		if (position < 0) return;
		//Insert Item
		if (position < mItems.size()) {
			Log.d(TAG, "addItem notifyItemInserted on position " + position);
			synchronized (mLock) {
				if (mOriginalValues != null) {
					mOriginalValues.add(position, item);
				}
				mItems.add(position, item);
			}

		//Add Item at the last position
		} else {
			Log.d(TAG, "addItem notifyItemInserted on last position");
			synchronized (mLock) {
				if (mOriginalValues != null) {
					mOriginalValues.add(item);
				}
				mItems.add(item);
				position = mItems.size();
			}
		}

		notifyItemInserted(position);
	}

	/* DELETE ITEMS METHODS */
	
	public void removeItem(int position) {
		if (position < 0) return;
		if (position < mItems.size()) {
			Log.d(TAG, "removeItem notifyItemRemoved on position " + position);
			synchronized (mLock) {
				saveDeletedItem(position);
				if (mOriginalValues != null) {
					mOriginalValues.remove(getOriginalPositionForItem(mItems.get(position)));
				}
				mItems.remove(position);
			}
			notifyItemRemoved(position);
		} else {
			Log.w(TAG, "removeItem WARNING! Position OutOfBound! Review the position to remove!");
		}
	}
	
	public void removeItems(List<Integer> selectedPositions) {
		Log.d(TAG, "removeItems reverse Sorting positions --------------");
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
			synchronized (mLock) {
				saveDeletedItem(positionStart);
				if (mOriginalValues != null) {
					mOriginalValues.remove(getOriginalPositionForItem(mItems.get(positionStart)));
				}
				mItems.remove(positionStart);
			}
		}
		Log.d(TAG, "removeRange notifyItemRangeRemoved");
		notifyItemRangeRemoved(positionStart, itemCount);
	}

	/* UNDO METHODS */

	/**
	 * Save temporary Items for an eventual Undo.
	 *
	 * @param position
	 */
	private void saveDeletedItem(int position) {
		if (mDeletedItems == null) {
			mDeletedItems = new ArrayList<T>();
			mOriginalPosition = new ArrayList<Integer>();
		}
		Log.d(TAG, "Recycled "+getItem(position)+" on position="+position);
		mDeletedItems.add(mItems.get(position));
		mOriginalPosition.add(position);
	}

	/**
	 * Restore items just removed.
	 */
	public void restoreDeletedItems() {
		stopUndoTimer();
		//Reverse insert (list was reverse ordered on Delete)
		for (int i = mOriginalPosition.size()-1; i >= 0; i--) {
			addItem(mOriginalPosition.get(i), mDeletedItems.get(i));
		}
		emptyBin();
	}

	/**
	 * Clean memory.
	 * <br/><b>Note:</b> This method is automatically called after timer is over and after a restoration.
	 */
	public void emptyBin() {
		mDeletedItems = null;
		mOriginalPosition = null;
	}

	/**
	 * Convenience method to start Undo timer with default timeout of 5''
	 */
	public void startUndoTimer() {
		startUndoTimer(0);
	}

	/**
	 * Start Undo timer with custom timeout
	 *
	 * @param timeout
	 */
	public void startUndoTimer(long timeout) {
		stopUndoTimer();
		this.mUndoTimer = new Timer();
		this.mUndoTimer.schedule(new UndoTimer(), timeout > 0 ? timeout : UNDO_TIMEOUT);
	}

	/**
	 * Stop Undo timer.
	 * <br/><b>Note:</b> This method is automatically called in case of restoration.
	 */
	private void stopUndoTimer() {
		if (this.mUndoTimer != null) {
			this.mUndoTimer.cancel();
			this.mUndoTimer = null;
		}
	}

	/**
	 * Inner class for TimerTask.
	 */
	private class UndoTimer extends TimerTask {
		public void run() {
			mUndoTimer = null;
			emptyBin();
		}
	}

	/* SEARCH-FILTER */

	public static String getSearchText() {
		return mSearchText != null ? mSearchText : "";
	}

	public static void setSearchText(String searchText) {
		if (searchText != null)
			mSearchText = searchText.trim().toLowerCase(Locale.getDefault());
		else mSearchText = "";
	}

	public static boolean hasSearchText() {
		return mSearchText != null && mSearchText.length() > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new FlexibleFilter();
		}
		return mFilter;
	}

	/**
	 * DEFAULT IMPLEMENTATION, OVERRIDE TO HAVE OWN FILTER!
	 *
	 * <br/><br/>Performs filtering on the provided object and returns true, if the object should be in the filtered collection,
	 * or false if it shouldn't.
	 *
	 * @param myObject The object to be inspected
	 * @param constraint Constraint, that the object has to fulfil
	 * @return true, if the object should be in the filteredResult, false otherwise
	 */
	protected boolean filterObject(T myObject, String constraint) {
		final String valueText = myObject.toString().toLowerCase();

		//First match against the whole, non-splitted value
		if (valueText.startsWith(constraint)) {
			return true;
		} else {
			final String[] words = valueText.split(" ");
			final int wordCount = words.length;

			//Start at index 0, in case valueText starts with space(s)
			for (int k = 0; k < wordCount; k++) {
				if (words[k].startsWith(constraint)) {
					return true;
				}
			}
		}

		//No match, so don't add to collection
		return false;
	}


	private class FlexibleFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			final FilterResults results = new FilterResults();

			//Save Original objects
			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<T>(mItems);
				}
			}

			//Prepare base list for each search
			List<T> values;
			synchronized (mLock) {
				values = new ArrayList<T>(mOriginalValues);
			}

			if (prefix == null || prefix.length() == 0) {
				//Restore original objects when filter is empty
				results.values = values;
				results.count = values.size();
				//This is extremely important otherwise it continues
				// to add/remove from OriginalValues
				mOriginalValues = null;

			} else { //FilterIn the objects
				String prefixString = prefix.toString().toLowerCase();

				final int count = values.size();
				final ArrayList<T> newValues = new ArrayList<T>();

				for (int i = 0; i < count; i++) {
					final T value = values.get(i);
					if (filterObject(value, prefixString)) {
						newValues.add(value);
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			Log.d(TAG, "performFiltering Size="+results.count+ " values="+results.values);
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			//noinspection unchecked
			mItems = (List<T>) results.values;
			notifyDataSetChanged();
		}
	}

}