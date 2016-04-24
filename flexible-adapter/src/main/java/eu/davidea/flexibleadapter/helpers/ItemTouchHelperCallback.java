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

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.util.Log;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.FlexibleAdapter.OnItemMoveListener;
import eu.davidea.flexibleadapter.FlexibleAdapter.OnItemSwipeListener;
import eu.davidea.viewholders.FlexibleViewHolder;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * This class is an implementation of {@link Callback} that enables drag & drop
 * and swipe actions. Drag and Swipe events are started depending by its configuration.
 * TODO: http://cjainewbw.blogspot.be/2015/06/design-support-library-recyclerview.html
 *
 * @author Davide Steduto
 * @since 23/01/2016 Created
 */
public class ItemTouchHelperCallback extends Callback {

	public static final int IDLE = 0;
	public static final int SWIPING = 1 << 1;
	public static final int PARTIAL_SWIPE = 1 << 2;
	public static final int FULL_SWIPE = 1 << 3;

	private static final String TAG = ItemTouchHelperCallback.class.getSimpleName();
	private static final float ALPHA_FULL = 1.0f;

	private AdapterCallback mItemTouchCallback;
	private boolean mIsLongPressDragEnabled = false;
	private boolean mIsSwipeEnabled = false;
	private float mSwipeThreshold = 0.5f, mTempThreshold = 0.5f, oldAmount = 0;
	private int mSwipeFlags = -1;
	private int mMargin = -1;

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
	 * Enable the swipe operation on the ViewHolder.
	 * <p>Default value is false.</p>
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
	 * <p>Default value is 0.5f.</p>
	 *
	 * @param threshold A float value that denotes the fraction of the View size.
	 */
	public void setSwipeThreshold(@FloatRange(from = 0.0, to = 1.0) float threshold) {
		this.mSwipeThreshold = threshold;
		this.mTempThreshold = threshold;
	}

	/**
	 * Configures the directions in which the item can be swiped.
	 * <p>Default values are {@link ItemTouchHelper#LEFT}, {@link ItemTouchHelper#RIGHT}
	 * for VERTICAL LinearLayout.</p>
	 * <b>NOTE:</b> Swipe is not supported in case of GridLayout and StaggeredGridLayout.
	 *
	 * @param swipeFlags flags directions, a combination of:
	 *                   {@link ItemTouchHelper#LEFT}, {@link ItemTouchHelper#RIGHT},
	 *                   {@link ItemTouchHelper#UP}, {@link ItemTouchHelper#DOWN}
	 */
	public void setSwipeFlags(int swipeFlags) {
		this.mSwipeFlags = swipeFlags;
	}

