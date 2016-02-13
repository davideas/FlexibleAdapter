package eu.davidea.examples.models;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexibleItem;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractSectionableItem} to benefit of the already
 * implemented methods relative to the header.
 */
public class HeaderItem extends AbstractExampleItem<HeaderItem.HeaderViewHolder>
		implements ISectionable<HeaderItem.HeaderViewHolder, IFlexibleItem> {

	private static final long serialVersionUID = -7408637077727563374L;

	/**
	 * The item to which this header is attached
	 */
	IFlexibleItem attachedItem;
	/**
	 * If this header should be sticky on the top until next header comes
	 */
	boolean sticky;

	public HeaderItem(String id, IFlexibleItem attachedItem, boolean shown, boolean sticky) {
		super(id);
		this.attachedItem = attachedItem;
		this.sticky = sticky;
		setHidden(!shown);
		setSelectable(false);
	}

	@Override
	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	@Override
	public boolean isSticky() {
		return sticky;
	}

	@Override
	public IFlexibleItem getAttachedItem() {
		return attachedItem;
	}

	@Override
	public void setAttachedItem(IFlexibleItem attachedItem) {
		this.attachedItem = attachedItem;
	}

	@Override
	public String getSubtitle() {
		return "Attached to " + (attachedItem == null ? "none" :
				((AbstractExampleItem) attachedItem).getTitle());
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
			holder.mSubtitle.setText(getSubtitle());
		} else {
			holder.mTitle.setText(getTitle());
			holder.mSubtitle.setText(getSubtitle());
		}
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

	@Override
	public String toString() {
		return super.toString() +
				", sticky=" + sticky +
				", attachedTo=" + attachedItem + "]";
	}
}