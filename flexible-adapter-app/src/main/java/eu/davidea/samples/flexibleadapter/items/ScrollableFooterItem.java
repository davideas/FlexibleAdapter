package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.utils.Utils;
import eu.davidea.viewholders.AnimatedViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This item is a Scrollable Footer.
 */
public class ScrollableFooterItem extends AbstractItem<ScrollableFooterItem.FooterViewHolder> {

	public ScrollableFooterItem(String id) {
		super(id);
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_scrollable_footer_item;
	}

	@Override
	public FooterViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new FooterViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, FooterViewHolder holder, int position, List payloads) {
		holder.mTitle.setText(Utils.fromHtmlCompat(getTitle()));
		holder.mSubtitle.setText(Utils.fromHtmlCompat(getSubtitle()));
	}

	class FooterViewHolder extends FlexibleViewHolder implements AnimatedViewHolder {

		TextView mTitle;
		TextView mSubtitle;
		ImageView mDismissIcon;

		FooterViewHolder(View view, FlexibleAdapter adapter) {
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
					mAdapter.removeScrollableFooter(ScrollableFooterItem.this);
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

		@Override
		public boolean preAnimateAddImpl() {
			ViewCompat.setTranslationY(itemView, itemView.getHeight());
			ViewCompat.setAlpha(itemView, 0);
			return true;
		}

		@Override
		public boolean preAnimateRemoveImpl() {
			return false;
		}

		@Override
		public boolean animateAddImpl(ViewPropertyAnimatorListener listener, long addDuration, int index) {
			ViewCompat.animate(itemView)
					.translationY(0)
					.alpha(1)
					.setDuration(addDuration)
					.setInterpolator(new DecelerateInterpolator())
					.setListener(listener)
					.start();
			return true;
		}

		@Override
		public boolean animateRemoveImpl(ViewPropertyAnimatorListener listener, long removeDuration, int index) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "ScrollableFooterItem[" + super.toString() + "]";
	}

}