package eu.davidea.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * Helper Class that implements the single Tap and Long Tap.<br/>
 * Must be extended for the own ViewHolder.
 *
 * @author Davide Steduto
 * @since 03/01/2016
 */
public abstract class FlexibleViewHolder extends RecyclerView.ViewHolder
		implements View.OnClickListener, View.OnLongClickListener {

	protected final FlexibleAdapter mAdapter;
	protected final OnListItemClickListener mListItemClickListener;

	public FlexibleViewHolder(View view, FlexibleAdapter adapter,
							  OnListItemClickListener listItemClickListener) {
		super(view);
		this.mAdapter = adapter;
		this.mListItemClickListener = listItemClickListener;
		if (this.itemView.isEnabled()) {
			this.itemView.setOnClickListener(this);
			this.itemView.setOnLongClickListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onClick(View view) {
		if (mListItemClickListener != null &&
				mListItemClickListener.onListItemClick(getAdapterPosition())) {
			toggleActivation();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onLongClick(View view) {
		if (mListItemClickListener != null) {
			mListItemClickListener.onListItemLongClick(getAdapterPosition());
			toggleActivation();
			return true;
		}
		return false;
	}

	/**
	 * Allow to perform object animation in the ItemView and selection on it.
	 * <br/><br/>
	 * <b>IMPORTANT NOTE!</b> <i>setActivated</i> changes the selection color of the item
	 * background if you added<i>android:background="?attr/selectableItemBackground"</i>
	 * on the item layout AND in the style.xml.
	 * <br/><br/>
	 * This must be called after the listener consumed the event in order to add the
	 * item number in the selection list.<br/>
	 * Adapter must have a reference to its instance to check selection state.
	 * <br/><br/>
	 * If you do this, it's not necessary to invalidate the row (with notifyItemChanged): In this way
	 * <i>onBindViewHolder</i> is NOT called on selection and custom animations on objects are NOT interrupted,
	 * so you can SEE the animation in the Item and have the selection smooth with ripple.
	 */
	protected void toggleActivation() {
		itemView.setActivated(mAdapter.isSelected(getAdapterPosition()));
	}

	public interface OnListItemClickListener {
		/**
		 * Delegate the click event to the listener and check if selection MULTI is enabled.<br/>
		 * If yes, call toggleActivation.
		 *
		 * @param position Adapter position
		 * @return true if MULTI selection is enabled, false for SINGLE selection and all others cases.
		 */
		boolean onListItemClick(int position);

		/**
		 * This always calls toggleActivation after listener event is consumed.
		 *
		 * @param position Adapter position
		 */
		void onListItemLongClick(int position);
	}

}