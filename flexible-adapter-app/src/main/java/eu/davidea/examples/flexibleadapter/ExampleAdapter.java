package eu.davidea.examples.flexibleadapter;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.text.Html;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.davidea.flexibleadapter.FlexibleExpandableAdapter;
import eu.davidea.flipview.FlipView;
import eu.davidea.utils.Utils;
import eu.davidea.viewholders.ExpandableViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;


public class ExampleAdapter extends FlexibleExpandableAdapter<ExpandableViewHolder, Item> {

	private static final String TAG = ExampleAdapter.class.getSimpleName();

	private Context mContext;
	public static final int CHILD_VIEW_TYPE = 0;
	public static final int EXAMPLE_VIEW_TYPE = 1;

	private LayoutInflater mInflater;

	//Selection fields
	private boolean
			mLastItemInActionMode = false,
			mSelectAll = false;

	public ExampleAdapter(Object activity, String listId) {
		super(DatabaseService.getInstance().getListById(listId), activity);
		this.mContext = (Context) activity;
		addUserLearnedSelection();

		//We have highlighted text while filtering, so let's enable this feature
		//to be consistent with the active filter
		setNotifyChangeOfUnfilteredItems(true);
	}

	/**
	 * Param, in this example, is not used.
	 *
	 * @param param A custom parameter to filter the type of the DataSet
	 */
	@Override
	public void updateDataSet(String param) {
		//Overwrite the list and fully notify the change
		//Watch out! The original list must a copy
		//TODO: We may create calls like removeAll, addAll or refreshList in order to animate changes
		mItems = DatabaseService.getInstance().getListById(param);
		notifyDataSetChanged();
		//Add example view
		addUserLearnedSelection();
	}

	private void addUserLearnedSelection() {
		if (!DatabaseService.userLearnedSelection && !hasSearchText() && !super.isEmpty()) {
			//Define Example View
			Item item = new Item();
			item.setId("0");
			item.setEnabled(false);
			item.setExpandable(true);//Mark Expandable also this (same level of others items)
			item.setTitle(mContext.getString(R.string.uls_title));
			item.setSubtitle(mContext.getString(R.string.uls_subtitle));
			super.addItem(0, item);
		}
	}

	private void removeUserLearnedSelection() {
		if (!DatabaseService.userLearnedSelection && isEmpty()) {
			super.removeItem(0);
		}
	}

	private void userLearnedSelection() {
		//TODO FOR YOU: Save the boolean into Settings!
		DatabaseService.userLearnedSelection = true;
		super.removeItem(0);
	}

