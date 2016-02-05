package eu.davidea.examples.flexibleadapter;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.davidea.examples.models.AbstractItem;
import eu.davidea.examples.models.SimpleItem;
import eu.davidea.examples.models.ULSItem;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flipview.FlipView;
import eu.davidea.utils.Utils;
import eu.davidea.viewholders.ExpandableViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;


public class ExampleAdapter extends FlexibleAdapter<AbstractItem> {

	private static final String TAG = ExampleAdapter.class.getSimpleName();

	public static final int CHILD_VIEW_TYPE = 0;
	public static final int EXAMPLE_VIEW_TYPE = 1;

	private Context mContext;


	public ExampleAdapter(Activity activity) {
		super(DatabaseService.getInstance().getListById(), activity);
		mContext = activity;
		addUserLearnedSelection();

		//NEW! We have highlighted text while filtering, so let's enable this feature
		//to be consistent with the active filter
		setNotifyChangeOfUnfilteredItems(true);
	}

	/**
	 * Param, in this example, is not used.
	 *
	 * @param param A custom parameter to filter the type of the DataSet
	 */
	@Override
	public void updateDataSet(String... param) {
		//Overwrite the list and fully notify the change
		//Watch out! The original list must a copy
		//TODO: We may create calls like removeAll, addAll or refreshList in order to animate changes
		mItems = DatabaseService.getInstance().getListById();
		notifyDataSetChanged();
		//Add example view
		addUserLearnedSelection();
	}

	private void addUserLearnedSelection() {
		SimpleItem uls = (SimpleItem) getItem(0);
		if (!DatabaseService.userLearnedSelection && !hasSearchText() &&
				(uls == null || !uls.getId().equals("ULS"))) {
			//Define Example View
			ULSItem item = new ULSItem();
			item.setEnabled(false);
			item.setTitle(mContext.getString(R.string.uls_title));
			item.setSubtitle(mContext.getString(R.string.uls_subtitle));
			super.addItem(0, item);
		}
	}


	@Override
	public synchronized void filterItems(@NonNull List<AbstractItem> unfilteredItems) {
		super.filterItems(unfilteredItems);
		addUserLearnedSelection();
	}

	@Override
	public void setMode(int mode) {
		super.setMode(mode);
		if (mode == MODE_IDLE) mLastItemInActionMode = true;
	}

	@Override
	public void selectAll(Integer... viewTypes) {
		mSelectAll = true;
		super.selectAll(CHILD_VIEW_TYPE);
	}

//	@Override
//	public int getItemViewType(int position) {
//		AbstractItem item = getItem(position);
//		if (item instanceof SimpleItem) //or ExpandableItem, since it extends SimpleItem!
//			return EXPANDABLE_VIEW_TYPE;
//		else if (item instanceof ULSItem) return EXAMPLE_VIEW_TYPE;
//		else return 0;
//	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		//METHOD A - NEW! Via Model objects. In this case you don't need to implement this method.
		return super.onCreateViewHolder(parent, viewType);

		//METHOD B - Normal way as you prefer
//		if (mInflater == null) {
//			mInflater = LayoutInflater.from(parent.getContext());
//		}
//		switch (viewType) {
//			case SECTION_VIEW_TYPE:
//				return new HeaderViewHolder(
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
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		//METHOD A - NEW! Via Model objects. In this case you don't need to implement this method.
		super.onBindViewHolder(holder, position);

