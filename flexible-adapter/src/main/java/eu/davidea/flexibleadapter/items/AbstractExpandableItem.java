package eu.davidea.flexibleadapter.items;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic implementation of {@link IExpandableItem} interface with most useful methods to manage
 * expansion and sub items.<br/>
 * This abstract class extends also {@link AbstractFlexibleItem}.
 *
 * @author Davide Steduto
 * @since 17/01/2016 Created
 */
public abstract class AbstractExpandableItem<S extends IExpandableItem>
		extends AbstractFlexibleItem
		implements IExpandableItem<S> {

	/** Reference to the Parent Item */
	//private T mParent;

	/* Flags for FlexibleExpandableAdapter */
	private boolean mExpandable = false, mExpanded = false;

	/** subItems list */
	private List<S> mSubItems;
	//SparseArray<T> mRemovedItems = new SparseArray<T>();


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

	//@Override
//	public final T getParent() {
//		return mParent;
//	}

	//@Override
//	public final void setParent(T item) {
//		mParent = item;
//	}

	public final boolean hasSubItems() {
		return mSubItems!= null && mSubItems.size() > 0;
	}

	@Override
	public final List<S> getSubItems() {
		return mSubItems;
	}

	//@Override
	public void setSubItems(List<S> subItem) {
//		for (T item : items) {
//			item.setParent((T) this);
//		}
		mSubItems = new ArrayList<>(subItem);
	}

	@Override
	public final int getSubItemsCount() {
		return mSubItems != null ? mSubItems.size() : 0;
	}

	//@Override
	public S getSubItem(int position) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			return mSubItems.get(position);
		}
		return null;
	}

	//@Override
	public final int getSubItemPosition(S subItem) {
		return mSubItems != null ? mSubItems.indexOf(subItem) : -1;
	}

	//@Override
	public void addSubItem(S subItem) {
		if (mSubItems == null)
			mSubItems = new ArrayList<S>();
//		item.setParent((T) this);
		mSubItems.add(subItem);
	}

	//@Override
	public void addSubItem(int position, S subItem) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
//			subItem.setParent((T) this);
			mSubItems.add(position, subItem);
		} else
			addSubItem(subItem);
	}

	//@Override
	public boolean contains(S subItem) {
		return mSubItems != null && mSubItems.contains(subItem);
	}

	//@Override
//	public boolean removeSubItem(T item) {
//		int position = mSubItems.indexOf(item);
//		if (mSubItems != null && position >= 0) {
//			mRemovedItems.put(position, item);
//			return mSubItems.remove(item);
//		}
//		return false;
//	}

	//@Override
//	public boolean removeSubItem(int position) {
//		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
//			mRemovedItems.put(position, mSubItems.remove(position));
//			return true;
//		}
//		return false;
//	}

//	public void restoreDeletedSubItems() {
//		for (int i = 0; i < mRemovedItems.size(); i++) {
//			int position = mRemovedItems.keyAt(i);
//			if (position >= 0)
//				addSubItem(position, mRemovedItems.get(position));
//		}
//	}

	@Override
	public String toString() {
		return ", mExpanded=" + mExpanded +
				", mExpandable=" + mExpandable +
				", mSubItems=" + (mSubItems != null ? mSubItems.size() : "null");
	}

}