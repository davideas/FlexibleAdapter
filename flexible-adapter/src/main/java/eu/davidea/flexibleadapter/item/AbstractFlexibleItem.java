package eu.davidea.flexibleadapter.item;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Davide Steduto
 * @since 17/01/2016
 */
public abstract class AbstractFlexibleItem<T> implements FlexibleItem<T> {

	boolean mExpanded = false;
	boolean mExpandable = false;
	boolean mSelectable = true;
	List<T> mSubItems;

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

	@Override
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

	@Override
	public void setInitiallyExpanded(boolean expanded) {
		this.mExpanded = expanded;
	}

	@Override
	public T withInitiallyExpanded(boolean expanded) {
		this.mExpanded = expanded;
		return (T) this;
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

	@Override
	public T withSelectable(boolean selectable) {
		this.mSelectable = selectable;
		return (T) this;
	}

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

	@Override
	public List<T> getSubItems() {
		return mSubItems;
	}

	@Override
	public void setSubItems(List<T> items) {
		mSubItems = items;
	}

	@Override
	public T withSubItems(List<T> items) {
		mSubItems = items;
		return (T) this;
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

	public void removeSubItem(T item) {
		if (mSubItems != null)
			mSubItems.remove(item);
	}

	public void removeSubItem(int position) {
		if (mSubItems != null) {
			int index = mSubItems.indexOf(position);
			if (index != -1)
				mSubItems.remove(position);
		}
	}

}