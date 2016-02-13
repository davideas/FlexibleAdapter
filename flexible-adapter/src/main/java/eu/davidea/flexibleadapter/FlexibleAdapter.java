package eu.davidea.flexibleadapter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFlexibleItem;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.viewholders.ExpandableViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This class provides a set of standard methods to handle changes on the data set such as
 * filtering, adding, removing, moving and animating an item.
 * <p><b>T</b> is your Model object containing the data, with version 5.0.0 it must implement
 * {@link IFlexibleItem} interface.</p>
 * With version 5.0.0, this Adapter supports a set of standard methods for Headers/Sections to
 * expand and collapse an Expandable item, to Drag&Drop and Swipe any item.
 * <p><b>NOTE:</b>This Adapter supports Expandable of Expandable, but selection and restoration
 * don't work well in conjunction of multi level expansion. You should not enable functionalities
 * like: ActionMode, Undo, Drag and CollapseOnExpand in that case. Better to change approach in
 * favor of a better and clearer design/layout: Open the list of the subItem in a new Activity...
 * <br/>Instead, this extra level of expansion is useful in situations where those items are not
 * selectable nor draggable, and information in them are in read only mode or are action buttons.</p>
 *
 * @author Davide Steduto
 * @see FlexibleAnimatorAdapter
 * @see SelectableAdapter
 * @see IFlexibleItem
 * @see FlexibleViewHolder
 * @see ExpandableViewHolder
 * @since 03/05/2015 Created
 * <br/>16/01/2016 Expandable feature
 * <br/>24/01/2016 Drag&Drop, Swipe features
 * <br/>30/01/2016 Class now extends {@link FlexibleAnimatorAdapter} that extends {@link SelectableAdapter}
 * <br/>02/02/2016 New code reorganization, new item interfaces and full refactoring
 * <br/>08/02/2016 Headers/Sections feature
 * <br/>10/02/2016 The class is not abstract anymore, it is ready to be used
 */
