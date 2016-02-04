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

import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFlexibleItem;
import eu.davidea.viewholders.ExpandableViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This adapter provides a set of standard methods to expand and collapse a expandable Item.
 * <p>Also, this adapter extends all the basic functionalities that {@link FlexibleAdapter} owns,
 * in order to customize all the possible behaviors coming from the events of expansion
 * or collapsing.</p>
 * 
 * <b>NOTE:</b>This Adapter supports Expandable of Expandable, but selection and restoration don't
 * work well in conjunction of multi level expansion. You should not enable functionalities like:
 * ActionMode, Undo, Drag and CollapseOnExpand in that case. Better change approach in favor
 * of a better and simpler design/layout: Allow to open the list of the subItem in a new Activity.
 * 
 * <p>Instead, this extra level of expansion is useful in situations where those items are not
 * selectable and draggable, and information in it are read only or action buttons.</p>
 *
 * @author Davide Steduto
 * @see FlexibleAdapter
 * @see FlexibleAnimatorAdapter
 * @see SelectableAdapter
 * @see IExpandable
 * @see ExpandableViewHolder
 * @see FlexibleViewHolder
 * @since 16/01/2016 Created
 * <br/>30/01/2016 New code reorganization
 */
//@SuppressWarnings({"unused", "Convert2Diamond", "unchecked"})
public abstract class FlexibleExpandableAdapter
			<EVH extends ExpandableViewHolder, T extends IExpandable<S>, S extends T>
		extends FlexibleAdapter<FlexibleViewHolder, T> {

	private static final String TAG = FlexibleExpandableAdapter.class.getSimpleName();
	private static final String EXTRA_PARENT = TAG + "_parentSelected";
	private static final String EXTRA_CHILD = TAG + "_childSelected";
	public static final int EXPANDABLE_VIEW_TYPE = -1;
	public static final int SECTION_VIEW_TYPE = -2;

	private List<RemovedItem> removedItems;
	private SparseArray<T> headers;
	boolean childSelected = false, parentSelected = false,
			scrollOnExpand = false, collapseOnExpand = false,
			adjustRemoved = true, adjustSelected = true, filtering = false;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	public FlexibleExpandableAdapter(@NonNull List<T> items) {
		this(items, null);
	}

	public FlexibleExpandableAdapter(@NonNull List<T> items, Object listener) {
		super(items, listener);
		headers = new SparseArray<T>();
		removedItems = new ArrayList<RemovedItem>();
		//Expand initial items also after a screen rotation
		expandInitialItems();
		//Get notified when items are inserted or removed (it adjusts selected and removed positions)
		registerAdapterDataObserver(new ExpandableAdapterDataObserver());
	}

	/**
	 * Expands items that are initially configured to be shown as expanded.
	 * <p>This method is also called after a screen rotation.</p>
	 */
	protected void expandInitialItems() {
		//Set initially expanded
		for (int position = 0; position < mItems.size(); position++) {
			T item = getItem(position);
			if (item != null && item.isExpanded() && item.getSubItemsCount() > 0) {
				if (DEBUG) Log.v(TAG, "Initially expand item on position " + position);
				List<S> subItems = getCurrentChildren(item);
				mItems.addAll(position + 1, subItems);
				position += subItems.size();
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
	 * <p>The priority is determined by the 1st item selected by the user:
	 * <br/>- if expandable, then only expandable items will be automatically selected.
	 * <br/>- if not expandable, then the items of the ViewType passed will be selected.</p>
	 *
	 * @param viewTypes All non expandable ViewTypes for which we want the selection,
	 *                  pass nothing to select expandable ViewTypes all the times.
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

	/*--------------------------*/
	/* HEADERS/SECTIONS METHODS */
	/*--------------------------*/

	public FlexibleExpandableAdapter setHeaders(SparseArray<T> headers) {
		return withHeaders(headers);
	}

	public FlexibleExpandableAdapter withHeaders(SparseArray<T> headers) {
		if (headers != null) {
			if (DEBUG) Log.v(TAG, "Settings " + headers.size() + " headers");
			this.headers = headers;
		}
		return this;
	}

	/**
	 * Add 1 header/section to the internal list at the user position.
	 *
	 * @param headerPosition header position
	 * @param headerItem     item of the header
	 * @param show           also immediate show the header to the user
	 */
	public FlexibleExpandableAdapter addHeader(int headerPosition, T headerItem, boolean show, boolean sticky) {
		if (headerItem == null) {
			Log.w(TAG, "Cannot addHeader with null item! headerPosition=" + headerPosition);
			return this;
		}
		if (headerPosition < 0) {
			Log.w(TAG, "Cannot addHeader on negative position! headerItem=" + headerItem);
			return this;
		}
		headers.put(headerPosition, headerItem);
		if (show) {
			headerItem.setHidden(false);
			super.addItem(headerPosition, headerItem);
		}
		return this;
	}

	/**
	 * Retrieves all the header items.
	 *
	 * @return list non-null with all the header items.
	 */
	public List<T> getHeadersList() {
		List<T> list = new ArrayList<T>(headers.size());
		for (int i = 0; i < headers.size(); i++) {
			list.add(headers.valueAt(i));
		}
		return list;
	}

	public List<Integer> getHeadersPositions() {
		List<Integer> list = new ArrayList<Integer>(headers.size());
		for (int i = 0; i < headers.size(); i++) {
			list.add(headers.keyAt(i));
		}
		return list;
	}

	/**
	 * Shows all headers in the RecyclerView at their position
	 */
	public void showAllHeaders() {
		for (int i = 0; i < headers.size(); i++) {
			if (DEBUG) Log.v(TAG, "Showing Header " + headers.keyAt(i) + "=" + headers.valueAt(i));
			if (!contains(headers.valueAt(i))) {
				T headerItem = headers.valueAt(i);
				headerItem.setHidden(false);
				super.addItem(headers.keyAt(i), headerItem);
			}
		}
	}

	/**
	 * Hides all headers from the RecyclerView.
	 */
	public void hideAllHeaders() {
		for (int i = headers.size() - 1; i >= 0; i--) {
			T headerItem = headers.valueAt(i);
			headerItem.setHidden(true);
			removeItem(headerItem);
		}
	}

	/**
	 * Completely remove the header from Adapter.
	 *
	 * @param position the known position of the header to remove
	 */
	public void deleteHeader(int position) {
		int index = headers.indexOfKey(position);
		if (index >= 0) {
			removeItem(headers.valueAt(index));
			headers.removeAt(index);
		}
	}

	/**
	 * Completely remove the header from Adapter.
	 *
	 * @param header the header item to remove
	 */
	public void deleteHeader(T header) {
		int index = headers.indexOfValue(header);
		if (index >= 0) {
			removeItem(headers.valueAt(index));
			headers.removeAt(index);
		}
	}

	/*---------------------*/
	/* VIEW HOLDER METHODS */
	/*---------------------*/

	//FIXME: Find a way to Not animate items with ItemAnimator!!!
	//TODO: Customize child items animations (don't use add or remove ItemAnimator)

	/**
	 * Automatically collapse all previous expanded parents before expand the clicked parent.
	 * <p>Default value is disabled.</p>
	 *
	 * @param collapseOnExpand true to collapse others items, false to just expand the current
	 */
	public void setAutoCollapseOnExpand(boolean collapseOnExpand) {
		this.collapseOnExpand = collapseOnExpand;
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
		this.scrollOnExpand = scrollOnExpand;
	}

	public boolean isExpanded(int position) {
		T item = getItem(position);
		return item != null && item.isExpanded();
	}

	public boolean isExpandable(int position) {
		T item = getItem(position);
		return item != null && item.isExpandable();
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
		//Header ViewType has priority
		int index = headers.indexOfKey(position);
		if (index >= 0) {
			T header = headers.valueAt(index);
			if (header != null && !header.isHidden())
				return SECTION_VIEW_TYPE;
		}
		//Then check if expandable
		T item = getItem(position);
		if (item != null) {
			//if (item.isHeader()) return SECTION_VIEW_TYPE;
			if (item.isExpandable()) return EXPANDABLE_VIEW_TYPE;
		}
		//User ViewType
		return super.getItemViewType(position);
	}

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
		if (viewType == SECTION_VIEW_TYPE) {
			return onCreateHeaderViewHolder(parent, viewType);
		} else if (viewType == EXPANDABLE_VIEW_TYPE) {
			return onCreateExpandableViewHolder(parent, viewType);
		} else {
			return onCreateFlexibleViewHolder(parent, viewType);
		}
	}

	public EVH onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
		return null;
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
		if (getItemViewType(position) == SECTION_VIEW_TYPE) {
			onBindHeaderViewHolder((EVH) holder, position);
		} else if (getItemViewType(position) == EXPANDABLE_VIEW_TYPE) {
			onBindExpandableViewHolder((EVH) holder, position);
		} else {
			onBindFlexibleViewHolder(holder, position);
		}
	}

	public void onBindHeaderViewHolder(EVH holder, int position) {

	}

	/**
	 * Method to bind only Expandable items that implement {@link IExpandable}.
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

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	/**
	 * Retrieves the parent of a child.
	 * <p>Only for a real child of an expanded parent.</p>
	 *
	 * @param child the child item
	 * @return the parent of this child item or null if not found
	 * @see #getExpandablePositionOf(IExpandable)
	 * @see #getRelativePositionOf(IExpandable)
	 */
	public T getExpandableOf(@NonNull T child) {
		for (T parent : mItems) {
			if (parent.isExpandable() && parent.isExpanded() && parent.getSubItemsCount() > 0) {
				for (S subItem : parent.getSubItems()) {
					//Pick up only no-hidden items
					if (!subItem.isHidden() && subItem.equals(child))
						return parent;
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves the parent position of a child.
	 * <p>Only for a real child of an expanded parent.</p>
	 *
	 * @param child the child item
	 * @return the parent position of this child item or -1 if not found
	 * @see #getExpandableOf(IExpandable)
	 * @see #getRelativePositionOf(IExpandable)
	 */
	public int getExpandablePositionOf(@NonNull T child) {
		return getGlobalPositionOf(getExpandableOf(child));
	}

	/**
	 * Provides the list where the child currently lays.
	 *
	 * @param child the child item
	 * @return the list of the child element, or a new list if item
	 * @see #getExpandableOf(IExpandable)
	 * @see #getExpandablePositionOf(IExpandable)
	 * @see #getRelativePositionOf(IExpandable)
	 * @see #getExpandedItems()
	 */
	public List<T> getSiblingsOf(@NonNull T child) {
		return getExpandableList(getExpandableOf(child));
	}

	/**
	 * Retrieves the position of a child in the list where it lays.
	 * <p>Only for a real child of an expanded parent.</p>
	 *
	 * @param child the child item
	 * @return the position in the parent or -1 if, child is a parent itself or not found
	 * @see #getExpandableOf(IExpandable)
	 * @see #getExpandablePositionOf(IExpandable)
	 */
	public int getRelativePositionOf(@NonNull T child) {
		return getSiblingsOf(child).indexOf(child);
	}

	/**
	 * Provides a list of all expandable items that are currently expanded.
	 *
	 * @return a list with all expanded items
	 * @see #getSiblingsOf(IExpandable)
	 * @see #getExpandedPositions()
	 */
	public List<T> getExpandedItems() {
		List<T> expandedItems = new ArrayList<T>();
		for (T item : mItems) {
			if (item.isExpanded()) expandedItems.add(item);
		}
		return expandedItems;
	}

	/**
	 * Provides a list of all expandable positions that are currently expanded.
	 *
	 * @return a list with the global positions of all expanded items
	 * @see #getSiblingsOf(IExpandable)
	 * @see #getExpandedItems()
	 */
	public List<Integer> getExpandedPositions() {
		List<Integer> expandedPosition = new ArrayList<Integer>();
		for (int i = 0; i < mItems.size() - 1; i++) {
			if (mItems.get(i).isExpanded()) expandedPosition.add(i);
		}
		return expandedPosition;
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
		T item = getItem(position);
		if (DEBUG) Log.v(TAG, "Request to Expand on position " + position +
				" expanded " + (item != null ? item.isExpanded() : "false") +
				" ExpandedItems=" + getExpandedPositions());
		int subItemsCount = 0;
		if (item != null && item.isExpandable() && !item.isExpanded() &&
				item.getSubItemsCount() > 0 && !parentSelected) {

			//Collapse others expandable if configured so
			//Skipped when expanding all is requested
			if (collapseOnExpand && !expandAll) {
				//Fetch again the new position after collapsing all!!
				if (collapseAll() > 0) position = getGlobalPositionOf(item);
			}

			//Every time an expansion is requested, subItems must be taken from the original Object!
			//without the subItems that are going to be removed
			//Save a copy child items list
			List<T> subItems = getExpandableList(item);
			mItems.addAll(position + 1, subItems);
			subItemsCount = subItems.size();
			//Save expanded state
			item.setExpanded(true);

			//Automatically scroll the current expandable item to show as much children as possible
			if (scrollOnExpand && !expandAll) {
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
	 * <p>All Expandable subItem, that are expanded, are recursively collapsed.</p>
	 *
	 * @param position the position of the item to collapse
	 * @return the number of subItems collapsed
	 */
	public int collapse(int position) {
		if (DEBUG)
			Log.v(TAG, "Request to Collapse on position " + position + " ExpandedItems=" + getExpandedPositions());
		T item = getItem(position);
		int subItemsCount = 0, recursiveCount = 0;
		if (item != null && item.isExpandable() && item.isExpanded() &&
				(!hasSubItemsSelected(item) || isExpandablePendingRemove(position))) {

			//Take the current subList
			List<T> subItems = getExpandableList(item);
			//Recursive collapse of all sub expandable
			recursiveCount = recursiveCollapse(subItems);
			mItems.removeAll(subItems);
			subItemsCount = subItems.size();
			//Save expanded state
			item.setExpanded(false);

			//Collapse!
			notifyItemRangeRemoved(position + 1, subItemsCount);

			if (DEBUG)
				Log.v(TAG, "Collapsed " + subItemsCount + " subItems on position=" + position + " ExpandedItems=" + getExpandedPositions());
		}
		return subItemsCount + recursiveCount;
	}

	private int recursiveCollapse(List<T> subItems) {
		int collapsed = 0;
		for (T subItem : subItems) {
			if (subItem.isExpanded()) {
				if (DEBUG) Log.v(TAG, "Recursive collapsing on expandable subItem " + subItem);
				collapsed += collapse(getGlobalPositionOf(subItem));
			}
		}
		return collapsed;
	}

	/**
	 * @return the number of parent successfully collapsed
	 */
	public int collapseAll() {
		int collapsed = 0;
		//More efficient if we collapse from last expanded position
		for (int i = mItems.size() - 1; i >= 0; i--) {
			if (collapse(i) > 0) collapsed++;
		}
		return collapsed;
	}

	/*---------------------------*/
	/* ADDING METHODS OVERRIDDEN */
	/*---------------------------*/

	/**
	 * Convenience method of {@link #addSubItem(int, int, IExpandable, boolean, boolean)}.
	 * <br/>In this case parent item will never be notified nor expanded if it is collapsed.
	 */
	public boolean addSubItem(int parentPosition, int subPosition, @NonNull T item) {
		return this.addSubItem(parentPosition, subPosition, item, false, false);
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
	public boolean addSubItem(int parentPosition, int subPosition, @NonNull T item,
							  boolean expandParent, boolean notifyParentChanged) {
		T parent = getItem(parentPosition);
		boolean added = false;
		if (parent != null && parent.isExpandable()) {
			//Expand parent if requested and not already expanded
			if (expandParent && !parent.isExpanded()) {
				expand(parentPosition);
			}
			//Notify the adapter of the new addition to display it and animate it.
			//If parent is collapsed there's no need to notify about the change.
			if (parent.isExpanded()) {
				//Add sub item inside the parent
				//addItemIn(getExpandableList(parentPosition), subPosition, item);
				super.addItem(parentPosition + 1 + Math.max(0, subPosition), item);
				added = true;
			}
			//Notify the parent about the change if requested
			if (notifyParentChanged) notifyItemChanged(parentPosition);
		}
		return added;
	}


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
	 * Wrapper method of {@link #addItem(int, IFlexibleItem)} for expandable items (Parents).
	 *
	 * @param position       the position of the item to add
	 * @param expandableItem item to add, must be an instance of {@link IExpandable}
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
	 * <p>The item must implement {@link IExpandable}.</p>
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
		//if item is a child of another item
		if (!item.isExpandable() || getExpandableOf(item) != null) {
			if (DEBUG) Log.v(TAG, "removeItem Child:" + removedItems);
			//It's a Child
			int parentPosition = createRemovedSubItem(item, notifyParentChanged);
			//Notify the Parent about the change if requested
			if (notifyParentChanged) notifyItemChanged(parentPosition);
		} else {
			if (DEBUG) Log.v(TAG, "removeItem Parent:" + removedItems);
			//Collapse Parent before removal if it is expanded!
			createRemovedItem(position, item);
		}
		//Remove and notify removals
		super.removeItem(position);
	}

	/**
	 * {@inheritDoc}
	 * <p>Parent will not be notified about the change, if a child is removed.</p>
	 */
	@Override
	public void removeItems(List<Integer> selectedPositions) {
		this.removeItems(selectedPositions, false);
	}

	/**
	 * Removes a list of items from internal list and notify the change.
	 * <p>Every item is retained for an eventual Undo.</p>
	 *
	 * @param selectedPositions   list with item positions to remove
	 * @param notifyParentChanged true to notify parent of a removal of a child, false if not
	 */
	public void removeItems(List<Integer> selectedPositions, boolean notifyParentChanged) {
		if (DEBUG)
			Log.v(TAG, "removeItems selectedPositions=" + selectedPositions + " notifyParentChanged=" + notifyParentChanged);
		//Check if list is empty
		if (selectedPositions == null || selectedPositions.isEmpty()) return;
		//Reverse-sort the list, start from last position for efficiency
		Collections.sort(selectedPositions, new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs) {
				return rhs - lhs;
			}
		});
		if (DEBUG)
			Log.v(TAG, "removeItems after reverse sort selectedPositions=" + selectedPositions);
		//Split the list in ranges
		int positionStart = 0, itemCount = 0;
		int lastPosition = selectedPositions.get(0);
		isMultiRange = true;
		for (Integer position : selectedPositions) {//10 9 8 //5 4 //1
			if (lastPosition - itemCount == position) {//10-0==10  10-1==9  10-2==8  10-3==5 NO  //5-1=4  5-2==1 NO
				itemCount++;             // 1  2  3  //2
				positionStart = position;//10  9  8  //4
			} else {
				//Remove range
				if (itemCount > 0)
					removeRange(positionStart, itemCount, notifyParentChanged);//8,3  //4,2
				positionStart = lastPosition = position;//5  //1
				itemCount = 1;
			}
			//Request to collapse after the notification of remove range
			collapse(position);
		}
		//Remove last range
		isMultiRange = false;
		if (itemCount > 0) {
			removeRange(positionStart, itemCount, notifyParentChanged);//1,1
		}
	}

	@Override
	public void removeRange(int positionStart, int itemCount) {
		removeRange(positionStart, itemCount, false);
	}

	public void removeRange(int positionStart, int itemCount, boolean notifyParentChanged) {
		int initialCount = getItemCount();
		if (DEBUG)
			Log.v(TAG, "removeRange positionStart=" + positionStart + " itemCount=" + itemCount);
		if (positionStart < 0 || (positionStart + itemCount) > initialCount) {
			Log.w(TAG, "Cannot removeRange with positionStart out of OutOfBounds!");
			return;
		}
		int parentPosition = -1;
		for (int position = positionStart + itemCount - 1; position >= positionStart; position--) {
			T item = getItem(position);
			if (item == null) continue;
			//if item is a child of another item
			if (!item.isExpandable() || getExpandableOf(item) != null) {
				parentPosition = createRemovedSubItem(item, notifyParentChanged);
			} else {
				createRemovedItem(position, item);
			}
			//Remove item from internal list
			synchronized (mLock) {
				mItems.remove(position);
			}
		}
		//Notify removals
		if (parentPosition >= 0) {
			adjustSelected = false;
			//Notify the Children removal only if Parent is expanded
			notifyItemRangeRemoved(positionStart, itemCount);
			//Notify the Parent about the change if requested
			if (notifyParentChanged) notifyItemChanged(parentPosition);
		} else {
			adjustRemoved = false;
			//Notify range removal
			notifyItemRangeRemoved(positionStart, itemCount);
		}
		//Update empty view
		if (mUpdateListener != null && !isMultiRange && initialCount != getItemCount())
			mUpdateListener.onUpdateEmptyView(getItemCount());
	}

	/**
	 * {@inheritDoc}
	 * <p>Parent will not be notified about the change, if a child is removed.</p>
	 */
	@Override
	public void removeAllSelectedItems() {
		this.removeAllSelectedItems(false);
	}

	/**
	 * Convenience method to remove all Items that are currently selected.<p>
	 * Optionally the Parent can be notified about the change, if a child is removed.
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
//		boolean move = false;
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
	 * {@inheritDoc}
	 */
	@Override
	public void restoreDeletedItems() {
		stopUndoTimer();
		//Be sure list is reverse insert (list was reverse ordered on Delete)
		Collections.sort(removedItems, new Comparator<RemovedItem>() {
			@Override
			public int compare(RemovedItem lhs, RemovedItem rhs) {
				return rhs.parentPosition - lhs.parentPosition;
			}
		});
		isMultiRange = true;
		int initialCount = getItemCount();
		//Selection coherence: start from a clear situation, clear selection if restoreSelection is active
		if (mRestoreSelection) clearSelection();
		for (int i = removedItems.size() - 1; i >= 0; i--) {
			RemovedItem removedItem = removedItems.get(i);
			boolean added;
			if (removedItem.relativePosition > 0) {
				//Restore child
				if (DEBUG) Log.v(TAG, "Restore Child " + removedItem);
				if (hasSearchText() && !super.filterObject((T) removedItem.item, getSearchText()))
					continue;
				added = addSubItem(removedItem.parentPosition, removedItem.relativePosition,
						(T) removedItem.item, false, removedItem.notifyParentChanged);
			} else {
				//Restore parent
				adjustRemoved = false;
				if (DEBUG) Log.v(TAG, "Restore Parent " + removedItem);
				if (hasSearchText() && !filterObject((T) removedItem.item, getSearchText()))
					continue;
				added = addItem(removedItem.parentPosition, (T) removedItem.item);
			}
			//Item is again visible
			removedItem.item.setHidden(false);
			//Restore selection if requested, before emptyBin.
			if (mRestoreSelection && added) {
				if (!removedItem.item.isExpandable()) {
					childSelected = true;
					getSelectedPositions().add(removedItem.parentPosition + 1 + removedItem.relativePosition);
				} else {
					parentSelected = true;
					getSelectedPositions().add(removedItem.parentPosition);
				}
			}
		}
		if (DEBUG && mRestoreSelection)
			Log.v(TAG, "Selected positions after restore " + getSelectedPositions());

		//Call listener to update EmptyView
		isMultiRange = false;
		if (mUpdateListener != null && initialCount != getItemCount())
			mUpdateListener.onUpdateEmptyView(getItemCount());

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
			if (!deletedItems.contains(removedItem.parentPosition))
				deletedItems.add(removedItem.parentPosition);
		}
		return deletedItems;
	}

	/**
	 * Retrieves only the deleted children of the specified parent.
	 *
	 * @param parentPosition the parent position
	 * @return the list of deleted children
	 */
	public List<T> getDeletedChildren(int parentPosition) {
		List<T> deletedChild = new ArrayList<T>();
		for (RemovedItem removedItem : removedItems) {
			//TODO: Review with isItemPendingRemove(), it looks like, having better result while filtering, to verify.
			if (removedItem.parentPosition == parentPosition && removedItem.relativePosition >= 0)
				deletedChild.add((T) removedItem.item);
		}
		return deletedChild;
	}

	/**
	 * Retrieves all the original children of the specified parent, filtering out all the
	 * deleted children if any.
	 *
	 * @param item the parent item
	 * @return the list of the original children minus the deleted children if some are
	 * pending removal.
	 */
	public List<S> getCurrentChildren(T item) {
		//Check item and subItems existence
		if (item == null || !item.isExpandable() || item.getSubItems() == null)
			return new ArrayList<S>();
		//Take a copy of the subItems list
		List<S> subItems = new ArrayList<>(item.getSubItems());
		//Remove all children pending removal
		if (removedItems.size() > 0) {
			subItems.removeAll(getDeletedChildren(getGlobalPositionOf(item)));
		}
		return subItems;
	}

	/**
	 * @param parentPosition the expandable position to check
	 * @return true if the item or some children are going to be removed, false otherwise
	 */
	private boolean isExpandablePendingRemove(int parentPosition) {
		for (RemovedItem removedItem : removedItems) {
			if (removedItem.parentPosition == parentPosition) return true;
		}
		return false;
	}

	/**
	 * @param item the item to compare
	 * @return the removed item if found, null otherwise
	 */
	private RemovedItem getPendingRemovedItem(T item) {
		for (RemovedItem removedItem : removedItems) {
			if (removedItem.item.equals(item)) return removedItem;
		}
		return null;
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
		//Enable flag: skip adjustPositions!
		filtering = true;
		//Reset values
		int initialCount = getItemCount();
		if (hasSearchText()) {
			int newOriginalPosition = -1;
			for (T item : unfilteredItems) {
				if (filterObject(item, getSearchText())) {
					RemovedItem removedItem = getPendingRemovedItem(item);
					if (removedItem != null) {
						//If found calculate new progressive position while filtering
						removedItem.parentPosition = ++newOriginalPosition;
					} else {
						values.add(item);
						newOriginalPosition++;
						if (item.isExpanded()) {
							List<S> subItems = new ArrayList<S>();
							//Add subItems if not hidden by filterObject()
							for (S subItem : item.getSubItems()) {
								if (!subItem.isHidden()) subItems.add(subItem);
							}
							values.addAll(subItems);
							newOriginalPosition += subItems.size();
						}
					}
				}
			}
		} else {
			values = unfilteredItems; //with no filter
			if (!removedItems.isEmpty()) {
				for (RemovedItem removedItem : removedItems) {
					//Retrieve the original position from the original list
					removedItem.parentPosition = values.indexOf(removedItem.item);
				}
				values.removeAll(getDeletedItems());
			}
			resetFilterFlags(values);
		}

		//Animate search results only in case of new SearchText
		if (!mOldSearchText.equalsIgnoreCase(mSearchText)) {
			mOldSearchText = mSearchText;
			animateTo(values);
		} else mItems = values;

		//Reset filtering flag
		filtering = false;

		//Call listener to update EmptyView
		if (mUpdateListener != null && initialCount != getItemCount())
			mUpdateListener.onUpdateEmptyView(getItemCount());
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
		boolean filtered = false;
		item.setExpanded(false);

		//Children scan filter
		for (S subItem : getCurrentChildren(item)) {
			//Reuse super filter for Children
			subItem.setHidden(!super.filterObject(subItem, constraint));
			if (!filtered && !subItem.isHidden()) {
				filtered = true;
			}
		}
		//Expand if filter found text in subItems
		item.setExpanded(filtered);

		//Super filter for Parent only if not filtered already
		return filtered || super.filterObject(item, constraint);
	}

	/*-----------------*/
	/* PRIVATE METHODS */
	/*-----------------*/

	/**
	 * Clears flags after searchText is cleared
	 */
	private void resetFilterFlags(List<T> items) {
		//Reset flags for all items!
		for (T item : items) {
			item.setExpanded(false);
			if (item.isExpandable() && item.getSubItemsCount() > 0) {
				for (T subItem : item.getSubItems()) {
					subItem.setHidden(false);
				}
			}
		}
	}

	private int createRemovedSubItem(T item, boolean notifyParentChanged) {
		T parent = getExpandableOf(item);
		int parentPosition = getGlobalPositionOf(parent);
		List<T> siblings = getExpandableList(parent);
		int childPosition = siblings.indexOf(item);
		item.setHidden(true);
		removedItems.add(new RemovedItem<T>(parentPosition, childPosition, item, notifyParentChanged));
		if (DEBUG) Log.v(TAG, "New RemovedItem Child " + removedItems.get(removedItems.size() - 1));
		return parentPosition;
	}

	private void createRemovedItem(int position, T item) {
		//Collapse Parent before removal if it is expanded!
		if (item.isExpanded()) collapse(position);
		item.setHidden(true);
		removedItems.add(new RemovedItem<T>(position, item));
		if (DEBUG)
			Log.v(TAG, "New RemovedItem Parent " + removedItems.get(removedItems.size() - 1));
	}

	/**
	 * Retrieves the list of subItems which are not hidden.
	 *
	 * @param parent the parent item
	 * @return the list of the passed expandable
	 */
	private List<T> getExpandableList(T parent) {
		List<T> subItems = new ArrayList<T>();
		if (parent != null && parent.getSubItemsCount() > 0) {
			for (T subItem : parent.getSubItems()) {
				//Pick up only no hidden items
				if (!subItem.isHidden()) subItems.add(subItem);
			}
		}
		return subItems;
	}

	private boolean hasSubItemsSelected(T item) {
		for (T subItem : getExpandableList(item)) {
			if (isSelected(getGlobalPositionOf(subItem))) return true;
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

	private void adjustHeaders(int startPosition, int itemCount) {
		boolean adjusted = false;
		for (int i = 0; i < headers.size(); i++) {
			int position = headers.keyAt(i);
			if (position > startPosition) {
				if (DEBUG)
					Log.v(TAG, "Adjust Header position " + position + " to " + Math.max(position + itemCount, startPosition));
				T header = headers.get(i);
				headers.delete(i);
				headers.put(position + itemCount, header);
				adjusted = true;
			}
		}
		if (DEBUG && adjusted) Log.v(TAG, "AdjustedHeaders=" + getHeadersPositions());
	}

	private void adjustSelected(int startPosition, int itemCount) {
		List<Integer> selectedPositions = getSelectedPositions();
		boolean adjusted = false;
		for (Integer position : selectedPositions) {
			if (position >= startPosition) {
				if (DEBUG)
					Log.v(TAG, "Adjust Selected position " + position + " to " + Math.max(position + itemCount, startPosition));
				selectedPositions.set(
						selectedPositions.indexOf(position),
						Math.max(position + itemCount, startPosition));
				adjusted = true;
			}
		}
		if (DEBUG && adjusted) Log.v(TAG, "AdjustedSelected=" + getSelectedPositions());
	}

	private void adjustRemoved(int startPosition, int itemCount) {
		boolean adjusted = false;
		int skipped = 0;
		//Reverse scan for potential expand/collapse while pending removal
		for (int i = removedItems.size() - 1; i >= 0; i--) {
			RemovedItem removedItem = removedItems.get(i);
			if (removedItem.parentPosition >= startPosition + skipped) {
				if (DEBUG) Log.v(TAG, "Adjust Removed startPosition " + (startPosition + skipped) +
						" / parentPosition " + removedItem.parentPosition +
						" to " + Math.max(removedItem.parentPosition + itemCount, startPosition) + (removedItem.relativePosition >= 0 ? " for childPosition " + removedItem.relativePosition : ""));
				removedItem.parentPosition = Math.max(removedItem.parentPosition + itemCount, startPosition);
				adjusted = true;
			} else if (removedItem.item.isExpandable()) {
				//Parent position to adjust must be below the number of the previous delete position
				skipped++;
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
		//Save selection state
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.v(TAG, "SaveInstanceState for expanded items");
		//Save selection coherence
		outState.putBoolean(EXTRA_CHILD, childSelected);
		outState.putBoolean(EXTRA_PARENT, parentSelected);
	}

	/**
	 * Restore the previous state of the expanded items.
	 *
	 * @param savedInstanceState Previous state
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		//Restore selection state
		super.onRestoreInstanceState(savedInstanceState);
		//Restore selection coherence
		parentSelected = savedInstanceState.getBoolean(EXTRA_PARENT);
		childSelected = savedInstanceState.getBoolean(EXTRA_CHILD);
	}

	/*---------------*/
	/* INNER CLASSES */
	/*---------------*/

	/**
	 * Observer Class responsible to recalculate Selection and Expanded positions.
	 */
	private class ExpandableAdapterDataObserver extends RecyclerView.AdapterDataObserver {

		private void adjustPositions(int positionStart, int itemCount) {
			if (!filtering) {//Filtering has multiple insert and removal, we skip this process
				//adjustHeaders(positionStart, itemCount);
				if (adjustSelected)//Don't, if remove range / restore children
					adjustSelected(positionStart, itemCount);
				if (adjustRemoved)//Don't, if remove range / restore parents
					adjustRemoved(positionStart, itemCount);
				adjustSelected = adjustRemoved = true;
			}
		}

		/**
		 * Triggered by {@link #notifyDataSetChanged()}.
		 */
		@Override
		public void onChanged() {
			expandInitialItems();
		}

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			adjustPositions(positionStart, itemCount);
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			adjustPositions(positionStart, -itemCount);
		}
	}

//	private static class ItemInfo<T> {
//		int parentPosition,
//				relativePosition;
//		T item = null;
//		T parent = null;
//		List<T> siblings = new ArrayList<T>();
//	}

	private static class RemovedItem<T extends IExpandable> {
		int parentPosition = -1, relativePosition = -1;
		T item = null;
		boolean notifyParentChanged = false;

		public RemovedItem(int parentPosition, T item) {
			this(parentPosition, -1, item, false);
		}

		public RemovedItem(int parentPosition, int relativePosition, T item, boolean notifyParentChanged) {
			this.parentPosition = parentPosition;
			this.relativePosition = relativePosition;
			this.item = item;
			this.notifyParentChanged = notifyParentChanged;
		}

		public void adjustBy(int itemCount) {
			parentPosition += itemCount;
		}

		@Override
		public String toString() {
			return "RemovedItem[parentPosition=" + parentPosition +
					", relativePosition=" + relativePosition +
					", item=" + item + "]";
		}
	}

	public class Header<T extends IExpandable> {
		int headerPosition;
		int firstPosition;
		T headerItem;
		boolean shown;
		boolean sticky;

		/**
		 * @param headerPosition header position
		 * @param firstPosition  the position of the first item which the header/section will represent
		 * @param headerItem     the header item with all content
		 * @param shown          display header at the startup
		 * @param sticky         make header sticky
		 */
		public Header(int headerPosition, int firstPosition, T headerItem, boolean shown, boolean sticky) {
			this.headerPosition = headerPosition;
			this.firstPosition = firstPosition;
			this.headerItem = headerItem;
			this.shown = shown;
			this.sticky = sticky;
		}

		public void hide() {
			addItem(headerPosition, headers.get(headerPosition));
			headerItem.setHidden(true);
			shown = false;
		}

		public void show() {
			removeItem(headerPosition);
			headerItem.setHidden(false);
			shown = true;
		}

		public boolean isShown() {
			return shown;
		}

		public void setSticky(boolean sticky) {
			this.sticky = sticky;
		}

		public boolean isSticky() {
			return sticky;
		}
	}

}