package eu.davidea.flexibleadapter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.davidea.flexibleadapter.item.IExpandableItem;
import eu.davidea.viewholder.ExpandableViewHolder;
import eu.davidea.viewholder.FlexibleViewHolder;

/**
 * This adapter provides a set of standard methods to expand and collapse an Item.
 *
 * @author Davide Steduto
 * @since 16/01/2016
 */
public abstract class FlexibleExpandableAdapter<EVH extends ExpandableViewHolder, T extends IExpandableItem<T>>
		extends FlexibleAnimatorAdapter<FlexibleViewHolder, T> {

	private static final String TAG = FlexibleExpandableAdapter.class.getSimpleName();
	public static int EXPANDABLE_VIEW_TYPE = -1;

	private SparseArray<T> mExpandedItems;
	private List<RemovedItem> removedItems;
	boolean childSelected = false,
			parentSelected = false,
			mScrollOnExpand = false,
			mCollapseOnExpand = false;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	public FlexibleExpandableAdapter(@NonNull List<T> items) {
		this(items, null);
	}

	public FlexibleExpandableAdapter(@NonNull List<T> items, Object listener) {
		super(items, listener);
		mExpandedItems = new SparseArray<T>();
		removedItems = new ArrayList<RemovedItem>();
		expandInitialItems();
	}

	protected void expandInitialItems() {
		//Set initially expanded
		for (int i = 0; i < mItems.size(); i++) {
			T item = mItems.get(i);
			//FIXME: Foreseen bug on Rotation: coordinate expansion with onRestoreInstanceState
			if (item.isExpanded() && hasSubItems(item)) {
				if (DEBUG) Log.v(TAG, "Initially expand item on position " + i);
				mExpandedItems.put(i, item);
				mItems.addAll(i + 1, item.getSubItems());
				i += item.getSubItems().size();
			}
		}
	}

	/*------------------------------*/
	/* SELECTION METHODS OVERRIDDEN */
	/*------------------------------*/

	@Override
	public void toggleSelection(int position) {
		T item = getItem(position);
		//Allow selection only for selectable items
		if (item.isSelectable()) {
			if (item.isExpandable() && !childSelected) {
				//Allow selection of Parent if no Child has been previously selected
				parentSelected = true;
				super.toggleSelection(position);
			} else if (!item.isExpandable() && !parentSelected) {
				//Allow selection of Child if no Parent has been previously selected
				childSelected = true;
				super.toggleSelection(position);
			}
		}
	}

	@Override
	public void selectAll() {
		//FIXME: select view type of only 1 type
		if (getSelectedItemCount() > 0 && getItem(getSelectedItems().get(0)).isExpandable())
			super.selectAll(EXPANDABLE_VIEW_TYPE + 1);//Select only Parents
		else
			super.selectAll(1);//Select others Views 0(default) + 1
	}

	@Override
	public void clearSelection() {
		parentSelected = childSelected = false;
		super.clearSelection();
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	//FIXME: Rewrite Filter logic: Expand Parent if subItem is filtered by searchText?
	//TODO: Customize items animations on search

	//FIXME: Find a way to not animate items with ItemAnimator!!!
	//TODO: Customize child items animations (don't use add or remove ItemAnimator)

	public boolean isExpanded(int position) {
		T item = getItem(position);
		return item.isExpandable() && item.isExpanded();
	}

	public boolean isExpandable(int position) {
		T item = getItem(position);
		return item.isExpandable();
	}

	/**
	 * Automatically collapse all previous expanded parents before expand the clicked parent.<br/>
	 * Default disabled.
	 *
	 * @param collapseOnExpand true to collapse others items, false to just expand the current
	 */
	public void setAutoCollapseOnExpand(boolean collapseOnExpand) {
		mCollapseOnExpand = collapseOnExpand;
	}

	/**
	 * Automatically scroll the clicked expandable item to the first visible position.<br/>
	 * Default disabled.<br/><br/>
	 * This works ONLY in combination with {@link SmoothScrollLinearLayoutManager}.
	 * GridLayout is still not supported.
	 *
	 * @param scrollOnExpand true to enable automatic scroll, false to disable
	 */
	public void setAutoScrollOnExpand(boolean scrollOnExpand) {
		mScrollOnExpand = scrollOnExpand;
	}

	/**
	 * Retrieve the parent of any child.
	 *
	 * @param child Child item
	 * @return The Parent of this child item or null if not found
	 */
	public T getExpandableOf(T child) {
		for (T parent : mItems) {
			if (parent.isExpandable() && parent.contains(child))
				return parent;
		}
		return null;
	}

	/**
	 * @return a set with the global positions of all expanded items
	 */
	public int[] getExpandedItems() {
		int[] expandedItems = new int[mExpandedItems.size()];
		int length = mExpandedItems.size();
		for (int i = 0; i < length; i++) {
			expandedItems[i] = mExpandedItems.keyAt(i);
		}
		return expandedItems;
	}

	/**
	 * ViewType for Expandable Item is -1.<br/>
	 * All others ViewTypes are different than -1.
	 *
	 * @param position position for which ViewType is requested
	 * @return -1 for {@link #EXPANDABLE_VIEW_TYPE}; 0 (default) or any user value for all others ViewType.
	 */
	@Override
	public int getItemViewType(int position) {
		if (getItem(position).isExpandable())
			return EXPANDABLE_VIEW_TYPE;
		return super.getItemViewType(position);
	}

	/**
	 * Create ViewHolder that are expandable.
	 *
	 * @param parent   The ViewGroup into which the new View will be added after it is bound to
	 *                 an adapter position.
	 * @param viewType The view type of the new View.
	 * @return A new ExpandableViewHolder that holds a View that can be expanded or collapsed.
	 */
	public abstract EVH onCreateExpandableViewHolder(ViewGroup parent, int viewType);

	/**
	 * Create ViewHolder that is not expandable or it's a child of an ExpandableViewHolder.
	 *
	 * @param parent   The ViewGroup into which the new View will be added after it is bound to
	 *                 an adapter position.
	 * @param viewType The view type of the new View.
	 * @return A new FlexibleViewHolder that holds a View that can be child of the expanded views.
	 */
	public abstract FlexibleViewHolder onCreateFlexibleViewHolder(ViewGroup parent, int viewType);

	@Override
	public final FlexibleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == EXPANDABLE_VIEW_TYPE) {
			return onCreateExpandableViewHolder(parent, viewType);
		} else {
			return onCreateFlexibleViewHolder(parent, viewType);
		}
	}

	public abstract void onBindExpandableViewHolder(EVH holder, int position);

	public abstract void onBindFlexibleViewHolder(FlexibleViewHolder holder, int position);

	@Override
	public final void onBindViewHolder(FlexibleViewHolder holder, int position) {
		if (getItemViewType(position) == EXPANDABLE_VIEW_TYPE) {
			onBindExpandableViewHolder((EVH) holder, position);
		} else {
			onBindFlexibleViewHolder(holder, position);
		}
	}

	public int expand(int position) {
		Log.v(TAG, "Request to Expand on position " + position);
		final T item = getItem(position);
		int subItemsCount = 0;
		if (item.isExpandable() && !item.isExpanded() && hasSubItems(item) && !parentSelected) {

			//Collapse others expandable if configured so
			if (mCollapseOnExpand) {
				int collapsed = 0;
				for (int i = 0; i < mExpandedItems.size(); i++) {
					collapsed += collapse(mExpandedItems.keyAt(i));
				}
				//Fetch the new position after collapsing
				if (collapsed > 0) position = getPositionForItem(item);
			}

			//Save expanded state
			subItemsCount = item.getSubItems().size();
			mExpandedItems.put(position, item);
			mItems.addAll(position + 1, item.getSubItems());
			item.setExpanded(true);

			//Adjust selection and expandable positions, that are grater than the expanded position
			adjustSelected(position, subItemsCount);
			adjustExpanded(position, subItemsCount);

			//Automatically scroll the current expandable item to show as much children as possible
			if (mScrollOnExpand) {
				//Must be delayed to give time at RecyclerView to recalculate positions
				//after an automatic collapse
				final int pos = position, subItems = subItemsCount;
				Handler animatorHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
					public boolean handleMessage(Message message) {
						autoScroll(pos, subItems);
						return true;
					}
				});
				animatorHandler.sendMessageDelayed(Message.obtain(mHandler), 150L);
			}

			//Expand!
			notifyItemRangeInserted(position + 1, subItemsCount);

			if (DEBUG)
				Log.v(TAG, "Expanded " + subItemsCount + " subItems on position=" + position);
		}
		return subItemsCount;
	}

	public int collapse(int position) {
		Log.v(TAG, "Request to Collapse on position " + position);
		T item = getItem(position);
		int subItemsCount = 0;
		if (item.isExpandable() && item.isExpanded() &&
				(!hasSubItemsSelected(item) || isItemPendingRemove(position))) {

			subItemsCount = item.getSubItems().size();
			int indexOfKey = mExpandedItems.indexOfKey(position);
			if (indexOfKey >= 0) {
				mExpandedItems.removeAt(indexOfKey);
			}
			mItems.removeAll(item.getSubItems());
			item.setExpanded(false);

			//Adjust selection and expandable positions, that are grater than the collapsed position
			adjustSelected(position, -subItemsCount);
			adjustExpanded(position, -subItemsCount);

			//Collapse!
			notifyItemRangeRemoved(position + 1, subItemsCount);

			if (DEBUG)
				Log.v(TAG, "Collapsed " + subItemsCount + " subItems on position=" + position);
		}
		return subItemsCount;
	}

	private void autoScroll(int position, int subItemsCount) {
		int firstVisibleItem = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
		int lastVisibleItem = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
		int itemsToShow = position + subItemsCount - lastVisibleItem;
		if (DEBUG)
			Log.v(TAG, "itemsToShow=" + itemsToShow + " firstVisibleItem=" + firstVisibleItem + " lastVisibleItem=" + lastVisibleItem + " RvChildCount=" + mRecyclerView.getChildCount());
		if (itemsToShow > 0) {
			int scrollMax = position - firstVisibleItem;
			int scrollMin = Math.max(0, position + subItemsCount - lastVisibleItem);
			int scrollBy = Math.min(scrollMax, scrollMin);
			//Adjust by 1 position for item not completely visible
			int fix = 0;//(position > lastVisibleItem || itemsToShow >= mRecyclerView.getChildCount() ? 1 : 0);
			int scrollTo = firstVisibleItem + scrollBy - fix;
			if (DEBUG)
				Log.v(TAG, "scrollMin=" + scrollMin + " scrollMax=" + scrollMax + " scrollBy=" + scrollBy + " scrollTo=" + scrollTo + " fix=" + fix);
			mRecyclerView.smoothScrollToPosition(scrollTo);
		}
	}

	/*-----------------*/
	/* PRIVATE METHODS */
	/*-----------------*/

	private boolean hasSubItems(T item) {
		return item.getSubItems() != null && item.getSubItems().size() > 0;
	}

	private boolean hasSubItemsSelected(T item) {
		if (item.getSubItems() == null) return false;

		for (T subItem : item.getSubItems()) {
			if (isSelected(getPositionForItem(subItem))) {
				return true;
			}
		}
		return false;
	}

	protected void adjustSelected(int startPosition, int itemCount) {
		List<Integer> selectedItems = getSelectedItems();
		for (Integer position : selectedItems) {
			if (position > startPosition) {
				if (DEBUG)
					Log.v(TAG, "Adjust Selected position " + position + " to " + (position + itemCount));
				int index = selectedItems.indexOf(position);
				position += itemCount;
				selectedItems.set(index, position);
			}
		}
	}

	protected void adjustExpanded(int startPosition, int itemCount) {
		for (int i = 0; i < mExpandedItems.size(); i++) {
			int key = mExpandedItems.keyAt(i);
			if (key > startPosition) {
				if (DEBUG)
					Log.v(TAG, "Adjust Expanded position " + key + " to " + (key + itemCount));
				T item = mExpandedItems.get(key);
				mExpandedItems.remove(key);
				key += itemCount;
				mExpandedItems.put(key, item);
			}
		}
	}

	/*---------------------------*/
	/* ADDING METHODS OVERRIDDEN */
	/*---------------------------*/

	/**
	 * Convenience method of {@link #addSubItem(int, IExpandableItem, IExpandableItem, boolean, boolean)}.
	 * <br/>In this case parent item will never be notified nor expanded if it is collapsed.
	 */
	public void addSubItem(int subPosition, @NonNull T item, @NonNull T parent) {
		this.addSubItem(subPosition, item, parent, false, false);
	}

	/**
	 * Add a sub item inside an expandable item (parent).
	 *
	 * @param subPosition         the new position of the sub item in the parent
	 * @param item                the sub item to add in the parent
	 * @param parent              expandable item that shall contain the sub item
	 * @param expandParent        true to first expand the parent (if needed) and after to add the
	 *                            sub item, false to simply add the sub item to the parent
	 * @param notifyParentChanged true if the parent View will be rebound and its content updated,
	 *                            false to not notify the parent about the addition
	 */
	public void addSubItem(int subPosition, @NonNull T item, @NonNull T parent,
						   boolean expandParent, boolean notifyParentChanged) {
		if (!item.isExpandable()) {
			//Expand parent if requested and not already expanded
			if (expandParent && !parent.isExpanded()) {
				expand(getPositionForItem(parent));
			}
			//Add sub item inside the parent
			parent.addSubItem(subPosition, item);
			//Notify the adapter of the new addition to display it and animate it.
			//If parent is collapsed there's no need to notify about the change.
			if (parent.isExpanded()) {
				int parentPosition = getPositionForItem(parent);
				adjustExpanded(parentPosition + subPosition + 1, 1);
				super.addItem(parentPosition + subPosition + 1, item);
			}
			//Notify the parent about the change if requested
			if (notifyParentChanged) notifyItemChanged(getPositionForItem(parent));
		}
	}

	@Override
	public void addItem(int position, T item) {
		if (!item.isExpandable()) addSubItem(position, item, getExpandableOf(item));
		else {
			adjustExpanded(position, 1);
			super.addItem(position, item);
		}
	}

	/**
	 * Wrapper method of {@link #addItem(int, Object)} for expandable items (parents).
	 *
	 * @param position       the position of the item to add
	 * @param expandableItem item to add, must be an instance of {@link IExpandableItem}
	 */
	public void addExpandableItem(int position, @NonNull T expandableItem) {
		addItem(position, expandableItem);
	}

	/*----------------------------*/
	/* REMOVAL METHODS OVERRIDDEN */
	/*----------------------------*/

	public boolean isItemPendingRemove(int position) {
		for (RemovedItem removedItem : removedItems) {
			if (removedItem.originalPosition == position) return true;
		}
		return false;
	}

	/**
	 * @param position            The position of item to remove
	 * @param notifyParentChanged true to Notify parent of a removal of a child
	 */
	public void removeItem(int position, boolean notifyParentChanged) {
		T item = getItem(position);
		if (!item.isExpandable()) {
			//It's a Child, so get the Parent
			T parent = getExpandableOf(item);
			if (parent != null) {
				int childPosition = parent.getSubItemPosition(item);
				if (childPosition >= 0) {
					removedItems.add(new RemovedItem<T>(position, item, childPosition, parent, notifyParentChanged));
					parent.removeSubItem(childPosition);
					//Notify the Parent about the change if requested
					if (notifyParentChanged) notifyItemChanged(getPositionForItem(parent));
					//Notify the Child removal only if Parent is expanded
					if (parent.isExpanded()) super.removeItem(position);
				}
			}
			if (DEBUG) Log.v(TAG, "removeItem Child:" + removedItems);
		} else {
			//Collapse Parent before removal if it is expanded!
			if (item.isExpanded()) collapse(position);
			removedItems.add(new RemovedItem<T>(position, item));
			if (DEBUG) Log.v(TAG, "removeItem Parent:" + removedItems);
			super.removeItem(position);
		}
	}

	@Override
	public void removeItem(int position) {
		this.removeItem(position, false);
	}

	@Override
	public void removeItems(List<Integer> selectedPositions) {
		this.removeItems(selectedPositions, false);
	}

	public void removeItems(List<Integer> selectedPositions, boolean notifyParentChanged) {
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
				removeItem(selectedPositions.get(0), notifyParentChanged);
				//Align the selection list when removing the item
				selectedPositions.remove(0);
			} else {
				//TODO: Create separated private function removeItemOrRange(selectedPositions)
				if (DEBUG) Log.v(TAG, "removeItems current selection " + getSelectedItems());
				int count = 1;
				while (selectedPositions.size() > count && selectedPositions.get(count).equals(selectedPositions.get(count - 1) - 1)) {
					++count;
				}

				if (count == 1) {
					removeItem(selectedPositions.get(0), notifyParentChanged);
				} else {
					removeRange(selectedPositions.get(count - 1), count, notifyParentChanged);
				}

				for (int i = 0; i < count; ++i) {
					selectedPositions.remove(0);
				}
			}
		}
		isAdapterRunning = false;
		if (mUpdateListener != null) mUpdateListener.onUpdateEmptyView(mItems.size());
	}

	public void removeRange(int positionStart, int itemCount, boolean notifyParentChanged) {
		if (DEBUG)
			Log.v(TAG, "removeRange positionStart=" + positionStart + " itemCount=" + itemCount);

		T parent = null;
		for (int i = 0; i < itemCount; ++i) {
			T item = getItem(positionStart);
			//If item is a Child then, all others are Children as well: we don't allow mixed selections of Parent and Children together
			if (!item.isExpandable()) {
				//It's a Child, so get the Parent
				if (parent == null) parent = getExpandableOf(item);
				if (parent != null) {
					int childPosition = parent.getSubItemPosition(item);
					if (childPosition >= 0) {
						synchronized (mLock) {
							removedItems.add(new RemovedItem<T>(positionStart, item, childPosition, parent, notifyParentChanged));
							parent.removeSubItem(childPosition);
							mItems.remove(positionStart);
						}
					}
				}
			} else {
				synchronized (mLock) {
					removedItems.add(new RemovedItem<T>(positionStart, item));
					//Collapse parent if expanded before removal due to current selection!
					if (item.isExpanded()) collapse(positionStart);
					mItems.remove(positionStart);
				}
			}
		}

		//Notify removals
		if (parent != null) {
			if (DEBUG) Log.v(TAG, "removeRange Children:" + removedItems);
			//Notify the Parent about the change if requested
			if (notifyParentChanged) notifyItemChanged(getPositionForItem(parent));
			//Notify the Children removal only if Parent is expanded
			if (parent.isExpanded()) notifyItemRangeRemoved(positionStart, itemCount);
		} else {
			if (DEBUG) Log.v(TAG, "removeRange Parents:" + removedItems);
			notifyItemRangeRemoved(positionStart, itemCount);
		}
	}

	@Override
	public void removeAllSelectedItems() {
		this.removeItems(getSelectedItems(), false);
	}

	public void removeAllSelectedItems(boolean notifyParentChanged) {
		this.removeItems(getSelectedItems(), notifyParentChanged);
	}

	/*-------------------------*/
	/* UNDO METHODS OVERRIDDEN */
	/*-------------------------*/

	@Override
	public void restoreDeletedItems() {
		stopUndoTimer();
		//TODO: Check if any expandable has been expanded or collapsed meanwhile, above the removed position
		//Reverse insert (list was reverse ordered on Delete)
		for (int i = removedItems.size() - 1; i >= 0; i--) {
			//Restore child
			RemovedItem removedItem = removedItems.get(i);
			if (!removedItem.item.isExpandable()) {
				Log.v(TAG, "Restore Child " + removedItem.item + " on position " + removedItem.originalPosition);
				addSubItem(removedItem.originalPositionInParent, (T) removedItem.item,
						(T) removedItem.parent, false, removedItem.notifyParentChanged);
			} else {//Restore parent
				Log.v(TAG, "Restore Parent " + removedItem.item + " on position " + removedItem.originalPosition);
				addItem(removedItem.originalPosition, (T) removedItem.item);
			}
		}
		emptyBin();
	}

	@Override
	public synchronized void emptyBin() {
		super.emptyBin();
		removedItems.clear();
	}

	@Override
	public List<T> getDeletedItems() {
		List<T> deletedItems = new ArrayList<T>();
		for (RemovedItem removedItem : removedItems) {
			deletedItems.add((T) removedItem.item);
		}
		return deletedItems;
	}

	/*----------------*/
	/* INSTANCE STATE */
	/*----------------*/

	/**
	 * Save the state of the current expanded items.
	 *
	 * @param outState Current state
	 */
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.v(TAG, "SaveInstanceState for expanded items");
		outState.putIntArray(TAG, getExpandedItems());
	}

	/**
	 * Restore the previous state of the expanded items.
	 *
	 * @param savedInstanceState Previous state
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "RestoreInstanceState for expanded items");
		//First restore opened collapsible items, as otherwise may not all selections could be restored
		int[] expandedItems = savedInstanceState.getIntArray(TAG);
		if (expandedItems != null) {
			for (Integer expandedItem : expandedItems) {
				expand(expandedItem);
			}
		}
		//Restore selection state
		super.onRestoreInstanceState(savedInstanceState);
	}

	/*---------------*/
	/* INNER CLASSES */
	/*---------------*/

	private static class RemovedItem<T extends IExpandableItem<T>> {
		int originalPosition = -1;
		int originalPositionInParent = -1;
		T item = null;
		T parent = null;
		boolean notifyParentChanged = false;

		public RemovedItem(int originalPosition, T item) {
			this(originalPosition, item, -1, null, false);
		}

		public RemovedItem(int originalPosition, T item, int originalPositionInParent, T parent, boolean notifyParentChanged) {
			this.originalPosition = originalPosition;
			this.item = item;
			this.originalPositionInParent = originalPositionInParent;
			this.parent = parent;
			this.notifyParentChanged = notifyParentChanged;
		}

		@Override
		public String toString() {
			return "RemovedItem[originalPosition=" + originalPosition +
					", item=" + item +
					", originalPositionInParent=" + originalPosition +
					", parent=" + parent + "]";
		}
	}

}