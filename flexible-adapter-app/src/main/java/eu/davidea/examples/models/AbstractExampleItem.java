package eu.davidea.examples.models;

import android.support.v7.widget.RecyclerView;

import java.io.Serializable;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public abstract class AbstractExampleItem<VH extends RecyclerView.ViewHolder>
		extends AbstractFlexibleItem<VH>
		implements Serializable {

	private static final long serialVersionUID = -6882745111884490060L;

	private String id;
	private String title;
	private String subtitle;

	public AbstractExampleItem(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof AbstractExampleItem) {
			AbstractExampleItem inItem = (AbstractExampleItem) inObject;
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
		return "[" +
				"id=" + id +
				", title=" + title + "]";
	}

}