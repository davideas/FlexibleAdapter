package eu.davidea.samples.flexibleadapter.models;

import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Item dedicated to display the status of the Layout currently displayed (located always at
 * position 0 in the Adapter).
 *
 * <p>If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).</p>
 */
public class LayoutItem extends AbstractModelItem<LayoutItem.ExampleViewHolder> {

	private static final long serialVersionUID = -5041296095060813327L;

	public LayoutItem(String id) {
		super(id);
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_layout_item;
	}

	@Override
	public ExampleViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ExampleViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, ExampleViewHolder holder, int position, List payloads) {
		holder.mTitle.setSelected(true);//For marquee
		holder.mTitle.setText(getTitle());
		holder.mSubtitle.setText(getSubtitle());
		adapter.animateView(holder.itemView, position, false);

		//Support for StaggeredGridLayoutManager
		if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
			((StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams()).setFullSpan(true);
			Log.d("LayoutItem", "LayoutItem configured fullSpan for StaggeredGridLayout");
		}
	}

	/**
	 * Used for UserLearnsSelection.
	 */
	static class ExampleViewHolder extends FlexibleViewHolder {

		public TextView mTitle;
		public TextView mSubtitle;

		public ExampleViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter, true);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
		}
	}

	@Override
	public String toString() {
		return "LayoutItem[" + super.toString() + "]";
	}
}