	@Override
	public synchronized void filterItems(@NonNull List<Item> unfilteredItems) {
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

	@Override
	public void addItem(int position, Item item) {
		if (isEmpty()) {
			addUserLearnedSelection();
			notifyItemInserted(0);
		}
		super.addItem(position, item);
	}

	@Override
	public void removeItems(List<Integer> selectedPositions) {
		super.removeItems(selectedPositions);
		removeUserLearnedSelection();
	}

	@Override
	public boolean isEmpty() {
		return !DatabaseService.userLearnedSelection && mItems.size() == 1 && !hasSearchText()
				|| super.isEmpty();
	}

	@Override
	public int getItemViewType(int position) {
		return (position == 0 && !DatabaseService.userLearnedSelection && !hasSearchText() ?
				EXAMPLE_VIEW_TYPE : super.getItemViewType(position));
	}

	@Override
	public ExpandableViewHolder onCreateExpandableViewHolder(ViewGroup parent, int viewType) {
		if (mInflater == null) {
			mInflater = LayoutInflater.from(parent.getContext());
		}
		return new ParentViewHolder(
				mInflater.inflate(R.layout.recycler_expandable_row, parent, false),
				this);
	}

	@Override
	public FlexibleViewHolder onCreateFlexibleViewHolder(ViewGroup parent, int viewType) {
		if (mInflater == null) {
			mInflater = LayoutInflater.from(parent.getContext());
		}
		switch (viewType) {
			case EXAMPLE_VIEW_TYPE:
				return new ExampleViewHolder(
						mInflater.inflate(R.layout.recycler_uls_row, parent, false),
						this);
			default:
				return new ChildViewHolder(
						mInflater.inflate(R.layout.recycler_row, parent, false),
						this);
		}
	}

	@Override
	public void onBindExpandableViewHolder(ExpandableViewHolder holder, int position) {
//		if (DEBUG) Log.d(TAG, "onBindParentViewHolder for position " + position);
		final Item item = getItem(position);

		ParentViewHolder pvHolder = (ParentViewHolder) holder;
		//When user scrolls, this line binds the correct selection status
		pvHolder.itemView.setActivated(isSelected(position));

		//ANIMATION EXAMPLE!! ImageView - Handle Flip Animation on Select ALL and Deselect ALL
		if (mSelectAll || mLastItemInActionMode) {
			//Reset the flags with delay
			pvHolder.mFlipView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mSelectAll = mLastItemInActionMode = false;
				}
			}, 200L);
			//Consume the Animation
			pvHolder.mFlipView.flip(isSelected(position), 200L);
		} else {
			//Display the current flip status
			pvHolder.mFlipView.flipSilently(isSelected(position));
		}

		//This "if-else" is just an example of what you can do with item animation
		if (isSelected(position)) {
			animateView(holder.itemView, position, true);
		} else {
			animateView(holder.itemView, position, false);
		}

		//In case of searchText matches with Title or with an Item's field
		// this will be highlighted
		if (hasSearchText()) {
			setHighlightText(pvHolder.mTitle, item.getTitle(), mSearchText);
			setHighlightText(pvHolder.mSubtitle, item.updateSubTitle(), mSearchText);
		} else {
			pvHolder.mTitle.setText(item.getTitle());
			pvHolder.mSubtitle.setText(item.updateSubTitle());
		}
	}

	@Override
	public void onBindFlexibleViewHolder(FlexibleViewHolder holder, int position) {
//		if (DEBUG) Log.d(TAG, "onBindChildViewHolder for position " + position);
		final Item item = getItem(position);

		//NOTE: ViewType Must be checked ALSO here to bind the correct view
		switch (getItemViewType(position)) {
			case EXAMPLE_VIEW_TYPE:
				ExampleViewHolder exHolder = (ExampleViewHolder) holder;
				exHolder.mImageView.setImageResource(R.drawable.ic_account_circle_white_24dp);
				exHolder.itemView.setActivated(true);
				exHolder.mTitle.setSelected(true);//For marquee
				exHolder.mTitle.setText(Html.fromHtml(item.getTitle()));
				exHolder.mSubtitle.setText(Html.fromHtml(item.getSubtitle()));
				animateView(holder.itemView, position, false);
				return;

			default:
				ChildViewHolder cvHolder = (ChildViewHolder) holder;
				//When user scrolls, this line binds the correct selection status
				cvHolder.itemView.setActivated(isSelected(position));

				//This "if-else" is just an example of what you can do with item animation
				if (isSelected(position)) {
					animateView(holder.itemView, position, true);
				} else {
					animateView(holder.itemView, position, false);
				}

				//In case of searchText matches with Title or with an Item's field
				// this will be highlighted
				if (hasSearchText()) {
					setHighlightText(cvHolder.mTitle, item.getTitle(), mSearchText);
				} else {
					cvHolder.mTitle.setText(item.getTitle());
				}
		}//end-switch
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
			return getItem(position).getTitle().substring(0, 1).toUpperCase();
		}
		return super.onCreateBubbleText(position);
	}

	private void setHighlightText(TextView textView, String text, String searchText) {
		Spannable spanText = Spannable.Factory.getInstance().newSpannable(text);
		int i = text.toLowerCase(Locale.getDefault()).indexOf(searchText);
		if (i != -1) {
			spanText.setSpan(new ForegroundColorSpan(Utils.getColorAccent(mContext)), i,
					i + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spanText.setSpan(new StyleSpan(Typeface.BOLD), i,
					i + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			textView.setText(spanText, TextView.BufferType.SPANNABLE);
		} else {
			textView.setText(text, TextView.BufferType.NORMAL);
		}
	}

	/**
	 * TODO: Rewrite Custom filter for the 2 examples Adapters (Expandable and simple Flexible)
	 * Custom filter.
	 *
	 * @param item   The item to filter
	 * @param constraint the current searchText
	 * @return true if a match exists in the title or in the subtitle, false if no match found.
	 */
//	@Override
//	protected boolean filterObject(Item item, String constraint) {
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

	/**
	 * Used for UserLearnsSelection.
	 */
	static class ExampleViewHolder extends FlexibleViewHolder {

		ImageView mImageView;
		TextView mTitle;
		TextView mSubtitle;
		ImageView mDismissIcon;

		ExampleViewHolder(View view, final ExampleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			mImageView = (ImageView) view.findViewById(R.id.image);
			mDismissIcon = (ImageView) view.findViewById(R.id.dismiss_icon);
			mDismissIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					adapter.userLearnedSelection();
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
			this.mContext = adapter.mContext;
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			this.mFlipView = (FlipView) view.findViewById(R.id.image);
			this.mFlipView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAdapter.mItemLongClickListener.onItemLongClick(getAdapterPosition());
					Toast.makeText(mContext, "ImageClick on " + mTitle.getText(), Toast.LENGTH_SHORT).show();
					toggleActivation();
				}
			});
			this.mHandleView = (ImageView) view.findViewById(R.id.row_handle);
			setDragHandleView(mHandleView);
		}

		@Override
		public void onClick(View view) {
			Toast.makeText(mContext, "Click on " + mTitle.getText(), Toast.LENGTH_SHORT).show();
			super.onClick(view);
		}

		@Override
		public boolean onLongClick(View view) {
			Toast.makeText(mContext, "LongClick on " + mTitle.getText(), Toast.LENGTH_SHORT).show();
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
		Context mContext;

		ChildViewHolder(View view, ExampleAdapter adapter) {
			super(view, adapter);
			this.mContext = adapter.mContext;
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