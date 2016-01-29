package eu.davidea.flexibleadapter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.davidea.flexibleadapter.items.IExpandableItem;
import eu.davidea.flexibleadapter.items.IFlexibleItem;
import eu.davidea.viewholders.ExpandableViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This adapter provides a set of standard methods to expand and collapse an Item.
 *
 * @author Davide Steduto
 * @since 16/01/2016 Created
 */
public abstract class FlexibleExpandableAdapter<EVH extends ExpandableViewHolder, T extends IExpandableItem<T>>
		extends FlexibleAnimatorAdapter<FlexibleViewHolder, T> {

	private static final String TAG = FlexibleExpandableAdapter.class.getSimpleName();
	public static final int EXPANDABLE_VIEW_TYPE = -1;

	private SparseArray<List<T>> mExpandedItems;
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
		mExpandedItems = new SparseArray<List<T>>();
		removedItems = new ArrayList<RemovedItem>();
		expandInitialItems();

		//Get notified when items are inserted or removed
		//(should adjust selected, expanded and removed positions)
		registerAdapterDataObserver(new ExpandableAdapterDataObserver());
	}

	protected void expandInitialItems() {
		//Set initially expanded
		for (int i = 0; i < mItems.size(); i++) {
			T item = mItems.get(i);
			//FIXME: Foreseen bug on Rotation: coordinate expansion with onRestoreInstanceState
			if (item.isExpanded() && item.getSubItemsCount() > 0) {
				if (DEBUG) Log.v(TAG, "Initially expand item on position " + i);
				List<T> subItems = new ArrayList<T>(item.getSubItems());
				mExpandedItems.put(i, subItems);
				mItems.addAll(i + 1, subItems);
				i += subItems.size();
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
		if (item != null && item.isSelectable()) {
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
		//Reset flags if necessary, just to be sure
		if (getSelectedItemCount() == 0) parentSelected = childSelected = false;
	}

	/**
	 * Helper to select only expandable items or specific view types.
	 *
	 * @param viewTypes All non expandable ViewTypes for which we want the selection,
	 *                  pass nothing to select expandable ViewTypes.
	 */
	public void selectAll(Integer... viewTypes) {
		T item = getItem(getSelectedPositions().get(0));
		if (getSelectedItemCount() > 0 && item != null && item.isExpandable())
			super.selectAll(EXPANDABLE_VIEW_TYPE);//Select only Parents, Skip others
		else
			super.selectAll(viewTypes);//Select others
	}

	@Override
	@CallSuper
	public void clearSelection() {
		parentSelected = childSelected = false;
		super.clearSelection();
	}

	public boolean isAnyParentSelected() {
		return parentSelected;
	}

	public boolean isAnyChildSelected() {
		return childSelected;
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	//FIXME: Find a way to Not animate items with ItemAnimator!!!
	//TODO: Customize child items animations (don't use add or remove ItemAnimator)

	/**
	 * Automatically collapse all previous expanded parents before expand the clicked parent.
	 * <p>Default value is disabled.</p>
	 *
	 * @param collapseOnExpand true to collapse others items, false to just expand the current
	 */
	public void setAutoCollapseOnExpand(boolean collapseOnExpand) {
		mCollapseOnExpand = collapseOnExpand;
	}

	/**
	 * Automatically scroll the clicked expandable item to the first visible position.<br/>
	 * Default disabled.
	 * <p>This works ONLY in combination with {@link SmoothScrollLinearLayoutManager}.
	 * GridLayout is still NOT supported.</p>
	 *
	 * @param scrollOnExpand true to enable automatic scroll, false to disable
	 */
	public void setAutoScrollOnExpand(boolean scrollOnExpand) {
		mScrollOnExpand = scrollOnExpand;
	}

	public boolean isExpanded(int position) {
		return mExpandedItems.indexOfKey(position) >= 0;
	}

	public boolean isExpandable(int position) {
		T item = getItem(position);
		return item != null && item.isExpandable();
	}

	/**
	 * Retrieves the parent of a child.
	 * <p>Only for a real child of an expanded parent.</p>
	 *
	 * @param child the child item
	 * @return the parent of this child item or null if, child is a parent itself or not found
	 * @see #getExpandablePositionOf(IExpandableItem)
	 * @see #getRelativePositionOf(IExpandableItem)
	 */
	public T getExpandableOf(T child) {
		return getItem(getExpandablePositionOf(child));
	}

	/**
	 * Retrieves the parent position of a child.
	 * <p>Only for a real child of an expanded parent.</p>
	 *
	 * @param child the child item
	 * @return the parent position of this child item or -1 if, child is a parent itself or not found
	 * @see #getExpandableOf(IExpandableItem)
	 * @see #getRelativePositionOf(IExpandableItem)
	 */
	public int getExpandablePositionOf(T child) {
		int index = getIndexOf(child);
		if (index >= 0) return mExpandedItems.keyAt(getIndexOf(child));
		return -1;
	}

	/**
	 * Retrieves the position of a child in the list where it lays.
	 * <p>Only for a real child of an expanded parent.</p>
	 *
	 * @param child the child item
	 * @return the position in the parent or -1 if, child is a parent itself or not found
	 * @see #getExpandableOf(IExpandableItem)
	 * @see #getExpandablePositionOf(IExpandableItem)
	 */
	public int getRelativePositionOf(T child) {
		return getSiblingsOf(child).indexOf(child);
	}

	/**
	 * Provides the list where the child currently lays.
	 *
	 * @param child the child item
	 * @return the list of the child element, or a new list if item
	 * @see #getExpandableOf(IExpandableItem)
	 * @see #getExpandablePositionOf(IExpandableItem)
	 * @see #getRelativePositionOf(IExpandableItem)
	 * @see #getExpandedItems()
	 */
	public List<T> getSiblingsOf(T child) {
		int index = getIndexOf(child);
		if (index >= 0) return mExpandedItems.valueAt(index);
		return new ArrayList<T>();
	}

	/**
	 * Provides a list of all expandable items that are currently expanded.
	 *
	 * @return a list with all expanded items
	 * @see #getSiblingsOf(IExpandableItem)
	 * @see #getExpandedPositions()
	 */
	public List<T> getExpandedItems() {
		int length = mExpandedItems.size();
		List<T> expandedItems = new ArrayList<T>(length);
		for (int i = 0; i < length; i++) {
			expandedItems.add(getItem(mExpandedItems.keyAt(i)));
		}
		return expandedItems;
	}

	/**
	 * Provides a list of all expandable positions that are currently expanded.
	 *
	 * @return a list with the global positions of all expanded items
	 * @see #getSiblingsOf(IExpandableItem)
	 * @see #getExpandedItems()
	 */
	public List<Integer> getExpandedPositions() {
		int length = mExpandedItems.size();
		List<Integer> expandedItems = new ArrayList<Integer>(length);
		for (int i = 0; i < length; i++) {
			expandedItems.add(mExpandedItems.keyAt(i));
		}
		return expandedItems;
	}

	/**
	 * Returns the ViewType for an Expandable Item and for all others ViewTypes depends
	 * by the current position.
	 *
	 * @param position position for which ViewType is requested
	 * @return -1 for {@link #EXPANDABLE_VIEW_TYPE};
	 * otherwise 0 (default) or any user value for all others ViewType
	 */
	@Override
	public int getItemViewType(int position) {
		T item = getItem(position);
		if (item != null && item.isExpandable())
			return EXPANDABLE_VIEW_TYPE;
		return super.getItemViewType(position);
	}

	/**
	 * Creates ViewHolder that is expandable. Will return any ViewHolder of class that extends
	 * {@link ExpandableViewHolder}.
	 *
	 * @param parent   The ViewGroup into which the new View will be added after it is bound to
	 *                 an adapter position.
	 * @param viewType The view type of the new View. Value {@link #EXPANDABLE_VIEW_TYPE}
	 *                 = {@value #EXPANDABLE_VIEW_TYPE}
	 * @return A new {@link ExpandableViewHolder} that holds a View that can be expanded or collapsed
	 */
	public abstract EVH onCreateExpandableViewHolder(ViewGroup parent, int viewType);

	/**
	 * Creates ViewHolder that generally is not expandable or it's a child of an
	 * {@link ExpandableViewHolder}. Will return any ViewHolder of class that extends
	 * {@link FlexibleViewHolder}.
	 * <p>This is the good place to create and return any custom ViewType that extends
	 * {@link FlexibleViewHolder}.</p>
	 *
	 * @param parent   The ViewGroup into which the new View will be added after it is bound to
	 *                 an adapter position.
	 * @param viewType The view type of the new View, must be different of
	 *                 {@link #EXPANDABLE_VIEW_TYPE} = {@value #EXPANDABLE_VIEW_TYPE}.
	 * @return A new FlexibleViewHolder that holds a View that can be child of the expanded views.
	 */
	public abstract FlexibleViewHolder onCreateFlexibleViewHolder(ViewGroup parent, int viewType);

	/**
	 * No more override is allowed here!
	 * <p>Use {@link #onCreateExpandableViewHolder(ViewGroup, int)} to create expandable
	 * ViewHolder.<br/>
	 * Use {@link #onCreateFlexibleViewHolder(ViewGroup, int)} to create normal or child
	 * ViewHolder instead.</p>
	 *
	 * @param parent   the ViewGroup into which the new View will be added after it is bound
	 *                 to an adapter position
	 * @param viewType the view type of the new View
	 * @return a new {@link FlexibleViewHolder} that holds a View of the given view type
	 * @see #onCreateExpandableViewHolder(ViewGroup, int)
	 * @see #onCreateFlexibleViewHolder(ViewGroup, int)
	 */
	@Override
	public final FlexibleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == EXPANDABLE_VIEW_TYPE) {
			return onCreateExpandableViewHolder(parent, viewType);
		} else {
			return onCreateFlexibleViewHolder(parent, viewType);
		}
	}

	/**
	 * Method to bind only Expandable items that implement {@link IExpandableItem}.
	 *
	 * @param holder   the ViewHolder created of type {@link ExpandableViewHolder}
	 * @param position the adapter position to bind
	 */
	public abstract void onBindExpandableViewHolder(EVH holder, int position);

	/**
	 * Method to bind all others no-Expandable items that implement {@link IFlexibleItem}.
	 *
	 * @param holder   the ViewHolder created of type {@link FlexibleViewHolder}
	 * @param position the adapter position to bind
	 * @see #onBindExpandableViewHolder(ExpandableViewHolder, int)
	 */
	public abstract void onBindFlexibleViewHolder(FlexibleViewHolder holder, int position);

	/**
	 * No more override is allowed here!
	 * <p>Use {@link #onBindExpandableViewHolder(ExpandableViewHolder, int)} to bind expandable
	 * ViewHolder.<br/>
	 * Use {@link #onBindFlexibleViewHolder(FlexibleViewHolder, int)} to bind normal or child
	 * ViewHolder instead.</p>
	 *
	 * @param holder   the ViewHolder created
	 * @param position the adapter position to bind
	 * @see #onBindFlexibleViewHolder(FlexibleViewHolder, int)
	 * @see #onBindExpandableViewHolder(ExpandableViewHolder, int)
	 */
	@Override
	public final void onBindViewHolder(FlexibleViewHolder holder, int position) {
		if (getItemViewType(position) == EXPANDABLE_VIEW_TYPE) {
			onBindExpandableViewHolder((EVH) holder, position);
		} else {
			onBindFlexibleViewHolder(holder, position);
		}
	}

	/**
	 * Expands an item that is Expandable, not yet expanded, that has subItems and
	 * no child is selected.
	 *
	 * @param position the position of the item to expand
	 * @return the number of subItems expanded
	 */
	public int expand(int position) {
		return expand(position, false);
	}

	private int expand(int position, boolean expandAll) {
		if (DEBUG)
			Log.v(TAG, "Request to Expand on position " + position + " ExpandedItems=" + getExpandedPositions());
		final T item = getItem(position);
		int subItemsCount = 0;
		if (item != null && item.isExpandable() && !item.isExpanded() &&
				item.getSubItemsCount() > 0 && !parentSelected) {

			//Collapse others expandable if configured so
			//Skipped when expanding all is requested
			if (mCollapseOnExpand && !expandAll) {
				//Fetch again the new position after collapsing!!
				if (collapseAll() > 0) position = getGlobalPositionOf(item);
			}

			//Every time an expansion is requested, subItems must be taken from the original Object!
			//Save a copy child items list and expanded state
			List<T> subItems = new ArrayList<T>(item.getSubItems());
			mItems.addAll(position + 1, subItems);
			subItemsCount = subItems.size();
			item.setExpanded(true);

			//Update the expanded list
			mExpandedItems.put(position, subItems);

			//Adjust selection and expandable positions, that are grater than the expanded position
//			adjustSelected(position, subItemsCount);
//			adjustExpanded(position, subItemsCount);
			adjustRemoved(position, subItemsCount);

			//Automatically scroll the current expandable item to show as much children as possible
			if (mScrollOnExpand) {
				//Must be delayed to give time at RecyclerView to recalculate positions
				//after an automatic collapse
				final int pos = position, count = subItemsCount;
				Handler animatorHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
					public boolean handleMessage(Message message) {
						autoScroll(pos, count);
						return true;
					}
				});
				animatorHandler.sendMessageDelayed(Message.obtain(mHandler), 150L);
			}

			//Expand!
			notifyItemRangeInserted(position + 1, subItemsCount);

			if (DEBUG)
				Log.v(TAG, "Expanded " + subItemsCount + " subItems on position=" + position + " ExpandedItems=" + getExpandedPositions());
		}
		return subItemsCount;
	}

	/**
	 * @return the number of parent successfully expanded
	 */
	public int expandAll() {
		int expanded = 0;
		//More efficient if we expand from first collapsible position
		for (int i = 0; i < mItems.size() - 1; i++) {
			if (expand(i, true) > 0) expanded++;
		}
		return expanded;
	}

	/**
	 * Collapses an Expandable item that is already expanded, in conjunction with no subItems
	 * selected or item is pending removal (used in combination with removeRange).
	 *
	 * @param position the position of the item to collapse
	 * @return the number of subItems collapsed
	 */
	public int collapse(int position) {
		Log.v(TAG, "Request to Collapse on position " + position + " ExpandedItems=" + getExpandedPositions());
		T item = getItem(position);
		int subItemsCount = 0;
		if (item != null && item.isExpandable() && item.isExpanded() &&
				(!hasSubItemsSelected(position) || isItemPendingRemove(position))) {

			//Take the current subList
			List<T> subItems = mExpandedItems.valueAt(position);
			mItems.removeAll(subItems);
			subItemsCount = subItems.size();
			item.setExpanded(false);

			//Update the expanded list
			int indexOfKey = mExpandedItems.indexOfKey(position);
			if (indexOfKey >= 0) {
				mExpandedItems.removeAt(indexOfKey);
			}

			//Adjust selection and expandable positions, that are grater than the collapsed position
//			adjustSelected(position, -subItemsCount);
//			adjustExpanded(position, -subItemsCount);
			adjustRemoved(position, -subItemsCount);

			//Collapse!
			notifyItemRangeRemoved(position + 1, subItemsCount);

			if (DEBUG)
				Log.v(TAG, "Collapsed " + subItemsCount + " subItems on position=" + position + " ExpandedItems=" + getExpandedPositions());
		}
		return subItemsCount;
	}

	/**
	 * @return the number of parent successfully collapsed
	 */
	public int collapseAll() {
		int collapsed = 0;
		//More efficient if we collapse from last expanded position
		for (int i = mExpandedItems.size() - 1; i >= 0; i--) {
			if (collapse(mExpandedItems.keyAt(i)) > 0) collapsed++;
		}
		return collapsed;
	}

	/*---------------------------*/
	/* ADDING METHODS OVERRIDDEN */
	/*---------------------------*/

	/**
	 * Convenience method of {@link #addSubItem(int, int, IExpandableItem, boolean, boolean)}.
	 * <br/>In this case parent item will never be notified nor expanded if it is collapsed.
	 */
	public void addSubItem(int parentPosition, int subPosition, @NonNull T item) {
		this.addSubItem(parentPosition, subPosition, item, false, false);
	}

	/**
	 * Add an item inside the list of an expandable item (parent).
	 * <p><b>In order to add a subItem</b>, the following conditions must be satisfied:
	 * <br/>- The subItem is not expandable
	 * <br/>- The item resulting from the parent position is actually an Expandable.</p>
	 *
	 * @param parentPosition      position of the expandable item that shall contain the subItem
	 * @param subPosition         the position of the subItem in the expandable list
	 * @param item                the subItem to add in the expandable list
	 * @param expandParent        true to initially expand the parent (if needed) and after to add
	 *                            the subItem, false to simply add the sub item to the parent
	 * @param notifyParentChanged true if the parent View must be rebound and its content updated,
	 *                            false to not notify the parent about the addition
	 */
	public void addSubItem(int parentPosition, int subPosition, @NonNull T item,
						   boolean expandParent, boolean notifyParentChanged) {
		T parent = getItem(parentPosition);
		if (parent != null && parent.isExpandable() && !item.isExpandable()) {
			//Expand parent if requested and not already expanded
			if (expandParent && !parent.isExpanded()) {
				expand(parentPosition);
			}

			//Add sub item inside the parent
			addItemIn(mExpandedItems.valueAt(parentPosition), subPosition, item);
			//Notify the adapter of the new addition to display it and animate it.
			//If parent is collapsed there's no need to notify about the change.
			if (parent.isExpanded()) {
				super.addItem(parentPosition + 1 + Math.max(0, subPosition), item);
			}
			//Notify the parent about the change if requested
			if (notifyParentChanged) notifyItemChanged(parentPosition);
		}
	}

	//TODO: make addItems for notifyItemRangeInserted()

	/**
	 * Internal method to add a new item at specific position in an expandable list.
	 * <p>If list is null, a new list will be created.</p>
	 *
	 * @param list     which list
	 * @param position position inside the list, -1 to add the item the end of the list
	 * @param item     item to insert
	 * @return always true
	 */
	private boolean addItemIn(List<T> list, int position, T item) {
		if (list == null) list = new ArrayList<T>(1);
		if (position >= 0 && position < list.size()) {
			list.add(position, item);
		} else
			list.add(item);
		return true;
	}

	/**
	 * Internal method to add a set of items at specific position in an expandable list.
	 * <p>If list is null, a new list will be created.</p>
	 * No effect if the set of items is null or empty.
	 *
	 * @param list     which list
	 * @param position position inside the list, -1 to add the set the end of the list
	 * @param items    set of items to insert
	 * @return true if the list has been modified through the insertion, false otherwise
	 */
	private boolean addAllItemsIn(List<T> list, int position, List<T> items) {
		if (items == null || items.isEmpty()) return false;
		if (list == null) list = new ArrayList<T>(items.size());
		if (position >= 0 && position < list.size()) {
			list.addAll(position, items);
		} else
			list.addAll(items);
		return true;
	}

	/**
	 * Wrapper method of {@link #addItem(int, Object)} for expandable items (Parents).
	 *
	 * @param position       the position of the item to add
	 * @param expandableItem item to add, must be an instance of {@link IExpandableItem}
	 */
	public void addExpandableItem(int position, @NonNull T expandableItem) {
		super.addItem(position, expandableItem);
	}

	/*----------------------------*/
	/* REMOVAL METHODS OVERRIDDEN */
	/*----------------------------*/

	/**
	 * Same as {@link #removeItem(int, boolean)} but in this case the Parent will not be
	 * notified about the removal.</p>
	 *
	 * @see #removeItem(int, boolean)
	 */
	@Override
	public void removeItem(int position) {
		this.removeItem(position, false);
	}

	/**
	 * Removes an item from the internal list and notify the change.
	 * <p>If the item, resulting from the passed position is:
	 * <br/>- expandable, it is removed as usual, also, it will be collapsed if expanded.
	 * <br/>- not expandable, it is removed only if the parent is expanded.
	 * Optionally the parent can be notified about the removal.</p>
	 * The item is retained for an eventual Undo.
	 * <p>The item must implement {@link IExpandableItem}.</p>
	 *
	 * @param position            The position of item to remove
	 * @param notifyParentChanged true to notify parent of a removal of a child, false if not
	 *                            necessary.
	 * @see #removeItem(int)
	 */
	public void removeItem(int position, boolean notifyParentChanged) {
		T item = getItem(position);
		if (item == null) {//Item is not displayed, warn it and finish.
			Log.w(TAG, "Cannot removeItem on position out of OutOfBound!");
			return;
		}
		if (!item.isExpandable()) {
			//It's a Child, so get the Parent that is already expanded
			int parentPosition = getExpandablePositionOf(item);
			if (parentPosition >= 0) {
				List<T> siblings = getSiblingsOf(item);
				int childPosition = siblings.indexOf(item);
				removedItems.add(new RemovedItem<T>(position, item, childPosition, parentPosition, notifyParentChanged));
				siblings.remove(item);
				super.removeItem(position);
				//Notify the Parent about the change if requested
				if (notifyParentChanged) notifyItemChanged(parentPosition);
			}//Else not possible to update the parentPosition, position is unknown.
			if (DEBUG) Log.v(TAG, "removeItem Child:" + removedItems);
		} else {
			//Collapse Parent before removal if it is expanded!
			if (item.isExpanded()) collapse(position);
			removedItems.add(new RemovedItem<T>(position, item));
			if (DEBUG) Log.v(TAG, "removeItem Parent:" + removedItems);
			super.removeItem(position);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>Parent will not be notified about the change, if a child is removed.</p>
	 */
	@Override
	public void removeItems(List<Integer> selectedPositions) {
		this.removeItems(selectedPositions, false);
	}

	//TODO: make removeItems simpler
	public void removeItems(List<Integer> selectedPositions, boolean notifyParentChanged) {
		//Check list is not empty
		if (selectedPositions == null || selectedPositions.isEmpty()) return;
		//Reverse-sort the list, start from last position for efficiency
		Collections.sort(selectedPositions, new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs) {
				return rhs - lhs;
			}
		});
		//Split the list in ranges
		int positionStart = 0, itemCount = 0;
		int lastPosition = selectedPositions.get(0);
		for (Integer position : selectedPositions) {//10 9 8 5 4 1
			collapse(position);
			if (lastPosition - itemCount == position) {//10-0==10   10-1==9   10-2==8   10-3==5 NO   //5-1=4   5-2==1 NO
				itemCount++;//1  2  3   //2
				positionStart = position;//10  9  8   //4
			} else {
				if (itemCount > 0)
					removeRange(positionStart, itemCount, notifyParentChanged);//8,3   //4,2
				positionStart = lastPosition = position;//5   //1
				itemCount = 1;
			}
		}
		if (itemCount > 0) removeRange(positionStart, itemCount, notifyParentChanged);//1,1
	}

	public void removeItemsOld(List<Integer> selectedPositions, boolean notifyParentChanged) {
		//Reverse-sort the list
		Collections.sort(selectedPositions, new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs) {
				return lhs - rhs;
			}
		});
		//Split the list in ranges
		while (!selectedPositions.isEmpty()) {
			isMultiRemove = true;
			if (selectedPositions.size() == 1) {
				removeItem(selectedPositions.get(0), notifyParentChanged);
				//Align the selection list when removing the item
				selectedPositions.remove(0);
			} else {
				if (DEBUG) Log.v(TAG, "removeItems current selection " + getSelectedPositions());
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
		isMultiRemove = false;
		if (mUpdateListener != null) mUpdateListener.onUpdateEmptyView(getItemCount());
	}

	@Override
	public void removeRange(int positionStart, int itemCount) {
		removeRange(positionStart, itemCount, false);
	}

	public void removeRange(int positionStart, int itemCount, boolean notifyParentChanged) {
		if (DEBUG)
			Log.v(TAG, "removeRange positionStart=" + positionStart + " itemCount=" + itemCount);
		int parentPosition = -1;
		for (int i = (positionStart + itemCount); i >= positionStart; i--) {
			T item = getItem(i);
			if (item == null) continue;
			if (!item.isExpandable()) {
				int index = getIndexOf(item);
				parentPosition = mExpandedItems.keyAt(index);
				List<T> siblings = mExpandedItems.valueAt(index);
				int childPosition = siblings.indexOf(item);
				removedItems.add(new RemovedItem<T>(i, item, childPosition, parentPosition, notifyParentChanged));
				siblings.remove(childPosition);
			} else {
				//Collapse Parent before removal if it is expanded!
				if (item.isExpanded()) collapse(i);
				removedItems.add(new RemovedItem<T>(i, item));
			}
			//Remove item from internal list
			synchronized (mLock) {
				mItems.remove(i);
			}
		}
		//Notify removals
		if (parentPosition >= 0) {
			if (DEBUG) Log.v(TAG, "removeRange Children:" + removedItems);
			//Notify the Parent about the change if requested
			if (notifyParentChanged) notifyItemChanged(parentPosition);
			//Notify the Children removal only if Parent is expanded
			notifyItemRangeRemoved(positionStart, itemCount);
		} else {
			if (DEBUG) Log.v(TAG, "removeRange Parents:" + removedItems);
			notifyItemRangeRemoved(positionStart, itemCount);
		}
	}

	public void removeRangeOld(int positionStart, int itemCount, boolean notifyParentChanged) {
		if (DEBUG)
			Log.v(TAG, "removeRange positionStart=" + positionStart + " itemCount=" + itemCount);
		T parent = null;
		for (int i = 0; i < itemCount; ++i) {
			T item = getItem(positionStart);
			//If item is a Child then, all others must be Children as well:
			//We didn't allow mixed selections of Parent and Children together
			if (item != null && !item.isExpandable()) {
				//It's a Child, so get the Parent
				if (parent == null) parent = getExpandableOf(item);
				if (parent != null) {
					int childPosition = parent.getSubItemPosition(item);
					if (childPosition >= 0) {
						synchronized (mLock) {
							removedItems.add(new RemovedItem<T>(positionStart, item, childPosition, getExpandablePositionOf(item), notifyParentChanged));
							//FIXME: Removal of child should not be done here, verify all childPositions
							parent.removeSubItem(childPosition);
							mItems.remove(positionStart);
						}
					}
				}
			} else if (item != null) {
				//It's a Parent
				synchronized (mLock) {
					removedItems.add(new RemovedItem<T>(positionStart, item));
					//Collapse parent if expanded before removal due to current selection!
					if (item.isExpanded()) {
						//Collapsing in removeRange, positionStart doesn't match, we fix with [+i]
						int indexOfKey = mExpandedItems.indexOfKey(positionStart + i);
						if (DEBUG)
							Log.v(TAG, "indexOfKey=" + indexOfKey + " key=" + (positionStart + i));
						if (indexOfKey >= 0) mExpandedItems.removeAt(indexOfKey);
						collapse(positionStart);
					}
					mItems.remove(positionStart);
				}
			}
		}

		//Notify removals
		if (parent != null) {
			if (DEBUG) Log.v(TAG, "removeRange Children:" + removedItems);
			//Notify the Parent about the change if requested
			if (notifyParentChanged) notifyItemChanged(getGlobalPositionOf(parent));
			//Notify the Children removal only if Parent is expanded
			if (parent.isExpanded()) notifyItemRangeRemoved(positionStart, itemCount);
		} else {
			if (DEBUG) Log.v(TAG, "removeRange Parents:" + removedItems);
			notifyItemRangeRemoved(positionStart, itemCount);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>Parent will not be notified about the change, if a child is removed.</p>
	 */
	@Override
	public void removeAllSelectedItems() {
		this.removeItems(getSelectedPositions(), false);
	}

	/**
	 * Convenience method to remove all Items that are currently selected.<p>
	 * User can choose to notify the Parent about the change, if a child is removed.
	 *
	 * @param notifyParentChanged true to Notify Parent of a removal of its child
	 */
	public void removeAllSelectedItems(boolean notifyParentChanged) {
		this.removeItems(getSelectedPositions(), notifyParentChanged);
	}

	/*-------------------------*/
	/* MOVE METHODS OVERRIDDEN */
	/*-------------------------*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean shouldMove(int fromPosition, int toPosition) {
		//TODO: Implement logic for views, when expandable items are already expanded or collapsed.
		boolean move = false;
//		T fromItem = null, toItem = null;
//		if (fromPosition > toPosition) {
//			fromItem = getItem(fromPosition);
//			toItem = getItem(Math.max(0, toPosition - 1));
//		} else {
//			fromItem = getItem(fromPosition);
//			toItem = getItem(toPosition);
//		}
//
//		if (DEBUG) Log.v(TAG, "shouldMove from=" + fromPosition + " to=" + toPosition);
//		if (DEBUG) Log.v(TAG, "shouldMove fromItem=" + fromItem + " toItem=" + toItem);
//
//		if (!fromItem.isExpandable() && toItem.isExpandable() && !toItem.isExpanded()) {
//			expand(getGlobalPositionOf(toItem));
//			move = false;
//		} else if (!fromItem.isExpandable() && !toItem.isExpandable()) {
//			move = true;
//		} else if (fromItem.isExpandable() && !toItem.isExpandable()) {
//			move = false;
//		}
//		if (DEBUG) Log.v(TAG, "shouldMove move=" + move);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onItemMove(int fromPosition, int toPosition) {
		return super.onItemMove(fromPosition, toPosition);
	}

	/*-------------------------*/
	/* UNDO METHODS OVERRIDDEN */
	/*-------------------------*/

	/**
	 * @param position the position to check
	 * @return true if item was removed for the Adapter but change not yet committed, false otherwise
	 */
	public boolean isItemPendingRemove(int position) {
		for (RemovedItem removedItem : removedItems) {
			if (removedItem.originalPosition == position) return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restoreDeletedItems() {
		stopUndoTimer();
		//Reverse insert (list was reverse ordered on Delete)
		for (int i = removedItems.size() - 1; i >= 0; i--) {
			RemovedItem removedItem = removedItems.get(i);
			if (!removedItem.item.isExpandable()) {
				//Restore child
				if (DEBUG)
					Log.v(TAG, "Restore Child " + removedItem.item + " on position " + removedItem.originalPosition);
				//TODO: Check if Child is filtered out by the current filter, if yes continue!
				addSubItem(removedItem.parentPosition, removedItem.originalPositionInParent,
						(T) removedItem.item, false, removedItem.notifyParentChanged);
			} else {
				//Restore parent
				if (DEBUG)
					Log.v(TAG, "Restore Parent " + removedItem.item + " on position " + removedItem.originalPosition);
				//TODO: Check if Parent is filtered out by the current filter, if yes continue!
				addItem(removedItem.originalPosition, (T) removedItem.item);
			}
			//Restore selection before emptyBin, if configured
			if (mRestoreSelection) {
				if (removedItem.item.isExpandable()) parentSelected = true;
				else childSelected = true;
				getSelectedPositions().add(removedItem.originalPosition);
			}
		}
		emptyBin();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void emptyBin() {
		super.emptyBin();
		removedItems.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> getDeletedItems() {
		List<T> deletedItems = new ArrayList<T>();
		for (RemovedItem removedItem : removedItems) {
			deletedItems.add((T) removedItem.item);
		}
		return deletedItems;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> getDeletedPositions() {
		List<Integer> deletedItems = new ArrayList<Integer>();
		for (RemovedItem removedItem : removedItems) {
			deletedItems.add(removedItem.originalPosition);
		}
		return deletedItems;
	}

	/*---------------------------*/
	/* FILTER METHODS OVERRIDDEN */
	/*---------------------------*/

	/**
	 * {@inheritDoc}
	 */
	@Override
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
						//Add subItems if not hidden by filterObject()
						if (item.getSubItems() != null) {
							for (T subItem : item.getSubItems())
								if (!subItem.isHidden()) values.add(subItem);
						}
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
			mUpdateListener.onUpdateEmptyView(super.getItemCount());
		}
	}

	/**
	 * This method performs filtering on the subItems of the provided expandable and returns
	 * true, if the expandable should be in the filtered collection, or false if it shouldn't.
	 * <p>DEFAULT IMPLEMENTATION, OVERRIDE TO HAVE OWN FILTER!</p>
	 *
	 * @param item       the object with subItems to be inspected
	 * @param constraint constraint, that the object has to fulfil
	 * @return true, if the object should be in the filteredResult, false otherwise
	 */
	@Override
	protected boolean filterObject(T item, String constraint) {
		//Reset expansion flag
		item.setExpanded(false);
		boolean filtered = false;

		//Children scan filter
		if (item.getSubItems() != null) {
			for (T subItem : item.getSubItems()) {
				//Reuse super filter for Children
				subItem.setHidden(!super.filterObject(subItem, constraint));
				if (!filtered && !subItem.isHidden()) {
					filtered = true;
				}
			}
			//Expand if filter found text in subItems
			item.setExpanded(filtered);
		}

		//Super filter for Parent only if not filtered already
		return filtered || super.filterObject(item, constraint);
	}

	/*-----------------*/
	/* PRIVATE METHODS */
	/*-----------------*/

	private int getIndexOf(T child) {
		if (!child.isExpandable()) {//Only for a real child
			for (int i = 0; i < mExpandedItems.size(); i++) {
				if (mExpandedItems.valueAt(i).contains(child)) return i;
			}
		}
		return -1;
	}

	private boolean hasSubItemsSelected(int position) {
		for (T subItem : mExpandedItems.valueAt(position)) {
			if (isSelected(getGlobalPositionOf(subItem))) {
				return true;
			}
		}
		return false;
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
		} else if (position < firstVisibleItem) {
			mRecyclerView.smoothScrollToPosition(position);
		}
	}

	//TODO: Deeply test adjustPositions
	private void adjustSelected(int startPosition, int itemCount) {
		List<Integer> selectedPositions = getSelectedPositions();
		boolean adjusted = false;
		for (Integer position : selectedPositions) {
			if (position >= startPosition) {
				if (DEBUG)
					Log.v(TAG, "Adjust Selected position " + position + " to " + Math.max(position + itemCount, startPosition));
				int index = selectedPositions.indexOf(position);
				position += itemCount;
				selectedPositions.set(index, Math.max(position, startPosition));
				adjusted = true;
			}
		}
		if (DEBUG && adjusted) Log.v(TAG, "AdjustedSelected=" + getSelectedPositions());
	}

	private void adjustExpanded(int startPosition, int itemCount) {
		boolean adjusted = false;
		for (int i = 0; i < mExpandedItems.size(); i++) {
			int position = mExpandedItems.keyAt(i);
			if (position >= startPosition) {//= is for insertion at that position
				if (DEBUG)
					Log.v(TAG, "Adjust Expanded from position " + position + " to " + (position + itemCount));
				List<T> subItems = mExpandedItems.get(position);
				mExpandedItems.remove(position);
				position += itemCount;
				mExpandedItems.put(position, subItems);
				adjusted = true;
			}
		}
		if (DEBUG && adjusted) Log.v(TAG, "AdjustedExpanded=" + getExpandedPositions());
	}

	private void adjustRemoved(int startPosition, int itemCount) {
		boolean adjusted = false;
		for (RemovedItem removedItem : removedItems) {
			if (removedItem.originalPosition > startPosition) {
				if (DEBUG)
					Log.v(TAG, "Adjust Removed position " + removedItem.originalPosition + " to " + (removedItem.originalPosition + itemCount));
				removedItem.originalPosition += itemCount;
				adjusted = true;
			}
		}
		if (DEBUG && adjusted) Log.v(TAG, "AdjustedRemoved=" + getDeletedPositions());
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
		outState.putIntegerArrayList(TAG, (ArrayList<Integer>) getExpandedPositions());
	}

	/**
	 * Restore the previous state of the expanded items.
	 *
	 * @param savedInstanceState Previous state
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "RestoreInstanceState for expanded items");
		//First, restore opened collapsible items, as otherwise may not all selections could be restored
		List<Integer> expandedItems = savedInstanceState.getIntegerArrayList(TAG);
		if (expandedItems != null) {
			for (Integer expandedItem : expandedItems) {
				expand(expandedItem);
			}
		}
		//Then, restore selection state
		super.onRestoreInstanceState(savedInstanceState);
	}

	/*---------------*/
	/* INNER CLASSES */
	/*---------------*/

	/**
	 * Observer Class responsible to recalculate Selection and Expanded positions.
	 */
	private class ExpandableAdapterDataObserver extends RecyclerView.AdapterDataObserver {

		private void adjustPositions(int positionStart, int itemCount) {
			adjustSelected(positionStart, itemCount);
			adjustExpanded(positionStart, itemCount);
		}

		/**
		 * Triggered by {@link #notifyDataSetChanged()}.
		 */
		@Override
		public void onChanged() {
			getSelectedPositions().clear();
			mExpandedItems = new SparseArray<List<T>>();
			removedItems = new ArrayList<RemovedItem>();
		}

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			adjustPositions(positionStart, itemCount);
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			adjustPositions(positionStart, -itemCount);
		}

		@Override
		public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
			//Take always the min position
			//TODO? adjustPositions(Math.min(fromPosition, toPosition), itemCount);
		}
	}

	private static class ItemInfo<T> {
		int parentPosition;
		int relativePosition;
		T item = null;
		T parent = null;
		List<T> siblings = new ArrayList<T>();
	}

	private static class RemovedItem<T extends IExpandableItem<T>> {
		int originalPosition = -1;
		int originalPositionInParent = -1;
		int parentPosition = -1;
		T item = null;
		boolean notifyParentChanged = false;

		public RemovedItem(int originalPosition, T item) {
			this(originalPosition, item, -1, -1, false);
		}

		public RemovedItem(int originalPosition, T item, int originalPositionInParent, int parentPosition, boolean notifyParentChanged) {
			this.originalPosition = originalPosition;
			this.originalPositionInParent = originalPositionInParent;
			this.item = item;
			this.parentPosition = parentPosition;
			this.notifyParentChanged = notifyParentChanged;
		}

		@Override
		public String toString() {
			return "RemovedItem[originalPosition=" + originalPosition +
					", originalPositionInParent=" + originalPositionInParent +
					", parentPosition=" + parentPosition +
					", item=" + item + "]";
		}
	}

}