//@SuppressWarnings({"unused", "Convert2Diamond"})
public class FlexibleAdapter<T extends IFlexibleItem>
		extends FlexibleAnimatorAdapter
		implements ItemTouchHelperCallback.AdapterCallback {

	private static final String TAG = FlexibleAdapter.class.getSimpleName();
	private static final String EXTRA_PARENT = TAG + "_parentSelected";
	private static final String EXTRA_CHILD = TAG + "_childSelected";
	private static final String EXTRA_HEADERS = TAG + "_headersShown";
	public static final int EXPANDABLE_VIEW_TYPE = -1;
	public static final int SECTION_VIEW_TYPE = -2;
	public static final long UNDO_TIMEOUT = 5000L;

	/**
	 * The main container for ALL items.
	 */
	private List<T> mItems;

	/**
	 * Header/Section items
	 */
	private List<ISectionable> mHeaders;
	private boolean headersShown = false;

	/**
	 * Handler for delayed {@link #filterItems(List)} and {@link OnDeleteCompleteListener#onDeleteConfirmed}
	 * <p>"What" already used:
	 * <br/>0 = filterItems delay
	 * <br/>1 = deleteConfirmed when Undo timeout is over</p>
	 */
	protected Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
		public boolean handleMessage(Message message) {
			switch (message.what) {
				case 0: //filterItems
					filterItems((List<T>) message.obj);
					return true;
				case 1: //confirm delete
					OnDeleteCompleteListener listener = (OnDeleteCompleteListener) message.obj;
					if (listener != null) listener.onDeleteConfirmed();
					emptyBin();
					return true;
			}
			return false;
		}
	});

	/**
	 * Used to save deleted items and to recover them (Undo).
	 */
	private List<RestoreInfo> mRestoreList;
	private boolean restoreSelection = false, multiRange = false,
			permanentDelete = false, adjustSelected = true;

	/* ViewTypes */
	protected LayoutInflater mInflater;
	private ArrayMap<Integer, T> mTypeInstances = new ArrayMap<Integer, T>();
	private boolean autoMap = false;

	/* Filter */
	private String mSearchText = "", mOldSearchText = "";
	private boolean mNotifyChangeOfUnfilteredItems = false, filtering = false;

	/* Expandable flags */
	private boolean scrollOnExpand = false, collapseOnExpand = false,
			childSelected = false, parentSelected = false;

	/* Drag&Drop and Swipe helpers */
	private boolean longPressDragEnabled = false, handleDragEnabled = true, swipeEnabled = false;
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
	 * Main Constructor with all managed listeners for ViewHolder and the Adapter itself.
	 * <p>The listener must be a single instance of a class, usually <i>Activity</i> or <i>Fragment</i>,
	 * where you can implement how to handle the different events.</p>
	 * Any write operation performed on the items list is <u>synchronized</u>.
	 * <p><b>PASS ALWAYS A <u>COPY</u> OF THE ORIGINAL LIST</b>: <i>new ArrayList&lt;T&gt;(originalList);</i></p>
	 *
	 * @param items     items to display
	 * @param listeners can be an instance of:
	 *                  <br/>- {@link OnUpdateListener}
	 *                  <br/>- {@link OnItemClickListener}
	 *                  <br/>- {@link OnItemLongClickListener}
	 *                  <br/>- {@link OnItemMoveListener}
	 *                  <br/>- {@link OnItemSwipeListener}
	 */
	public FlexibleAdapter(@NonNull List<T> items, @Nullable Object listeners) {
		mItems = Collections.synchronizedList(items);
		mRestoreList = new ArrayList<RestoreInfo>();

		//Expand initial items also after a screen rotation
		expandInitialItems();

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

		//Get notified when items are inserted or removed (it adjusts selected positions)
		registerAdapterDataObserver(new ExpandableAdapterDataObserver());
	}

	/**
	 * Expands items that are initially configured to be shown as expanded.
	 * <p>This method is also called after a screen rotation.</p>
	 */
	protected void expandInitialItems() {
		for (int position = 0; position < mItems.size(); position++) {
			T item = getItem(position);
			//Map the parent view type if not done yet
			mapViewTypeFrom(item);
			if (isExpandable(item)) {
				IExpandable expandable = (IExpandable) item;
				if (expandable.isExpanded()) {
					if (DEBUG) Log.v(TAG, "Initially expand item on position " + position);
					position += addAllSubItemsFrom(position + 1, expandable, false, null);
				}
			}
		}
	}

	/*------------------------------*/
	/* SELECTION METHODS OVERRIDDEN */
	/*------------------------------*/

	public boolean isEnabled(int position) {
		//noinspection ConstantConditions
		return getItem(position).isEnabled();
	}

	@Override
	public boolean isSelectable(int position) {
		//noinspection ConstantConditions
		return getItem(position).isSelectable();
	}

	@Override
	public void toggleSelection(@IntRange(from = 0) int position) {
		T item = getItem(position);
		//Allow selection only for selectable items
		if (item != null && item.isSelectable()) {
			boolean hasParent = getExpandableOf(item) != null;
			if ((isExpandable(item) || !hasParent) && !childSelected) {
				//Allow selection of Parent if no Child has been previously selected
				parentSelected = true;
				super.toggleSelection(position);
			} else if (!parentSelected && hasParent) {
				//Allow selection of Child if no Parent has been previously selected
				childSelected = true;
				super.toggleSelection(position);
			}
		}

		//Reset flags if necessary, just to be sure
		if (getSelectedItemCount() == 0) parentSelected = childSelected = false;
	}

	/**
	 * Helper to automatically select all the items of the viewType equal to the viewType of
	 * the first selected item.
	 * <p>Examples:
	 * <br/>- if user initially selects an expandable of type A, then only expandable items of
	 * type A will be selected.
	 * <br/>- if user initially selects a non expandable of type B, then only items of Type B
	 * will be selected.
	 * <br/>- The developer can override this behaviour by passing a list of viewTypes for which
	 * he wants to force the selection.</p>
	 *
	 * @param viewTypes All the desired viewTypes to be selected, pass nothing to automatically
	 *                  select all the viewTypes of the first item user selected
	 */
	@Override
	public void selectAll(Integer... viewTypes) {
		if (getSelectedItemCount() > 0 && viewTypes.length == 0) {
			super.selectAll(getItemViewType(getSelectedPositions().get(0)));//Priority on the first item
		} else {
			super.selectAll(viewTypes);//Force the selection for the viewTypes passed
		}
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

	/**
	 * This method will refresh the entire DataSet content.
	 *
	 * @param items the new data set
	 */
	public void updateDataSet(List<T> items) {
		mItems = items;
		notifyDataSetChanged();
		showAllHeadersAfterRefresh();
	}

	/**
	 * Returns the custom object "Item".
	 * <p>This cannot be overridden since the entire library relies on it.</p>
	 *
	 * @param position the position of the item in the list
	 * @return The custom "Item" object or null if item not found
	 */
	public final T getItem(@IntRange(from = 0) int position) {
		if (position < 0 || position >= mItems.size()) return null;
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * This cannot be overridden since the selection relies on it.
	 *
	 * @return the total number of the items currently displayed by the adapter
	 * @see #getItemCountOfTypes(Integer...)
	 * @see #isEmpty()
	 */
	@Override
	public final int getItemCount() {
		return mItems != null ? mItems.size() : 0;
	}

	/**
	 * Provides the number of items currently displayed of one or more certain types.
	 *
	 * @param viewTypes the viewTypes to count
	 * @return number of the viewTypes counted
	 * @see #getItemCount()
	 * @see #isEmpty()
	 */
	public int getItemCountOfTypes(Integer... viewTypes) {
		List<Integer> viewTypeList = Arrays.asList(viewTypes);
		int count = 0;
		for (int i = 0; i < mItems.size(); i++) {
			//Privilege faster counting if autoMap is active
			if ((autoMap && viewTypeList.contains(mItems.get(i).getLayoutRes())) ||
					viewTypeList.contains(getItemViewType(i)))
				count++;
		}
		return count;
	}

	/**
	 * You can override this method to define your own concept of "Empty". This method is never
	 * called internally.
	 * <p>Default value is the result of {@link #getItemCount()}.</p>
	 *
	 * @return true if the list is empty, false otherwise
	 * @see #getItemCount()
	 * @see #getItemCountOfTypes(Integer...)
	 */
	public boolean isEmpty() {
		return getItemCount() == 0;
	}

	/**
	 * Retrieve the global position of the Item in the Adapter list.
	 *
	 * @param item the item to find
	 * @return the global position in the Adapter if found, -1 otherwise
	 */
	public int getGlobalPositionOf(@NonNull T item) {
		return item != null && mItems != null && mItems.size() > 0 ? mItems.indexOf(item) : -1;
	}

	/**
	 * This method is never called internally.
	 *
	 * @param item the item to find
	 * @return true if the provided item is currently displayed, false otherwise
	 */
	public boolean contains(@NonNull T item) {
		return item != null && mItems != null && mItems.contains(item);
	}

	/*--------------------------*/
	/* HEADERS/SECTIONS METHODS */
	/*--------------------------*/

	//TODO: Boolean for saved instance state and notifyDataSetChanged
	//TODO: On item removed / inserted / moved switch header linkage
	//TODO: On restore item after the position of an header switch header linkage

	/**
	 * @return true if all headers are currently displayed, false otherwise
	 */
	public boolean areHeadersShown() {
		return headersShown;
	}

	/**
	 * Sets if all headers should be shown at startup. To call before setting the headers!
	 * <p>Default value is false.</p>
	 *
	 * @param displayHeaders true to display them, false to keep them hidden
	 */
	public void setDisplayHeadersAtStartUp(boolean displayHeaders) {
		headersShown = displayHeaders;
	}

	/**
	 * Sets the new headers list.
	 * <p>The headers will be automatically displayed if all headers are currently shown,
	 * otherwise they will be kept hidden.</p>
	 *
	 * @param headers a non null and not empty list of {@link ISectionable} header objects
	 * @return this adapter so the call can be chained
	 * @see #setDisplayHeadersAtStartUp(boolean)
	 */
	public FlexibleAdapter withHeaders(@NonNull List<ISectionable> headers) {
		setHeaders(headers);
		return this;
	}

	/**
	 * Sets the new headers list.
	 * <p>The headers will be automatically displayed if all headers are currently shown,
	 * otherwise they will be kept hidden.</p>
	 *
	 * @param headers a non null and not empty list of {@link ISectionable} header objects
	 * @see #setDisplayHeadersAtStartUp(boolean)
	 */
	public void setHeaders(@NonNull List<ISectionable> headers) {
		if (headers != null) {
			//TODO: Preliminary check if all attached items are free to be attached (no duplicated items allowed)
			//Save the current status to know if headers will be already displayed
			boolean alreadyShown = headersShown;
			//Hide all previous headers if they exists
			if (mHeaders != null) hideAllHeaders();
			mHeaders = headers;
			if (DEBUG) Log.v(TAG, "Imported " + headers.size() + " headers");
			if (alreadyShown) showAllHeaders();
		}
	}

	/**
	 * Adds 1 header/section to the internal list at its linked position.
	 * <p>The header will be automatically displayed if all headers are currently shown,
	 * otherwise it will be kept hidden.</p>
	 *
	 * @param headerItem item of the header
	 * @return this adapter so the call can be chained
	 * @see #areHeadersShown()
	 */
	public FlexibleAdapter addHeader(@NonNull ISectionable headerItem) {
		if (headerItem == null) {
			Log.e(TAG, "Cannot add null Header!");
			return this;
		}
		//Initialize the list
		if (mHeaders == null) mHeaders = new ArrayList<ISectionable>();
		//TODO: Before adding the header, check the attached item:
		// - is not already linked to another header!
		// - is not pending removal
		//TODO: Check if child item already supports the header item (in theory yes)
		mHeaders.add(headerItem);
		if (headersShown) {
			showHeader(headerItem);
		} else {
			headerItem.setHidden(true);
			mapViewTypeFrom((T) headerItem);
		}
		return this;
	}

	/**
	 * Retrieves all the header items.
	 *
	 * @return list non-null with all the header items.
	 */
	public List<ISectionable> getHeaderItems() {
		return mHeaders;
	}

	public List<Integer> getHeadersActualPositions() {
		List<Integer> list = new ArrayList<Integer>(mHeaders.size());
		for (int i = 0; i < mItems.size(); i++) {
			if (mItems.get(i) instanceof ISectionable)
				list.add(i);
		}
		return list;
	}

	/**
	 * Shows all headers in the RecyclerView at their attached position.
	 * <p>Headers can be shown or hidden all together.</p>
	 *
	 * @see #hideAllHeaders()
	 */
	public void showAllHeaders() {
		if (mHeaders == null) {
			Log.w(TAG, "Header list is empty, nothing to show!");
			return;
		}
		multiRange = true;
		for (int i = mHeaders.size() - 1; i >= 0; i--) {
			//Headers must be added from last position: an header can be
			// attached to another one, if all items are removed
			showHeader(mHeaders.get(i));
		}
		if (mHeaders.size() > 0) headersShown = true;
		multiRange = false;
	}

	/**
	 * Hides all headers from the RecyclerView.
	 * <p>Headers can be shown or hidden all together.</p>
	 *
	 * @see #showAllHeaders()
	 */
	public void hideAllHeaders() {
		if (mHeaders == null) {
			Log.w(TAG, "Header list is empty, nothing to hide!");
			return;
		}
		multiRange = true;
		for (int i = mHeaders.size() - 1; i >= 0; i--) {
			hideHeader(mHeaders.get(i));
		}
		headersShown = false;
		multiRange = false;
	}

	/**
	 * Hides and completely remove the header from Adapter.
	 *
	 * @param header the header item to remove
	 */
	public void deleteHeader(@NonNull ISectionable header) {
		if (mHeaders == null) {
			Log.w(TAG, "Header list is empty, nothing to delete!");
			return;
		}
		int index = mHeaders.indexOf(header);
		if (index >= 0) {
			if (DEBUG) Log.v(TAG, "Deleting header " + header);
			int position = getGlobalPositionOf((T) header);
			if (position >= 0) {
				//TODO: Restore of an header and restore previous reference of the attached item
				//TODO: Modify the reference of the attached item with the next item
				removeItem(position);
			}
			mHeaders.remove(index);
		} else {
			if (DEBUG) Log.w(TAG, "Header not found " + header);
		}
	}

	private void showAllHeadersAfterRefresh() {
		if (mHeaders != null && headersShown) {
			for (ISectionable header : mHeaders) header.setHidden(true);
			showAllHeaders();
		}
	}

	/**
	 * Internal method to show/add an header in the internal list.
	 *
	 * @param header the header
	 */
	private void showHeader(@NonNull ISectionable header) {
		int position = getGlobalPositionOf((T) header.getAttachedItem());
		if (position < 0) {
			Log.e(TAG, "Cannot add Header. Cannot find item to which attach the Header! headerItem=" + header);
			return;
		}
		if (header.isHidden() && getPendingRemovedItem((T) header) == null) {
			if (DEBUG) Log.v(TAG, "Showing header at position " + position + "=" + header);
			addItem(position, (T) header);
			header.setHidden(false);
		} else {
			if (DEBUG) Log.w(TAG, "Header already shown at position " + position + "=" + header);
		}
	}

	/**
	 * Internal method to hides/remove an header from the internal list.
	 *
	 * @param header the header
	 */
	private void hideHeader(@NonNull ISectionable header) {
		int position = getGlobalPositionOf((T) header);
		if (position >= 0) {
			if (DEBUG) Log.v(TAG, "Hiding header at position " + position + "=" + header);
			header.setHidden(true);
			//Remove and notify removals, don't create RestoreInfo object by calling removeItem()
			mItems.remove(position);
			notifyItemRemoved(position);
		} else {
			if (DEBUG) Log.w(TAG, "Header already hidden at position " + position + "=" + header);
		}
	}

	/**
	 * Attach the new item to the header.
	 *
	 * @param deletedItem the deleted item
	 * @param newPosition the position of the new item to be attached to the header
	 * @param payload     any non-null user object to notify the parent (the payload will be
	 *                    therefore passed to the bind method of the parent ViewHolder),
	 *                    pass null to <u>not</u> notify the parent
	 * @return the modified header if found, null otherwise
	 */
	private ISectionable attachHeaderTo(T deletedItem, int newPosition, Object payload) {
		for (ISectionable header : mHeaders) {
			if (header.getAttachedItem() != null && header.getAttachedItem().equals(deletedItem)) {
				header.setAttachedItem(getItem(newPosition));
				if (!header.isHidden())
					notifyItemChanged(getGlobalPositionOf((T) header), payload);
				return header;
			}
		}
		return null;
	}

	/*---------------------*/
	/* VIEW HOLDER METHODS */
	/*---------------------*/

	/**
	 * Returns the ViewType for all Items depends by the current position.
	 * <p>You can override this method to return specific values or you can let this method
	 * to call the implementation of {@link IFlexibleItem#getLayoutRes()} so ViewTypes are
	 * automatically mapped.</p>
	 *
	 * @param position position for which ViewType is requested
	 * @return if Item is found, any integer value from user layout resource if defined in
	 * {@code IFlexibleItem#getLayoutRes()}
	 * @throws IllegalStateException if {@link IFlexibleItem#getLayoutRes()} is not implemented
	 *                               and if this method is not overridden.
	 */
	@Override
	public int getItemViewType(int position) {
		T item = getItem(position);
		assert item != null;
		autoMap = true;
		return item.getLayoutRes();//User ViewType or throws IllegalStateException
	}

	/**
	 * You can override this method to create ViewHolder from inside the Adapter or
	 * you can let this method to call the implementation of
	 * {@link IFlexibleItem#createViewHolder(FlexibleAdapter, LayoutInflater, ViewGroup)}
	 * to create ViewHolder from inside the Item.
	 *
	 * @param parent   the ViewGroup into which the new View will be added after it is bound
	 *                 to an adapter position
	 * @param viewType the view type of the new View
	 * @return a new ViewHolder that holds a View of the given view type
	 * @throws IllegalStateException if {@link IFlexibleItem#createViewHolder(FlexibleAdapter, LayoutInflater, ViewGroup)}
	 *                               is not implemented and if this method is not overridden. Also
	 *                               it is thrown if ViewType instance has not been correcly mapped.
	 * @see IFlexibleItem#createViewHolder(FlexibleAdapter, LayoutInflater, ViewGroup)
	 */
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (mInflater == null) {
			mInflater = LayoutInflater.from(parent.getContext());
		}
		T item = getViewTypeInstance(viewType);
		if (item == null) {
			//If everything has been set properly, this should never happen ;-)
			Log.wtf(TAG, "ViewType instance has not been correcly mapped for viewType " + viewType);
			throw new IllegalStateException("ViewType instance has not been correcly mapped for viewType " + viewType);
		}
		return item.createViewHolder(this, mInflater, parent);
	}

	/**
	 * You can override this method to bind the items into the corresponding ViewHolder from
	 * inside the Adapter or you can let this method to call the implementation of
	 * {@link IFlexibleItem#bindViewHolder(FlexibleAdapter, RecyclerView.ViewHolder, int, List)}
	 * to bind the item inside itself.
	 *
	 * @param holder   the ViewHolder created
	 * @param position the adapter position to bind
	 * @throws IllegalStateException if {@link IFlexibleItem#bindViewHolder(FlexibleAdapter, RecyclerView.ViewHolder, int, List)}
	 *                               is not implemented and if this method is not overridden.
	 * @see IFlexibleItem#bindViewHolder(FlexibleAdapter, RecyclerView.ViewHolder, int, List)
	 */
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		this.onBindViewHolder(holder, position, Collections.unmodifiableList(new ArrayList<Object>()));
	}

	/**
	 * Same concept of {@link #onBindViewHolder(RecyclerView.ViewHolder, int)}, but with Payload.
	 * <p>How to use Payload, please refer to
	 * {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int, List)}</p>
	 *
	 * @param holder   the ViewHolder instance
	 * @param position the current position
	 * @param payloads a non-null list of merged payloads. Can be empty list if requires full update.
	 * @throws IllegalStateException if {@link IFlexibleItem#bindViewHolder(FlexibleAdapter, RecyclerView.ViewHolder, int, List)}
	 *                               is not implemented and if this method is not overridden.
	 */
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
		//When user scrolls, this line binds the correct selection status
		holder.itemView.setActivated(isSelected(position));
		T item = getItem(position);
		if (item != null) {
			holder.itemView.setEnabled(item.isEnabled());
			item.bindViewHolder(this, holder, position, payloads);
		}
	}

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

	//FIXME: Expanded children: find a way to Not animate items from custom ItemAnimator!!!
	// (ItemAnimators should work in conjunction with AnimatorViewHolder???)
	//TODO: Customize children animations (don't use animateAdd or animateRemove from custom ItemAnimator)
	//TODO: Check if multiple types of sub items are already supported (in theory yes)
	//TODO: Add new feature (Load More)

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

	public boolean isExpanded(@IntRange(from = 0) int position) {
		return isExpanded(getItem(position));
	}

	public boolean isExpanded(T item) {
		if (isExpandable(item)) {
			IExpandable expandable = (IExpandable) item;
			return expandable.isExpanded();
		}
		return false;
	}

	public boolean isExpandable(T item) {
		return item != null && item instanceof IExpandable;
	}

	public boolean hasSubItems(IExpandable expandable) {
		return expandable.getSubItems() != null && expandable.getSubItems().size() > 0;
	}

	/**
	 * Retrieves the parent of a child.
	 * <p>Only for a real child of an expanded parent.</p>
	 *
	 * @param child the child item
	 * @return the parent of this child item or null if not found
	 * @see #getExpandablePositionOf(IFlexibleItem)
	 * @see #getRelativePositionOf(IFlexibleItem)
	 */
	public IExpandable getExpandableOf(@NonNull T child) {
		for (T parent : mItems) {
			if (isExpandable(parent)) {
				IExpandable expandable = (IExpandable) parent;
				if (expandable.isExpanded() && hasSubItems(expandable)) {
					List<T> list = expandable.getSubItems();
					for (T subItem : list) {
						//Pick up only no-hidden items
						if (!subItem.isHidden() && subItem.equals(child))
							return expandable;
					}
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
	 * @see #getExpandableOf(IFlexibleItem)
	 * @see #getRelativePositionOf(IFlexibleItem)
	 */
	public int getExpandablePositionOf(@NonNull T child) {
		return getGlobalPositionOf((T) getExpandableOf(child));
	}

	/**
	 * Provides the list where the child currently lays.
	 *
	 * @param child the child item
	 * @return the list of the child element, or a new list if item
	 * @see #getExpandableOf(IFlexibleItem)
	 * @see #getExpandablePositionOf(IFlexibleItem)
	 * @see #getRelativePositionOf(IFlexibleItem)
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
	 * @see #getExpandableOf(IFlexibleItem)
	 * @see #getExpandablePositionOf(IFlexibleItem)
	 */
	public int getRelativePositionOf(@NonNull T child) {
		return getSiblingsOf(child).indexOf(child);
	}

	/**
	 * Provides a list of all expandable items that are currently expanded.
	 *
	 * @return a list with all expanded items
	 * @see #getSiblingsOf(IFlexibleItem)
	 * @see #getExpandedPositions()
	 */
	public List<T> getExpandedItems() {
		List<T> expandedItems = new ArrayList<T>();
		for (T item : mItems) {
			if (isExpanded(item))
				expandedItems.add(item);
		}
		return expandedItems;
	}

	/**
	 * Provides a list of all expandable positions that are currently expanded.
	 *
	 * @return a list with the global positions of all expanded items
	 * @see #getSiblingsOf(IFlexibleItem)
	 * @see #getExpandedItems()
	 */
	public List<Integer> getExpandedPositions() {
		List<Integer> expandedPositions = new ArrayList<Integer>();
		for (int i = 0; i < mItems.size() - 1; i++) {
			T item = mItems.get(i);
			if (isExpanded(item))
				expandedPositions.add(i);
		}
		return expandedPositions;
	}

	/**
	 * Expands an item that is Expandable, not yet expanded, that has subItems and
	 * no child is selected.
	 *
	 * @param position the position of the item to expand
	 * @return the number of subItems expanded
	 */
	public int expand(@IntRange(from = 0) int position) {
		return expand(position, false);
	}

	private int expand(int position, boolean expandAll) {
		T item = getItem(position);
		if (item == null || !item.isEnabled() || !isExpandable(item)) return 0;

		IExpandable expandable = (IExpandable) item;
		if (DEBUG) Log.v(TAG, "Request to Expand on position " + position +
				" expanded " + expandable.isExpanded() + " ExpandedItems=" + getExpandedPositions());

		int subItemsCount = 0;
		if (!expandable.isExpanded() && !parentSelected && hasSubItems(expandable)) {

			//Collapse others expandable if configured so
			//Skipped when expanding all is requested
			if (collapseOnExpand && !expandAll) {
				//Fetch again the new position after collapsing all!!
				if (collapseAll() > 0) position = getGlobalPositionOf(item);
			}

			//Every time an expansion is requested, subItems must be taken from the original Object!
			//without the subItems that are going to be removed
			//Save a copy child items list
			List<T> subItems = getExpandableList(expandable);
			mItems.addAll(position + 1, subItems);
			subItemsCount = subItems.size();
			//Save expanded state
			expandable.setExpanded(true);
			//Map all the view types if not done yet
			mapViewTypesFrom(subItems);

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
		//More efficient if we expand from First expandable position
		for (int i = 0; i < mItems.size() - 1; i++) {
			if (expand(i, true) > 0) expanded++;
		}
		return expanded;
	}

	/**
	 * Collapses an Expandable item that is already expanded, in conjunction with no subItems
	 * selected or item is pending removal (used in combination with removeRange).
	 * <p>All Expandable subItem, that are expanded, are recursively collapsed.</p>
	 *
	 * @param position the position of the item to collapse
	 * @return the number of subItems collapsed
	 */
	public int collapse(@IntRange(from = 0) int position) {
		T item = getItem(position);
		if (item == null || !item.isEnabled() || !isExpandable(item)) return 0;

		IExpandable expandable = (IExpandable) item;
		if (DEBUG)
			Log.v(TAG, "Request to Collapse on position " + position + " ExpandedItems=" + getExpandedPositions());

		int subItemsCount = 0, recursiveCount = 0;
		if (expandable.isExpanded() &&
				(!hasSubItemsSelected(expandable) || getPendingRemovedItem(item) != null)) {

			//Take the current subList
			List<T> subItems = getExpandableList(expandable);
			//Recursive collapse of all sub expandable
			recursiveCount = recursiveCollapse(subItems);
			mItems.removeAll(subItems);
			subItemsCount = subItems.size();
			//Save expanded state
			expandable.setExpanded(false);

			//Collapse!
			notifyItemRangeRemoved(position + 1, subItemsCount);

			if (DEBUG)
				Log.v(TAG, "Collapsed " + subItemsCount + " subItems on position=" + position + " ExpandedItems=" + getExpandedPositions());
		}
		return subItemsCount + recursiveCount;
	}

	@SuppressWarnings("Range")
	private int recursiveCollapse(List<T> subItems) {
		int collapsed = 0;
		for (T subItem : subItems) {
			if (isExpanded(subItem)) {
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
		//More efficient if we collapse from Last expanded position
		for (int i = mItems.size() - 1; i >= 0; i--) {
			if (collapse(i) > 0) collapsed++;
		}
		return collapsed;
	}

	/*----------------*/
	/* UPDATE METHODS */
	/*----------------*/

	public void updateItem(@IntRange(from = 0) int position, @NonNull T item, Object payload) {
		if (position < 0 && position >= mItems.size()) {
			Log.e(TAG, "Cannot updateItem on position out of OutOfBounds!");
			return;
		}
		mItems.set(position, item);
		if (DEBUG) Log.v(TAG, "updateItem notifyItemChanged on position " + position);
		notifyItemChanged(position, payload);
	}

	/*----------------*/
	/* ADDING METHODS */
	/*----------------*/

	/**
	 * Inserts the given Item at desired position or Add Item at last position with a delay
	 * and auto-scroll to the position.
	 * <p>Useful at startup, when there's an item to add after Adapter Animations is completed.</p>
	 *
	 * @param position         position of the item to add
	 * @param item             the item to add
	 * @param delay            a non negative delay
	 * @param scrollToPosition true if RecyclerView should scroll after item has been added,
	 *                         false otherwise
	 */
	public void addItemWithDelay(@IntRange(from = 0) final int position, @NonNull final T item,
								 @IntRange(from = 0) long delay, final boolean scrollToPosition) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (addItem(position, item) && scrollToPosition)
					mRecyclerView.scrollToPosition(
							Math.min(Math.max(0, position), getItemCount() - 1));
			}
		}, delay);
	}

	/**
	 * Inserts the given item in the internal list at the specified position or Adds the item
	 * at last position.
	 *
	 * @param position position of the item to add
	 * @param item     the item to add
	 * @return true if is has been modified by the addition, false otherwise
	 */
	public boolean addItem(@IntRange(from = 0) int position, @NonNull T item) {
		if (item == null) {
			Log.e(TAG, "No items to add!");
			return false;
		}
		if (DEBUG) Log.v(TAG, "addItem delegates addition to addItems!");
		List<T> items = new ArrayList<T>(1);
		items.add(item);
		return addItems(position, items);
	}

	/**
	 * Inserts a set of items in the internal list at specified position or Adds the items
	 * at last position.
	 *
	 * @param position position inside the list, -1 to add the set the end of the list
	 * @param items    the items to add
	 * @return true if the addition was successful, false otherwise
	 */
	public boolean addItems(@IntRange(from = 0) int position, @NonNull List<T> items) {
		if (position < 0) {
			Log.e(TAG, "Cannot addItems on negative position!");
			return false;
		}
		if (items == null || items.isEmpty()) {
			Log.e(TAG, "No items to add!");
			return false;
		}
		if (DEBUG)
			Log.v(TAG, "addItems on position=" + position + " itemCount=" + items.size());

		//Insert Items
		if (position < mItems.size()) {
			mItems.addAll(position, items);
		} else
			mItems.addAll(items);
		//Map all the view types if not done yet
		mapViewTypesFrom(items);
		//Notify range addition
		notifyItemRangeInserted(position, items.size());
		//Call listener to update EmptyView
		if (mUpdateListener != null && !multiRange)
			mUpdateListener.onUpdateEmptyView(getItemCount());
		return true;
	}

	/**
	 * Convenience method of {@link #addSubItem(int, int, IFlexibleItem, boolean, Object)}.
	 * <br/>In this case parent item will never be notified nor expanded if it is collapsed.
	 */
	public boolean addSubItem(@IntRange(from = 0) int parentPosition,
							  @IntRange(from = 0) int subPosition, @NonNull T item) {
		return this.addSubItem(parentPosition, subPosition, item, false, null);
	}

	/**
	 * Add an item inside the list of an expandable item (parent).
	 * <p><b>In order to add a subItem</b>, the following condition must be satisfied:
	 * <br/>- The item resulting from the parent position is actually an Expandable.</p>
	 * Optionally you can pass any payload to notify the parent about the change and optimize the
	 * view binding.
	 *
	 * @param parentPosition position of the expandable item that shall contain the subItem
	 * @param subPosition    the position of the subItem in the expandable list
	 * @param item           the subItem to add in the expandable list
	 * @param expandParent   true to initially expand the parent (if needed) and after to add
	 *                       the subItem, false to simply add the sub item to the parent
	 * @param payload        any non-null user object to notify the parent (the payload will be
	 *                       therefore passed to the bind method of the parent ViewHolder),
	 *                       pass null to <u>not</u> notify the parent
	 */
	public boolean addSubItem(@IntRange(from = 0) int parentPosition,
							  @IntRange(from = 0) int subPosition,
							  @NonNull T item, boolean expandParent, Object payload) {
		if (item == null) {
			Log.e(TAG, "No items to add!");
			return false;
		}
		//Build a new list with 1 item to chain the methods of addSubItems
		List<T> subItems = new ArrayList<T>(1);
		subItems.add(item);
		//Reuse the method for subItems
		return addSubItems(parentPosition, subPosition, subItems, expandParent, payload);
	}

	/**
	 * TODO: javadoc
	 *
	 * @param parentPosition
	 * @param expandable
	 * @param expandParent
	 * @param payload
	 * @return
	 */
	public int addAllSubItemsFrom(@IntRange(from = 0) int parentPosition,
								  @NonNull IExpandable expandable, boolean expandParent, Object payload) {
		List<T> subItems = getCurrentChildren(expandable);
		addSubItems(parentPosition, 0, expandable, subItems, expandParent, payload);
		return subItems.size();
	}

	/**
	 * TODO: javadoc
	 *
	 * @param parentPosition
	 * @param subPosition
	 * @param items
	 * @param expandParent
	 * @param payload
	 */
	public boolean addSubItems(@IntRange(from = 0) int parentPosition,
							   @IntRange(from = 0) int subPosition,
							   @NonNull List<T> items, boolean expandParent, Object payload) {
		T parent = getItem(parentPosition);
		if (isExpandable(parent)) {
			IExpandable expandable = (IExpandable) parent;
			return addSubItems(parentPosition, subPosition, expandable, items, expandParent, payload);
		}
		Log.e(TAG, "Passed parentPosition doesn't belong to an Expandable item!");
		return false;
	}

	private boolean addSubItems(@IntRange(from = 0) int parentPosition,
								@IntRange(from = 0) int subPosition,
								@NonNull IExpandable expandable,
								@NonNull List<T> items, boolean expandParent, Object payload) {
		boolean added = false;
		//Expand parent if requested and not already expanded
		if (expandParent && !expandable.isExpanded()) {
			expand(parentPosition);
		}
		//Notify the adapter of the new addition to display it and animate it.
		//If parent is collapsed there's no need to notify about the change.
		if (expandable.isExpanded()) {
			added = addItems(parentPosition + 1 + Math.max(0, subPosition), items);
		}
		//Notify the parent about the change if requested
		if (payload != null) notifyItemChanged(parentPosition, payload);
		return added;//FIXME? should be return always true??
	}

	/*----------------------*/
	/* DELETE ITEMS METHODS */
	/*----------------------*/

	/**
	 * Removes an item from internal list and notify the change.
	 * <p>The item is retained for an eventual Undo.</p>
	 *
	 * @param position the position of item to remove
	 * @see #removeItem(int, Object)
	 * @see #startUndoTimer(long, OnDeleteCompleteListener)
	 * @see #restoreDeletedItems()
	 * @see #setRestoreSelectionOnUndo(boolean)
	 * @see #emptyBin()
	 */
	public void removeItem(@IntRange(from = 0) int position) {
		this.removeItem(position, null);
	}

	/**
	 * Removes an item from the internal list and notify the change.
	 * <p>If the item, resulting from the passed position is:</p>
	 * - <u>not expandable</u> with <u>no</u> parent, it is removed as usual.<br/>
	 * - <u>not expandable</u> with a parent, it is removed only if the parent is expanded.
	 * Optionally the parent can be notified about the removal if a payload is passed.<br/>
	 * - <u>expandable</u> implementing {@link IExpandable}, it is removed as usual, but
	 * it will be collapsed if expanded.
	 * <p>The item is retained for an eventual Undo.</p>
	 *
	 * @param position The position of item to remove
	 * @param payload  any non-null user object to notify the parent (the payload will be
	 *                 therefore passed to the bind method of the parent ViewHolder),
	 *                 pass null to <u>not</u> notify the parent
	 * @see #removeItem(int)
	 */
	public void removeItem(@IntRange(from = 0) int position, Object payload) {
		if (DEBUG) Log.v(TAG, "removeItem delegates removal to removeRange");
		removeRange(position, 1, payload);
	}

	/**
	 * Same as {@link #removeItems(List, Object)}, but in this case the parent will not be
	 * notified about the change, if a child is removed.
	 */
	public void removeItems(List<Integer> selectedPositions) {
		this.removeItems(selectedPositions, null);
	}

	/**
	 * Removes a list of items from internal list and notify the change.
	 * <p>Every item is retained for an eventual Undo.</p>
	 * Optionally you can pass any payload to notify the parent about the change and optimize the
	 * view binding.
	 *
	 * @param selectedPositions list with item positions to remove
	 * @param payload           any non-null user object to notify the parent (the payload will be
	 *                          therefore passed to the bind method of the parent ViewHolder),
	 *                          pass null to <u>not</u> notify the parent
	 */
	public void removeItems(List<Integer> selectedPositions, Object payload) {
		if (DEBUG)
			Log.v(TAG, "removeItems selectedPositions=" + selectedPositions + " payload=" + payload);
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
		multiRange = true;
		for (Integer position : selectedPositions) {//10 9 8 //5 4 //1
			if (lastPosition - itemCount == position) {//10-0==10  10-1==9  10-2==8  10-3==5 NO  //5-1=4  5-2==1 NO
				itemCount++;             // 1  2  3  //2
				positionStart = position;//10  9  8  //4
			} else {
				adjustSelected = false;
				//Remove range
				if (itemCount > 0)
					removeRange(positionStart, itemCount, payload);//8,3  //4,2
				positionStart = lastPosition = position;//5  //1
				itemCount = 1;
			}
			//Request to collapse after the notification of remove range
			collapse(position);
		}
		multiRange = false;
		//Clear also the selection
		clearSelection();
		//Remove last range
		if (itemCount > 0) {
			removeRange(positionStart, itemCount, payload);//1,1
		}
	}

	/**
	 * Same as {@link #removeRange(int, int, Object)}, but in this case the parent will not be
	 * notified about the change, if children are removed.
	 */
	public void removeRange(int positionStart, int itemCount) {
		this.removeRange(positionStart, itemCount, null);
	}

	/**
	 * Removes a list of consecutive items from internal list and notify the change.
	 * <p>Optionally you can pass any payload to notify the parent about the change and optimize
	 * the view binding.</p>
	 *
	 * @param positionStart the start position of the first item
	 * @param itemCount     how many items should be removed
	 * @param payload       any non-null user object to notify the parent (the payload will be
	 *                      therefore passed to the bind method of the parent ViewHolder),
	 *                      pass null to <u>not</u> notify the parent
	 */
	public void removeRange(int positionStart, int itemCount, Object payload) {
		int initialCount = getItemCount();
		if (DEBUG)
			Log.v(TAG, "removeRange positionStart=" + positionStart + " itemCount=" + itemCount);
		if (positionStart < 0 || (positionStart + itemCount) > initialCount) {
			Log.e(TAG, "Cannot removeRange with positionStart out of OutOfBounds!");
			return;
		}
		//Handle header linkage
		ISectionable header = attachHeaderTo(getItem(positionStart), positionStart + itemCount, payload);

		int parentPosition = -1;
		IExpandable parent = null;
		//for (int position = positionStart + itemCount - 1; position >= positionStart; position--) {
		for (int position = positionStart; position < positionStart + itemCount; position++) {
			if (!permanentDelete) {
				T item = getItem(positionStart);
				assert item != null;
				//When removing a range of children, parent is always the same :-)
				if (parent == null) parent = getExpandableOf(item);
				//Differentiate: (Expandable & NonExpandable with No parent) from (NonExpandable with a parent)
				if (isExpandable(item) || parent == null) {
					createRestoreItemInfo(positionStart, item,
							(position == positionStart ? header : null), payload);
				} else {
					parentPosition = createRestoreSubItemInfo(parent, item,
							(position == positionStart ? header : null), payload);
				}
			}
			//Remove item from internal list
			mItems.remove(positionStart);
		}

		//Notify removals
		if (parentPosition >= 0) {
			adjustSelected = false;
			//Notify the Children removal only if Parent is expanded
			notifyItemRangeRemoved(positionStart, itemCount);
			//Notify the Parent about the change if requested
			if (payload != null) notifyItemChanged(parentPosition, payload);
		} else {
			//Notify range removal
			notifyItemRangeRemoved(positionStart, itemCount);
		}
		//Update empty view
		if (mUpdateListener != null && !multiRange && initialCount != getItemCount())
			mUpdateListener.onUpdateEmptyView(getItemCount());
	}

	/**
	 * Convenience method to remove all Items that are currently selected.
	 * <p>Parent will not be notified about the change, if a child is removed.</p>
	 *
	 * @see #removeAllSelectedItems(Object)
	 */
	public void removeAllSelectedItems() {
		this.removeItems(getSelectedPositions());
	}

	/**
	 * Convenience method to remove all Items that are currently selected.<p>
	 * Optionally the Parent can be notified about the change, if a child is removed, by passing
	 * any payload.
	 *
	 * @param payload any non-null user object to notify the parent (the payload will be
	 *                therefore passed to the bind method of the parent ViewHolder),
	 *                pass null to <u>not</u> notify the parent
	 */
	public void removeAllSelectedItems(Object payload) {
		this.removeItems(getSelectedPositions(), payload);
	}

	/*----------------------*/
	/* UNDO/RESTORE METHODS */
	/*----------------------*/

	public boolean isPermanentDelete() {
		return permanentDelete;
	}

	public void setPermanentDelete(boolean permanentDelete) {
		this.permanentDelete = permanentDelete;
	}

	/**
	 * Returns the current configuration to restore selections on Undo.
	 *
	 * @return true if selection will be restored, false otherwise
	 */
	public boolean isRestoreWithSelection() {
		return restoreSelection;
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
		this.restoreSelection = restoreSelection;
	}

	/**
	 * Restore items just removed.
	 * <p><b>NOTE:</b> If filter is active, only items that match that filter will be shown(restored).</p>
	 *
	 * @see #setRestoreSelectionOnUndo(boolean)
	 */
	@SuppressWarnings("ResourceType")
	public void restoreDeletedItems() {
		stopUndoTimer();
		//Be sure list is reverse insert (list was reverse ordered on Delete)
		multiRange = true;
		int initialCount = getItemCount();
		//Selection coherence: start from a clear situation, clear selection if restoreSelection is active
		if (restoreSelection) clearSelection();
		//Start from latest item deleted, since others could rely on it
		for (int i = mRestoreList.size() - 1; i >= 0; i--) {
			RestoreInfo restoreInfo = mRestoreList.get(i);
			boolean added;
			if (restoreInfo.relativePosition >= 0) {
				//Restore child, if not deleted
				if (DEBUG) Log.v(TAG, "Restore Child " + restoreInfo);
				//Restore header linkage
				restoreInfo.restoreHeader();
				//Skip subItem addition if filter is active
				if (hasSearchText() && !filterObject(restoreInfo.item, getSearchText()))
					continue;
				//Add subItem
				added = addSubItem(restoreInfo.getRefPosition(), restoreInfo.relativePosition,
						restoreInfo.item, false, restoreInfo.payload);
			} else {
				//Restore parent or simple item, if not deleted
				if (DEBUG) Log.v(TAG, "Restore Parent " + restoreInfo);
				//Restore header linkage
				restoreInfo.restoreHeader();
				//Skip item addition if filter is active
				if (hasSearchText() && !filterExpandableObject(restoreInfo.item, getSearchText()))
					continue;
				//Add item
				added = addItem(restoreInfo.getRestorePosition(), restoreInfo.item);
			}
			//Item is again visible
			restoreInfo.item.setHidden(false);
			//Restore selection if requested, before emptyBin.
			if (restoreSelection && restoreInfo.item.isSelectable() && added) {
				if (restoreInfo.item instanceof IExpandable || getExpandableOf(restoreInfo.item) == null) {
					parentSelected = true;
					getSelectedPositions().add(restoreInfo.getRestorePosition());
				} else {
					childSelected = true;
					getSelectedPositions().add(restoreInfo.getRefPosition() + 1 + restoreInfo.relativePosition);
				}
			}
		}
		if (DEBUG && restoreSelection)
			Log.v(TAG, "Selected positions after restore " + getSelectedPositions());

		//Call listener to update EmptyView
		multiRange = false;
		if (mUpdateListener != null && initialCount != getItemCount())
			mUpdateListener.onUpdateEmptyView(getItemCount());

		emptyBin();
	}

	/**
	 * Clean memory from items just removed.
	 * <p><b>Note:</b> This method is automatically called after timer is over and after a
	 * restoration.</p>
	 */
	public synchronized void emptyBin() {
		if (DEBUG) Log.v(TAG, "emptyBin!");
		mRestoreList.clear();
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
		//Make longer the timer for new coming deleted items
		mHandler.removeMessages(1);
		mHandler.sendMessageDelayed(Message.obtain(mHandler, 1, listener), timeout > 0 ? timeout : UNDO_TIMEOUT);
	}

	/**
	 * Stop Undo timer.
	 * <p><b>Note:</b> This method is automatically called in case of restoration.</p>
	 */
	protected void stopUndoTimer() {
		mHandler.removeCallbacksAndMessages(null);
	}

	public boolean isRestoreInTime() {
		return mRestoreList != null && mRestoreList.size() > 0;
	}

	/**
	 * @return the list of deleted items
	 */
	public List<T> getDeletedItems() {
		List<T> deletedItems = new ArrayList<T>();
		for (RestoreInfo restoreInfo : mRestoreList) {
			deletedItems.add(restoreInfo.item);
		}
		return deletedItems;
	}

	/**
	 * @return a list with the global positions of all deleted items
	 */
//	public List<Integer> getDeletedPositions() {
//		List<Integer> deletedItems = new ArrayList<Integer>();
//		for (RestoreInfo restoreInfo : mRestoreList) {
//			if (!deletedItems.contains(restoreInfo.refPosition))
//				deletedItems.add(restoreInfo.refPosition);
//		}
//		return deletedItems;
//	}

	/**
	 * Retrieves the expandable of the deleted child.
	 *
	 * @param child the deleted child
	 * @return the expandable(parent) of this child, or null if no parent found.
	 */
	public IExpandable getExpandableOfDeletedChild(T child) {
		for (RestoreInfo restoreInfo : mRestoreList) {
			if (restoreInfo.item.equals(child))
				if (isExpandable(restoreInfo.refItem))
					return (IExpandable) restoreInfo.refItem;
		}
		return null;
	}

	/**
	 * Retrieves only the deleted children of the specified parent.
	 *
	 * @param expandable the parent item
	 * @return the list of deleted children
	 */
	public List<T> getDeletedChildren(T expandable) {
		List<T> deletedChild = new ArrayList<T>();
		for (RestoreInfo restoreInfo : mRestoreList) {
			if (restoreInfo.refItem != null && restoreInfo.refItem.equals(expandable) && restoreInfo.relativePosition >= 0)
				deletedChild.add(restoreInfo.item);
		}
		return deletedChild;
	}

	/**
	 * Retrieves all the original children of the specified parent, filtering out all the
	 * deleted children if any.
	 *
	 * @param expandable the parent item
	 * @return a non null list of the original children minus the deleted children if some are
	 * pending removal.
	 */
	public List<T> getCurrentChildren(IExpandable expandable) {
		//Check item and subItems existence
		if (expandable == null || !hasSubItems(expandable))
			return new ArrayList<T>();

		//Take a copy of the subItems list
		List<T> subItems = new ArrayList<T>(expandable.getSubItems());
		//Remove all children pending removal
		if (mRestoreList.size() > 0) {
			subItems.removeAll(getDeletedChildren((T) expandable));
		}
		return subItems;
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
	 * <p>This happens systematically when searchText is reduced in length by the user. It doesn't
	 * reduce performance in other cases, since the notification is triggered only in
	 * {@link #applyAndAnimateAdditions(List, List)} when new items are not added.</p>
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
	 * <p>Same as {@link #filterItems(List)}, but with a delay in the execution, useful to grab
	 * more characters from user before starting the search.</p>
	 *
	 * @param unfilteredItems the list to filter
	 * @param delay           any non negative delay
	 * @see #filterObject(IFlexibleItem, String)
	 */
	public void filterItems(@NonNull List<T> unfilteredItems, @IntRange(from = 0) long delay) {
		//Make longer the timer for new coming deleted items
		mHandler.removeMessages(0);
		mHandler.sendMessageDelayed(Message.obtain(mHandler, 0, unfilteredItems), delay > 0 ? delay : 0);
	}

	/**
	 * <b>WATCH OUT! PASS ALWAYS A <u>COPY</u> OF THE ORIGINAL LIST</b>: due to internal mechanism,
	 * items are removed and/or added in order to animate items in the final list.
	 * <p>
	 * Filters the provided list with the search text previously set with {@link #setSearchText(String)}.
	 * </p>
	 * <b>Note:</b>
	 * <br/>- This method calls {@link #filterObject(IFlexibleItem, String)}.
	 * <br/>- If search text is empty or null, the provided list is the current list.
	 * <br/>- Any pending deleted items are always filtered out.
	 * <br/>- Original positions of deleted items are recalculated.
	 * <br/>- <b>NEW!</b> Items are animated thanks to {@link #animateTo(List)}.
	 *
	 * @param unfilteredItems the list to filter
	 * @see #filterObject(IFlexibleItem, String)
	 */
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
				if (filterExpandableObject(item, getSearchText())) {
					RestoreInfo restoreInfo = getPendingRemovedItem(item);
					if (restoreInfo != null) {
						//If found point to the new reference while filtering
						restoreInfo.filterRefItem = ++newOriginalPosition < values.size() ? values.get(newOriginalPosition) : null;
					} else {
						values.add(item);
						newOriginalPosition++;
						if (isExpandable(item)) {
							IExpandable expandable = (IExpandable) item;
							if (expandable.isExpanded()) {
								List<T> filteredSubItems = new ArrayList<T>();
								//Add subItems if not hidden by filterObject()
								List<T> subItems = expandable.getSubItems();
								for (T subItem : subItems) {
									if (!subItem.isHidden()) filteredSubItems.add(subItem);
								}
								//Map the view types if not done yet
								mapViewTypesFrom(filteredSubItems);
								values.addAll(filteredSubItems);
								newOriginalPosition += filteredSubItems.size();
							}
						}
					}
				}
			}
		} else {
			values = unfilteredItems; //with no filter
			if (!mRestoreList.isEmpty()) {
				for (RestoreInfo restoreInfo : mRestoreList) {
					//Clear the refItem generated by the filter
					restoreInfo.clearFilterRef();
					//Find the real reference
					restoreInfo.refItem = values.get(Math.max(0, values.indexOf(restoreInfo.item) - 1));
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
		//Restore headers if necessary
		if (mSearchText.isEmpty()) {
			showAllHeadersAfterRefresh();
		}

		//Reset filtering flag
		filtering = false;

		//Call listener to update EmptyView
		if (mUpdateListener != null && initialCount != getItemCount())
			mUpdateListener.onUpdateEmptyView(getItemCount());
	}

	/**
	 * This method is a wrapper filter for expandable items.<br/>
	 * It performs filtering on the subItems returns true, if the any child should be in the
	 * filtered collection.
	 * <p>If the provided item is not an expandable it will be filtered as usual by
	 * {@link #filterObject(IFlexibleItem, String)}.</p>
	 *
	 * @param item       the object with subItems to be inspected
	 * @param constraint constraint, that the object has to fulfil
	 * @return true, if the object should be in the filteredResult, false otherwise
	 */
	private boolean filterExpandableObject(T item, String constraint) {
		//Reset expansion flag
		boolean filtered = false;
		if (isExpandable(item)) {
			IExpandable expandable = (IExpandable) item;
			expandable.setExpanded(false);
			//Children scan filter
			for (T subItem : getCurrentChildren(expandable)) {
				//Reuse normal filter for Children
				subItem.setHidden(!filterObject(subItem, constraint));
				if (!filtered && !subItem.isHidden()) {
					filtered = true;
				}
			}
			//Expand if filter found text in subItems
			expandable.setExpanded(filtered);
		}
		//if not filtered already, fallback to Normal filter
		return filtered || filterObject(item, constraint);
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
	 * Animate from the current list to the another.
	 * <p>Used by the filter.</p>
	 * Unchanged items will be notified if {@code mNotifyChangeOfUnfilteredItems} is set true, and
	 * payload will be set as a Boolean.
	 *
	 * @param models the new list containing the new items
	 * @return the cleaned up item list. make sure to set your new list to this one
	 * @see #setNotifyChangeOfUnfilteredItems(boolean)
	 */
	public List<T> animateTo(List<T> models) {
		applyAndAnimateRemovals(mItems, models);
		applyAndAnimateAdditions(mItems, models);
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
				notifyItemChanged(i, mNotifyChangeOfUnfilteredItems);
				if (DEBUG) Log.v(TAG, "animateAdditions keep position=" + i + " item=" + item);
			}
		}
		if (DEBUG) Log.v(TAG, "animateAdditions total out=" + out + " in=" + newItems.size());
	}

	/*---------------*/
	/* TOUCH METHODS */
	/*---------------*/

	/**
	 * Used by {@link FlexibleViewHolder#onTouch(View, MotionEvent)}
	 * to start Drag when HandleView is touched.
	 *
	 * @return the ItemTouchHelper instance already initialized.
	 */
	public final ItemTouchHelper getItemTouchHelper() {
		initializeItemTouchHelper();
		return mItemTouchHelper;
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
		this.longPressDragEnabled = longPressDragEnabled;
		mItemTouchHelperCallback.setLongPressDragEnabled(longPressDragEnabled);
	}

	/**
	 * Enabled by default.
	 * <p>To use, it is sufficient to set the HandleView by calling
	 * {@link FlexibleViewHolder#setDragHandleView(View)}.</p>
	 *
	 * @return true if active, false otherwise
	 */
	public boolean isHandleDragEnabled() {
		return handleDragEnabled;
	}

	/**
	 * Enable/Disable the drag with handle.
	 * <p>Default value is true.</p>
	 *
	 * @param handleDragEnabled true to activate, false otherwise
	 */
	public void setHandleDragEnabled(boolean handleDragEnabled) {
		this.handleDragEnabled = handleDragEnabled;
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
	 * Enable the Swipe of the items
	 * <p>Default value is false.</p>
	 *
	 * @param swipeEnabled true to activate, false otherwise
	 */
	public final void setSwipeEnabled(boolean swipeEnabled) {
		initializeItemTouchHelper();
		this.swipeEnabled = swipeEnabled;
		mItemTouchHelperCallback.setSwipeEnabled(swipeEnabled);
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

		//TODO: If a section (being dragged or being swapped) change first item in that section
		//TODO: If item in a section has being dragged, should swap section as well ??

		//TODO: Allow child to be moved into another parent, update the 2 parents, optionally: 1) collapse the new parent 2) expand it 3) leave as it is
	}

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

	/*-----------------*/
	/* PRIVATE METHODS */
	/*-----------------*/

	/**
	 * Internal mapper to remember and add all ViewTypes for the Items.
	 *
	 * @param items list of items to map
	 */
	private void mapViewTypesFrom(Iterable<T> items) {
		if (items != null) {
			for (T item : items) {
				mapViewTypeFrom(item);
			}
		}
	}

	/**
	 * Internal mapper to remember and add all types for the RecyclerView.
	 *
	 * @param item the item to map
	 */
	private void mapViewTypeFrom(T item) {
		if (item != null && !mTypeInstances.containsKey(item.getLayoutRes())) {
			mTypeInstances.put(item.getLayoutRes(), item);
			if (DEBUG) Log.i(TAG, "Mapped viewType " + item.getLayoutRes() + " from " + item);
		}
	}

	/**
	 * Retrieves the TypeInstance remembered within the FlexibleAdapter for an item.
	 *
	 * @param viewType the ViewType of the item
	 * @return the IFlexibleItem instance, creator of the ViewType
	 */
	private T getViewTypeInstance(int viewType) {
		return mTypeInstances.get(viewType);
	}

	/**
	 * Clears flags after searchText is cleared out for Expandable items and sub items.
	 */
	private void resetFilterFlags(List<T> items) {
		//Reset flags for all items!
		for (T item : items) {
			if (isExpandable(item)) {
				IExpandable expandable = (IExpandable) item;
				expandable.setExpanded(false);
				List<T> subItems = expandable.getSubItems();
				for (T subItem : subItems) {
					subItem.setHidden(false);
				}
			}
		}
	}

	/**
	 * @param item the item to compare
	 * @return the removed item if found, null otherwise
	 */
	private RestoreInfo getPendingRemovedItem(T item) {
		for (RestoreInfo restoreInfo : mRestoreList) {
			if (restoreInfo.item.equals(item)) return restoreInfo;
		}
		return null;
	}

	/**
	 * @param expandable the expandable, parent of this sub item
	 * @param item       the deleted item
	 * @param header     the header item
	 * @param payload    any payload object
	 * @return the parent position
	 */
	private int createRestoreSubItemInfo(IExpandable expandable, T item, ISectionable header, Object payload) {
		int parentPosition = getGlobalPositionOf((T) expandable);
		List<T> siblings = getExpandableList(expandable);
		int childPosition = siblings.indexOf(item);
		item.setHidden(true);
		mRestoreList.add(new RestoreInfo((T) expandable, item, childPosition, header, payload));
		if (DEBUG)
			Log.v(TAG, "Recycled Child " + mRestoreList.get(mRestoreList.size() - 1) + " with Parent position=" + parentPosition);
		return parentPosition;
	}

	/**
	 * @param position the position of the item to retain.
	 * @param header   the header item
	 * @param item     the deleted item
	 */
	private void createRestoreItemInfo(int position, T item, ISectionable header, Object payload) {
		//Collapse Parent before removal if it is expanded!
		if (isExpanded(item))
			collapse(position);
		item.setHidden(true);
		//Get the reference of the previous item (getItem returns null if outOfBounds)
		//If null, it will be restored at position = 0
		T refItem = getItem(position - 1);
		if (refItem != null) {
			//Check if the refItem is a child of an Expanded parent, take the parent!
			IExpandable expandable = getExpandableOf(refItem);
			if (expandable != null) refItem = (T) expandable;
		}
		mRestoreList.add(new RestoreInfo(refItem, item, header, payload));
		if (DEBUG)
			Log.v(TAG, "Recycled Parent " + mRestoreList.get(mRestoreList.size() - 1) + " on position=" + position);
	}

	/**
	 * @param expandable the parent item
	 * @return the list of the subItems not hidden
	 */
	private List<T> getExpandableList(IExpandable expandable) {
		List<T> subItems = new ArrayList<T>();
		if (expandable != null && hasSubItems(expandable)) {
			List<T> allSubItems = expandable.getSubItems();
			for (T subItem : allSubItems) {
				//Pick up only no hidden items (doesn't get into account the filtered items)
				if (!subItem.isHidden()) subItems.add(subItem);
			}
		}
		return subItems;
	}

	/**
	 * Allows or disallows the request to collapse the Expandable item.
	 *
	 * @param expandable the expandable item to check
	 * @return true if at least 1 subItem is currently selected, false if no subItems are selected
	 */
	private boolean hasSubItemsSelected(IExpandable expandable) {
		for (T subItem : getExpandableList(expandable)) {
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
			int scrollTo = firstVisibleItem + scrollBy;
			if (DEBUG)
				Log.v(TAG, "scrollMin=" + scrollMin + " scrollMax=" + scrollMax + " scrollBy=" + scrollBy + " scrollTo=" + scrollTo);
			mRecyclerView.smoothScrollToPosition(scrollTo);
		} else if (position < firstVisibleItem) {
			mRecyclerView.smoothScrollToPosition(position);
		}
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
		//Save headers shown status
		outState.putBoolean(EXTRA_HEADERS, headersShown);
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
		//Restore headers shown status
		headersShown = savedInstanceState.getBoolean(EXTRA_HEADERS);
		if (headersShown) {
			showAllHeaders();
		}
	}

	/*---------------*/
	/* INNER CLASSES */
	/*---------------*/

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
		 * <p>Delegates the click event to the listener and checks if selection MODE if
		 * SINGLE or MULTI is enabled in order to activate the ItemView.</p>
		 * For Expandable Views it will toggle the Expansion if configured so.
		 *
		 * @param position the adapter position of the item clicked
		 * @return true if the click should activate the ItemView, false for no change.
		 */
		boolean onItemClick(int position);
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemLongClickListener {
		/**
		 * Called when long tap occurs.
		 * <p>This method always calls
		 * {@link FlexibleViewHolder#toggleActivation}
		 * after listener event is consumed in order to activate the ItemView.</p>
		 * For Expandable Views it will collapse the View if configured so.
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

	/**
	 * Observer Class responsible to recalculate Selection and Expanded positions.
	 */
	private class ExpandableAdapterDataObserver extends RecyclerView.AdapterDataObserver {

		private void adjustPositions(int positionStart, int itemCount) {
			if (!filtering) {//Filtering has multiple insert and removal, we skip this process
				if (adjustSelected)//Don't, if remove range / restore children
					adjustSelected(positionStart, itemCount);
				adjustSelected = true;
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

	private class RestoreInfo {
		// Positions
		int refPosition = -1, relativePosition = -1;
		// The item to which the deleted item is referring to
		T refItem = null, filterRefItem = null;
		// The deleted item
		T item = null;
		//Any header to which this item was attached
		ISectionable header = null;
		// Payload for the refItem
		Object payload = false;

		public RestoreInfo(T refItem, T item, ISectionable header, Object payload) {
			this(refItem, item, -1, header, payload);
		}

		public RestoreInfo(T refItem, T item, int relativePosition, ISectionable header, Object payload) {
			this.refItem = refItem;//This can be an Expandable or an Header
			this.item = item;
			this.relativePosition = relativePosition;
			this.header = header;
			this.payload = payload;
		}

		/**
		 * @return the reference position which this deleted item is referring to.
		 * It is the parent position if this is a sub item
		 */
		public int getRefPosition() {
			if (refPosition < 0) {
				refPosition = getGlobalPositionOf(filterRefItem != null ? filterRefItem : refItem);
			}
			return refPosition;
		}

		/**
		 * @return the position where the deleted item should be restored
		 */
		public int getRestorePosition() {
			//noinspection Range
			T item = getItem(getRefPosition());
			if (isExpanded(item)) {
				refPosition += getExpandableList((IExpandable) item).size();
			}
			return refPosition + 1;
		}

		public void restoreHeader() {
			if (header != null) {
				header.setAttachedItem(item);
				if (!header.isHidden())
					notifyItemChanged(getGlobalPositionOf((T) header), payload);
			}
		}

		public void clearFilterRef() {
			filterRefItem = null;
			refPosition = -1;
		}

		@Override
		public String toString() {
			return "RestoreInfo[item=" + item +
					", refItem=" + refItem +
					", filterRefItem=" + filterRefItem + "]";
		}
	}

}