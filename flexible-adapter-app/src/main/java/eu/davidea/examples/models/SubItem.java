package eu.davidea.examples.models;

import java.io.Serializable;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class SubItem extends AbstractFlexibleItem implements Serializable {

	private static final long serialVersionUID = -6882745111884490060L;

	private String id;
	private String title;

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof SubItem) {
			SubItem inItem = (SubItem) inObject;
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

	@Override
	public String toString() {
		return "Item[" +
//				"id=" + id +
				"title=" + title +
				super.toString() + ']';
	}

}