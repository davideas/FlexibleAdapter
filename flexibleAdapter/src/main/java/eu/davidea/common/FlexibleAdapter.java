package eu.davidea.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
public abstract class FlexibleAdapter<VH extends RecyclerView.ViewHolder, T> extends SelectableAdapter<VH> {
	
	private static final String TAG = FlexibleAdapter.class.getSimpleName();
	public static final long UNDO_TIMEOUT = 5000L;

	/**
	 * Lock used to modify the content of {@link #mItems}. Any write operation performed on the array should be
	 * synchronized on this lock.
	 */
	private final Object mLock = new Object();

	protected List<T> mItems;
	protected List<T> mDeletedItems;
	protected List<Integer> mOriginalPosition;
	private Timer mUndoTimer;

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

	public boolean contains(T item) {
		return mItems != null && mItems.contains(item);
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
				mItems.add(position, item);
			}

		//Add Item at the last position
		} else {
			Log.d(TAG, "addItem notifyItemInserted on last position");
			synchronized (mLock) {
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

}