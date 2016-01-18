package eu.davidea.flexibleadapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import java.util.List;

import eu.davidea.flexibleadapter.item.FlexibleItem;
import eu.davidea.viewholder.ExpandableViewHolder;
import eu.davidea.viewholder.FlexibleViewHolder;

/**
 * This adapter provides a set of standard methods to expand and collapse an item.
 *
 * @author Davide Steduto
 * @since 16/01/2016
 */
public abstract class FlexibleExpandableAdapter<EVH extends ExpandableViewHolder, T extends FlexibleItem<T>>
		extends FlexibleAnimatorAdapter<FlexibleViewHolder, T> {

	private static final String TAG = FlexibleExpandableAdapter.class.getSimpleName();
	private static int EXPANDABLE_VIEW_TYPE = -1;

	private SparseIntArray mExpandedItems;
	boolean parentSelected = false;
	boolean childSelected = false;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	public FlexibleExpandableAdapter(@NonNull List<T> items) {
		this(items, null);
	}

	public FlexibleExpandableAdapter(@NonNull List<T> items, Object listener) {
		super(items, listener);
		mExpandedItems = new SparseIntArray();
		expandInitialItems(items);
	}

	protected void expandInitialItems(List<T> items) {
		//Set initially expanded
		Log.d(TAG, "Items=" + items.size());
		for (int i = 0; i < items.size(); i++) {
			T item = items.get(i);
			//FIXME: Foreseen bug on Rotation: coordinate expansion with onRestoreInstanceState
			if (item.isExpanded() && hasSubItems(item)) {
				if (DEBUG) Log.d(TAG, "Initially expand item on position " + i);
				mExpandedItems.put(i, item.getSubItems().size());
				mItems.addAll(i + 1, item.getSubItems());
				i += item.getSubItems().size();
			}
		}
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	//TODO: Add and Remove subItems for a specific parent
	//TODO: Find a way to notify parent position if child is added or removed

	//FIXME: Rewrite Filter logic: Expand Parent if subItem is filtered by searchText?
	//FIXME: Rewrite Restore logic: Restore Deleted child items at right position. Check if Parent was collapsed in the meantime

	//FIXME: Find a way to not animate items with ItemAnimator
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
	 * Create ViewHolder that are not expandable or that are children of the Expandable ViewHolder.
	 *
	 * @param parent   The ViewGroup into which the new View will be added after it is bound to
	 *                 an adapter position.
	 * @param viewType The view type of the new View.
	 * @return A new ViewHolder that holds a View that can are child of the expanded views.
	 */
	public abstract FlexibleViewHolder onCreateFlexibleViewHolder(ViewGroup parent, int viewType);

	@Override
	public final FlexibleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (DEBUG) Log.d(TAG, "onCreateViewHolder for viewType " + viewType);
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

	private boolean hasSubItems(T item) {
		return item.getSubItems() != null && item.getSubItems().size() > 0;
	}

	public void expand(int position) {
		T item = getItem(position);
		if (item.isExpandable() && !item.isExpanded() && hasSubItems(item) && !parentSelected) {

			int subItemsCount = item.getSubItems().size();
			mExpandedItems.put(position, subItemsCount);
			mItems.addAll(position + 1, item.getSubItems());
			item.setExpanded(true);

			//Automatically scroll the current expandable item to the first visible position
			//FIXME: Bug with SmoothScrollLinearLayoutManager: Parent Item content seems duplicated to previous item after the scroll
			mRecyclerView.smoothScrollToPosition(position);

			notifyItemRangeInserted(position + 1, subItemsCount);
			if (DEBUG)
				Log.d(TAG, "Expanded " + subItemsCount + " subItems on position=" + position);
		}
	}

	public void collapse(int position) {
		T item = getItem(position);
		if (item.isExpandable() && item.isExpanded() && !hasSubItemsSelected(item)) {

			int subItemsCount = item.getSubItems().size();
			int indexOfKey = mExpandedItems.indexOfKey(position);
			if (indexOfKey >= 0) {
				mExpandedItems.removeAt(indexOfKey);
			}
			mItems.removeAll(item.getSubItems());
			item.setExpanded(false);

//			final RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
//			mRecyclerView.setItemAnimator(new DefaultItemAnimator());
			notifyItemRangeRemoved(position + 1, subItemsCount);
//			Handler animatorHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
//				public boolean handleMessage(Message message) {
//					mRecyclerView.setItemAnimator(itemAnimator);
//					return true;
//				}
//			});
//			animatorHandler.sendMessageDelayed(Message.obtain(mHandler), 100L);

			if (DEBUG)
				Log.d(TAG, "Collapsed " + subItemsCount + " subItems on position=" + position);
		}
	}

	/*------------------------------*/
	/* SELECTION METHODS OVERRIDDEN */
	/*------------------------------*/

	@Override
	public void toggleSelection(int position) {
		this.toggleSelection(position, false);
	}

	@Override
	public void toggleSelection(int position, boolean invalidate) {
		T item = getItem(position);
		//Allow selection only for selectable items
		if (item.isSelectable()) {
			if (item.isExpandable() && !childSelected) {
				//Allow selection of Parent if no Child has been previously selected
				parentSelected = true;
				super.toggleSelection(position, invalidate);
			} else if (!item.isExpandable() && !parentSelected) {
				//Allow selection of Child if no Parent has been previously selected
				childSelected = true;
				super.toggleSelection(position, invalidate);
			}
		}
	}

	@Override
	public void clearSelection() {
		parentSelected = childSelected = false;
		super.clearSelection();
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
		if (DEBUG) Log.d(TAG, "SaveInstanceState for expanded items");
		outState.putIntArray(TAG, getExpandedItems());
	}

	/**
	 * Restore the previous state of the expanded items.
	 *
	 * @param savedInstanceState Previous state
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (DEBUG) Log.d(TAG, "RestoreInstanceState for expanded items");
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

}