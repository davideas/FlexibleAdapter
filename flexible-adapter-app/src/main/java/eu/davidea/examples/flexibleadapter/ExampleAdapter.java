package eu.davidea.examples.flexibleadapter;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.GridLayoutManager;
import android.text.Html;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.davidea.flexibleadapter.FlexibleExpandableAdapter;
import eu.davidea.utils.Utils;
import eu.davidea.viewholder.ExpandableViewHolder;
import eu.davidea.viewholder.FlexibleViewHolder;
import eu.davidea.viewholder.FlexibleViewHolder.OnListItemClickListener;


public class ExampleAdapter extends FlexibleExpandableAdapter<ExpandableViewHolder, Item> {

	private static final String TAG = ExampleAdapter.class.getSimpleName();

	private Context mContext;
	private static final int EXAMPLE_VIEW_TYPE = 1;

	private LayoutInflater mInflater;
	private OnListItemClickListener mClickListener;

	//Selection fields
	private boolean
			mLastItemInActionMode = false,
			mSelectAll = false;

	public ExampleAdapter(Object activity, String listId) {
		super(DatabaseService.getInstance().getListById(listId), activity);
		this.mContext = (Context) activity;
		this.mClickListener = (OnListItemClickListener) activity;
		if (!isEmpty()) addUserLearnedSelection();
	}

	/**
	 * Param, in this example, is not used.
	 *
	 * @param param A custom parameter to filter the type of the DataSet
	 */
	@Override
	public void updateDataSet(String param) {
		//Refresh the original content
		mItems = DatabaseService.getInstance().getListById(param);

		if (!super.isEmpty()) addUserLearnedSelection();
		//FIXME: This should be done automatically by the Library
		expandInitialItems(mItems);

		//Fill and Filter mItems with your custom list
		//Note: In case of userLearnSelection, mItems is pre-initialized and after filtered.
		filterItems(mItems);
		notifyDataSetChanged();

		//Update Empty View
		mUpdateListener.onUpdateEmptyView(mItems.size());
	}

	private void addUserLearnedSelection() {
		if (!DatabaseService.userLearnedSelection && !hasSearchText()) {
			//Define Example View
			Item item = new Item();
			item.setId("0");
			item.setTitle(mContext.getString(R.string.uls_title));
			item.setSubtitle(mContext.getString(R.string.uls_subtitle));
			mItems.add(0, item);
		}
	}

	private void removeUserLearnedSelection() {
		if (!DatabaseService.userLearnedSelection && isEmpty()) {
			mItems.remove(0);
			notifyItemRemoved(0);
		}
	}

	private void userLearnedSelection() {
		//TODO FOR YOU: Save the boolean into Settings!
		DatabaseService.userLearnedSelection = true;
		mItems.remove(0);
		notifyItemRemoved(0);
	}

	@Override
	public void setMode(int mode) {
		super.setMode(mode);
		if (mode == MODE_SINGLE) mLastItemInActionMode = true;
	}

	@Override
	public void selectAll() {
		mSelectAll = true;
		super.selectAll(EXAMPLE_VIEW_TYPE);
	}

	@Override
	public void addItem(int position, Item item) {
		if (isEmpty()) {
			addUserLearnedSelection();
			notifyItemInserted(0);
		}
		super.addItem(position, item);
	}

	/*---------------------------*/
	/* REMOVE METHODS OVERRIDDEN */
	/*---------------------------*/

//	/**
//	 * Internally called to search the Parent position of a Child item.
//	 *
//	 * @param position Child position
//	 * @return the Parent position
//	 */
//	private int getParentPosition(int position) {
//		position--;
//		while (position > 0) {
//			if (getItem(position).isExpandable()) {
//				if (DEBUG) Log.d(TAG, "Found parent at position " + position);
//				break;
//			}
//			position--;
//		}
//		return position;
//	}

