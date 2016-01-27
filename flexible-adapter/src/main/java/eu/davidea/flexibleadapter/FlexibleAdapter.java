package eu.davidea.flexibleadapter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This class provides a set of standard methods to handle changes on the data set
 * such as adding, removing, moving an item.
 * <p><strong>VH</strong> is your implementation of {@link RecyclerView.ViewHolder}.
 * <strong>T</strong> is your domain object containing the data.</p>
 *
 * @author Davide Steduto
 * @since 03/05/2015 Created
 *   <br/>20/01/2016 New code reorganization
 *   <br/>24/01/2016 Drag&Drop, Swipe
 *   <br/>26/01/2016 New interfaces and full refactoring
 */
public abstract class FlexibleAdapter<VH extends RecyclerView.ViewHolder, T> extends SelectableAdapter<VH>
		implements ItemTouchHelperCallback.AdapterCallback {

	private static final String TAG = FlexibleAdapter.class.getSimpleName();
	public static final long UNDO_TIMEOUT = 5000L;

	/**
	 * The main container for ALL items.
	 */
	protected List<T> mItems;
	
	/**
	 * Used to avoid multiple calls to the {@link OnUpdateListener#onUpdateEmptyView(int)}
	 */
	protected boolean isMultiRemove = false;
	
	/**
	 * Lock object used to modify the content of {@link #mItems}.
	 * Any write operation performed on the list items should be synchronized on this lock.
	 */
	protected final Object mLock = new Object();
	
	//Undo
	protected List<T> mDeletedItems;
	protected List<Integer> mOriginalPositions;
	protected SparseArray<T> mRemovedItems;//beta test
	protected boolean mRestoreSelection = false;
	protected Handler mHandler;

	/* Filter */
	protected String mSearchText = "";
	protected String mOldSearchText = "";
	protected boolean mNotifyChangeOfUnfilteredItems = false;

	/* Drag&Drop and Swipe helpers */
	private ItemTouchHelperCallback mItemTouchHelperCallback;
	private ItemTouchHelper mItemTouchHelper;

	/* Listeners */
	protected OnUpdateListener mUpdateListener;
	public OnItemClickListener mItemClickListener;
	public OnItemLongClickListener mItemLongClickListener;
	protected OnItemMoveListener mItemMoveListener;
	protected OnItemSwipeListener mItemSwipeListener;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	/**
	 * Simple Constructor with NO listeners!
	 *
	 * @param items items to display.
	 */
	public FlexibleAdapter(@NonNull List<T> items) {
		this(items, null);
	}

	/**
	 * Main Constructor with all managed Listeners for ViewHolder and the Adapter itself.
	 * <p>The listener must be a single instance of a class, usually Activity or Fragment.</p>
	 *
	 * @param items     items to display
	 * @param listeners can be an instance of:
	 * 					<br/>{@link OnUpdateListener}
	 * 					<br/>{@link OnItemClickListener}
	 * 					<br/>{@link OnItemLongClickListener}
	 * 					<br/>{@link OnItemMoveListener}
	 * 					<br/>{@link OnItemSwipeListener}
	 */
	public FlexibleAdapter(@NonNull List<T> items, @Nullable Object listeners) {
		mItems = items;

		if (listeners instanceof OnUpdateListener) {
			mUpdateListener = (OnUpdateListener) listeners;
			mUpdateListener.onUpdateEmptyView(mItems.size());
		}
		if (listeners instanceof OnItemClickListener)
			mItemClickListener = (OnItemClickListener) listeners;
		if (listeners instanceof OnItemLongClickListener)
			mItemLongClickListener = (OnItemLongClickListener) listeners;
		if (listeners instanceof OnItemMoveListener)
			mItemMoveListener = (OnItemMoveListener) listeners;
		if (listeners instanceof OnItemSwipeListener)
			mItemSwipeListener = (OnItemSwipeListener) listeners;
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
	 * This method will refresh the entire DataSet content.
	 * <p>The parameter is useful to filter the type of the DataSet.<br/>
	 * Pass null value in case not used.</p>
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
	 * @return the total number of the items currently displayed by the adapter
	 */
	@Override
	@CallSuper
	public int getItemCount() {
		return mItems != null ? mItems.size() : 0;
	}

	/**
	 * Retrieve the global position of the Item in the Adapter list.
	 *
	 * @param item the item
	 * @return the global position in the Adapter if found, -1 otherwise
	 */
	public int getPositionForItem(@NonNull T item) {
		return mItems != null && mItems.size() > 0 ? mItems.indexOf(item) : -1;
	}

	public boolean contains(@NonNull T item) {
		return mItems != null && mItems.contains(item);
	}

	public boolean isEmpty() {
		return this.getItemCount() == 0;
	}

	@Override
	public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

	@Override
	public abstract void onBindViewHolder(VH holder, final int position);

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
		if (mUpdateListener != null) mUpdateListener.onUpdateEmptyView(getItemCount());
	}

	/*----------------------*/
	/* DELETE ITEMS METHODS */
	/*----------------------*/

	/**
	 * Removes an item from internal list and notify the change.
	 * <p>The item is retained for an eventual Undo.</p>
	 *
	 * @param position the position of item to remove
	 * @see #startUndoTimer(long, OnDeleteCompleteListener)
	 * @see #restoreDeletedItems()
	 * @see #setRestoreSelectionOnUndo(boolean)
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
		if (mUpdateListener != null && !isMultiRemove)
			mUpdateListener.onUpdateEmptyView(getItemCount());
	}

	/**
	 * Removes a list of items from internal list and notify the change.
	 * <p>Every item is retained for an eventual Undo.</p>
	 *
	 * @param selectedPositions list of item positions to remove
	 * @see #startUndoTimer(long, OnDeleteCompleteListener)
	 * @see #restoreDeletedItems()
	 * @see #setRestoreSelectionOnUndo(boolean)
	 * @see #emptyBin()
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
			isMultiRemove = true;
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
		isMultiRemove = false;
		if (mUpdateListener != null) mUpdateListener.onUpdateEmptyView(getItemCount());
	}

	/**
	 * Removes an ordered set items from internal list and notify the change once.
	 * <p>Items are retained for an eventual Undo.</p>
	 *
	 * @param positionStart Previous position of the first item that was removed
	 * @param itemCount     Number of items removed from the data set
	 */
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
	 * Convenience method to remove all Items that are currently selected.
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

	/**
	 * @return a list with the global positions of all deleted items
	 */
	public List<Integer> getDeletedPositions() {
		return mOriginalPositions;
	}

	public boolean isRestoreInTime() {
		return mDeletedItems != null && mDeletedItems.size() > 0;
	}

	/**
	 * Returns the current configuration to restore selections on Undo.
	 *
	 * @return true if selection will be restored, false otherwise
	 */
	public boolean isRestoreWithSelection() {
		return mRestoreSelection;
	}

	/**
	 * Gives the possibility to restore the selection on Undo, when {@link #restoreDeletedItems()}
	 * is called.
	 * <p>To use in combination with {@code ActionMode} in order to not disable it.</p>
	 * Default value is false.
	 *
	 * @param restoreSelection true to have restored items still selected, false to empty selections.
	 */
	public void setRestoreSelectionOnUndo(boolean restoreSelection) {
		this.mRestoreSelection = restoreSelection;
	}

	/**
	 * Restore items just removed.
	 * <p><b>NOTE:</b> If filter is active, only items that match that filter will be shown(restored).</p>
	 *
	 * @see #setRestoreSelectionOnUndo(boolean)
	 */
	public void restoreDeletedItems() {
		stopUndoTimer();
		//Reverse insert (list was reverse ordered on Delete)
		for (int i = mOriginalPositions.size() - 1; i >= 0; i--) {
			T item = mDeletedItems.get(i);
			T item2 = mRemovedItems.get(mRemovedItems.keyAt(i));
			if (DEBUG)
				Log.v(TAG, "Restoring item " + item + " on position " + mOriginalPositions.get(i));
			if (DEBUG)
				Log.v(TAG, "Restoring item2 " + item2 + " on position " + mRemovedItems.keyAt(i));
			//Avoid to restore(show) Items not filtered by the current filter
			if (hasSearchText() && !filterObject(item, getSearchText()))
				continue;
			addItem(mOriginalPositions.get(i), item);
			//Restore selection before emptyBin, if configured
			if (mRestoreSelection)
				getSelectedPositions().add(mOriginalPositions.get(i));
		}
		emptyBin();
	}

	/**
	 * Clean memory from items just removed.
	 * <p><b>Note:</b> This method is automatically called after timer is over and after a
	 * restoration.</p>
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
	 * @param listener the listener that will be called after timeout to commit the change
	 */
	public void startUndoTimer(OnDeleteCompleteListener listener) {
		startUndoTimer(0, listener);
	}

	/**
	 * Start Undo timer with custom timeout
	 *
	 * @param timeout  custom timeout
	 * @param listener the listener that will be called after timeout to commit the change
	 */
	public void startUndoTimer(long timeout, final OnDeleteCompleteListener listener) {
		if (mHandler != null) {
			//Make longer the timer for new coming deleted items
			mHandler.removeCallbacksAndMessages(null);
		} else {
			mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
				public boolean handleMessage(Message message) {
					if (listener != null) listener.onDeleteConfirmed();
					emptyBin();
					return true;
				}
			});
		}
		mHandler.sendMessageDelayed(Message.obtain(mHandler), timeout > 0 ? timeout : UNDO_TIMEOUT);
	}

	/**
	 * Stop Undo timer.
	 * <p><b>Note:</b> This method is automatically called in case of restoration.</p>
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
	 * Sometimes it is necessary, while filtering, to rebound the items that remain unfiltered.
	 * If the items have highlighted text, those items must be refreshed in order to update the
	 * highlighted text.
	 * <p>
	 * This happens systematically when searchText is reduced in length by the user. It doesn't
	 * reduce performance in other cases, since the notification is triggered only in
	 * {@link #applyAndAnimateAdditions(List, List)} when new items are not added.
	 * </p>
	 *
	 * @param notifyChange true to trigger {@link #notifyItemChanged(int)} while filtering,
	 *                     false otherwise
	 */
	public final void setNotifyChangeOfUnfilteredItems(boolean notifyChange) {
		this.mNotifyChangeOfUnfilteredItems = notifyChange;
	}

	/**
	 * <b>WATCH OUT! PASS ALWAYS A <u>COPY</u> OF THE ORIGINAL LIST</b>: due to internal mechanism,
	 * items are removed and/or added in order to animate items in the final list.
	 * <p>
	 * Filters the provided list with the search text previously set with {@link #setSearchText(String)}.
	 * </p>
	 * <b>Note:</b>
	 * <br/>- This method calls {@link #filterObject(T, String)}.
	 * <br/>- If search text is empty or null, the provided list is the current list.
	 * <br/>- Any pending deleted items are always filtered out.
	 * <br/>- Original positions of deleted items are recalculated.
	 * <br/>- <b>NEW!</b> Items are animated thanks to {@link #animateTo(List)}.
	 *
	 * @param unfilteredItems the list to filter
	 * @see #filterObject(Object, String)
	 */
	public synchronized void filterItems(@NonNull List<T> unfilteredItems) {
		// NOTE: In case user has deleted some items and he changes or applies a filter while
		// deletion is pending (Undo started), in order to be consistent, we need to recalculate
		// the new position in the new list and finally skip those items to avoid they are shown!
		List<T> values = new ArrayList<T>();
		if (hasSearchText()) {
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
						values.add(item);
					}
				}
			}
		} else {
			values = unfilteredItems; //with no filter
			if (mDeletedItems != null && !mDeletedItems.isEmpty()) {
				mOriginalPositions = new ArrayList<Integer>(mDeletedItems.size());
				for (T item : mDeletedItems) {
					mOriginalPositions.add(values.indexOf(item));
				}
				values.removeAll(mDeletedItems);
			}
		}

		//Animate search results only in case of new SearchText
		if (!mOldSearchText.equalsIgnoreCase(mSearchText)) {
			mOldSearchText = mSearchText;
			animateTo(values);
		} else mItems = values;

		//Call listener to update EmptyView
		if (mUpdateListener != null) {
			mUpdateListener.onUpdateEmptyView(getItemCount());
		}
	}

	/**
	 * This method performs filtering on the provided object and returns, <b>true</b> if the object
	 * should be in the filtered collection or <b>false</b> if it shouldn't.
	 * <p>THIS IS THE DEFAULT IMPLEMENTATION, OVERRIDE TO HAVE OWN FILTER!
	 * The item will result filtered if its {@code toString()} contains the searchText.</p>
	 *
	 * @param item       the object to be inspected
	 * @param constraint constraint, that the object has to fulfil
	 * @return true, if the object should be in the filteredResult, false otherwise
	 */
	protected boolean filterObject(T item, String constraint) {
		final String valueText = item.toString().toLowerCase();

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

	/**
	 * Animate from one list to the other
	 *
	 * @param models the new list containing the new items
	 * @return the cleaned up item list. make sure to set your new list to this one
	 */
	public List<T> animateTo(List<T> models) {
		applyAndAnimateRemovals(mItems, models);
		applyAndAnimateAdditions(mItems, models);
		applyAndAnimateMovedItems(mItems, models);
		return mItems;
	}

	/**
	 * Find out all removed items and animate them.
	 */
	protected void applyAndAnimateRemovals(List<T> from, List<T> newItems) {
		int out = 0;
		for (int i = from.size() - 1; i >= 0; i--) {
			final T item = from.get(i);
			if (!newItems.contains(item)) {
				if (DEBUG) Log.v(TAG, "animateRemovals remove position=" + i + " item=" + item);
				from.remove(i);
				notifyItemRemoved(i);
				out++;
			} else {
				if (DEBUG) Log.v(TAG, "animateRemovals   keep position=" + i + " item=" + item);
			}
		}
		if (DEBUG) Log.v(TAG, "animateRemovals total out=" + out + " in=" + newItems.size());
	}

	/**
	 * Find out all added items and animate them.
	 */
	protected void applyAndAnimateAdditions(List<T> from, List<T> newItems) {
		int out = 0;
		for (int i = 0, count = newItems.size(); i < count; i++) {
			final T item = newItems.get(i);
			if (!from.contains(item)) {
				if (DEBUG) Log.v(TAG, "animateAdditions  add position=" + i + " item=" + item);
				from.add(i, item);
				notifyItemInserted(i);
			} else if (mNotifyChangeOfUnfilteredItems) {
				out++;
				notifyItemChanged(i);
				if (DEBUG) Log.v(TAG, "animateAdditions keep position=" + i + " item=" + item);
			}
		}
		if (DEBUG) Log.v(TAG, "animateAdditions total out=" + out + " in=" + newItems.size());
	}

	/**
	 * Find out all moved items and animate them.
	 */
	protected void applyAndAnimateMovedItems(List<T> from, List<T> newItems) {
		for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
			final T item = newItems.get(toPosition);
			final int fromPosition = from.indexOf(item);
			if (fromPosition >= 0 && fromPosition != toPosition) {
				if (DEBUG) Log.v(TAG, "animateMovedItems from=" + toPosition + " to=" + toPosition);
				from.add(toPosition, from.remove(fromPosition));
				moveItem(fromPosition, toPosition);
			}
		}
	}

	/*---------------*/
	/* TOUCH METHODS */
	/*---------------*/

	/**
	 * Used by {@link FlexibleViewHolder#onTouch(View, MotionEvent)} to start Drag when
	 * HandleView is touched.
	 *
	 * @return the ItemTouchHelper instance already initialized.
	 */
	public final ItemTouchHelper getItemTouchHelper() {
		initializeItemTouchHelper();
		return mItemTouchHelper;
	}

	/**
	 * Enable the Drag on LongPress on the entire ViewHolder.
	 * <p><b>NOTE:</b> This will skip LongClick on the view in order to handle the LongPress,
	 * however the LongClick listener will be called if necessary in the new
	 * {@link FlexibleViewHolder#onActionStateChanged(int, int)}.</p>
	 * Default value is false.
	 *
	 * @param longPressDragEnabled true to activate, false otherwise
	 */
	public final void setLongPressDragEnabled(boolean longPressDragEnabled) {
		initializeItemTouchHelper();
		mItemTouchHelperCallback.setLongPressDragEnabled(longPressDragEnabled);
	}

	/**
	 * Returns whether ItemTouchHelper should start a drag and drop operation if an item is
	 * long pressed.<p>
	 * Default value returns false.
	 *
	 * @return true if ItemTouchHelper should start dragging an item when it is long pressed,
	 * false otherwise. Default value is false.
	 */
	public boolean isLongPressDragEnabled() {
		return mItemTouchHelperCallback.isLongPressDragEnabled();
	}

	/**
	 * Enabled by default.
	 * <p>It is sufficient to call {@link FlexibleViewHolder#setDragHandleView(View)}.</p>
	 *
	 * @return true if LongPressDragEnabled is disabled, false otherwise
	 */
	public boolean isHandleDragEnabled() {
		return true;
	}

	/**
	 * Enable the Swipe of the items
	 * <p>Default value is false.</p>
	 *
	 * @param swipeEnabled true to activate, false otherwise
	 */
	public final void setSwipeEnabled(boolean swipeEnabled) {
		initializeItemTouchHelper();
		mItemTouchHelperCallback.setSwipeEnabled(swipeEnabled);
	}

	/**
	 * Returns whether ItemTouchHelper should start a swipe operation if a pointer is swiped
	 * over the View.
	 * <p>Default value returns false.</p>
	 *
	 * @return true if ItemTouchHelper should start swiping an item when user swipes a pointer
	 * over the View, false otherwise. Default value is false.
	 */
	public final boolean isSwipeEnabled() {
		return mItemTouchHelperCallback.isItemViewSwipeEnabled();
	}

	/**
	 * Swaps the elements of list list at indices fromPosition and toPosition and notify the change.
	 * <p>Selection of swiped elements is automatically updated.</p>
	 *
	 * @param fromPosition previous position of the item.
	 * @param toPosition   new position of the item.
	 */
	@CallSuper
	public void moveItem(int fromPosition, int toPosition) {
		if (DEBUG) {
			Log.v(TAG, "moveItem from=" +
					fromPosition + "[" + (isSelected(fromPosition) ? "selected" : "unselected") + "] to=" +
					toPosition + "[" + (isSelected(toPosition) ? "selected" : "unselected") + "]");
			Log.v(TAG, "moveItem fromItem=" + getItem(fromPosition) + " toItem=" + getItem(toPosition));
		}
		Collections.swap(mItems, fromPosition, toPosition);
		if ((isSelected(fromPosition) && !isSelected(toPosition)) ||
				(!isSelected(fromPosition) && isSelected(toPosition))) {
			super.toggleSelection(fromPosition);
			super.toggleSelection(toPosition);
		}
		notifyItemMoved(fromPosition, toPosition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean shouldMove(int fromPosition, int toPosition) {
		//Confirm move by default
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@CallSuper
	public boolean onItemMove(int fromPosition, int toPosition) {
		moveItem(fromPosition, toPosition);
		//After the swap, delegate further actions to the user
		if (mItemMoveListener != null) {
			mItemMoveListener.onItemMove(fromPosition, toPosition);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@CallSuper
	public void onItemSwiped(int position, int direction) {
		//Delegate actions to the user
		if (mItemSwipeListener != null) {
			mItemSwipeListener.onItemSwipe(position, direction);
		}
	}

	private void initializeItemTouchHelper() {
		if (mItemTouchHelper == null) {
			if (mRecyclerView == null) {
				throw new IllegalStateException("RecyclerView cannot be null. Enabling LongPressDrag or Swipe must be done after the Adapter is added to the RecyclerView.");
			}
			mItemTouchHelperCallback = new ItemTouchHelperCallback(this);
			mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
			mItemTouchHelper.attachToRecyclerView(mRecyclerView);
		}
	}

	/*------------------*/
	/* INNER INTERFACES */
	/*------------------*/

	/**
	 * @since 03/01/2016
	 */
	public interface OnUpdateListener {
		/**
		 * Called at startup and every time an item is inserted, removed or filtered.
		 *
		 * @param size the current number of items in the adapter, result of {@link #getItemCount()}
		 */
		void onUpdateEmptyView(int size);
	}

	/**
	 * @since 29/11/2015
	 */
	public interface OnDeleteCompleteListener {
		/**
		 * Called when Undo timeout is over and removal must be committed in the user Database.
		 * <p>Due to Java Generic, it's too complicated and not
		 * well manageable if we pass the List&lt;T&gt; object.<br/>
		 * To get deleted items, use {@link #getDeletedItems()} from the
		 * implementation of this method.</p>
		 */
		void onDeleteConfirmed();
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemClickListener {
		/**
		 * Called when single tap occurs.
		 * <p>Delegation of the click event to the listener and check if selection MODE is
		 * SINGLE or MULTI is enabled in order to activate the ItemView.</p>
		 *
		 * @param position the adapter position of the item clicked
		 * @return true the click should activate the ItemView, false for no change.
		 */
		boolean onItemClick(int position);
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemLongClickListener {
		/**
		 * Called when long tap occurs.
		 * <p>This method always calls {@link FlexibleViewHolder#toggleActivation} after listener
		 * event is consumed in order to activate the ItemView.</p>
		 *
		 * @param position the adapter position of the item clicked
		 */
		void onItemLongClick(int position);
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemMoveListener {
		/**
		 * Called when an item has been dragged far enough to trigger a move. <b>This is called
		 * every time an item is shifted</b>, and <strong>not</strong> at the end of a "drop" event.
		 * <p>The end of the "drop" event is instead handled by
		 * {@link FlexibleViewHolder#onItemReleased(int)}</p>.
		 *
		 * @param fromPosition the start position of the moved item
		 * @param toPosition   the resolved position of the moved item
		 */
		void onItemMove(int fromPosition, int toPosition);
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemSwipeListener {
		/**
		 * Called when swiping ended its animation and Item is not visible anymore.
		 *
		 * @param position  the position of the item swiped
		 * @param direction the direction to which the ViewHolder is swiped
		 */
		void onItemSwipe(int position, int direction);
	}

}