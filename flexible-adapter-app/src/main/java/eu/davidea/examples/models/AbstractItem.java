package eu.davidea.examples.models;

import android.support.v7.widget.RecyclerView;

import java.io.Serializable;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Customized item for example purpose.
 */
public abstract class AbstractItem<VH extends RecyclerView.ViewHolder>
		extends AbstractFlexibleItem<VH>
		implements Serializable {

	private static final long serialVersionUID = -6882745111884490060L;

	private String id;
	private String title;
	private String subtitle;

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof AbstractItem) {
			AbstractItem inItem = (AbstractItem) inObject;
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
		return "SimpleItem[" +
				"id=" + id +
				", title=" + title +
				super.toString() + ']';
	}

}