		//METHOD B - Normal way, inside the Adapter, as you prefer
		//NOTE: ViewType Must be checked ALSO here to bind the correct view
		//When user scrolls, this line binds the correct selection status
//		holder.itemView.setActivated(isSelected(position));
//		switch (getItemViewType(position)) {
//			case SECTION_VIEW_TYPE:
//				final SimpleItem header = (SimpleItem) getItem(position);
//				HeaderViewHolder hvHolder = (HeaderViewHolder) holder;
//				hvHolder.mTitle.setText(((SimpleItem) header).getTitle());
//				break;
//
//			case EXPANDABLE_VIEW_TYPE:
//				SimpleItem item = (SimpleItem) getItem(position);
//				ParentViewHolder pvHolder = (ParentViewHolder) holder;
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
//					setHighlightText(pvHolder.itemView.getContext(), pvHolder.mTitle, item.getTitle(), mSearchText);
//					setHighlightText(pvHolder.itemView.getContext(), pvHolder.mSubtitle, updateSubTitle(item), mSearchText);
//				} else {
//					pvHolder.mTitle.setText(item.getTitle());
//					pvHolder.mSubtitle.setText(updateSubTitle(item));
//				}
//				break;
//
//			case EXAMPLE_VIEW_TYPE:
//				final ULSItem ulsItem = (ULSItem) getItem(position);
//				ExampleViewHolder exHolder = (ExampleViewHolder) holder;
//				exHolder.mImageView.setImageResource(R.drawable.ic_account_circle_white_24dp);
//				exHolder.itemView.setActivated(true);
//				exHolder.mTitle.setSelected(true);//For marquee
//				exHolder.mTitle.setText(Html.fromHtml(ulsItem.getTitle()));
//				exHolder.mSubtitle.setText(Html.fromHtml(ulsItem.getSubtitle()));
//				animateView(holder.itemView, position, false);
//				break;
//
//			default:
//				final SubItem subItem = (SubItem) getItem(position);
//				ChildViewHolder cvHolder = (ChildViewHolder) holder;
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
//					setHighlightText(cvHolder.itemView.getContext(), cvHolder.mTitle, subItem.getTitle(), mSearchText);
//				} else {
//					cvHolder.mTitle.setText(subItem.getTitle());
//				}
//		}//end-switch
	}

	private String updateSubTitle(SimpleItem item) {
		return getCurrentChildren(item).size() + " subItems";
	}

	@Override
	public List<Animator> getAnimators(View itemView, int position, boolean isSelected) {
		List<Animator> animators = new ArrayList<Animator>();
		//GridLayout
		if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {

			if (position % 2 != 0)
				addSlideInFromRightAnimator(animators, itemView, 0.5f);
			else
				addSlideInFromLeftAnimator(animators, itemView, 0.5f);
			//LinearLayout
		} else {
			switch (getItemViewType(position)) {
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
			return ""+position;
		}
		return super.onCreateBubbleText(position);
	}

	public static void setHighlightText(Context context, TextView textView, String text, String searchText) {
		Spannable spanText = Spannable.Factory.getInstance().newSpannable(text);
		int i = text.toLowerCase(Locale.getDefault()).indexOf(searchText);
		if (i != -1) {
			spanText.setSpan(new ForegroundColorSpan(Utils.getColorAccent(context)), i,
					i + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spanText.setSpan(new StyleSpan(Typeface.BOLD), i,
					i + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			textView.setText(spanText, TextView.BufferType.SPANNABLE);
		} else {
			textView.setText(text, TextView.BufferType.NORMAL);
		}
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

	static class HeaderViewHolder extends FlexibleViewHolder {

		TextView mTitle;

		public HeaderViewHolder(View view, ExampleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
		}
	}

	/**
	 * Used for UserLearnsSelection.
	 */
	static class ExampleViewHolder extends FlexibleViewHolder {

		ImageView mImageView;
		TextView mTitle;
		TextView mSubtitle;
		ImageView mDismissIcon;

		public ExampleViewHolder(View view, final ExampleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			mImageView = (ImageView) view.findViewById(R.id.image);
			mDismissIcon = (ImageView) view.findViewById(R.id.dismiss_icon);
			mDismissIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//TODO FOR YOU: Save the boolean into Settings!
					DatabaseService.userLearnedSelection = true;
					adapter.removeItem(0);
				}
			});
		}
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

		public ParentViewHolder(View view, ExampleAdapter adapter) {
			super(view, adapter);
			this.mContext = view.getContext();
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			this.mFlipView = (FlipView) view.findViewById(R.id.image);
			this.mFlipView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAdapter.mItemLongClickListener.onItemLongClick(getAdapterPosition());
					Toast.makeText(mContext, "ImageClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
					toggleActivation();
				}
			});
			this.mHandleView = (ImageView) view.findViewById(R.id.row_handle);
			setDragHandleView(mHandleView);
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
		protected void toggleActivation() {
			super.toggleActivation();
			//Here we use a custom Animation inside the ItemView
			mFlipView.flip(mAdapter.isSelected(getAdapterPosition()));
		}
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	static final class ChildViewHolder extends FlexibleViewHolder {

		ImageView mHandleView;
		TextView mTitle;

		public ChildViewHolder(View view, ExampleAdapter adapter) {
			super(view, adapter);
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mHandleView = (ImageView) view.findViewById(R.id.row_handle);
			setDragHandleView(mHandleView);
		}
	}

	@Override
	public String toString() {
		return mItems.toString();
	}

}