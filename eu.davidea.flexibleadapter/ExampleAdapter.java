package eu.davidea.flexibleadapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class ExampleAdapter extends FlexibleAdapter<ManageLabelsAdapter.ViewHolder, Label> {

	private static final String TAG = ManageLabelsAdapter.class.getSimpleName();
	
	public interface OnItemClickListener {
		public void onListItemClick(int position);
		public boolean onListItemLongClick(int position);
	}
	
	private LayoutInflater mInflater;
	
	private OnItemClickListener mClickListener;
    
	public ManageLabelsAdapter(OnItemClickListener listener) {
		this.mClickListener = listener;
		updateDataSet();
	}
	
	public void updateDataSet() {
		this.mItems = DatabaseService.getInstance().getUserLabelsList();
	}
	
	public Label getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Log.d(TAG, "onCreateViewHolder for viewType "+viewType);
		if (mInflater == null) {
			mInflater = LayoutInflater.from(parent.getContext());
		}
		//NOTE: Example for multiple layout. getItemViewType needs to be Overridden
		//final int layout = viewType == XXX ? R.layout.item : R.layout.item2;
		
		View view = mInflater.inflate(R.layout.drawer_list_row, parent, false);
		//Eventually set the view's size, margins, paddings and layout parameters
		return new ViewHolder(view, mClickListener);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		Log.d(TAG, "onBindViewHolder for position "+position);
		final Label label = mItems.get(position);
		//TODO: Set the proper Icon with tint
		holder.mImageView.setImageResource(label.getIconResource()!=null?label.getIconResource():R.drawable.ic_label_grey600_24dp);
		holder.mTextView.setText(label.getName());
		holder.mCounterView.setText(String.valueOf(label.getCounter()));
		holder.itemView.setActivated(isSelected(position));
	}
	
	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	static final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
			View.OnLongClickListener {
		
		ImageView mImageView;
		TextView mTextView;
		TextView mCounterView;
		private OnItemClickListener mClickListener;
		
		public ViewHolder(View view, OnItemClickListener listener) {
			super(view);
			mClickListener = listener;
			
			this.mImageView = (ImageView) view.findViewById(R.id.icon);
	        this.mTextView = (TextView) view.findViewById(R.id.name);
	        this.mCounterView = (TextView) view.findViewById(R.id.counter);
	        
	        this.itemView.setOnClickListener(this);
			this.itemView.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View view) {
			mClickListener.onListItemClick(getAdapterPosition());
		}
		
		@Override
		public boolean onLongClick(View view) {
			return mClickListener.onListItemLongClick(getAdapterPosition());
		}

	}

}