package eu.davidea.viewholders;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;

/**
 * Helper Class that implements the single Tap and Long Tap.<br/>
 * Must be extended for the own ViewHolder.
 *
 * @author Davide Steduto
 * @since 03/01/2016 Created<br/>23/01/2016 ItemTouch
 */
public abstract class FlexibleViewHolder extends RecyclerView.ViewHolder
		implements View.OnClickListener, View.OnLongClickListener,
		View.OnTouchListener, ItemTouchHelperCallback.ViewHolderCallback {

	private static final String TAG = FlexibleViewHolder.class.getSimpleName();

	protected final FlexibleAdapter mAdapter;
	protected final OnListItemClickListener mListItemClickListener;
	protected final OnListItemTouchListener mListItemTouchListener;

	public FlexibleViewHolder(View view, FlexibleAdapter adapter) {
		this(view, adapter, null);
	}

	public FlexibleViewHolder(View view, FlexibleAdapter adapter,
							  OnListItemClickListener listItemClickListener) {
		this(view, adapter, listItemClickListener, null);
	}

	public FlexibleViewHolder(View view, FlexibleAdapter adapter,
							  OnListItemClickListener listItemClickListener,
							  OnListItemTouchListener listItemTouchListener) {
		super(view);
		this.mAdapter = adapter;
		this.mListItemClickListener = listItemClickListener;
		this.mListItemTouchListener = listItemTouchListener;
		if (this.itemView.isEnabled()) {
			this.itemView.setOnClickListener(this);
			this.itemView.setOnLongClickListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@CallSuper
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
	@CallSuper
	public boolean onLongClick(View view) {
		if (mListItemClickListener != null) {
			mListItemClickListener.onListItemLongClick(getAdapterPosition());
			toggleActivation();
			return true;
		}
		return false;
	}

	/**
	 * Set the view which will be used to drag the Item ViewHolder.
	 *
	 * @param view handle view
	 * @see #onTouch(View, MotionEvent)
	 */
	public final void setDragHandleView(@NonNull View view) {
		if (view != null) view.setOnTouchListener(this);
	}

	/**
	 * <b>Used only by the Handle View!</b><br/>
	 * {@inheritDoc}
	 *
	 * @see #setDragHandleView(View)
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN &&
				mAdapter.isHandleDragEnabled()) {
			mAdapter.getItemTouchHelper().startDrag(FlexibleViewHolder.this);
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
	@CallSuper
	protected void toggleActivation() {
		itemView.setActivated(mAdapter.isSelected(getAdapterPosition()));
	}

	@Override
	@CallSuper
	public void onItemTouched(int position, int actionState) {
		if (FlexibleAdapter.DEBUG)
			Log.d(TAG, "onItemTouched position=" + position + " actionState=" +
					(actionState == ItemTouchHelper.ACTION_STATE_SWIPE ? "Swipe(1)" : "Drag(2)"));
		if (mListItemTouchListener != null) {
			mListItemTouchListener.onListItemTouch(position, actionState);
		}
	}

	@Override
	@CallSuper
	public void onItemReleased(int position) {
		if (FlexibleAdapter.DEBUG)
			Log.d(TAG, "onItemReleased position=" + position);
		//Log.d("FlexibleViewHolder", "onItemReleased isSelected=" + mAdapter.isSelected(position) + " item=" + mAdapter.getItem(position));
		//mAdapter.toggleSelection(position);
		toggleActivation();
		if (mListItemTouchListener != null) {
			mListItemTouchListener.onListItemRelease(position);
		}
	}

	/*------------------*/
	/* INNER INTERFACES */
	/*------------------*/

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

	public interface OnListItemTouchListener {
		/**
		 * @param position    the position of the item touched
		 * @param actionState one of {@link ItemTouchHelper#ACTION_STATE_SWIPE} or
		 *                    {@link ItemTouchHelper#ACTION_STATE_DRAG}.
		 */
		void onListItemTouch(int position, int actionState);

		/**
		 * @param position Adapter position
		 */
		void onListItemRelease(int position);
	}

}