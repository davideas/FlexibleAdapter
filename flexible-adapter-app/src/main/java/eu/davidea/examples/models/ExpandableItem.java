package eu.davidea.examples.models;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.items.IExpandable;

/**
 * If you don't have fields in common (my example: SimpleItem) better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractExpandableItem} to benefit of the already
 * implemented methods around subItems list.
 */
public class ExpandableItem extends SimpleItem
		implements IExpandable<SimpleItem.ParentViewHolder, SubItem> {

	private static final long serialVersionUID = -6882745111884490060L;

	/* Flags for FlexibleAdapter */
	private boolean mExpanded = false;

	/* subItems list */
	private List<SubItem> mSubItems;


	public ExpandableItem(String id) {
		super(id);
	}

	public ExpandableItem(String id, HeaderItem header) {
		super(id, header);
	}

	@Override
	public boolean isExpanded() {
		return mExpanded;
	}

	@Override
	public void setExpanded(boolean expanded) {
		mExpanded = expanded;
	}

	@Override
	public List<SubItem> getSubItems() {
		return mSubItems;
	}

	public final boolean hasSubItems() {
		return mSubItems!= null && mSubItems.size() > 0;
	}

	public boolean removeSubItem(SubItem item) {
		return item != null && mSubItems.remove(item);
	}

	public boolean removeSubItem(int position) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			mSubItems.remove(position);
			return true;
		}
		return false;
	}

	public void addSubItem(SubItem subItem) {
		if (mSubItems == null)
			mSubItems = new ArrayList<SubItem>();
		mSubItems.add(subItem);
	}

	public void addSubItem(int position, SubItem subItem) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			mSubItems.add(position, subItem);
		} else
			addSubItem(subItem);
	}

//	@Override
//	public int getLayoutRes() {
//		return R.layout.recycler_expandable_row;
//	}

	@Override
	public String toString() {
		return "ExpandableItem[" + super.toString() + "//SubItems" + mSubItems + "]";
	}

}