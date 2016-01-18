package eu.davidea.flexibleadapter.item;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic implementation of {@link FlexibleItem} interface with most useful methods to manage
 * expansion, selection and sub items.
 *
 * @author Davide Steduto
 * @since 17/01/2016
 */
public abstract class AbstractFlexibleItem<T> implements FlexibleItem<T> {

	/* Flags for FlexibleExpandableAdapter */
	boolean mExpanded = false;
	boolean mExpandable = false;
	boolean mSelectable = true;
	/** subItems list */
	List<T> mSubItems;
	/** Reference to the Parent Item */
	T mParent;

	/**
	 * You should implement this method to compare items Identifiers.
	 *
	 * @param o Instance to compare
	 * @return true if items are equals, false otherwise.
	 */
	@Override
	public abstract boolean equals(Object o);

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

	public T withExpandable(boolean expandable) {
		this.mExpandable = expandable;
		return (T) this;
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

	/*--------------------*/
	/* SELECTABLE METHODS */
	/*--------------------*/

	@Override
	public boolean isSelectable() {
		return mSelectable;
	}

	@Override
	public void setSelectable(boolean selectable) {
		this.mSelectable = selectable;
	}

	public T withSelectable(boolean selectable) {
		this.mSelectable = selectable;
		return (T) this;
	}

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

	public T getParentItem() {
		return mParent;
	}

	public void setParentItem(T item) {
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
		mSubItems = items;
	}

	public T withSubItems(List<T> items) {
		mSubItems = items;
		return (T) this;
	}

	public int getSubItemsCount() {
		return mSubItems != null ? mSubItems.size() : 0;
	}

	public T getSubItem(int position) {
		if (mSubItems != null) {
			int index = mSubItems.indexOf(position);
			if (index != -1)
				return mSubItems.get(position);
		}
		return null;
	}

	public void addSubItem(T item) {
		if (mSubItems == null)
			mSubItems = new ArrayList<T>();
		mSubItems.add(item);
	}

	public void addSubItem(int position, T item) {
		if (mSubItems != null && position < mSubItems.size())
			mSubItems.add(position, item);
		else
			addSubItem(item);
	}

	public boolean contains(T item) {
		return mSubItems != null && mSubItems.contains(item);
	}

	public boolean removeSubItem(T item) {
		return mSubItems != null && mSubItems.remove(item);
	}

	public boolean removeSubItemAt(int position) {
		if (mSubItems != null && position > 0 && position < mSubItems.size()) {
			mSubItems.remove(position);
			return true;
		}
		return false;
	}

}