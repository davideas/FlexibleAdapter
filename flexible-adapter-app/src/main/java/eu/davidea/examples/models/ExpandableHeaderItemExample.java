package eu.davidea.examples.models;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * This is an another example (not used in the demo) of how a Section with header can also be
 * expanded/collapsed.<br/>
 * The new object AbstractExpandableHeaderItem is an AbstractExpandableItem that implements IHeader.
 * It's important to note that, the ViewHolder must be specified in all &lt;diamond&gt; signature.
 */
public class ExpandableHeaderItemExample
		extends AbstractExpandableHeaderItem<ExpandableHeaderItemExample.ExpandableHeaderViewHolder, SubItem>
		implements Serializable {

	private static final long serialVersionUID = -1882711111814491060L;

	private String id;
	private String title;
	private String subtitle;

	public ExpandableHeaderItemExample(String id) {
		super();//Call super to auto-configure the section status as shown, expanded, not selectable
		this.id = id;
	}

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof ExpandableHeaderItemExample) {
			ExpandableHeaderItemExample inItem = (ExpandableHeaderItemExample) inObject;
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

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_header_row;
	}

	@Override
	public ExpandableHeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ExpandableHeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, ExpandableHeaderViewHolder holder, int position, List payloads) {
		if (payloads.size() > 0) {
			Log.i(this.getClass().getSimpleName(), "ExpandableHeaderItem Payload " + payloads);
		} else {
			holder.mTitle.setText(getTitle());
		}
		setSubtitle(adapter.getCurrentChildren(this).size() + " subItems");
		holder.mSubtitle.setText(getSubtitle());
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	public static class ExpandableHeaderViewHolder extends ExpandableViewHolder {

		public TextView mTitle;
		public TextView mSubtitle;

		public ExpandableHeaderViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
		}

		@Override
		protected boolean isViewExpandableOnClick() {
			return true;
		}
	}

	@Override
	public String toString() {
		return "ExpandableHeaderItem[" + super.toString() + "//SubItems" + mSubItems + "]";
	}

}