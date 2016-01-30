package eu.davidea.flexibleadapter.items;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Generic implementation of {@link IExpandableItem} interface with most useful methods to manage
 * expansion and sub items.<br/>
 * This abstract class expands also {@link AbstractFlexibleItem}.
 *
 * @author Davide Steduto
 * @since 17/01/2016 Created
 */
public abstract class AbstractExpandableItem<T extends IExpandableItem<T>, VH extends ExpandableViewHolder>
		extends AbstractFlexibleItem<T>
		implements IExpandableItem<T> {

	/** Reference to the Parent Item */
	T mParent;

	/* Flags for FlexibleExpandableAdapter */
	boolean mExpanded = false,
			mExpandable = false;

	/** subItems list */
	List<T> mSubItems;
	SparseArray<T> mRemovedItems = new SparseArray<T>();


	/*---------------------*/
	/* VIEW HOLDER METHODS */
	/*---------------------*/

//	@Override
//	@IdRes
//	public int getItemViewType() {
//		return FlexibleExpandableAdapter.EXPANDABLE_VIEW_TYPE;
//	}
//
//	@Override
//	@LayoutRes
//	public abstract int getLayoutRes();
//
//	@Override
//	public abstract VH getViewHolder(Inflater inflater, ViewGroup parent);
//
//	@Override
//	public abstract void bindViewHolder(VH holder);

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

	@Override
	public boolean isExpandable() {
		return mExpandable;
	}

	@Override
	public void setExpandable(boolean expandable) {
		this.mExpandable = expandable;
	}

	@Override
	public boolean isExpanded() {
		return mExpanded;
	}

	@Override
	public void setExpanded(boolean expanded) {
		this.mExpanded = expanded;
	}

	public void setInitiallyExpanded(boolean expanded) {
		this.mExpanded = expanded;
	}

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

	@Override
	public T getParent() {
		return mParent;
	}

	@Override
	public final void setParent(T item) {
		mParent = item;
	}

	public boolean hasSubItems() {
		return mSubItems!= null && mSubItems.size() > 0;
	}

	@Override
	public List<T> getSubItems() {
		return mSubItems;
	}

	@Override
	public void setSubItems(List<T> items) {
		for (T item : items) {
			item.setParent((T) this);
		}
		mSubItems = new ArrayList<>(items);
	}

	@Override
	public int getSubItemsCount() {
		return mSubItems != null ? mSubItems.size() : 0;
	}

	@Override
	public T getSubItem(int position) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			return mSubItems.get(position);
		}
		return null;
	}

	@Override
	public int getSubItemPosition(T item) {
		return mSubItems.indexOf(item);
	}

	@Override
	public void addSubItem(T item) {
		if (mSubItems == null)
			mSubItems = new ArrayList<T>();
		item.setParent((T) this);
		mSubItems.add(item);
	}

	@Override
	public void addSubItem(int position, T item) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			item.setParent((T) this);
			mSubItems.add(position, item);
		} else
			addSubItem(item);
	}

	@Override
	public boolean contains(T item) {
		return mSubItems != null && mSubItems.contains(item);
	}

	@Override
	public boolean removeSubItem(T item) {
		int position = mSubItems.indexOf(item);
		if (mSubItems != null && position >= 0) {
			mRemovedItems.put(position, item);
			return mSubItems.remove(item);
		}
		return false;
	}

	@Override
	public boolean removeSubItem(int position) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			mRemovedItems.put(position, mSubItems.remove(position));
			return true;
		}
		return false;
	}

	public void restoreDeletedSubItems() {
		for (int i = 0; i < mRemovedItems.size(); i++) {
			int position = mRemovedItems.keyAt(i);
			if (position >= 0)
				addSubItem(position, mRemovedItems.get(position));
		}
	}

	@Override
	public String toString() {
		return ", mExpanded=" + mExpanded +
				", mExpandable=" + mExpandable +
				", mSubItems=" + (mSubItems != null ? mSubItems.size() : "null");
	}

}