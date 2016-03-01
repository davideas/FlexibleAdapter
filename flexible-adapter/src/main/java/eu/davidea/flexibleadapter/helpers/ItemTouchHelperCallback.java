/*
 * Copyright 2016 Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.flexibleadapter.helpers;

import android.graphics.Canvas;
import android.support.annotation.FloatRange;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;

import eu.davidea.flexibleadapter.FlexibleAdapter.OnItemMoveListener;
import eu.davidea.flexibleadapter.FlexibleAdapter.OnItemSwipeListener;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This class is an implementation of {@link Callback} that enables drag & drop
 * and swipe actions. Drag and Swipe events are started depending by its configuration.
 *
 * @author Davide Steduto
 * @since 23/01/2016 Created
 */
public class ItemTouchHelperCallback extends Callback {

	private static final float ALPHA_FULL = 1.0f;

	private AdapterCallback mItemTouchCallback;
	private boolean mIsLongPressDragEnabled = false;
	private boolean mIsSwipeEnabled = false;
	private float mSwipeThreshold = 1.0f;
	private int mSwipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

	/*-------------*/
	/* CONSTRUCTOR */
	/*-------------*/

	public ItemTouchHelperCallback(AdapterCallback itemTouchCallback) {
		this.mItemTouchCallback = itemTouchCallback;
	}

	/*-----------------------*/
	/* CONFIGURATION SETTERS */
	/*-----------------------*/
	/* DRAG */

