package eu.davidea.examples.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import eu.davidea.examples.flexibleadapter.ExampleAdapter;
import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

public class SubItem extends AbstractItem<SubItem.ChildViewHolder> implements Serializable {

	private static final long serialVersionUID = 2519281529221244210L;

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
		return "SimpleItem[" +
				"id=" + id +
				", title=" + title +
				super.toString() + ']';
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_child_row;
	}

	@Override
	public ChildViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ChildViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, ChildViewHolder holder, int position, List payloads) {
		//This "if-else" is just an example of what you can do with item animation
		if (adapter.isSelected(position)) {
			adapter.animateView(holder.itemView, position, true);
		} else {
			adapter.animateView(holder.itemView, position, false);
		}

		//In case of searchText matches with Title or with an SimpleItem's field
		// this will be highlighted
		if (adapter.hasSearchText()) {
			ExampleAdapter.setHighlightText(holder.itemView.getContext(),
					holder.mTitle, title, adapter.getSearchText());
		} else {
			holder.mTitle.setText(title);
		}
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	public static final class ChildViewHolder extends FlexibleViewHolder {
		ImageView mHandleView;
		TextView mTitle;

		public ChildViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mHandleView = (ImageView) view.findViewById(R.id.row_handle);
			setDragHandleView(mHandleView);
		}
	}

}