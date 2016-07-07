package eu.davidea.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * This Class separates the initialization of an eventual StickyHeader ViewHolder from a Normal
 * ViewHolder. It improves code readability of FlexibleViewHolder.
 *
 * @author Davide Steduto
 * @since 18/06/2016 Created
 */
abstract class ContentViewHolder extends RecyclerView.ViewHolder {

	private int mBackupPosition = RecyclerView.NO_POSITION;
	private View contentView;

	/**
	 * @param view         The {@link View} being hosted in this ViewHolder
	 * @param adapter      Adapter instance of type {@link FlexibleAdapter}
	 * @param stickyHeader true if the ViewHolder is a header to be sticky
	 * @since 5.0.0-b7
	 */
	public ContentViewHolder(View view, FlexibleAdapter adapter, boolean stickyHeader) {
		//Since itemView is declared "final", the split is done before the View is initialized
		super(stickyHeader ? new FrameLayout(view.getContext()) : view);

		if (stickyHeader) {
			itemView.setLayoutParams(adapter.getRecyclerView().getLayoutManager()
					.generateLayoutParams(view.getLayoutParams()));
			((FrameLayout) itemView).addView(view);//Add View after setLayoutParams
			contentView = view;
		}
	}

	/*-----------------------*/
	/* STICKY HEADER METHODS */
	/*-----------------------*/

	/**
	 * In case this ViewHolder represents a Header Item, this method returns the contentView of the
	 * FrameLayout, otherwise it returns the basic itemView.
	 *
	 * @return the real contentView
	 * @since 5.0.0-b7
	 */
	public View getContentView() {
		return contentView != null ? contentView : itemView;
	}

	/**
	 * Overcomes the situation of returning an unknown position (-1) of ViewHolders created out of
	 * the LayoutManager (ex. StickyHeaders).
	 * <p><b>NOTE:</b> Always call this method, instead of {@code getAdapterPosition()}, in case
	 * of StickyHeaders use case.</p>
	 *
	 * @return the Adapter position result of {@link #getAdapterPosition()} OR the backup position
	 * preset and known, if the previous result was {@link RecyclerView#NO_POSITION}.
	 * @see #setBackupPosition(int)
	 * @since 5.0.0-b6
	 */
	public int getFlexibleAdapterPosition() {
		int position = getAdapterPosition();
		if (position == RecyclerView.NO_POSITION) {
			position = mBackupPosition;
		}
		return position;
	}

	/**
	 * Restore the Adapter position if the original Adapter position is unknown.
	 * <p>Called by StickyHeaderHelper to support the clickListeners events.</p>
	 *
	 * @param backupPosition the known position of this ViewHolder
	 * @since 5.0.0-b6
	 */
	public void setBackupPosition(int backupPosition) {
		mBackupPosition = backupPosition;
	}

}