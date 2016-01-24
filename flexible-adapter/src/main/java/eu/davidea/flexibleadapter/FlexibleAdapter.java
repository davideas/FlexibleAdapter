package eu.davidea.flexibleadapter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;

/**
 * This class provides a set of standard methods to handle changes on the data set
 * such as adding, removing, moving an item.
 * <p/>
 * <strong>VH</strong> is your implementation of {@link RecyclerView.ViewHolder}.
 * <strong>T</strong> is your domain object containing the data.
 *
 * @author Davide Steduto
 * @since 03/05/2015 Created
 */
public abstract class FlexibleAdapter<VH extends RecyclerView.ViewHolder, T> extends SelectableAdapter<VH>
		implements ItemTouchHelperCallback.AdapterCallback {

	private static final String TAG = FlexibleAdapter.class.getSimpleName();
	public static final long UNDO_TIMEOUT = 5000L;

	/**
	 * Lock object used to modify the content of {@link #mItems}.
	 * Any write operation performed on the list items should be synchronized on this lock.
	 */
	protected final Object mLock = new Object();
	protected boolean isAdapterRunning;

	protected List<T> mItems;
	protected List<T> mDeletedItems;
	protected List<Integer> mOriginalPositions;
	protected SparseArray<T> mRemovedItems;//beta test
	protected String mSearchText;
	protected Handler mHandler;
	protected OnUpdateListener mUpdateListener;

	//Drag&Drop and dismiss-on-swipe
	private ItemTouchHelperCallback mItemTouchHelperCallback;
	private ItemTouchHelper mItemTouchHelper;
	private boolean mHandleDragEnabled = true;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	/**
	 * Simple Constructor.
	 *
	 * @param items items to display.
	 */
	public FlexibleAdapter(@NonNull List<T> items) {
		this(items, null);
	}

	/**
	 * Main Constructor.
	 *
	 * @param items    items to display
	 * @param listener must be an instance of {@link OnUpdateListener}
	 */
	public FlexibleAdapter(@NonNull List<T> items, @Nullable Object listener) {
		mItems = items;

		if (listener instanceof OnUpdateListener) {
			mUpdateListener = (OnUpdateListener) listener;
			mUpdateListener.onUpdateEmptyView(mItems.size());
		}
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	/**
	 * Convenience method of {@link #updateDataSet(String)} with {@link null} as param.
	 */
	public void updateDataSet() {
		updateDataSet(null);
	}

	/**
	 * This method will refresh the entire DataSet content.<br/>
	 * The parameter is useful to filter the type of the DataSet.<br/>
	 * Pass null value in case not used.
	 *
	 * @param param A custom parameter to filter the type of the DataSet
	 */
	public abstract void updateDataSet(String param);

	/**
	 * Returns the custom object "Item".
	 *
	 * @param position the position of the item in the list
	 * @return The custom "Item" object or null if item not found
	 */
	public T getItem(int position) {
		if (position < 0 || position >= mItems.size()) return null;
		return mItems.get(position);
	}

	/**
	 * Retrieve the position of the Item in the Adapter
	 *
	 * @param item the item
	 * @return The position in the Adapter if found, -1 otherwise
	 */
	public int getPositionForItem(@NonNull T item) {
		return mItems != null && mItems.size() > 0 ? mItems.indexOf(item) : -1;
	}

	public boolean contains(@NonNull T item) {
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

	public boolean isEmpty() {
		return getItemCount() == 0;
	}

	public void updateItem(int position, @NonNull T item) {
		if (position < 0) {
			Log.w(TAG, "Cannot updateItem on negative position");
			return;
		}
		synchronized (mLock) {
			mItems.set(position, item);
		}
		if (DEBUG) Log.v(TAG, "updateItem notifyItemChanged on position " + position);
		notifyItemChanged(position);
	}

	/**
	 * Insert the given Item at last position.
	 *
	 * @param item the item to add
	 */
	public void addItem(@NonNull T item) {
		if (DEBUG) Log.v(TAG, "addItem notifyItemInserted on last position");
		synchronized (mLock) {
			mItems.add(item);
		}
	}

	/**
	 * Insert the given Item at desired position or Add Item at last position.
	 *
	 * @param position position of the item to add
	 * @param item     the item to add
	 */
	public void addItem(int position, @NonNull T item) {
		if (position < 0) {
			Log.w(TAG, "Cannot addItem on negative position");
			return;
		}
		//Insert Item
		if (position < mItems.size()) {
			if (DEBUG) Log.v(TAG, "addItem notifyItemInserted on position " + position);
			synchronized (mLock) {
				mItems.add(position, item);
			}
		} else { //Add Item at the last position
			addItem(item);
			position = mItems.size();
		}
		notifyItemInserted(position);
		if (mUpdateListener != null) mUpdateListener.onUpdateEmptyView(mItems.size());
	}

	/*----------------------*/
	/* DELETE ITEMS METHODS */
	/*----------------------*/

	/**
	 * The item is retained in a list for an eventual Undo.
	 *
	 * @param position the position of item to remove
	 * @see #startUndoTimer(long, OnDeleteCompleteListener)
	 * @see #restoreDeletedItems()
	 * @see #emptyBin()
	 */
	public void removeItem(int position) {
		if (position < 0 && position >= mItems.size()) {
			Log.w(TAG, "Cannot removeItem on position out of OutOfBound!");
			return;
		}
		if (DEBUG) Log.v(TAG, "removeItem notifyItemRemoved on position " + position);
		synchronized (mLock) {
			saveDeletedItem(position, mItems.remove(position));
		}
		notifyItemRemoved(position);
		if (mUpdateListener != null && !isAdapterRunning)
			mUpdateListener.onUpdateEmptyView(mItems.size());
	}

	/**
	 * Every item is retained in a list for an eventual Undo.
	 *
	 * @param selectedPositions list of item positions to remove
	 * @see #startUndoTimer(OnDeleteCompleteListener)
	 * @see #restoreDeletedItems()
	 */
	public void removeItems(List<Integer> selectedPositions) {
		if (DEBUG) Log.v(TAG, "removeItems reverse Sorting positions");
		// Reverse-sort the list
		Collections.sort(selectedPositions, new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs) {
				return rhs - lhs;
			}
		});

		// Split the list in ranges
		while (!selectedPositions.isEmpty()) {
			isAdapterRunning = true;
			if (selectedPositions.size() == 1) {
				removeItem(selectedPositions.get(0));
				//Align the selection list when removing the item
				selectedPositions.remove(0);
			} else {
				if (DEBUG) Log.v(TAG, "removeItems current selection " + getSelectedPositions());
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
		}
		isAdapterRunning = false;
		if (mUpdateListener != null) mUpdateListener.onUpdateEmptyView(mItems.size());
	}

	public void removeRange(int positionStart, int itemCount) {
		if (DEBUG)
			Log.v(TAG, "removeRange positionStart=" + positionStart + " itemCount=" + itemCount);
		for (int i = 0; i < itemCount; ++i) {
			synchronized (mLock) {
				saveDeletedItem(positionStart, mItems.remove(positionStart));
			}
		}
		if (DEBUG) Log.v(TAG, "removeRange notifyItemRangeRemoved");
		notifyItemRangeRemoved(positionStart, itemCount);
	}

	/**
	 * Convenience method to remove all Items that are currently selected.<br/>
	 *
	 * @see #removeItems(List)
	 */
	public void removeAllSelectedItems() {
		removeItems(getSelectedPositions());
	}

	/*--------------*/
	/* UNDO METHODS */
	/*--------------*/

	/**
	 * Save temporary Items for an eventual Undo.
	 *
	 * @param position the position of the item to retain.
	 */
	public void saveDeletedItem(int position, @NonNull T item) {
		if (mDeletedItems == null) {
			mDeletedItems = new ArrayList<T>();
			mOriginalPositions = new ArrayList<Integer>();
			mRemovedItems = new SparseArray<T>();
		}
		if (DEBUG) Log.v(TAG, "Recycled " + item + " on position=" + position);
		mDeletedItems.add(item);
		mOriginalPositions.add(position);
		mRemovedItems.put(position, item);
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
	 * Restore items just removed.<br/>
	 * <b>NOTE:</b> If filter is active, only items that match that filter will be shown(restored).
	 */
	public void restoreDeletedItems() {
		stopUndoTimer();
		//Reverse insert (list was reverse ordered on Delete)
		for (int i = mOriginalPositions.size() - 1; i >= 0; i--) {
			T item = mDeletedItems.get(i);
			T item2 = mRemovedItems.get(mRemovedItems.keyAt(i));
			if (DEBUG) Log.d(TAG, "Restoring item " + item + " on position " + mOriginalPositions.get(i));
			if (DEBUG) Log.d(TAG, "Restoring item2 " + item2 + " on position " + mRemovedItems.keyAt(i));
			//Avoid to restore(show) Items not filtered by the current filter
			if (hasSearchText() && !filterObject(item, getSearchText()))
				continue;
			addItem(mOriginalPositions.get(i), item);
		}
		emptyBin();
	}

	/**
	 * Clean memory from items just removed.<br/>
	 * <b>Note:</b> This method is automatically called after timer is over and after a restoration.
	 */
	public synchronized void emptyBin() {
		if (mDeletedItems != null) {
			mDeletedItems.clear();
			mOriginalPositions.clear();
			mRemovedItems.clear();
		}
	}

	/**
	 * Convenience method to start Undo timer with default timeout of 5''
	 *
	 * @param listener delete listener called after timeout
	 */
	public void startUndoTimer(OnDeleteCompleteListener listener) {
		startUndoTimer(0, listener);
	}

	/**
	 * Start Undo timer with custom timeout
	 *
	 * @param listener delete listener called after timeout
	 * @param timeout  custom timeout
	 */
	public void startUndoTimer(long timeout, final OnDeleteCompleteListener listener) {
		mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
			public boolean handleMessage(Message message) {
				if (listener != null) listener.onDeleteConfirmed();
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
	protected void stopUndoTimer() {
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
	}

	/*----------------*/
	/* FILTER METHODS */
	/*----------------*/

	public boolean hasSearchText() {
		return mSearchText != null && mSearchText.length() > 0;
	}

	public String getSearchText() {
		return mSearchText;
	}

	public void setSearchText(String searchText) {
		if (searchText != null)
			mSearchText = searchText.trim().toLowerCase(Locale.getDefault());
		else mSearchText = "";
	}

	/**
	 * Filter the provided list with the search text previously set
	 * with {@link #setSearchText(String)}.
	 * <br/><br/>
	 * <b>Note: </b>
	 * <br/>- This method calls {@link #filterObject(T, String)}.
	 * <br/>- If search text is empty or null, the provided list is the current list.
	 * <br/>- Any pending deleted items are always filtered out.
	 * <br/>- Original positions of deleted items are recalculated.
	 *
	 * @param unfilteredItems the list to filter
	 * @see #filterObject(Object, String)
	 */
	protected synchronized void filterItems(@NonNull List<T> unfilteredItems) {
		// NOTE: In case user has deleted some items and he changes or applies a filter while
		// deletion is pending (Undo started), in order to be consistent, we need to recalculate
		// the new position in the new list and finally skip those items to avoid they are shown!
		if (hasSearchText()) {
			mItems = new ArrayList<T>(); //with filter
			int newOriginalPosition = -1, oldOriginalPosition = -1;
			for (T item : unfilteredItems) {
				if (filterObject(item, getSearchText())) {
					if (mDeletedItems != null && mDeletedItems.contains(item)) {
						int index = mDeletedItems.indexOf(item);
						//Calculate new original position: skip counting position if item was deleted in range
						if (mOriginalPositions.get(index) != oldOriginalPosition) {
							newOriginalPosition++;
							oldOriginalPosition = mOriginalPositions.get(index);
						}
						mOriginalPositions.set(index, newOriginalPosition + mItems.size());
					} else {
						mItems.add(item);
					}
				}
			}
		} else {
			mItems = unfilteredItems; //with no filter
			if (mDeletedItems != null && !mDeletedItems.isEmpty()) {
				mOriginalPositions = new ArrayList<Integer>(mDeletedItems.size());
				for (T item : mDeletedItems) {
					mOriginalPositions.add(mItems.indexOf(item));
				}
				mItems.removeAll(mDeletedItems);
			}
		}
	}

	/**
	 * This method performs filtering on the provided object and returns true, if the object
	 * should be in the filtered collection, or false if it shouldn't.
	 * <br/><br/>
	 * DEFAULT IMPLEMENTATION, OVERRIDE TO HAVE OWN FILTER!
	 *
	 * @param myObject   the object to be inspected
	 * @param constraint constraint, that the object has to fulfil
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

	/*---------------*/
	/* TOUCH METHODS */
	/*---------------*/

	private void initializeItemTouchHelper () {
		if (mItemTouchHelper == null) {
			if (mRecyclerView == null) {
				throw new IllegalStateException("RecyclerView cannot be null. Call this method after the Adapter is added to the RecyclerView.");
			}
			mItemTouchHelperCallback = new ItemTouchHelperCallback(this);
			mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
			mItemTouchHelper.attachToRecyclerView(mRecyclerView);
		}
	}

	public ItemTouchHelper getItemTouchHelper() {
		return mItemTouchHelper;
	}

	public void setLongPressDragEnabled(boolean longPressDragEnabled) {
		initializeItemTouchHelper();
		mItemTouchHelperCallback.setLongPressDragEnabled(longPressDragEnabled);
		mHandleDragEnabled = !longPressDragEnabled;
	}

	public boolean isLongPressDragEnabled() {
		return mItemTouchHelperCallback.isLongPressDragEnabled();
	}

	public boolean isHandleDragEnabled() {
		return mItemTouchHelperCallback.isLongPressDragEnabled();
	}

	public void setSwipeEnabled(boolean swipeEnabled) {
		initializeItemTouchHelper();
		mItemTouchHelperCallback.setSwipeEnabled(swipeEnabled);
	}

	public boolean isSwipeEnabled() {
		return mItemTouchHelperCallback.isItemViewSwipeEnabled();
	}

	public void moveItem(int fromPosition, int toPosition) {
		if (DEBUG) Log.v(TAG, "moveItem from=" + fromPosition + " to=" + toPosition);
		if (DEBUG) Log.v(TAG, "moveItem fromItem=" + getItem(fromPosition) + " toItem=" + getItem(toPosition));
		Collections.swap(mItems, fromPosition, toPosition);

		//TODO: toggleSelection??
		if (!isSelected(fromPosition) && isSelected(toPosition)) {
			toggleSelection(toPosition);
		} else if (isSelected(fromPosition) && !isSelected(toPosition)) {
			toggleSelection(fromPosition);
		}

		notifyItemMoved(fromPosition, toPosition);
	}

	@Override
	public boolean shouldMove(int fromPosition, int toPosition) {
		return true;
	}

	@Override
	public boolean onItemMove(int fromPosition, int toPosition) {
		moveItem(fromPosition, toPosition);
		return true;
	}

	@Override
	public void onItemSwiped(int position, int direction) {

	}

	/*------------------*/
	/* INNER INTERFACES */
	/*------------------*/

	public interface OnUpdateListener {
		/**
		 * Called at startup and every time an item is inserted or removed.
		 */
		void onUpdateEmptyView(int size);
	}

	public interface OnDeleteCompleteListener {

		/**
		 * Due to Java Generic, it's too complicated and not
		 * well manageable if we pass the List&lt;T&gt; object.<br/>
		 * To get deleted items, use {@link #getDeletedItems()} from the
		 * implementation of this method.
		 */
		void onDeleteConfirmed();
	}

}