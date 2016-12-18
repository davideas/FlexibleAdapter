package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * You should extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class SimpleItem extends AbstractItem<SimpleItem.ParentViewHolder>
		implements ISectionable<SimpleItem.ParentViewHolder, HeaderItem>, IFilterable, Serializable {

	/**
	 * The header of this item
	 */
	HeaderItem header;

	SimpleItem(String id) {
		super(id);
		setDraggable(true);
		setSwipeable(true);
	}

	public SimpleItem(String id, HeaderItem header) {
		this(id);
		this.header = header;
	}

	@Override
	public String getSubtitle() {
		return getId()
				+ (getHeader() != null ? " - " + getHeader().getId() : "")
				+ (getUpdates() > 0 ? " - u" + getUpdates() : "");
	}

	@Override
	public HeaderItem getHeader() {
		return header;
	}

	@Override
	public void setHeader(HeaderItem header) {
		this.header = header;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_expandable_item;
	}

	@Override
	public ParentViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ParentViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public void bindViewHolder(final FlexibleAdapter adapter, ParentViewHolder holder, int position, List payloads) {
		Context context = holder.itemView.getContext();

		// Subtitle
		if (adapter.isExpandable(this)) {
			setSubtitle(adapter.getCurrentChildren((IExpandable) this).size() + " subItems"
					+ (getHeader() != null ? " - " + getHeader().getId() : "")
					+ (getUpdates() > 0 ? " - u" + getUpdates() : ""));
		}

		// Background, when bound the first time
		if (payloads.size() == 0) {
			Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
					Color.WHITE, Color.parseColor("#dddddd"), //Same color of divider
					DrawableUtils.getColorControlHighlight(context));
			DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
			DrawableUtils.setBackgroundCompat(holder.frontView, drawable);
		}

		if (adapter.isExpandable(this) && payloads.size() > 0) {
			Log.d(this.getClass().getSimpleName(), "ExpandableItem Payload " + payloads);
			if (adapter.hasSearchText()) {
				Utils.highlightText(holder.mSubtitle, getSubtitle(), adapter.getSearchText());
			} else {
				holder.mSubtitle.setText(getSubtitle());
			}
			// We stop the process here, we only want to update the subtitle

		} else {
			// DemoApp: INNER ANIMATION EXAMPLE! ImageView - Handle Flip Animation
//			if (adapter.isSelectAll() || adapter.isLastItemInActionMode()) {
//				// Consume the Animation
//				holder.mFlipView.flip(adapter.isSelected(position), 200L);
//			} else {
				// Display the current flip status
				holder.mFlipView.flipSilently(adapter.isSelected(position));
//			}

			// In case of searchText matches with Title or with an SimpleItem's field
			// this will be highlighted
			if (adapter.hasSearchText()) {
				Utils.highlightText(holder.mTitle, getTitle(), adapter.getSearchText());
				Utils.highlightText(holder.mSubtitle, getSubtitle(), adapter.getSearchText());
			} else {
				holder.mTitle.setText(getTitle());
				holder.mSubtitle.setText(getSubtitle());
			}
		}
	}

	@Override
	public boolean filter(String constraint) {
		return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint) ||
				getSubtitle() != null && getSubtitle().toLowerCase().trim().contains(constraint);
	}

	/**
	 * This ViewHolder is expandable and collapsible.
	 */
	static final class ParentViewHolder extends ExpandableViewHolder {

		FlipView mFlipView;
		TextView mTitle;
		TextView mSubtitle;
		ImageView mHandleView;
		Context mContext;
		View frontView;
		View rearLeftView;
		View rearRightView;

		public boolean swiped = false;

		ParentViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			this.mContext = view.getContext();
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			this.mFlipView = (FlipView) view.findViewById(R.id.image);
			this.mFlipView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mAdapter.mItemLongClickListener != null) {
						mAdapter.mItemLongClickListener.onItemLongClick(getAdapterPosition());
						Toast.makeText(mContext, "ImageClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
						toggleActivation();
					}
				}
			});
			this.mHandleView = (ImageView) view.findViewById(R.id.row_handle);
			setDragHandleView(mHandleView);

			this.frontView = view.findViewById(R.id.front_view);
			this.rearLeftView = view.findViewById(R.id.rear_left_view);
			this.rearRightView = view.findViewById(R.id.rear_right_view);
		}

		@Override
		protected void setDragHandleView(@NonNull View view) {
			if (mAdapter.isHandleDragEnabled()) {
				view.setVisibility(View.VISIBLE);
				super.setDragHandleView(view);
			} else {
				view.setVisibility(View.GONE);
			}
		}

		@Override
		public void onClick(View view) {
			Toast.makeText(mContext, "Click on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
			super.onClick(view);
		}

		@Override
		public boolean onLongClick(View view) {
			Toast.makeText(mContext, "LongClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
			return super.onLongClick(view);
		}

		@Override
		public void toggleActivation() {
			super.toggleActivation();
			//Here we use a custom Animation inside the ItemView
			mFlipView.flip(mAdapter.isSelected(getAdapterPosition()));
		}

		@Override
		public float getActivationElevation() {
			return eu.davidea.utils.Utils.dpToPx(itemView.getContext(), 4f);
		}

		@Override
		protected boolean shouldActivateViewWhileSwiping() {
			return false;//default=false
		}

		@Override
		protected boolean shouldAddSelectionInActionMode() {
			return false;//default=false
		}

		@Override
		public View getFrontView() {
			return frontView;
		}

		@Override
		public View getRearLeftView() {
			return rearLeftView;
		}

		@Override
		public View getRearRightView() {
			return rearRightView;
		}

		@Override
		public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
			if (mAdapter.getRecyclerView().getLayoutManager() instanceof GridLayoutManager ||
					mAdapter.getRecyclerView().getLayoutManager() instanceof StaggeredGridLayoutManager) {
				if (position % 2 != 0)
					AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
				else
					AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
			} else {
				//Linear layout
				if (mAdapter.isSelected(position))
					AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
				else
					AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
			}
		}

		@Override
		public void onItemReleased(int position) {
			swiped = (mActionState == ItemTouchHelper.ACTION_STATE_SWIPE);
			super.onItemReleased(position);
		}
	}

	@Override
	public String toString() {
		return this instanceof ExpandableItem ? super.toString() :
				"SimpleItem[" + super.toString() + "]";
	}

}