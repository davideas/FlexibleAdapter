package eu.davidea.samples.flexibleadapter.models;

import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.models.OverallItem.LabelViewHolder;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Displays Overall example items as label in CardView.
 *
 * @author Davide Steduto
 * @since 12/04/2016
 */
public class OverallItem extends AbstractFlexibleItem<LabelViewHolder> {

	private int id;
	private String title;
	private String description;
	private Drawable icon;

	public OverallItem(int id, String title) {
		this.id = id;
		this.title = title;
		setSelectable(false);
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

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_label_item;
	}

	@Override
	public LabelViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new LabelViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, LabelViewHolder holder, int position, List payloads) {
		if (title != null) {
			holder.mTitle.setText(title);
			//Appear disabled if item is disabled
			holder.mTitle.setEnabled(isEnabled());
		}
		if (description != null) {
			holder.mSubtitle.setText(Html.fromHtml(description));
			holder.mSubtitle.setEnabled(isEnabled());
		}
		if (icon != null) {
			holder.mIcon.setImageDrawable(icon);
		}

		adapter.animateView(holder.itemView, position, adapter.isSelected(position));
	}

	static class LabelViewHolder extends FlexibleViewHolder {

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