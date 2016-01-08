package eu.davidea.flexibleadapter;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * This class provides a set of standard methods to handle changes on the data set
 * such as adding, removing, moving an item.
 * <p/>
 * Remember to call {@link RecyclerView#scheduleLayoutAnimation()} after adding or
 * removing an item from the Adapter when the activity is paused.
 * <p/>
 * <strong>VH</strong> is your implementation of {@link RecyclerView.ViewHolder}.
 * <strong>T</strong> is your domain object containing the data.
 *
 * @author Davide Steduto
 */
public abstract class FlexibleAdapter<VH extends RecyclerView.ViewHolder, T> extends SelectableAdapter<VH> {

	private static final String TAG = FlexibleAdapter.class.getSimpleName();
	public static final long UNDO_TIMEOUT = 5000L;

	/**
	 * Lock object used to modify the content of {@link #mItems}.
	 * Any write operation performed on the list items should be synchronized on this lock.
	 */
	private final Object mLock = new Object();
	private OnClickItemListener mOnItemClickListener;
	private OnLongClickItemListener mOnItemLongClickListener;
	private RecyclerView mRecyclerView;

	protected List<T> mItems;
	protected List<T> mDeletedItems;
	protected List<Integer> mOriginalPosition;
	protected String mSearchText;
	protected Handler mHandler;
	protected OnUpdateListener mUpdateListener;

	public FlexibleAdapter() {
	}

	public FlexibleAdapter(@NonNull List<T> items) {
		mItems = items;
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
	}
	
	/**
	 * Callback method to be invoked when an item in the RecyclerView
	 * has been clicked.
	 *
	 * @param dataItem (T) The model in item 
	 * @param position The position of the view in the list
	 * @param view The view within the RecyclerView that was clicked and held
	 */
	public interface OnClickItemListener {

        void onClicked(Object dataItem, int position, View view);
    }

	/**
	 * Callback method to be invoked when an item in the RecyclerView
	 * has been clicked and held.
	 *
	 * @param dataItem (T) The model in item 
	 * @param position The position of the view in the list
	 * @param view The view within the RecyclerView that was clicked and held
	 *
	 * @return true if the callback consumed the long click, false otherwise
	 */
    public interface OnLongClickItemListener {

        boolean onLongClicked(Object dataItem, int position, View view);
    }

	/**
     * Register a callback to be invoked when an item in the
     * RecyclerView has been clicked.
     *
	 * @param recyclerView RecyclerView object to add an OnClickItemListener
     * @param listener The callback that will be invoked.
     */
	public void setOnClickItemListener(RecyclerView recyclerView, OnClickItemListener listener) {
        mOnItemClickListener = listener;
		if(mRecyclerView == null){
			mRecyclerView = recyclerView;
			mRecyclerView.setTag(R.id.item_click_support, this);
			mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
		}
    }

	/**
     * Register a callback to be invoked when an item in the
     * RecyclerView has been long clicked.
     *
	 * @param recyclerView RecyclerView object to add an OnClickItemListener
     * @param listener The callback that will be invoked.
     */
    public void setOnLongClickItemListener(RecyclerView recyclerView, OnLongClickItemListener listener) {
        mOnItemLongClickListener = listener;
		if(mRecyclerView == null){
			mRecyclerView = recyclerView;
			mRecyclerView.setTag(R.id.item_click_support, this);
			mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
		}
    }

	/**
	 * ReciclerView.OnChildAttachStateChangeListener to listen changes
	 */
	private RecyclerView.OnChildAttachStateChangeListener mAttachListener = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            if (mOnItemClickListener != null) {
                view.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if (mOnItemClickListener != null) {
								RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
								mOnItemClickListener.onClicked(getItem(holder.getAdapterPosition()), holder.getAdapterPosition(), view);
							}
						}
					});
            }

            if (mOnItemLongClickListener != null) {
                view.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View view) {
							if (mOnItemLongClickListener != null) {
								RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
								return mOnItemLongClickListener.onLongClicked(getItem(holder.getAdapterPosition()), holder.getAdapterPosition(), view);
							}
							return false;
						}
					});
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {

        }
    };

	/**
	 * Convenience method to call {@link #updateDataSet(String)} with {@link null} as param.
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
	 * This method execute {@link #updateDataSet(String)} asynchronously
	 * with {@link FilterAsyncTask}.<br/><br/>
	 * <b>Note:</b> {@link #notifyDataSetChanged()} is automatically called at the end of the process.
	 *
	 * @param param A custom parameter to filter the DataSet
	 * @deprecated Not giving any added values. Use {@link #updateDataSet()} instead.
	 */
	@Deprecated
	public void updateDataSetAsync(String param) {
		//Synchronous
		if (mUpdateListener != null) {
			Log.w(TAG, "OnUpdateListener is not initialized. updateDataSetAsync is not using FilterAsyncTask!");
			updateDataSet(param);
			notifyDataSetChanged();
			return;
		}
		//Asynchronous
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

	public boolean isEmpty() {
		return getItemCount() == 0;
	}

	public void updateItem(int position, T item) {
		if (position < 0) {
			Log.w(TAG, "Cannot updateItem on negative position");
			return;
		}
		synchronized (mLock) {
			mItems.set(position, item);
		}
		Log.v(TAG, "updateItem notifyItemChanged on position " + position);
		notifyItemChanged(position);
	}

	/**
	 * Insert given Item at position or Add Item at last position.
	 *
	 * @param position Position of the item to add
	 * @param item     The item to add
	 */
	public void addItem(int position, T item) {
		if (position < 0) {
			Log.w(TAG, "Cannot addItem on negative position");
			return;
		}
		//Insert Item
		if (position < mItems.size()) {
			Log.v(TAG, "addItem notifyItemInserted on position " + position);
			synchronized (mLock) {
				mItems.add(position, item);
			}
		} else { //Add Item at the last position
			Log.v(TAG, "addItem notifyItemInserted on last position");
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
		if (position < 0) {
			Log.w(TAG, "Cannot removeItem on negative position");
			return;
		}
		if (position < mItems.size()) {
			Log.v(TAG, "removeItem notifyItemRemoved on position " + position);
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
		Log.v(TAG, "removeItems reverse Sorting positions --------------");
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
			Log.v(TAG, "removeItems current selection " + getSelectedItems());
		}
	}

	private void removeRange(int positionStart, int itemCount) {
		Log.v(TAG, "removeRange positionStart=" + positionStart + " itemCount=" + itemCount);
		for (int i = 0; i < itemCount; ++i) {
			synchronized (mLock) {
				saveDeletedItem(positionStart, mItems.remove(positionStart));
			}
		}
		Log.v(TAG, "removeRange notifyItemRangeRemoved");
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
		Log.v(TAG, "Recycled " + getItem(position) + " on position=" + position);
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
	 * Restore items just removed.<br/>
	 * <b>NOTE:</b> If filter is active, only items that match that filter will be shown(restored).
	 */
	public void restoreDeletedItems() {
		stopUndoTimer();
		//Reverse insert (list was reverse ordered on Delete)
		for (int i = mOriginalPosition.size() - 1; i >= 0; i--) {
			T item = mDeletedItems.get(i);
			//Avoid to restore(show) Items not filtered by the current filter
			if (hasSearchText() && !filterObject(item, getSearchText()))
				continue;
			addItem(mOriginalPosition.get(i), item);
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
			mOriginalPosition.clear();
		}
	}

	/**
	 * Convenience method to start Undo timer with default timeout of 5''
	 *
	 * @deprecated Use {@link #startUndoTimer(OnDeleteCompleteListener)} instead.
	 */
	@Deprecated
	public void startUndoTimer() {
		startUndoTimer(0, null);
	}

	/**
	 * Start Undo timer with custom timeout.
	 *
	 * @param timeout Custom timeout
	 * @deprecated Use {@link #startUndoTimer(long, OnDeleteCompleteListener)} instead.
	 */
	@Deprecated
	public void startUndoTimer(long timeout) {
		mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
			public boolean handleMessage(Message message) {
				if (mUpdateListener != null) mUpdateListener.onDeleteConfirmed();
				emptyBin();
				return true;
			}
		});
		mHandler.sendMessageDelayed(Message.obtain(mHandler), timeout > 0 ? timeout : UNDO_TIMEOUT);
	}

	/**
	 * Convenience method to start Undo timer with default timeout of 5''
	 *
	 * @param listener Delete listener called after timeout
	 */
	public void startUndoTimer(OnDeleteCompleteListener listener) {
		startUndoTimer(0, listener);
	}

	/**
	 * Start Undo timer with custom timeout
	 *
	 * @param listener Delete listener called after timeout
	 * @param timeout  Custom timeout
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
	private void stopUndoTimer() {
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
	}

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
	 * @param unfilteredItems The list to filter
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
						if (mOriginalPosition.get(index) != oldOriginalPosition) {
							newOriginalPosition++;
							oldOriginalPosition = mOriginalPosition.get(index);
						}
						mOriginalPosition.set(index, newOriginalPosition + mItems.size());
					} else {
						mItems.add(item);
					}
				}
			}
		} else {
			mItems = unfilteredItems; //with no filter
			if (mDeletedItems != null && !mDeletedItems.isEmpty()) {
				mOriginalPosition = new ArrayList<Integer>(mDeletedItems.size());
				for (T item : mDeletedItems) {
					mOriginalPosition.add(mItems.indexOf(item));
				}
				mItems.removeAll(mDeletedItems);
			}
		}
	}

	/**
	 * DEFAULT IMPLEMENTATION, OVERRIDE TO HAVE OWN FILTER!
	 * <p/>
	 * <br/><br/>
	 * Performs filtering on the provided object and returns true, if the object should be in the filtered collection,
	 * or false if it shouldn't.
	 *
	 * @param myObject   The object to be inspected
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

	@Deprecated
	public class FilterAsyncTask extends AsyncTask<String, Void, Void> {

		//private final String TAG = FilterAsyncTask.class.getSimpleName();

		@Override
		protected Void doInBackground(String... params) {
			//Log.v(TAG, "doInBackground - started FilterAsyncTask!");
			updateDataSet(params[0]);
			//Log.v(TAG, "doInBackground - ended FilterAsyncTask!");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mUpdateListener.onLoadComplete();
			notifyDataSetChanged();
		}
	}


	@Deprecated
	public interface OnUpdateListener {
		/**
		 * Called when Async load is completed.
		 */
		@Deprecated
		void onLoadComplete();

		/**
		 * Due to Java Generic, it's too complicated and not
		 * well manageable if we pass the List&lt;T&gt; object.<br/>
		 * To get deleted items, use {@link #getDeletedItems()} from the
		 * implementation of this method.
		 */
		@Deprecated
		void onDeleteConfirmed();

		/**
		 * Called at startup and every time an item is inserted or removed.
		 */
		//void onUpdateEmptyView();
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