	public void setLongPressDragEnabled(boolean isLongPressDragEnabled) {
		this.mIsLongPressDragEnabled = isLongPressDragEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLongPressDragEnabled() {
		return mIsLongPressDragEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
		return super.canDropOver(recyclerView, current, target);
	}

	/* SWIPE */

	/**
	 * Enable the swipe operation on the ViewHolder.<p>
	 * Default value is false.
	 *
	 * @param isSwipeEnabled true to enable swipe, false to disable
	 */
	public void setSwipeEnabled(boolean isSwipeEnabled) {
		this.mIsSwipeEnabled = isSwipeEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isItemViewSwipeEnabled() {
		return mIsSwipeEnabled;
	}

	/**
	 * Configures the fraction that the user should move the View to be considered as swiped.
	 *
	 * @param threshold A float value that denotes the fraction of the View size.
	 *                  Default value is 1.0f
	 */
	public void setSwipeThreshold(@FloatRange(from = 0.0, to = 1.0) float threshold) {
		this.mSwipeThreshold = threshold;
	}

	/**
	 * Configures the directions in which the item can be swiped. Values:
	 * <p>{@link ItemTouchHelper#START} | {@link ItemTouchHelper#END} |
	 * {@link ItemTouchHelper#UP} | {@link ItemTouchHelper#DOWN}</p>
	 * Default values are {@link ItemTouchHelper#START} and {@link ItemTouchHelper#END}.
	 *
	 * @param swipeFlags flags directions
	 */
	public void setSwipeFlags(int swipeFlags) {
		this.mSwipeFlags = swipeFlags;
	}

	/**
	 * Returns the fraction that the user should move the View to be considered as swiped.
	 * The fraction is calculated with respect to RecyclerView's bounds.
	 * <p>Default value is 1.0f, which means, to swipe a View, user must move the View at least
	 * half of RecyclerView's width or height, depending on the swipe direction.</p>
	 *
	 * @param viewHolder The ViewHolder that is being dragged.
	 * @return A float value that denotes the fraction of the View size. Default value is 1.0f
	 */
	@Override
	public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
		return mSwipeThreshold;
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
		if (!mItemTouchCallback.shouldMove(viewHolder.getAdapterPosition(), target.getAdapterPosition())) {
			return false;
		}
		//Notify the adapter of the move
		mItemTouchCallback.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		// Notify the adapter of the swipe dismissal
		mItemTouchCallback.onItemSwiped(viewHolder.getAdapterPosition(), direction);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		//Set movement flags based on the layout manager
		if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
			final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
			final int swipeFlags = 0;
			return makeMovementFlags(dragFlags, swipeFlags);
		} else {
			final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
			final int swipeFlags = mSwipeFlags;
			return makeMovementFlags(dragFlags, swipeFlags);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
		//We only want the active item to change
		if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
			if (viewHolder instanceof ViewHolderCallback) {
				//Let the ViewHolder to know that this item is swiping or dragging
				ViewHolderCallback viewHolderCallback = (ViewHolderCallback) viewHolder;
				viewHolderCallback.onActionStateChanged(viewHolder.getAdapterPosition(), actionState);
			}
		}
		super.onSelectedChanged(viewHolder, actionState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		super.clearView(recyclerView, viewHolder);
		//Force full Alpha
		viewHolder.itemView.setAlpha(ALPHA_FULL);
		if (viewHolder instanceof ViewHolderCallback) {
			//Tell the view holder it's time to restore the idle state
			ViewHolderCallback viewHolderCallback = (ViewHolderCallback) viewHolder;
			viewHolderCallback.onItemReleased(viewHolder.getAdapterPosition());
		}
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
//							float dX, float dY, int actionState, boolean isCurrentlyActive) {
//		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
//								float dX, float dY, int actionState, boolean isCurrentlyActive) {
//		super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//	}

	/*------------------*/
	/* INNER INTERFACES */
	/*------------------*/

	/**
	 * Internal Interface for Adapter to listen for a move or swipe dismissal event
	 * from a {@link ItemTouchHelperCallback}.
	 *
	 * @since 23/01/2016
	 */
	public interface AdapterCallback {
		/**
		 * Evaluate if positions are compatible for moving.
		 *
		 * @param fromPosition the start position of the moved item
		 * @param toPosition   the resolved position of the moved item
		 * @return true if the item is allowed to moved to the new adapter position
		 */
		boolean shouldMove(int fromPosition, int toPosition);

		/**
		 * Called when an item has been dragged far enough to trigger a move. <b>This is called
		 * every time an item is shifted</b>, and <strong>not</strong> at the end of a "drop" event.
		 * <p>Implementations should call {@link Adapter#notifyItemMoved(int, int)}
		 * after adjusting the underlying data to reflect this move.</p>
		 *
		 * @param fromPosition the start position of the moved item
		 * @param toPosition   the resolved position of the moved item
		 * @return true if the item was moved to the new adapter position
		 */
		boolean onItemMove(int fromPosition, int toPosition);

		/**
		 * Called when an item has been dismissed by a swipe.
		 * <p>Implementations should decide to call or not {@link Adapter#notifyItemRemoved(int)}
		 * after adjusting the underlying data to reflect this removal.</p>
		 *
		 * @param position  the position of the item dismissed
		 * @param direction the direction to which the ViewHolder is swiped
		 */
		void onItemSwiped(int position, int direction);
	}

	/**
	 * Internal Interface for ViewHolder to notify of relevant callbacks from
	 * {@link Callback}.<br/>
	 * This listener, is to intend as a further way to display How a ViewHolder will display
	 * the middle and final activation state.
	 * <p>Generally the final action should be handled by the listeners
	 * {@link OnItemMoveListener} and {@link OnItemSwipeListener}.</p>
	 *
	 * @since 23/01/2016
	 */
	public interface ViewHolderCallback {
		/**
		 * Called when the {@link ItemTouchHelper} first registers an item as being moved or swiped.
		 * <br/>Implementations should update the item view to indicate it's active state.
		 * <p>{@link FlexibleViewHolder} class already provides an implementation to handle the
		 * active state.</p>
		 *
		 * @param position    the position of the item touched
		 * @param actionState one of {@link ItemTouchHelper#ACTION_STATE_SWIPE} or
		 *                    {@link ItemTouchHelper#ACTION_STATE_DRAG}.
		 * @see FlexibleViewHolder#onActionStateChanged(int, int)
		 */
		void onActionStateChanged(int position, int actionState);

		/**
		 * Called when the {@link ItemTouchHelper} has completed the move or swipe, and the active
		 * item state should be cleared.
		 * <p>{@link FlexibleViewHolder} class already provides an implementation to disable the
		 * active state.</p>
		 *
		 * @param position the position of the item released
		 */
		void onItemReleased(int position);
	}

}