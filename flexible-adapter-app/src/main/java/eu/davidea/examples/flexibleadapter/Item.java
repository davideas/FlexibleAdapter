package eu.davidea.examples.flexibleadapter;

import java.io.Serializable;

import eu.davidea.flexibleadapter.items.AbstractExpandableItem;

public class Item extends AbstractExpandableItem<Item> implements Serializable {

	private static final long serialVersionUID = -6882745111884490060L;

	private String id;
	private String title;
	private String subtitle;

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof Item) {
			Item inItem = (Item) inObject;
			return this.id.equals(inItem.id);
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
		return "Item[" +
//				"id=" + id +
				"title=" + title +
				super.toString() + ']';
	}

	public String updateSubTitle() {
		setSubtitle(getSubItemsCount() + " subItems");
		return subtitle;
	}

}