package eu.davidea.examples.models;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractSectionableItem} to benefit of the already
 * implemented methods relative to the header.
 */
public class HeaderItem extends AbstractExampleItem<HeaderItem.HeaderViewHolder, AbstractExampleItem> {

	private static final long serialVersionUID = -7408637077727563374L;

	public HeaderItem(String id) {
		super(id);
	}

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof HeaderItem) {
			HeaderItem inItem = (HeaderItem) inObject;
			return this.getId().equals(inItem.getId());
		}
		return false;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_header_row;
	}

	@Override
	public HeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new HeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
		if (payloads.size() > 0) {
			Log.i(this.getClass().getSimpleName(), "Payload " + payloads);
		} else {
			holder.mTitle.setText(getTitle());
		}
		AbstractExampleItem item = (AbstractExampleItem) adapter.getSectionableOf(this);
		String subTitle = "Attached to " + (item != null ? item.getTitle() : "none");
		holder.mSubtitle.setText(subTitle);
		//holder.mSubtitle.setText(getSubtitle());
	}

	public static class HeaderViewHolder extends FlexibleViewHolder {

		public TextView mTitle;
		public TextView mSubtitle;

		public HeaderViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
		}
	}

}