	/**
	 * @param position            The position of item to remove
	 * @param notifyParentChanged true to Notify parent of a removal of a child
	 */
//	public void removeItem(int position, boolean notifyParentChanged) {
//		Item item = getItem(position);
//		if (notifyParentChanged && !item.isExpandable()) {
//			//It's a child, so notify the parent
////			int parentPosition = getParentPosition(position);
//			Item parent = item.getParent();
//			parent.removeSubItem(item);
//			parent.updateSubTitle();
//			notifyItemChanged(getPositionForItem(parent));
//		} else {
//			//Assert parent is collapsed before removal
//			collapse(position);
//		}
//		super.removeItem(position);
//	}

	/**
	 * Before remove Items identified by selectedPositions, this method scan all immediate
	 * previous Items to retrieve parent position only in case of Child removal.
	 * <br/>If parent was found it is notified of the change by {@link #notifyItemChanged(int)}.
	 * <br/>If no children are selected, the scan is skipped and items are removed
	 * as usual calling {@link #removeItems(List)}.
	 *
	 * @param selectedPositions   list of item positions to remove
	 * @param notifyParentChanged true to Notify parent of a removal of the children
	 */
//	public void removeItems(List<Integer> selectedPositions, boolean notifyParentChanged) {
//		List<Item> parentsToNotify = new ArrayList<Item>();
//		if (notifyParentChanged) {
//			for (Integer position : selectedPositions) {
//				//Take only children: verify that is a child
//				Item item = getItem(position);
//				Item parent = getExpandableOf(item);
//				if (parent != null) {
//					//TODO: Save position
//					parent.removeSubItem(item);
//					//Assert to notify only once the parent
//					if (!parentsToNotify.contains(parent)) {
//						parentsToNotify.add(parent);
//					}
//				}
//			}
//		}
//		//Remove items as usual
//		super.removeItems(selectedPositions);
//		//Notify all identified parents of the removal of child items
//		Log.d(TAG, "Parents to notify " + parentsToNotify.size());
//		for (Item parent : parentsToNotify) {
//			parent.updateSubTitle();
//			Log.d(TAG, parent.getTitle() + " - " + parent.getSubtitle());
//			notifyItemChanged(getPositionForItem(parent));
//		}
//	}

	@Override
	public void removeItems(List<Integer> selectedPositions) {
		super.removeItems(selectedPositions);
		removeUserLearnedSelection();
	}

	@Override
	public boolean isEmpty() {
		return !DatabaseService.userLearnedSelection && mItems.size() == 1 || super.isEmpty();
	}

	@Override
	public int getItemViewType(int position) {
		return (position == 0 && !DatabaseService.userLearnedSelection
				&& !hasSearchText() ? EXAMPLE_VIEW_TYPE : super.getItemViewType(position));
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

		//ANIMATION EXAMPLE!! ImageView - Handle Flip Animation on Select and Deselect ALL
		if (mSelectAll || mLastItemInActionMode) {
			//Reset the flags with delay
			pvHolder.mImageView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mSelectAll = mLastItemInActionMode = false;
				}
			}, 200L);
			//Consume the Animation
			//flip(holder.mImageView, isSelected(position), 200L);
		} else {
			//Display the current flip status
			//setFlipped(holder.mImageView, isSelected(position));
		}

		//This "if-else" is just an example
		if (isSelected(position)) {
			pvHolder.mImageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.image_round_selected));
			animateView(holder.itemView, position, true);
		} else {
			pvHolder.mImageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.image_round_normal));
			animateView(holder.itemView, position, false);
		}

		pvHolder.mImageView.setImageResource(R.drawable.ic_account_circle_white_24dp);

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

				//ANIMATION EXAMPLE!! ImageView - Handle Flip Animation on Select and Deselect ALL
				if (mSelectAll || mLastItemInActionMode) {
					//Reset the flags with delay
//					cvHolder.mImageView.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							mSelectAll = mLastItemInActionMode = false;
//						}
//					}, 200L);
					//Consume the Animation
					//flip(holder.mImageView, isSelected(position), 200L);
				} else {
					//Display the current flip status
					//setFlipped(holder.mImageView, isSelected(position));
				}

				//This "if-else" is just an example
				if (isSelected(position)) {
//					cvHolder.mImageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.image_round_selected));
					animateView(holder.itemView, position, true);
				} else {
//					cvHolder.mImageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.image_round_normal));
					animateView(holder.itemView, position, false);
				}