	/**
	 * Returns the fraction that the user should move the View to be considered as swiped.
	 * The fraction is calculated with respect to RecyclerView's bounds.
	 * <p>Default value is 0.5f, which means, to swipe a View, user must move the View at least
	 * half of RecyclerView's width or height, depending on the swipe direction.</p>
	 *
	 * @param viewHolder The ViewHolder that is being dragged.
	 * @return A float value that denotes the fraction of the View size.
	 */
	@Override
	public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
		return mTempThreshold;
	}

	/**
	 * Configures the amount of Partial Swipe of the items depending by the specified margin.
	 *
	 * @param context the context
	 * @param dp      max margin for a partial swipe (in dpi)
	 */
	public void setSwipeMaxMarginDp(Context context, @IntRange(from = 1) int dp) {
		setSwipeMaxMarginPx((int) (context.getResources().getDisplayMetrics().density * dp));
	}

	/**
	 * @see #setSwipeMaxMarginDp(Context, int)
	 */
	public void setSwipeMaxMarginPx(int pixel) {
		mMargin = pixel;
	}

	/**
	 * Returns the max margin for partial swiping.
	 *
	 * @return max margin for partial swiping in pixel
	 */
	public int getSwipeMaxMarginPx() {
		return mMargin;
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
		//Notify the adapter of the swipe dismissal
		if (viewHolder instanceof ViewHolderCallback) {
			int status = ((ViewHolderCallback) viewHolder).getSwipeStatus();
			mItemTouchCallback.onItemSwiped(viewHolder.getAdapterPosition(), direction, status);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		int dragFlags;
		int swipeFlags;
		//Set movement flags based on the Layout Manager and Orientation (if linear)
		if (layoutManager instanceof GridLayoutManager || layoutManager instanceof StaggeredGridLayoutManager) {
			dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
			swipeFlags = 0;
		} else if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.VERTICAL) {
			dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
			swipeFlags = mSwipeFlags > 0 ? mSwipeFlags : ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
		} else {
			dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
			swipeFlags = mSwipeFlags > 0 ? mSwipeFlags : ItemTouchHelper.UP | ItemTouchHelper.DOWN;
		}
		return makeMovementFlags(dragFlags, swipeFlags);
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
				if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
					getDefaultUIUtil().onSelected(viewHolderCallback.getFrontView());
				}
			}
		} else {
			super.onSelectedChanged(viewHolder, actionState);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		//Force full Alpha
		viewHolder.itemView.setAlpha(ALPHA_FULL);
		if (viewHolder instanceof ViewHolderCallback) {
			//Tell the view holder it's time to restore the idle state
			ViewHolderCallback viewHolderCallback = (ViewHolderCallback) viewHolder;
			getDefaultUIUtil().clearView(viewHolderCallback.getFrontView());
			//Hide Left or Right View
			setLayoutVisibility(viewHolderCallback, 0);
			viewHolderCallback.onItemReleased(viewHolder.getAdapterPosition());
			mTempThreshold = mSwipeThreshold;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
							float dX, float dY, int actionState, boolean isCurrentlyActive) {
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE &&
				viewHolder instanceof ViewHolderCallback) {

			//Convert to custom VH
			ViewHolderCallback viewHolderCallback = (ViewHolderCallback) viewHolder;
			View frontView = viewHolderCallback.getFrontView();

			//Orientation
			float dragAmount = dX;
			if (dY != 0) dragAmount = dY;

			if (mMargin > 0) {
				if (FlexibleAdapter.DEBUG)
					Log.d(TAG, "onChildDraw before dragAmount=" + dragAmount + " oldAmount=" + oldAmount + " frontView(X)=" + frontView.getTranslationX() + " isCurrentlyActive=" + isCurrentlyActive);

				//Adjust back swiping starting from last margin
				boolean stillSwiping = false;
				if (Math.abs(dragAmount) > oldAmount || Math.abs(dragAmount) < mMargin) {
					oldAmount = Math.abs(dragAmount);
					stillSwiping = true;
				}

				if (!stillSwiping && isCurrentlyActive) {
					float delta = frontView.getRight() - Math.abs(dragAmount);
					dragAmount = Math.max(0, mMargin - delta) * (dragAmount >= 0 ? 1 : -1);
					mTempThreshold = 0.9f;
					Log.d(TAG, "onChildDraw new dragAmount=" + dragAmount + " delta=" + delta);
				} else if (!isCurrentlyActive && Math.abs(frontView.getTranslationX()) < mMargin / 3) {
					dragAmount = 0;
				}
			}

			//Is Left or Right View? Set margins!
			int swipingDirection = 0;//0 is for frontView
			if (dragAmount > 0) {
				swipingDirection = ItemTouchHelper.RIGHT;
				if (mMargin > 0 && Math.abs(dragAmount) > mMargin) {
					dragAmount = mMargin;
				}
			} else if (dragAmount < 0) {
				swipingDirection = ItemTouchHelper.LEFT;
				if (mMargin > 0 && Math.abs(dragAmount) > mMargin) {
					dragAmount = -mMargin;
				}
			}

			//Set SwipeStatus
			String log;
			if (Math.abs(dragAmount) > mMargin) {
				viewHolderCallback.setSwipeStatus(FULL_SWIPE);
				log = "FULL_SWIPE";
			} else if (Math.abs(dragAmount) == mMargin) {
				viewHolderCallback.setSwipeStatus(PARTIAL_SWIPE);
				log = "PARTIAL_SWIPE";
			} else if (Math.abs(dragAmount) == 0) {
				viewHolderCallback.setSwipeStatus(IDLE);
				log = "IDLE";
			} else {
				viewHolderCallback.setSwipeStatus(SWIPING);
				log = "SWIPING";
			}

			if (FlexibleAdapter.DEBUG)
				Log.d(TAG, "onChildDraw after dragAmount=" + dragAmount + " swipeStatus=" + log);

			//Update visibility for RearViews
			setLayoutVisibility(viewHolderCallback, swipingDirection);
			//Translate the FrontView
			getDefaultUIUtil().onDraw(c, recyclerView, frontView, dX != 0 ? dragAmount : 0, dY != 0 ? dragAmount : 0, actionState, isCurrentlyActive);

		} else {
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
								float dX, float dY, int actionState, boolean isCurrentlyActive) {
//		if (FlexibleAdapter.DEBUG) Log.d(TAG, "onChildDrawOver dX=" + dX + " dY=" + dY);
//
//		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE &&
//				viewHolder instanceof ViewHolderCallback) {
//			ViewHolderCallback viewHolderCallback = (ViewHolderCallback) viewHolder;
//			View frontView = viewHolderCallback.getFrontView();
//			getDefaultUIUtil().onDrawOver(c, recyclerView, frontView, dX, dY, actionState, isCurrentlyActive);
//		} else {
//			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//		}
	}

	private static void setLayoutVisibility(ViewHolderCallback viewHolderCallback, int swipeDirection) {
		if (viewHolderCallback.getRearRightView() != null)
			viewHolderCallback.getRearRightView().setVisibility(
					swipeDirection == ItemTouchHelper.LEFT ? View.VISIBLE : View.GONE);
		if (viewHolderCallback.getRearLeftView() != null)
			viewHolderCallback.getRearLeftView().setVisibility(
					swipeDirection == ItemTouchHelper.RIGHT ? View.VISIBLE : View.GONE);
	}

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
		 * @param position    the position of the item dismissed
		 * @param direction   the direction to which the ViewHolder is swiped
		 * @param swipeStatus the status of the swipe: one of {@link #IDLE}, {@link #PARTIAL_SWIPE},
		 *                    {@link #FULL_SWIPE}
		 */
		void onItemSwiped(int position, int direction, @SwipeStatus int swipeStatus);
	}

	/**
	 * Internal Interface for ViewHolder to notify of relevant callbacks from {@link Callback}.
	 * <br/>This listener, is to intend as a further way to display How a ViewHolder will display
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

		@SwipeStatus
		int getSwipeStatus();

		void setSwipeStatus(@SwipeStatus int status);

		/**
		 * On Swipe, override to return the Front View.
		 * <p>Default is itemView.</p>
		 *
		 * @return the item Front View
		 */
		View getFrontView();

		/**
		 * On Swipe, override to return the Rear Left View.
		 * <p>Default is null (no view).</p>
		 *
		 * @return the item Front View
		 */
		View getRearLeftView();

		/**
		 * On Swipe, override to return the Rear Right View.
		 * <p>Default is null (no view).</p>
		 *
		 * @return the item Front View
		 */
		View getRearRightView();
	}

	@Retention(SOURCE)
	@Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
	@IntDef({IDLE, SWIPING, PARTIAL_SWIPE, FULL_SWIPE})
	public @interface SwipeStatus {
	}

}