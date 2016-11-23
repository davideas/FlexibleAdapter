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
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This item is a Scrollable Footer.
 */
public class ScrollableFooterItem extends AbstractItem<ScrollableFooterItem.FooterViewHolder> {

	private static final long serialVersionUID = -5041296095060813327L;

	public ScrollableFooterItem(String id) {
		super(id);
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_scrollable_footer_item;
	}

	@Override
	public FooterViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new FooterViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter, this);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, FooterViewHolder holder, int position, List payloads) {
//		holder.mTitle.setSelected(true);//For marquee
		holder.mTitle.setText(Html.fromHtml(getTitle()));
		holder.mSubtitle.setText(Html.fromHtml(getSubtitle()));
	}

	class FooterViewHolder extends FlexibleViewHolder {

		public TextView mTitle;
		public TextView mSubtitle;
		public ImageView mDismissIcon;

		public FooterViewHolder(View view, FlexibleAdapter adapter, final IFlexible item) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			mDismissIcon = (ImageView) view.findViewById(R.id.dismiss_icon);
			mDismissIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//Don't need anymore to set permanent for Scrollable Headers and Footers
					//mAdapter.setPermanentDelete(true);
					mAdapter.removeScrollableFooter(item);
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
			AnimatorHelper.slideInFromBottomAnimator(animators, itemView, mAdapter.getRecyclerView());
		}
	}

	@Override
	public String toString() {
		return "FooterItem[" + super.toString() + "]";
	}

}