//				cvHolder.mImageView.setImageResource(R.drawable.ic_account_circle_white_24dp);

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
		//Alpha Animator is needed (it will be added automatically if not here)
		addAlphaAnimator(animators, itemView, 0);

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

		return animators;
	}

	@Override
	public String getTextToShowInBubble(int position) {
		if (!DatabaseService.userLearnedSelection && position == 0) {//This 'if' is for my example only
			//TODO FOR YOU: This is the normal line you should use: Usually it's the first letter
			return getItem(position).getTitle().substring(0, 1).toUpperCase();
		}
		return super.getTextToShowInBubble(position);
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
	 * Custom filter.
	 *
	 * @param myObject   The item to filter
	 * @param constraint the current searchText
	 * @return true if a match exists in the title or in the subtitle, false if no match found.
	 */
	@Override
	protected boolean filterObject(Item myObject, String constraint) {
		String valueText = myObject.getTitle();
		//Filter on Title
		if (valueText != null && valueText.toLowerCase().contains(constraint)) {
			return true;
		}
		//Filter on Subtitle
		valueText = myObject.getSubtitle();
		return valueText != null && valueText.toLowerCase().contains(constraint);
	}

	/**
	 * Used for UserLearnsSelection.
	 */
	static class ExampleViewHolder extends FlexibleViewHolder {

		ImageView mImageView;
		TextView mTitle;
		TextView mSubtitle;
		ImageView mDismissIcon;

		ExampleViewHolder(View view, final ExampleAdapter adapter) {
			super(view, adapter, null);
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
		ImageView mImageView;
		TextView mTitle;
		TextView mSubtitle;
		Context mContext;

		public ParentViewHolder(View view, ExampleAdapter adapter) {
			super(view, adapter, adapter.mClickListener);
			this.mContext = adapter.mContext;
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			this.mImageView = (ImageView) view.findViewById(R.id.image);
			this.mImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListItemClickListener.onListItemLongClick(getAdapterPosition());
					toggleActivation();
				}
			});
		}

		@Override
		protected void toggleActivation() {
			super.toggleActivation();
			//This "if-else" is just an example
			if (itemView.isActivated()) {
				mImageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.image_round_selected));
			} else {
				mImageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.image_round_normal));
			}
			//Example of custom Animation inside the ItemView
			//flip(mImageView, itemView.isActivated());
		}
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	static final class ChildViewHolder extends FlexibleViewHolder {
//		ImageView mImageView;
		TextView mTitle;
		Context mContext;

		ChildViewHolder(View view, ExampleAdapter adapter) {
			super(view, adapter, adapter.mClickListener);
			this.mContext = adapter.mContext;
			this.mTitle = (TextView) view.findViewById(R.id.title);
//			this.mImageView = (ImageView) view.findViewById(R.id.image);
//			this.mImageView.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					mListItemClickListener.onListItemLongClick(getAdapterPosition());
//					toggleActivation();
//				}
//			});
		}

//		@Override
//		protected void toggleActivation() {
//			super.toggleActivation();
//			//This "if-else" is just an example
//			if (itemView.isActivated()) {
//				mImageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.image_round_selected));
//			} else {
//				mImageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.image_round_normal));
//			}
//			//Example of custom Animation inside the ItemView
//			//flip(mImageView, itemView.isActivated());
//		}
	}

	@Override
	public String toString() {
		return mItems.toString();
	}

}