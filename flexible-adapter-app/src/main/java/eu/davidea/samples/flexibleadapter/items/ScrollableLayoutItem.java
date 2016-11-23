package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Item dedicated to display which Layout is currently displayed.
 * This item is a Scrollable Header.
 *
 * <p>If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).</p>
 */
public class ScrollableLayoutItem extends AbstractItem<ScrollableLayoutItem.LayoutViewHolder> {

	public ScrollableLayoutItem(String id) {
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
		return R.layout.recycler_scrollable_layout_item;
	}

	@Override
	public LayoutViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new LayoutViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, LayoutViewHolder holder, int position, List payloads) {
		holder.mTitle.setSelected(true);//For marquee
		holder.mTitle.setText(getTitle());
		holder.mSubtitle.setText(getSubtitle());

		//Support for StaggeredGridLayoutManager
		if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
			((StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams()).setFullSpan(true);
			Log.d("LayoutItem", "LayoutItem configured fullSpan for StaggeredGridLayout");
		}
	}

	public static class LayoutViewHolder extends FlexibleViewHolder {

		public TextView mTitle;
		public TextView mSubtitle;

		public LayoutViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter, true);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
		}

		@Override
		public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
			AnimatorHelper.slideInFromTopAnimator(animators, itemView, mAdapter.getRecyclerView());
		}
	}

	@Override
	public String toString() {
		return "LayoutItem[" + super.toString() + "]";
	}
}