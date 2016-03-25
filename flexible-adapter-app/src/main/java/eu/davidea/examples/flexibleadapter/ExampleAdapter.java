package eu.davidea.examples.flexibleadapter;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.examples.models.ULSItem;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * NOTE: AbstractExampleItem is for example purpose only. I wanted to have in common
 * some Fields and Layout.
 * You, having different Layout for each item type, would use IFlexible or AbstractFlexibleItem
 * as base item to extend this Adapter.
 */
public class ExampleAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

	private static final String TAG = ExampleAdapter.class.getSimpleName();

	public static final int CHILD_VIEW_TYPE = 0;
	public static final int EXAMPLE_VIEW_TYPE = 1;

	private Context mContext;//this should not be necessary for view holders


	public ExampleAdapter(Activity activity) {
		super(DatabaseService.getInstance().getListById(), activity);
		mContext = activity;

		//NEW! We have highlighted text while filtering, so let's enable this feature
		//to be consistent with the active filter
		setNotifyChangeOfUnfilteredItems(true);
	}

	@Override
	public void updateDataSet(List<AbstractFlexibleItem> items) {
		super.updateDataSet(items);
		//Overwrite the list and fully notify the change
		//Watch out! The original list must a copy
		//TODO: We may create calls like removeAll, addAll or refreshList in order to animate changes

		//Add example view
		addUserLearnedSelection(true);
	}

	public void addUserLearnedSelection(boolean scrollToPosition) {
		if (!DatabaseService.userLearnedSelection && !hasSearchText() && !(getItem(0) instanceof ULSItem)) {
			//Define Example View
			final ULSItem item = new ULSItem("ULS");
			item.setTitle(mContext.getString(R.string.uls_title));
			item.setSubtitle(mContext.getString(R.string.uls_subtitle));
			addItemWithDelay(0, item, 1700L, scrollToPosition);
		}
	}

	@Override
	public synchronized void filterItems(@NonNull List<AbstractFlexibleItem> unfilteredItems) {
		super.filterItems(unfilteredItems);
		addUserLearnedSelection(false);
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
////						Utils.setHighlightText(holder.itemView.getContext(), pvHolder.mSubtitle,
////								item.getSubtitle(), getSearchText(), R.color.colorAccent_light);
////					} else {
////						pvHolder.mSubtitle.setText(item.getSubtitle());
////					}
////					break;//We stop the process here, we only want to update the subtitle
////				}
//
//				//When user scrolls, this line binds the correct selection status
//				pvHolder.itemView.setActivated(isSelected(position));
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
//					Utils.setHighlightText(pvHolder.itemView.getContext(), pvHolder.mTitle,
//							item.getTitle(), getSearchText(), R.color.colorAccent_light);
//					Utils.setHighlightText(pvHolder.itemView.getContext(), pvHolder.mSubtitle,
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
//				//When user scrolls, this line binds the correct selection status
//				cvHolder.itemView.setActivated(isSelected(position));
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
//					Utils.setHighlightText(cvHolder.itemView.getContext(), cvHolder.mTitle,
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
			if (position % 2 != 0)
				addSlideInFromRightAnimator(animators, itemView, 0.5f);
			else
				addSlideInFromLeftAnimator(animators, itemView, 0.5f);
		} else {
			//LinearLayout
			switch (getItemViewType(position)) {
				case R.layout.recycler_uls_row:
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

//	/**
//	 * TODO: Rewrite Custom filter for the 2 examples Adapters (Expandable and simple Flexible)
//	 * Custom filter.
//	 *
//	 * @param item   The item to filter
//	 * @param constraint the current searchText
//	 * @return true if a match exists in the title or in the subtitle, false if no match found.
//	 */
//	@Override
//	protected boolean filterObject(SimpleItem item, String constraint) {
//		String valueText = item.getTitle();
//
//		//Filter on Title
//		if (valueText != null && valueText.toLowerCase().contains(constraint)) {
//			return true;
//		}
//		//Filter on Subtitle
//		valueText = item.getSubtitle();
//		return valueText != null && valueText.toLowerCase().contains(constraint);
//	}

}