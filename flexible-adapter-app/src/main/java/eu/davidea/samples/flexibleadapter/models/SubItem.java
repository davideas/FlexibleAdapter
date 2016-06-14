package eu.davidea.samples.flexibleadapter.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class SubItem extends AbstractModelItem<SubItem.ChildViewHolder>
		implements ISectionable<SubItem.ChildViewHolder, IHeader>, IFilterable {

	private static final long serialVersionUID = 2519281529221244210L;

	/**
	 * The header of this item
	 */
	IHeader header;

	public SubItem(String id) {
		super(id);
		setDraggable(true);
	}

	@Override
	public IHeader getHeader() {
		return header;
	}

	@Override
	public void setHeader(IHeader header) {
		this.header = header;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_sub_item;
	}

	@Override
	public ChildViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ChildViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void bindViewHolder(FlexibleAdapter adapter, ChildViewHolder holder, int position, List payloads) {
		//In case of searchText matches with Title or with an SimpleItem's field
		// this will be highlighted
		if (adapter.hasSearchText()) {
			Context context = holder.itemView.getContext();
			Utils.highlightText(context, holder.mTitle, getTitle(), adapter.getSearchText(),
					context.getResources().getColor(R.color.colorAccent_light));
		} else {
			holder.mTitle.setText(getTitle());
		}

		if (getHeader() != null) {
			setSubtitle("Header " + getHeader().toString());
		}

		//This "if-else" is just an example of what you can do with item animation
		if (adapter.isSelected(position)) {
			adapter.animateView(holder.itemView, position, true);
		} else {
			adapter.animateView(holder.itemView, position, false);
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
	static final class ChildViewHolder extends FlexibleViewHolder {

		public ImageView mHandleView;
		public TextView mTitle;

		public ChildViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mHandleView = (ImageView) view.findViewById(R.id.row_handle);
			if (adapter.isHandleDragEnabled()) {
				this.mHandleView.setVisibility(View.VISIBLE);
				setDragHandleView(mHandleView);
			} else {
				this.mHandleView.setVisibility(View.GONE);
			}
		}

		@Override
		public float getActivationElevation() {
			return eu.davidea.utils.Utils.dpToPx(itemView.getContext(), 4f);
		}
	}

	@Override
	public String toString() {
		return "SubItem[" + super.toString() + "]";
	}

}