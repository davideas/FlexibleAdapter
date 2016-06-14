package eu.davidea.samples.flexibleadapter;

import android.animation.Animator;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.samples.flexibleadapter.models.LayoutItem;
import eu.davidea.samples.flexibleadapter.models.ULSItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

/**
 * NOTE: AbstractModelItem is for example purpose only. I wanted to have in common
 * some Fields and Layout.
 * You, having different Layout for each item type, would use IFlexible or AbstractFlexibleItem
 * as base item to extend this Adapter.
 */
public class ExampleAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

	private static final String TAG = ExampleAdapter.class.getSimpleName();

	public static final int CHILD_VIEW_TYPE = 0;
	public static final int EXAMPLE_VIEW_TYPE = 1;

	private AbstractFlexibleItem mUseCaseItem;
	private int positionOld = -1;

	public ExampleAdapter(Activity activity) {
		super(DatabaseService.getInstance().getDatabaseList(), activity);

		//NEW! We have highlighted text while filtering, so let's enable this feature
		//to be consistent with the active filter
		setNotifyChangeOfUnfilteredItems(true);
	}

	@Override
	public void updateDataSet(List<AbstractFlexibleItem> items, boolean animate) {
		//NOTE: To have views/items not changed, set them into "items" before passing the final
		// list to the Adapter, also pass animate=true in order to not delete those items.

		//Overwrite the list and fully notify the change, pass false to not animate changes.
		//Watch out! The original list must a copy
		super.updateDataSet(items, animate);

		//Add example view
		showLayoutInfo(true);
		addUserLearnedSelection(true);
	}

	/*
	 * HEADER/FOOTER VIEW
	 * This method show how to add Header/Footer View as it was for ListView.
	 * The secret is the position! 0 for Header; itemCount for Footer ;-)
	 * The view is represented by a custom Item type to better represent any dynamic content.
	 */
	public void showLayoutInfo(boolean scrollToPosition) {
		if (!hasSearchText() && !isEmpty()) {
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
			addItemWithDelay((getItem(0) instanceof ULSItem ? 1 : 0), item, 100L, (!(getItem(0) instanceof ULSItem) && scrollToPosition));
			removeItemWithDelay(item, 2000L, true, true);
		}
	}

	/*
	 * ANOTHER HEADER/FOOTER VIEW
	 * This method show how to add Header/Footer View as it was for ListView.
	 * The secret is the position! 0 for Header; itemCount for Footer ;-)
	 * The view is represented by a custom Item type to better represent any dynamic content.
	 */
	public void addUserLearnedSelection(boolean scrollToPosition) {
		if (!DatabaseService.userLearnedSelection && !hasSearchText() && !(getItem(0) instanceof ULSItem)) {
			//Define Example View
			final ULSItem item = new ULSItem("ULS");
			item.setTitle(mRecyclerView.getContext().getString(R.string.uls_title));
			item.setSubtitle(mRecyclerView.getContext().getString(R.string.uls_subtitle));
			addItemWithDelay(0, item, 1400L, scrollToPosition);
		}
	}

	@Override
	public synchronized void filterItems(@NonNull List<AbstractFlexibleItem> unfilteredItems) {
		super.filterItems(unfilteredItems);
		addUserLearnedSelection(false);
		showLayoutInfo(false);
	}

	@Override
	public void selectAll(Integer... viewTypes) {
		super.selectAll();
	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer.
	 */
//	@Override
//	public int getItemViewType(int position) {
//		IFlexible item = getItem(position);
//		if (item instanceof SimpleItem) //or ExpandableItem, since it extends SimpleItem!
//			return EXPANDABLE_VIEW_TYPE;
//		else if (item instanceof IHeader) return SECTION_VIEW_TYPE;
//		else if (item instanceof ULSItem) return EXAMPLE_VIEW_TYPE;
//		else return 0;
//	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer.
	 */
//	@Override
//	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//		if (mInflater == null) {
//			mInflater = LayoutInflater.from(parent.getContext());
//		}
//		switch (viewType) {
//			case SECTION_VIEW_TYPE:
//				return new HeaderItem.HeaderViewHolder(
//						mInflater.inflate(R.layout.recycler_header_row, parent, false), this);
//			case EXPANDABLE_VIEW_TYPE:
//				return new ExpandableItem.ParentViewHolder(
//						mInflater.inflate(R.layout.recycler_expandable_row, parent, false), this);
//			case EXAMPLE_VIEW_TYPE:
//				return new ULSItem.ExampleViewHolder(
//						mInflater.inflate(R.layout.recycler_uls_row, parent, false), this);
//			default:
//				return new SubItem.ChildViewHolder(
//						mInflater.inflate(R.layout.recycler_child_row, parent, false), this);
//		}
//	}

	/**
	 * METHOD A - NEW! Via Model objects. In this case you don't need to implement this method!
	 * METHOD B - You override and implement this method as you prefer.
	 */
//	@Override
//	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//		//NOTE: ViewType Must be checked ALSO here to bind the correct view
//		//When user scrolls, this line binds the correct selection status
//		holder.itemView.setActivated(isSelected(position));
//		switch (getItemViewType(position)) {
//			case SECTION_VIEW_TYPE:
//				final HeaderItem header = (HeaderItem) getItem(position);
//				assert header != null;
//				HeaderItem.HeaderViewHolder hvHolder = (HeaderItem.HeaderViewHolder) holder;
//				hvHolder.mTitle.setText(header.getTitle());
//				hvHolder.mSubtitle.setText(header.getSubtitle());
//				break;
//
//			case EXPANDABLE_VIEW_TYPE:
//				SimpleItem item = (SimpleItem) getItem(position);
//				ExpandableItem.ParentViewHolder pvHolder = (ExpandableItem.ParentViewHolder) holder;
//				assert item != null;
//
////				if (payloads.size() > 0) {
////					Log.i(this.getClass().getSimpleName(), "Payload " + payloads);
////					item.setSubtitle(getCurrentChildren(item).size() + " subItems");
////					if (hasSearchText()) {
////						Utils.highlightText(holder.itemView.getContext(), pvHolder.mSubtitle,
////								item.getSubtitle(), getSearchText(), R.color.colorAccent_light);
////					} else {
////						pvHolder.mSubtitle.setText(item.getSubtitle());
////					}
////					break;//We stop the process here, we only want to update the subtitle
////				}
//
//				//ANIMATION EXAMPLE!! ImageView - Handle Flip Animation on Select ALL and Deselect ALL
//				if (mSelectAll || mLastItemInActionMode) {
//					//Reset the flags with delay
//					pvHolder.mFlipView.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							mSelectAll = mLastItemInActionMode = false;
//						}
//					}, 200L);
//					//Consume the Animation
//					pvHolder.mFlipView.flip(isSelected(position), 200L);
//				} else {
//					//Display the current flip status
//					pvHolder.mFlipView.flipSilently(isSelected(position));
//				}
//
//				//This "if-else" is just an example of what you can do with item animation
//				if (isSelected(position)) {
//					animateView(holder.itemView, position, true);
//				} else {
//					animateView(holder.itemView, position, false);
//				}
//
//				//In case of searchText matches with Title or with an SimpleItem's field
//				// this will be highlighted
//				if (hasSearchText()) {
//					Utils.highlightText(pvHolder.itemView.getContext(), pvHolder.mTitle,
//							item.getTitle(), getSearchText(), R.color.colorAccent_light);
//					Utils.highlightText(pvHolder.itemView.getContext(), pvHolder.mSubtitle,
//							updateSubTitle(item), getSearchText(), R.color.colorAccent_light);
//				} else {
//					pvHolder.mTitle.setText(item.getTitle());
//					pvHolder.mSubtitle.setText(updateSubTitle(item));
//				}
//				break;
//
//			case EXAMPLE_VIEW_TYPE:
//				final ULSItem ulsItem = (ULSItem) getItem(position);
//				ULSItem.ExampleViewHolder exHolder = (ULSItem.ExampleViewHolder) holder;
//				exHolder.mImageView.setImageResource(R.drawable.ic_account_circle_white_24dp);
//				exHolder.itemView.setActivated(true);
//				exHolder.mTitle.setSelected(true);//For marquee
//				exHolder.mTitle.setText(Html.fromHtml(ulsItem.getTitle()));
//				exHolder.mSubtitle.setText(Html.fromHtml(ulsItem.getSubtitle()));
//				animateView(holder.itemView, position, false);
//				break;
//
//			default:
//				SubItem subItem = (SubItem) getItem(position);
//				SubItem.ChildViewHolder cvHolder = (SubItem.ChildViewHolder) holder;
//				assert subItem != null;
//
//				//This "if-else" is just an example of what you can do with item animation
//				if (isSelected(position)) {
//					animateView(holder.itemView, position, true);
//				} else {
//					animateView(holder.itemView, position, false);
//				}
//
//				//In case of searchText matches with Title or with an SimpleItem's field
//				// this will be highlighted
//				if (hasSearchText()) {
//					Utils.highlightText(cvHolder.itemView.getContext(), cvHolder.mTitle,
//							subItem.getTitle(), getSearchText(), R.color.colorAccent_light);
//				} else {
//					cvHolder.mTitle.setText(subItem.getTitle());
//				}
//		}//end-switch
//	}
//
//	private String updateSubTitle(SimpleItem item) {
//		if (item instanceof IExpandable)
//			return getCurrentChildren((IExpandable)item).size() + " subItems";
//		else
//			return item.getSubtitle();
//	}

	@Override
	public List<Animator> getAnimators(View itemView, int position, boolean isSelected) {
		List<Animator> animators = new ArrayList<Animator>();
		if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
			//GridLayout
			if (position % 2 == 0)
				addSlideInFromRightAnimator(animators, itemView, 0.5f);
			else
				addSlideInFromLeftAnimator(animators, itemView, 0.5f);
		} else {
			//LinearLayout
			switch (getItemViewType(position)) {
				case R.layout.recycler_staggered_item:
					if (position < positionOld) //inverted to have items animated up-side-down
						addSlideInFromBottomAnimator(animators, itemView);
					else
						addSlideInFromTopAnimator(animators, itemView);
					break;
				case R.layout.recycler_staggered_header_item:
				case R.layout.recycler_layout_item:
				case R.layout.recycler_uls_item:
					addSlideInFromTopAnimator(animators, itemView);
					break;
				case R.layout.recycler_sub_item:
				case EXAMPLE_VIEW_TYPE:
					addScaleInAnimator(animators, itemView, 0.0f);
					break;
				default:
					if (isSelected)
						addSlideInFromRightAnimator(animators, itemView, 0.5f);
					else
						addSlideInFromLeftAnimator(animators, itemView, 0.5f);
					break;
			}
		}

		if (position != positionOld)
			positionOld = position;

		//Alpha Animator is automatically added
		return animators;
	}

	@Override
	public String onCreateBubbleText(int position) {
		if (!DatabaseService.userLearnedSelection && position == 0) {//This 'if' is for my example only
			//TODO FOR YOU: This is the normal line you should use: Usually it's the first letter
			return Integer.toString(position);
		}
		return super.onCreateBubbleText(position);
	}

}