package eu.davidea.samples.flexibleadapter.models;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Model object representing Overall functionality as CardView.
 * This Model object is bound via METHOD B: {@code OverallAdapter} implements the 3 methods to
 * bind this item.
 *
 * @author Davide Steduto
 * @see eu.davidea.samples.flexibleadapter.OverallAdapter
 * @since 12/04/2016
 */
public class OverallItem extends AbstractFlexibleItem<OverallItem.LabelViewHolder> {

	private int id;
	private String title;
	private String description;
	private Drawable icon;

	public OverallItem(int id, String title) {
		this.id = id;
		this.title = title;
		setSelectable(false);
		//Allow dragging
		setDraggable(true);
	}

	public OverallItem withDescription(String description) {
		this.description = description;
		return this;
	}

	public OverallItem withIcon(Drawable icon) {
		this.icon = icon;
		return this;
	}

	public OverallItem withEnabled(boolean enabled) {
		setEnabled(enabled);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OverallItem that = (OverallItem) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return id;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}


	public static class LabelViewHolder extends FlexibleViewHolder {

		public TextView mTitle;
		public TextView mSubtitle;
		public ImageView mIcon;

		public LabelViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			mIcon = (ImageView) view.findViewById(R.id.label_background);
		}
	}

}