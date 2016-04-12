package eu.davidea.examples.flexibleadapter.models;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Displays Overall example items as label in CardView
 *
 * @author Davide Steduto
 * @since 12/04/2016
 */
public class OverallItem extends AbstractFlexibleItem {

	private int id;
	private int titleResId;
	private int descriptionResId;
	private int iconResId;

	public OverallItem(int id, @StringRes int titleResId) {
		this.id = id;
		this.titleResId = titleResId;
	}

	public OverallItem withDescription(@StringRes int descriptionResId) {
		this.descriptionResId = descriptionResId;
		return this;
	}

	public OverallItem withIcon(@DrawableRes int iconResId) {
		this.iconResId = iconResId;
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

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_label;
	}

	@Override
	public RecyclerView.ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		//TODO: createViewHolder
		return super.createViewHolder(adapter, inflater, parent);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, RecyclerView.ViewHolder holder, int position, List payloads) {
		//TODO: bindViewHolder
		super.bindViewHolder(adapter, holder, position, payloads);
	}

	//TODO: ViewHolder

}