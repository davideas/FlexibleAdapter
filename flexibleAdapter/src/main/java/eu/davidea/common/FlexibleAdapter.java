package eu.davidea.common;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

	public interface OnUpdateListener {
		void onLoadComplete();
		//void onProgressUpdate(int progress);
	}

	/**
	 * Lock used to modify the content of {@link #mItems}. Any write operation performed on the array should be
	 * synchronized on this lock.
	 */
	private final Object mLock = new Object();

	protected List<T> mItems;
	protected List<T> mDeletedItems;
	protected List<Integer> mOriginalPosition;
	//Searchable fields
	protected static String mSearchText; //Static: It can exist only 1 searchText
	protected OnUpdateListener mUpdateListener;
	protected Handler mHandler;

	public FlexibleAdapter() {
	}

	/**
	 * Constructor for Asynchronous loading.<br/>
	 * Experimental: not working very well, it might be slow.
	 *
	 * @param listener {@link OnUpdateListener}
	 */
	public FlexibleAdapter(Object listener) {
		if (listener instanceof OnUpdateListener)
			this.mUpdateListener = (OnUpdateListener) listener;
		else
			Log.w(TAG, "Listener is not an instance of OnUpdateListener!");
	}

	/**
	 * Convenience method to call {@link #updateDataSet(String)} with {@link null} as param.
	 */
	public void updateDataSet() {
		updateDataSet(null);
	};
	/**
	 * This method will refresh the entire DataSet content.<br/>
	 * The parameter is useful to filter the DataSet. It can be removed
	 * or the type can be changed accordingly (String is the most used value).<br/>
	 * Pass null value in case not used.
	 * 
	 * @param param A custom parameter to filter the DataSet
	 */
	public abstract void updateDataSet(String param);

	/**
	 * This method execute {@link #updateDataSet(String)} asynchronously
	 * with {@link FilterAsyncTask}.<br/><br/>
	 * <b>Note:</b> {@link #notifyDataSetChanged()} is automatically called at the end of the process.
	 *
	 * @param param A custom parameter to filter the DataSet
	 */
	public void updateDataSetAsync(String param) {
		if (mUpdateListener == null) {
			Log.w(TAG, "OnUpdateListener is not initialized. UpdateDataSet is not using FilterAsyncTask!");
			updateDataSet(param);
			return;
		}
		new FilterAsyncTask().execute(param);
	}
	
	/**
	 * Returns the custom object "Item".
	 *
	 * @param position The position of the item in the list
	 * @return The custom "Item" object or null if item not found
	 */
	public T getItem(int position) {
		if (position < 0 || position >= mItems.size()) return null;
		return mItems.get(position);
	}
	
	/**
	 * Retrieve the position of the Item in the Adapter
	 *
	 * @param item The item
	 * @return The position in the Adapter if found, -1 otherwise
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
		return mItems != null ? mItems.size() : 0;
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
	 * @param position Position of the item to add
	 * @param item The item to add
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

	/**
	 * The item is retained in a list for an eventual Undo.
	 *
	 * @param position The position of item to remove
	 * @see #startUndoTimer()
	 * @see #restoreDeletedItems()
	 * @see #emptyBin()
	 */
	public void removeItem(int position) {
		if (position < 0) return;
		if (position < mItems.size()) {
			Log.d(TAG, "removeItem notifyItemRemoved on position " + position);
			synchronized (mLock) {
				saveDeletedItem(position, mItems.remove(position));
			}
			notifyItemRemoved(position);
		} else {
			Log.w(TAG, "removeItem WARNING! Position OutOfBound! Review the position to remove!");
		}
	}

	/**
	 * Every item is retained in a list for an eventual Undo.
	 *
	 * @param selectedPositions List of item positions to remove
	 * @see #startUndoTimer()
	 * @see #restoreDeletedItems()
	 * @see #emptyBin()
	 */
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
				saveDeletedItem(positionStart, mItems.remove(positionStart));
			}
		}
		Log.d(TAG, "removeRange notifyItemRangeRemoved");
		notifyItemRangeRemoved(positionStart, itemCount);
	}

	/* UNDO METHODS */

	/**
	 * Save temporary Items for an eventual Undo.
	 *
	 * @param position The position of the item to retain.
	 */
	public void saveDeletedItem(int position, T item) {
		if (mDeletedItems == null) {
			mDeletedItems = new ArrayList<T>();
			mOriginalPosition = new ArrayList<Integer>();
		}
		Log.d(TAG, "Recycled "+getItem(position)+" on position="+position);
		mDeletedItems.add(item);
		mOriginalPosition.add(position);
	}

	/**
	 * @return The list of deleted items
	 */
	public List<T> getDeletedItems() {
		return mDeletedItems;
	}

	public boolean isRestoreInTime() {
		return mDeletedItems != null && mDeletedItems.size() > 0;
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
	 * Clean memory from items just removed.<br/>
	 * <b>Note:</b> This method is automatically called after timer is over and after a restoration.
	 */
	public void emptyBin() {
		if (mDeletedItems != null) {
			mDeletedItems.clear();
			mOriginalPosition.clear();
		}
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
	 * @param timeout Custom timeout
	 */
	public void startUndoTimer(long timeout) {
		mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
				public boolean handleMessage(Message message) {
					emptyBin();
					return true;
				}
			});
		mHandler.sendMessageDelayed(Message.obtain(mHandler), timeout > 0 ? timeout : UNDO_TIMEOUT);
	}

	/**
	 * Stop Undo timer.
	 * <br/><b>Note:</b> This method is automatically called in case of restoration.
	 */
	private void stopUndoTimer() {
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
	}

	public static boolean hasSearchText() {
		return mSearchText != null && mSearchText.length() > 0;
	}

	public static String getSearchText() {
		return mSearchText;
	}

	public static void setSearchText(String searchText) {
		if (searchText != null)
			mSearchText = searchText.trim().toLowerCase(Locale.getDefault());
		else mSearchText = "";
	}

	/**
	 * DEFAULT IMPLEMENTATION, OVERRIDE TO HAVE OWN FILTER!
	 *
	 * <br/><br/>
	 * Performs filtering on the provided object and returns true, if the object should be in the filtered collection,
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

			//Start at index 0, in case valueText starts with space(s)
			for (String word : words) {
				if (word.startsWith(constraint)) {
					return true;
				}
			}
		}
		//No match, so don't add to collection
		return false;
	}

	public class FilterAsyncTask extends AsyncTask<String, Void, Void> {

		private final String TAG = FilterAsyncTask.class.getSimpleName();

		@Override
		protected Void doInBackground(String... params) {
			Log.i(TAG, "doInBackground - started FilterAsyncTask!");
			updateDataSet(params[0]);
			Log.i(TAG, "doInBackground - ended FilterAsyncTask!");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mUpdateListener.onLoadComplete();
			notifyDataSetChanged();
		}
	}

}