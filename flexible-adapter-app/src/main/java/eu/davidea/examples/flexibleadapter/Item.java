package eu.davidea.examples.flexibleadapter;

import android.view.ViewGroup;

import java.io.Serializable;
import java.util.zip.Inflater;

import eu.davidea.flexibleadapter.item.AbstractExpandableItem;
import eu.davidea.viewholder.ExpandableViewHolder;

public class Item extends AbstractExpandableItem<Item, ExpandableViewHolder> implements Serializable {

	private static final long serialVersionUID = -6882745111884490060L;

	private int id;
	private String title;
	private String subtitle;

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof Item) {
			Item inItem = (Item) inObject;
			return this.id == inItem.id;
		}
		return false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	@Override
	public String toString() {
		return title;
	}

	public String updateSubTitle() {
		setSubtitle(getSubItemsCount() + " subItems");
		return subtitle;
	}

	@Override
	public int getLayoutRes() {
		return 0;
	}

	@Override
	public ExpandableViewHolder getViewHolder(Inflater inflater, ViewGroup parent) {
		return null;
	}

	@Override
	public void bindViewHolder(ExpandableViewHolder holder) {

	}

}