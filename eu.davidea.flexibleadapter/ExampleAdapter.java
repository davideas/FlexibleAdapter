package eu.davidea.flexibleadapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class ExampleAdapter extends FlexibleAdapter<ManageLabelsAdapter.SimpleViewHolder, Label> {

	private static final String TAG = ManageLabelsAdapter.class.getSimpleName();
	
	public interface OnItemClickListener {
		boolean onListItemClick(int position);
		void onListItemLongClick(int position);
	}
	
	private static final int
			EXAMPLE_VIEW_TYPE = 0,
			ROW_VIEW_TYPE = 1;
	private LayoutInflater mInflater;
	private OnItemClickListener mClickListener;
		//Selection fields
	private boolean
			mUserLearnedSelection = true,
			mLastItemInActionMode = false,
			mSelectAll = false;
    
	public ManageLabelsAdapter(OnItemClickListener listener) {
		this.mClickListener = listener;
		updateDataSet();
	}
	
	public void updateDataSet() {
		//Fill mItems with your custom list
		//this.mItems = DatabaseService.getInstance().getUserLabelsList();
		if (!mUserLearnedSelection && mItems.size() > 0) {
			//Define your Example View
			//...
			//Then
			this.mItems.add(0, item);
		}
	}
	
	public Label getItem(int position) {
		return mItems.get(position);
	}
	
	@Override
	public void setMode(int mode) {
		super.setMode(mode);
		if (mode == MODE_SINGLE) mLastItemInActionMode = true;
	}
	
	@Override
	public void selectAll() {
		mSelectAll = true;
		super.selectAll();
	}

	@Override
	public int getItemViewType(int position) {
		return (position == 0 && !mUserLearnedSelection ? EXAMPLE_VIEW_TYPE : ROW_VIEW_TYPE);
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
						mInflater.inflate(R.layout.item_list_uls_row, parent, false),
						this);
			default:
				return new ViewHolder(
						mInflater.inflate(R.layout.item_list_recycler_row, parent, false),
						this);
		}
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		Log.d(TAG, "onBindViewHolder for position "+position);
		final Label label = mItems.get(position);
		
		//NOTE: ViewType Must be checked also here to bind the correct view
		
		//When user scrolls this bind the correct selection status
		holder.itemView.setActivated(isSelected(position));
		
		holder.mImageView.setImageResource(label.getIconResource()!=null?label.getIconResource():R.drawable.ic_label_grey600_24dp);
		holder.mTextView.setText(label.getName());
		holder.mCounterView.setText(String.valueOf(label.getCounter()));
	}
	
	/**
	 * Used for UserLearnsSelection.
	 * Must be the base class of extension for Adapter Class.
	 */
	static class SimpleViewHolder extends RecyclerView.ViewHolder {

		SymbolView mSymbolView;
		TextView mTitle;
		TextView mSubtitle;
		ImageView mFavIcon;
		ItemListRecyclerAdapter mAdapter;

		SimpleViewHolder(View view) {
			super(view);
		}

		SimpleViewHolder(View view, ItemListRecyclerAdapter adapter) {
			super(view);
			mAdapter = adapter;
			mTitle = (TextView) view.findViewById(R.id.name);
			mSymbolView = (SymbolView) view.findViewById(R.id.symbol);
			mFavIcon = (ImageView) view.findViewById(R.id.dismiss_icon);
			mFavIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					SettingsService.getInstance().setUserLearnedSelection(true);
					mAdapter.mUserLearnedSelection = true;
					mAdapter.removeItem(0);
				}
			});
		}
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	static final class ViewHolder extends SimpleViewHolder implements View.OnClickListener,
			View.OnLongClickListener {
			View.OnLongClickListener {
		
		ImageView mImageView;
		TextView mTextView;
		TextView mCounterView;
		
		ViewHolder(View view, final ItemListRecyclerAdapter adapter) {
			super(view);

			this.mAdapter = adapter;
			this.mTitle = (TextView) view.findViewById(R.id.name);
			this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			this.mFavIcon = (ImageView) view.findViewById(R.id.fav_icon);

			mSymbolView = (SymbolView) view.findViewById(R.id.symbol);
			mSymbolView.setOnClickListener(new View.OnClickListener() {
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
		 * Perform animation and selection on the current ItemView.<br/>
		 * This must be called after the listener consumed the event.<br/>
		 * Adapter must have a reference to its instance to check selection state.
		 */
		private void toggleActivation() {
			itemView.setActivated(mAdapter.isSelected(getAdapterPosition()));
			//Example of custom Animation inside the ItemView
			//mSymbolView.flip(itemView.isActivated(), 0L);
		}

		@Override
		public void onClick(View view) {
			if (mAdapter.mClickListener.onListItemClick(getAdapterPosition()))
				toggleActivation();
		}
		
		@Override
		public boolean onLongClick(View view) {
			mAdapter.mClickListener.onListItemLongClick(getAdapterPosition());
			toggleActivation();
			return true;
		}

}