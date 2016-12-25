package eu.davidea.samples.flexibleadapter;

import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.samples.flexibleadapter.items.ScrollableLayoutItem;
import eu.davidea.samples.flexibleadapter.items.OverallItem;
import eu.davidea.samples.flexibleadapter.items.ScrollableUseCaseItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;
import eu.davidea.utils.Utils;

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
		//true = Items implement hashCode() and have stableIds!
		super(DatabaseService.getInstance().getDatabaseList(), activity, true);
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
			final ScrollableLayoutItem item = new ScrollableLayoutItem("LAY-L");
			if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
				item.setId("LAY-S");
				item.setTitle(mRecyclerView.getContext().getString(R.string.staggered_layout));
			} else if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
				item.setId("LAY-G");
				item.setTitle(mRecyclerView.getContext().getString(R.string.grid_layout));
			} else {
				item.setTitle(mRecyclerView.getContext().getString(R.string.linear_layout));
			}
			item.setSubtitle(mRecyclerView.getContext().getString(
					R.string.columns,
					String.valueOf(eu.davidea.flexibleadapter.utils.Utils.getSpanCount(mRecyclerView.getLayoutManager())))
			);
			addScrollableHeaderWithDelay(item, 500L, scrollToPosition);
			removeScrollableHeaderWithDelay(item, 3000L);
		}
	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer (don't call super).
	 */
	@Override
	public int getItemViewType(int position) {
		IFlexible item = getItem(position);
		if (item instanceof ScrollableUseCaseItem) return R.layout.recycler_scrollable_usecase_item;
		else if (item instanceof ScrollableLayoutItem) return R.layout.recycler_scrollable_layout_item;
		else return R.layout.recycler_overall_item;
	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer (don't call super).
	 */
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		mInflater = LayoutInflater.from(parent.getContext());
		switch (viewType) {
			case R.layout.recycler_scrollable_usecase_item:
				return new ScrollableUseCaseItem.UCViewHolder(
						mInflater.inflate(viewType, parent, false), this);

			case R.layout.recycler_scrollable_layout_item:
				return new ScrollableLayoutItem.LayoutViewHolder(
						mInflater.inflate(viewType, parent, false), this);
			default:
				return new OverallItem.LabelViewHolder(
						mInflater.inflate(viewType, parent, false), this);
		}
	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer (don't call super).
	 *
	 * Using Method B, some methods need to be called by the user, see bottom of this method!
	 */
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payload) {
		int viewType = getItemViewType(position);

		if (viewType == R.layout.recycler_scrollable_usecase_item) {
			ScrollableUseCaseItem item = (ScrollableUseCaseItem) getItem(position);
			ScrollableUseCaseItem.UCViewHolder vHolder = (ScrollableUseCaseItem.UCViewHolder) holder;
			assert item != null;

			vHolder.mTitle.setText(Utils.fromHtmlCompat(item.getTitle()));
			vHolder.mSubtitle.setText(Utils.fromHtmlCompat(item.getSubtitle()));

			//Support for StaggeredGridLayoutManager
			if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
				((StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams()).setFullSpan(true);
				Log.d("LayoutItem", "LayoutItem configured fullSpan for StaggeredGridLayout");
			}

		} else if (viewType == R.layout.recycler_scrollable_layout_item) {
			ScrollableLayoutItem item = (ScrollableLayoutItem) getItem(position);
			ScrollableLayoutItem.LayoutViewHolder vHolder = (ScrollableLayoutItem.LayoutViewHolder) holder;
			assert item != null;

			vHolder.mTitle.setSelected(true);//For marquee
			vHolder.mTitle.setText(item.getTitle());
			vHolder.mSubtitle.setText(item.getSubtitle());

			//Support for StaggeredGridLayoutManager
			if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
				((StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams()).setFullSpan(true);
				Log.d("LayoutItem", "LayoutItem configured fullSpan for StaggeredGridLayout");
			}

		} else if (viewType == R.layout.recycler_overall_item) {
			OverallItem item = (OverallItem) getItem(position);
			OverallItem.LabelViewHolder vHolder = (OverallItem.LabelViewHolder) holder;
			assert item != null;

			if (item.getTitle() != null) {
				vHolder.mTitle.setText(item.getTitle());
				vHolder.mTitle.setEnabled(isEnabled(position));
			}
			if (item.getDescription() != null) {
				vHolder.mSubtitle.setText(Utils.fromHtmlCompat(item.getDescription()));
				vHolder.mSubtitle.setEnabled(isEnabled(position));
			}
			if (item.getIcon() != null) {
				vHolder.mIcon.setImageDrawable(item.getIcon());
			}
		}

		// IMPORTANT!!!
		// With method B, animateView() needs to be called by the user!
		// With method A, the call is handled by the Adapter
		animateView(holder, position);
		// Same concept for EndlessScrolling and View activation:
		// - onLoadMore(position);
		// - holder.itemView.setActivated(isSelected(position));
	}

}