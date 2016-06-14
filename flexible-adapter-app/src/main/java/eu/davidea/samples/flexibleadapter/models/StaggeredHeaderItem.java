package eu.davidea.samples.flexibleadapter.models;

import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

public class StaggeredHeaderItem extends AbstractHeaderItem<StaggeredHeaderItem.HeaderViewHolder> {

	private int order;//Custom order for sorting purpose
	private String title;

	public StaggeredHeaderItem(int order, String title) {
		this.order = order;
		this.title = title;
		setEnabled(false);
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_staggered_header_item;
	}

	@Override
	public HeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new HeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
		if (payloads.size() > 0) {
			Log.i(this.getClass().getSimpleName(), "StaggeredHeaderItem Payload " + payloads);
		} else {
			String title = this.title + " (" + adapter.getSectionItems(this).size() + ")";
			holder.title.setText(title);
		}

		adapter.animateView(holder.itemView, position, adapter.isSelected(position));
	}

	static class HeaderViewHolder extends FlexibleViewHolder {

		@BindView(R.id.title)
		TextView title;
		@BindView(R.id.layout_elevation)
		View elevationView;

		public HeaderViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter, true);//True for sticky
			ButterKnife.bind(this, view);

			//Support for StaggeredGridLayoutManager
			if (itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
				((StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams()).setFullSpan(true);
			}
		}
	}

	@Override
	public String toString() {
		return "StaggeredHeaderItem[order=" + order + ", title=" + title + "]";
	}

}