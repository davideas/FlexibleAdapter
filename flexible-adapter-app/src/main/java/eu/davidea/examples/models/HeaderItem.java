package eu.davidea.examples.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

public class HeaderItem extends AbstractExampleItem<HeaderItem.HeaderViewHolder> {

	public HeaderItem(String id) {
		super(id);
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
		holder.mTitle.setText(getTitle());
	}

	public static class HeaderViewHolder extends FlexibleViewHolder {

		public TextView mTitle;

		public HeaderViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
		}
	}

}