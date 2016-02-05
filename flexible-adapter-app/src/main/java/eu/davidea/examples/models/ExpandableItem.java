package eu.davidea.examples.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.items.IExpandable;

public class ExpandableItem extends SimpleItem implements Serializable, IExpandable<SubItem> {

	private static final long serialVersionUID = -6882745111884490060L;

	/* Flags for FlexibleAdapter */
	private boolean mExpanded = false;

	/* subItems list */
	private List<SubItem> mSubItems;

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof ExpandableItem) {
			ExpandableItem inItem = (ExpandableItem) inObject;
			return this.getId().equals(inItem.getId());
		}
		return false;
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

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_expandable_row;
	}

	public String updateSubTitle() {
		setSubtitle(mSubItems.size() + " subItems");
		return getSubtitle();
	}

}