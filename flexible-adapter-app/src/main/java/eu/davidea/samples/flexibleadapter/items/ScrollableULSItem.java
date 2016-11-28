package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Item dedicated only for User Learns Selection view (located always at position 0 in the Adapter).
 * <p>If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).</p>
 */
public class ScrollableULSItem extends AbstractItem<ScrollableULSItem.ULSViewHolder> {

	public ScrollableULSItem(String id) {
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
		return R.layout.recycler_scrollable_uls_item;
	}

	@Override
	public ULSViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ULSViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, ULSViewHolder holder, int position, List payloads) {
		holder.mImageView.setImageResource(R.drawable.ic_account_circle_white_24dp);
		holder.itemView.setActivated(true);
		holder.mTitle.setSelected(true);//For marquee
		holder.mTitle.setText(Html.fromHtml(getTitle()));
		holder.mSubtitle.setText(Html.fromHtml(getSubtitle()));
	}

	/**
	 * Used for UserLearnsSelection.
	 */
	class ULSViewHolder extends FlexibleViewHolder {

		ImageView mImageView;
		TextView mTitle;
		TextView mSubtitle;
		ImageView mDismissIcon;

		ULSViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			mImageView = (ImageView) view.findViewById(R.id.image);
			mDismissIcon = (ImageView) view.findViewById(R.id.dismiss_icon);
			mDismissIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DatabaseConfiguration.userLearnedSelection = true;
					//Don't need anymore to set permanent for Scrollable Headers and Footers
					//mAdapter.setPermanentDelete(true);
					//noinspection unchecked
					mAdapter.removeScrollableHeader(ScrollableULSItem.this);
					//mAdapter.setPermanentDelete(false);
				}
			});

			//Support for StaggeredGridLayoutManager
			if (itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
				((StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams()).setFullSpan(true);
			}
		}

		@Override
		public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
			AnimatorHelper.slideInFromTopAnimator(animators, itemView, mAdapter.getRecyclerView());
		}
	}

	@Override
	public String toString() {
		return "ULSItem[" + super.toString() + "]";
	}

}