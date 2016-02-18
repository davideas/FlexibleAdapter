package eu.davidea.examples.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class SubItem extends AbstractExampleItem<SubItem.ChildViewHolder>
		implements ISectionable<SubItem.ChildViewHolder, HeaderItem>, IFilterable {

	private static final long serialVersionUID = 2519281529221244210L;

	/**
	 * The header of this item
	 */
	HeaderItem header;

	public SubItem(String id) {
		super(id);
	}

	@Override
	public HeaderItem getHeader() {
		return header;
	}

	@Override
	public IFlexible setHeader(HeaderItem header) {
		this.header = header;
		return this;
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
			Utils.setHighlightText(holder.itemView.getContext(), holder.mTitle,
					getTitle(), adapter.getSearchText(), R.color.colorAccent_light);
		} else {
			holder.mTitle.setText(getTitle());
		}
	}

	@Override
	public boolean filter(String constraint) {
		return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint);
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	public static final class ChildViewHolder extends FlexibleViewHolder {

		public ImageView mHandleView;
		public TextView mTitle;

		public ChildViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mHandleView = (ImageView) view.findViewById(R.id.row_handle);
			setDragHandleView(mHandleView);
		}
	}

	@Override
	public String toString() {
		return "SubItem[" + super.toString() + "]";
	}

}