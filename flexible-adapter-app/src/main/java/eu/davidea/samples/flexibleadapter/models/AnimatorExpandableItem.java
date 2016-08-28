package eu.davidea.samples.flexibleadapter.models;

import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.models.AnimatorExpandableItem.AnimatorExpandableViewHolder;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * This is an experiment to evaluate how a Section with header can also be expanded/collapsed.
 * <p>Here, it still benefits of the common fields declared in AbstractModelItem.</p>
 * It's important to note that, the ViewHolder must be specified in all &lt;diamond&gt; signature.
 */
public class AnimatorExpandableItem
		extends AbstractExpandableHeaderItem<AnimatorExpandableViewHolder, AnimatorSubItem> {

	private String id;
	private String title;
	private String subtitle;

	public AnimatorExpandableItem(String id) {
		super();
		this.id = id;
		setSwipeable(true);
	}

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof AnimatorExpandableItem) {
			AnimatorExpandableItem inItem = (AnimatorExpandableItem) inObject;
			return this.id.equals(inItem.id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
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
		return R.layout.recycler_animator_expandable_item;
	}

	@Override
	public AnimatorExpandableViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new AnimatorExpandableViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, AnimatorExpandableViewHolder holder, int position, List payloads) {
		if (payloads.size() > 0) {
			Log.i(this.getClass().getSimpleName(), "ExpandableHeaderItem Payload " + payloads);
		} else {
			holder.mTitle.setText(getTitle());
		}
		setSubtitle(String.valueOf(adapter.getCurrentChildren(this).size()) +
				" subItems (" + (isExpanded() ? "expanded" : "collapsed") + ")");
		holder.mSubtitle.setText(getSubtitle());
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	static class AnimatorExpandableViewHolder extends ExpandableViewHolder {

		public TextView mTitle;
		public TextView mSubtitle;

		public AnimatorExpandableViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter, true);//True for sticky
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);

			//Support for StaggeredGridLayoutManager
			if (itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
				((StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams()).setFullSpan(true);
			}
		}

		@Override
		protected void expandView(int position) {
			super.expandView(position);
			//Let's notify the item has been expanded
			if (mAdapter.isExpanded(position)) mAdapter.notifyItemChanged(position, true);
		}

		@Override
		protected void collapseView(int position) {
			super.collapseView(position);
			//Let's notify the item has been collapsed
			if (!mAdapter.isExpanded(position)) mAdapter.notifyItemChanged(position, true);
		}

	}

	@Override
	public String toString() {
		return "AnimatorExpandableItem[id=" + id +
				", title=" + title +
				", SubItems" + mSubItems + "]";
	}

}