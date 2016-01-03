package eu.davidea.examples.flexibleadapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
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

import java.util.List;
import java.util.Locale;

import eu.davidea.examples.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.utils.Utils;


public class ExampleAdapter extends FlexibleAdapter<ExampleAdapter.SimpleViewHolder, Item>
		implements FastScroller.BubbleTextGetter {

	private static final String TAG = ExampleAdapter.class.getSimpleName();

	public interface OnItemClickListener {
		/**
		 * Delegate the click event to the listener and check if selection MULTI enabled.<br/>
		 * If yes, call toggleActivation.
		 *
		 * @param position
		 * @return true if MULTI selection is enabled, false for SINGLE selection
		 */
		boolean onListItemClick(int position);

		/**
		 * This always calls toggleActivation after listener event is consumed.
		 *
		 * @param position
		 */
		void onListItemLongClick(int position);
	}

	private Context mContext;
	private static final int
			EXAMPLE_VIEW_TYPE = 0,
			ROW_VIEW_TYPE = 1;

	private LayoutInflater mInflater;
	private OnItemClickListener mClickListener;

	//Selection fields
	private boolean
			mLastItemInActionMode = false,
			mSelectAll = false;

	public ExampleAdapter(Object activity, String listId) {
		super(DatabaseService.getInstance().getListById(listId), activity);
		this.mContext = (Context) activity;
		this.mClickListener = (OnItemClickListener) activity;
		if (!isEmpty()) addUserLearnedSelection();
	}

	private void addUserLearnedSelection() {
		if (!DatabaseService.userLearnedSelection && !hasSearchText()) {
			//Define Example View
			Item item = new Item();
			item.setId(0);
			item.setTitle(mContext.getString(R.string.uls_title));
			item.setSubtitle(mContext.getString(R.string.uls_subtitle));
			mItems.add(0, item);
		}
	}

	private void removeUserLearnedSelection() {
		Log.d(TAG, "removed UserLearnedSelection isEmpty=" + isEmpty());
		if (!DatabaseService.userLearnedSelection && isEmpty()) {
			mItems.remove(0);
			notifyItemRemoved(0);
		}
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

		//Fill and Filter mItems with your custom list
		//Note: In case of userLearnSelection mItems is pre-initialized and after filtered.
		filterItems(mItems);
		notifyDataSetChanged();

		//Update Empty View
		mUpdateListener.onUpdateEmptyView(mItems.size());
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
				&& !hasSearchText() ? EXAMPLE_VIEW_TYPE : ROW_VIEW_TYPE);
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Log.d(TAG, "onCreateViewHolder for viewType " + viewType);
		if (mInflater == null) {
			mInflater = LayoutInflater.from(parent.getContext());
		}
		switch (viewType) {
			case EXAMPLE_VIEW_TYPE:
				return new SimpleViewHolder(
						mInflater.inflate(R.layout.recycler_uls_row, parent, false),
						this);
			default:
				return new ViewHolder(
						mInflater.inflate(R.layout.recycler_row, parent, false),
						this);
		}
	}

	@Override
	public void onBindViewHolder(SimpleViewHolder holder, final int position) {
		Log.d(TAG, "onBindViewHolder for position " + position);
		final Item item = getItem(position);

		holder.mImageView.setImageResource(R.drawable.ic_account_circle_white_24dp);

		//NOTE: ViewType Must be checked ALSO here to bind the correct view
		if (getItemViewType(position) == EXAMPLE_VIEW_TYPE) {
			holder.itemView.setActivated(true);
			holder.mTitle.setSelected(true);//For marquee
			holder.mTitle.setText(Html.fromHtml(item.getTitle()));
			holder.mSubtitle.setText(Html.fromHtml(item.getSubtitle()));
			//IMPORTANT: Example View finishes here!!
			return;
		}

		//When user scrolls this bind the correct selection status
		holder.itemView.setActivated(isSelected(position));

		//ANIMATION EXAMPLE!! ImageView - Handle Flip Animation on Select and Deselect ALL
		if (mSelectAll || mLastItemInActionMode) {
			//Reset the flags with delay
			holder.mImageView.postDelayed(new Runnable() {
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
			holder.mImageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.image_round_selected));
		} else {
			holder.mImageView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.image_round_normal));
		}

		//In case of searchText matches with Title or with an Item's field
		// this will be highlighted
		if (hasSearchText()) {
			setHighlightText(holder.mTitle, item.getTitle(), mSearchText);
			setHighlightText(holder.mSubtitle, item.getSubtitle(), mSearchText);
		} else {
			holder.mTitle.setText(item.getTitle());
			holder.mSubtitle.setText(item.getSubtitle());
		}
	}

	@Override
	public String getTextToShowInBubble(int position) {
		if (!DatabaseService.userLearnedSelection && position == 0) {//This 'if' is for my example only
			//This is the normal line you should use: Usually it's the first letter
			return getItem(position).getTitle().substring(0, 1).toUpperCase();
		}
		return getItem(position).getTitle().substring(5); //This is for my example only
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
	 * Must be the base class of extension for Adapter Class.
	 */
	static class SimpleViewHolder extends RecyclerView.ViewHolder {

		ImageView mImageView;
		TextView mTitle;
		TextView mSubtitle;
		ImageView mDismissIcon;
		ExampleAdapter mAdapter;

		SimpleViewHolder(View view) {
			super(view);
		}

		SimpleViewHolder(View view, ExampleAdapter adapter) {
			super(view);
			mAdapter = adapter;
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			mImageView = (ImageView) view.findViewById(R.id.image);
			mDismissIcon = (ImageView) view.findViewById(R.id.dismiss_icon);
			mDismissIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAdapter.userLearnedSelection();
				}
			});
		}
	}

	private void userLearnedSelection() {
		//TODO: Save the boolean into Settings!
		DatabaseService.userLearnedSelection = true;
		mItems.remove(0);
		notifyItemRemoved(0);
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	static final class ViewHolder extends SimpleViewHolder implements View.OnClickListener,
			View.OnLongClickListener {

		ViewHolder(View view, final ExampleAdapter adapter) {
			super(view);

			this.mAdapter = adapter;
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			this.mImageView = (ImageView) view.findViewById(R.id.image);
			this.mImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAdapter.mClickListener.onListItemLongClick(getAdapterPosition());
					toggleActivation();
				}
			});

			this.itemView.setOnClickListener(this);
			this.itemView.setOnLongClickListener(this);
		}

		/**
		 * Perform animation and selection on the current ItemView.
		 * <br/><br/>
		 * <b>IMPORTANT NOTE!</b> <i>setActivated</i> changes the selection color of the item
		 * background if you added<i>android:background="?attr/selectableItemBackground"</i>
		 * on the row layout AND in the style.xml.
		 * <br/><br/>
		 * This must be called after the listener consumed the event in order to add the
		 * item number in the selection list.<br/>
		 * Adapter must have a reference to its instance to check selection state.
		 * <br/><br/>
		 * If you do this, it's not necessary to invalidate the row (with notifyItemChanged): In this way
		 * <i>onBindViewHolder</i> is NOT called on selection and custom animations on objects are NOT interrupted,
		 * so you can SEE the animation in the Item and have the selection smooth with ripple.
		 */
		private void toggleActivation() {
			itemView.setActivated(mAdapter.isSelected(getAdapterPosition()));
			//This "if-else" is just an example
			if (itemView.isActivated()) {
				mImageView.setBackgroundDrawable(mAdapter.mContext.getResources().getDrawable(R.drawable.image_round_selected));
			} else {
				mImageView.setBackgroundDrawable(mAdapter.mContext.getResources().getDrawable(R.drawable.image_round_normal));
			}
			//Example of custom Animation inside the ItemView
			//flip(mImageView, itemView.isActivated());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onClick(View view) {
			if (mAdapter.mClickListener.onListItemClick(getAdapterPosition()))
				toggleActivation();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean onLongClick(View view) {
			mAdapter.mClickListener.onListItemLongClick(getAdapterPosition());
			toggleActivation();
			return true;
		}
	}

	@Override
	public String toString() {
		return mItems.toString();
	}

}