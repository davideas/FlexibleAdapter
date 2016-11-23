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
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This item is a Scrollable Header.
 */
public class ScrollableUseCaseItem extends AbstractItem<ScrollableUseCaseItem.HeaderViewHolder> {

	public ScrollableUseCaseItem(String id) {
		super(id);
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_scrollable_usecase_item;
	}

	@Override
	public HeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new HeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
//		holder.mTitle.setSelected(true);//For marquee
		holder.mTitle.setText(Html.fromHtml(getTitle()));
		holder.mSubtitle.setText(Html.fromHtml(getSubtitle()));
	}

	class HeaderViewHolder extends FlexibleViewHolder {

		TextView mTitle;
		TextView mSubtitle;
		ImageView mDismissIcon;

		public HeaderViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			mDismissIcon = (ImageView) view.findViewById(R.id.dismiss_icon);
			mDismissIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//Don't need anymore to set permanent for Scrollable Headers and Footers
					//mAdapter.setPermanentDelete(true);
					//noinspection unchecked
					mAdapter.removeScrollableHeader(ScrollableUseCaseItem.this);
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
		return "FooterItem[" + super.toString() + "]";
	}

}