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
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;

/**
 * Helper Class that implements:
 * <br/>- Single tap
 * <br/>- Long tap
 * <br/>- Touch for Drag and Swipe.
 * <p/>
 * You must extend and implement this class for the own ViewHolder.
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

	/* These 2 fields avoid double tactile feedback triggered by Android and allow to Drag an
	   item maintaining LongClick events for ActionMode, all at the same time */
	private int mActionState = ItemTouchHelper.ACTION_STATE_IDLE;
	private boolean mLongClickSkipped = false;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	/**
	 * Default constructor with no ClickListener or TouchListener.<p>
	 * <b>Note:</b> using this constructor, click events on the entire View will not have any effect.
	 *
	 * @param view    The {@link View} being hosted in this ViewHolder
	 * @param adapter Adapter instance of type {@link FlexibleAdapter}
	 */
	public FlexibleViewHolder(View view, FlexibleAdapter adapter) {
		this(view, adapter, null);
	}

	/**
	 * @param view                  The {@link View} being hosted in this ViewHolder
	 * @param adapter               Adapter instance of type {@link FlexibleAdapter}
	 * @param listItemClickListener ClickListener instance of type {@link OnListItemClickListener}
	 */
	public FlexibleViewHolder(View view, FlexibleAdapter adapter,
							  OnListItemClickListener listItemClickListener) {
		this(view, adapter, listItemClickListener, null);
	}

	/**
	 * @param view                  The {@link View} being hosted in this ViewHolder
	 * @param adapter               Adapter instance of type {@link FlexibleAdapter}
	 * @param listItemClickListener ClickListener instance of type {@link OnListItemClickListener}
	 * @param listItemTouchListener TouchListener instance of type {@link OnListItemTouchListener}
	 */
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

	/*--------------------------*/
	/* LISTENERS IMPLEMENTATION */
	/*--------------------------*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	@CallSuper
	public void onClick(View view) {
		//Experimented that, if LongClick is not consumed, onClick is fired. We skip the
		//call to the listener in this case, which is allowed only in ACTION_STATE_IDLE.
		if (mListItemClickListener != null && mActionState == ItemTouchHelper.ACTION_STATE_IDLE) {
			if (FlexibleAdapter.DEBUG)
				Log.v(TAG, "onClick on position " + getAdapterPosition() + " mode=" + mAdapter.getMode());
			if (mListItemClickListener.onListItemClick(getAdapterPosition()))
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
			if (FlexibleAdapter.DEBUG)
				Log.v(TAG, "onLongClick on position " + getAdapterPosition() + " mode=" + mAdapter.getMode());
			//Call the listener and activate View only in case Drag is disabled
			//If Drag is enabled, activation will be done in onItemTouched
			if (!mAdapter.isLongPressDragEnabled()) {
				mListItemClickListener.onListItemLongClick(getAdapterPosition());
				toggleActivation();
				return true;
			} else {
				mLongClickSkipped = true;
			}
		}
		return false;
	}

	/**
	 * <b>Should be used only by the Handle View!</b><br/>
	 * {@inheritDoc}
	 *
	 * @see #setDragHandleView(View)
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (FlexibleAdapter.DEBUG)
			Log.v(TAG, "onTouch with DragHandleView on position " + getAdapterPosition() + " mode=" + mAdapter.getMode());
		if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN &&
				mAdapter.isHandleDragEnabled()) {
			//Start Drag!
			mAdapter.getItemTouchHelper().startDrag(FlexibleViewHolder.this);
		}
		return false;
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	/**
	 * Sets the inner view which will be used to drag the Item ViewHolder.
	 *
	 * @param view handle view
	 * @see #onTouch(View, MotionEvent)
	 */
	protected final void setDragHandleView(@NonNull View view) {
		if (view != null) view.setOnTouchListener(this);
	}

	/**
	 * Allow to perform object animation in the ItemView and make [in]visible the selection on it.
	 * <p/>
	 * <b>IMPORTANT NOTE!</b> <i>setActivated</i> changes the selection color of the item
	 * background if you added<i>android:background="?attr/selectableItemBackground"</i>
	 * on the item layout AND in the style.xml.
	 * <p/>
	 * This must be called after the listener consumed the event in order to add the
	 * item number in the selection list.<br/>
	 * Adapter must have a reference to its instance to check selection state.
	 * <p/>
	 * If you do this, it's not necessary to invalidate the row (with notifyItemChanged): In this way
	 * <i>onBindViewHolder</i> is NOT called on selection and custom animations on objects are NOT interrupted,
	 * so you can SEE the animation in the Item and have the selection smooth with ripple.
	 */
	@CallSuper
	protected void toggleActivation() {
		itemView.setActivated(mAdapter.isSelected(getAdapterPosition()));
	}

	/**
	 * {@inheritDoc}<p>
	 * Selection and View activation is automatically handled depends the current Selection MODE.
	 */
	@Override
	@CallSuper
	public void onItemTouched(int position, int actionState) {
		mActionState = actionState;
		if (FlexibleAdapter.DEBUG)
			Log.v(TAG, "onItemTouched position=" + position + " mode=" + mAdapter.getMode() +
					" actionState=" + (actionState == ItemTouchHelper.ACTION_STATE_SWIPE ? "Swipe(1)" : "Drag(2)"));
		if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && !mAdapter.isSelected(getAdapterPosition())) {
			//Be sure, if MODE_MULTI is active, to add this item to the selection list (call listener)
			//Also be sure user consumes the long click event
			if ((mLongClickSkipped || mAdapter.getMode() == SelectableAdapter.MODE_MULTI) &&
					mListItemClickListener != null) {
				mLongClickSkipped = false;
				mListItemClickListener.onListItemLongClick(getAdapterPosition());
			} else {
				//If not, be sure current item appears selected
				mAdapter.toggleSelection(getAdapterPosition());
			}
		}
		//Activate view and make selection visible if necessary
		if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && !itemView.isActivated())
			toggleActivation();
		if (mListItemTouchListener != null) {
			mListItemTouchListener.onListItemTouch(position, actionState);
		}
	}

	/**
	 * {@inheritDoc}<p>
	 * Selection and View activation is automatically handled, selection on Release depends
	 * by current selection mode.
	 */
	@Override
	@CallSuper
	public void onItemReleased(int position) {
		if (FlexibleAdapter.DEBUG)
			Log.v(TAG, "onItemReleased position=" + position + " mode=" + mAdapter.getMode() +
					" actionState=" + (mActionState == ItemTouchHelper.ACTION_STATE_SWIPE ? "Swipe(1)" : "Drag(2)"));
		//Be sure to remove selection if not MODE_MULTI
		if (mAdapter.getMode() != FlexibleAdapter.MODE_MULTI) {
			if (mAdapter.isSelected(position)) {
				mAdapter.toggleSelection(position);
				toggleActivation();
			} else if (itemView.isActivated()) {
				toggleActivation();
			}
		}
		if (mListItemTouchListener != null) {
			mListItemTouchListener.onListItemRelease(position, mActionState);
		}
		mActionState = ItemTouchHelper.ACTION_STATE_IDLE;
	}

	/*------------------*/
	/* INNER INTERFACES */
	/*------------------*/

	/**
	 * @author Davide Steduto
	 * @since 03/01/2016
	 */
	public interface OnListItemClickListener {
		/**
		 * Called when single tap occurs.
		 * Delegate the click event to the listener and check if selection SINGLE or MULTI are
		 * enabled. If yes, call {@link #toggleActivation}.
		 *
		 * @param position the adapter position of the item touched
		 * @return true if MULTI selection is enabled, false for SINGLE selection and all others cases.
		 */
		boolean onListItemClick(int position);

		/**
		 * Called when long tap occurs.
		 * <p/>
		 * This method always calls {@link #toggleActivation} after listener event is consumed.
		 *
		 * @param position the adapter position of the item touched
		 */
		void onListItemLongClick(int position);
	}

	/**
	 * @author Davide Steduto
	 * @since 23/01/2016
	 */
	public interface OnListItemTouchListener {
		/**
		 * Called when the {@link ItemTouchHelper} first registers an item as being moved or swiped.
		 * Implementations should update the item view to indicate its active state.<p>
		 * {@link FlexibleViewHolder} class already provides an implementation to handle the
		 * active state.
		 *
		 * @param position    the adapter position of the item touched
		 * @param actionState one of {@link ItemTouchHelper#ACTION_STATE_SWIPE} or
		 *                    {@link ItemTouchHelper#ACTION_STATE_DRAG} or
		 *                    {@link ItemTouchHelper#ACTION_STATE_IDLE}.
		 * @see #onItemTouched(int, int)
		 */
		void onListItemTouch(int position, int actionState);

		/**
		 * Called when the {@link ItemTouchHelper} has completed the move or swipe, and the active
		 * item state should be cleared.<p>
		 * {@link FlexibleViewHolder} class already provides an implementation to disable the
		 * active state.
		 *
		 * @param position    the adapter position of the item touched
		 * @param actionState one of {@link ItemTouchHelper#ACTION_STATE_SWIPE} or
		 *                    {@link ItemTouchHelper#ACTION_STATE_DRAG} or
		 *                    {@link ItemTouchHelper#ACTION_STATE_IDLE}.
		 * @see #onItemReleased(int)
		 */
		void onListItemRelease(int position, int actionState);
	}

}