package eu.davidea.samples.flexibleadapter;

import android.animation.Animator;
import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.samples.flexibleadapter.models.LayoutItem;
import eu.davidea.samples.flexibleadapter.models.OverallItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

/**
 * This is a custom implementation extending FlexibleAdapter. {@code AbstractFlexibleItem} is
 * used as most common Item for ALL view types.
 * <p>Items are bound with <b>METHOD B</b> (classic way): AutoMap is disabled, you <u>have to</u>
 * implement {@code getItemViewType, onCreateViewHolder, onBindViewHolder} without calling
 * {@code super()}.</p>
 * Check {@code ExampleAdapter} for <b>METHOD A</b> (new way).
 *
 * @see ExampleAdapter
 * @see AbstractFlexibleItem
 */
public class OverallAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

	private static final String TAG = OverallAdapter.class.getSimpleName();

	public OverallAdapter(Activity activity) {
		super(DatabaseService.getInstance().getDatabaseList(), activity);
	}

	/*
	 * HEADER/FOOTER VIEW
	 * This method show how to add Header/Footer View as it was for ListView.
	 * The secret is the position! 0 for Header; itemCount for Footer ;-)
	 * The view is represented by a custom Item type to better represent any dynamic content.
	 */
	public void showLayoutInfo(boolean scrollToPosition) {
		if (!hasSearchText()) {
			//Define Example View
			final LayoutItem item = new LayoutItem("LAY-L");
			if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
				item.setId("LAY-S");
				item.setTitle(mRecyclerView.getContext().getString(R.string.staggered_layout));
			} else if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
				item.setId("LAY-G");
				item.setTitle(mRecyclerView.getContext().getString(R.string.grid_layout));
			} else {
				item.setTitle(mRecyclerView.getContext().getString(R.string.linear_layout));
			}
			item.setSubtitle(mRecyclerView.getContext().getString(R.string.columns, getSpanCount(mRecyclerView.getLayoutManager())));
			addItemWithDelay(0, item, 100L, scrollToPosition);
			removeItemWithDelay(item, 2000L, true, true);
		}
	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer (don't call super).
	 */
	@Override
	public int getItemViewType(int position) {
		IFlexible item = getItem(position);
		if (item instanceof LayoutItem) return R.layout.recycler_layout_item;
		else return R.layout.recycler_label_item;
	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer (don't call super).
	 */
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		mInflater = LayoutInflater.from(parent.getContext());
		switch (viewType) {
			case R.layout.recycler_layout_item:
				return new LayoutItem.ExampleViewHolder(
						mInflater.inflate(viewType, parent, false), this);
			default:
				return new OverallItem.LabelViewHolder(
						mInflater.inflate(viewType, parent, false), this);
		}
	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer (don't call super).
	 */
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payload) {
		int viewType = getItemViewType(position);
		if (viewType == R.layout.recycler_layout_item) {
			LayoutItem item = (LayoutItem) getItem(position);
			LayoutItem.ExampleViewHolder vHolder = (LayoutItem.ExampleViewHolder) holder;
			assert item != null;

			vHolder.mTitle.setSelected(true);//For marquee
			vHolder.mTitle.setText(item.getTitle());
			vHolder.mSubtitle.setText(item.getSubtitle());

			//Support for StaggeredGridLayoutManager
			if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
				((StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams()).setFullSpan(true);
				Log.d("LayoutItem", "LayoutItem configured fullSpan for StaggeredGridLayout");
			}

		} else if (viewType == R.layout.recycler_label_item) {
			OverallItem item = (OverallItem) getItem(position);
			OverallItem.LabelViewHolder vHolder = (OverallItem.LabelViewHolder) holder;
			assert item != null;

			if (item.getTitle() != null) {
				vHolder.mTitle.setText(item.getTitle());
				vHolder.mTitle.setEnabled(item.isEnabled());
			}
			if (item.getDescription() != null) {
				vHolder.mSubtitle.setText(item.getDescription());
				vHolder.mSubtitle.setEnabled(item.isEnabled());
			}
			if (item.getIcon() != null) {
				vHolder.mIcon.setImageDrawable(item.getIcon());
			}

		}
		animateView(holder.itemView, position, isSelected(position));
	}

	@Override
	public List<Animator> getAnimators(View itemView, int position, boolean isSelected) {
		List<Animator> animators = new ArrayList<Animator>();
		if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
			//GridLayout
			if (position % 2 != 0)
				addSlideInFromRightAnimator(animators, itemView, 0.5f);
			else
				addSlideInFromLeftAnimator(animators, itemView, 0.5f);
		} else {
			//LinearLayout
			switch (getItemViewType(position)) {
				default:
					addSlideInFromBottomAnimator(animators, itemView);
					break;
			}
		}

		//Alpha Animator is automatically added
		return animators;
	}

}