/*
 * Copyright (C) 2015 Dift.co
 * http://dift.co
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.flexibleadapter.helpers;

import android.animation.Animator;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ItemSwipeHelper {

	private static final int SWIPE_ANIMATION_DURATION = 300;
	private static final int RESET_ANIMATION_DURATION = 500;
	private static final int REVEAL_THRESHOLD = 50;
	private static final int SWIPE_THRESHOLD_WIDTH_RATIO = 5;
	private static final int LONG_PRESS_TIME = 500; // 500 is the standard long press time

	private static final int INVALID_POINTER_ID = -1;
	private int activePointerId = INVALID_POINTER_ID;

	private RecyclerView recyclerView;
	private SwipeListener swipeListener;
	private View touchedView;
	private ViewHolder touchedViewHolder;
	private View frontView;
	private View revealLeftView;
	private View revealRightView;

	private float frontViewX;
	private float frontViewW;
	private float frontViewLastX;

	private float downY;
	private float downX;
	private float upX;
	private float upY;

	private long downTime;
	private long upTime;

	private Set<View> runningAnimationsOn = new HashSet<>();
	private Queue<Integer> swipeQueue = new LinkedList<>();


	/**
	 * Constructor
	 **/

	public ItemSwipeHelper(RecyclerView recyclerView, SwipeListener swipeListener) {
		this.recyclerView = recyclerView;
		this.swipeListener = swipeListener;

		init();
	}


	/**
	 * Private methods
	 **/

	private void init() {
		recyclerView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent ev) {
				switch (ev.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_DOWN: {
						// http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
						activePointerId = ev.getPointerId(0);

						// starting point
						downX = ev.getX(activePointerId);
						downY = ev.getY(activePointerId);

						// to check for long click
						downTime = new Date().getTime();

						// which item are we touching
						resolveItem(downX, downY);

						break;
					}

					case MotionEvent.ACTION_UP: {
						upX = ev.getX();
						upY = ev.getY();
						upTime = new Date().getTime();
						activePointerId = INVALID_POINTER_ID;

						resolveState();
						break;
					}

					case MotionEvent.ACTION_POINTER_UP: {
						final int pointerIndex = (ev.getAction() &
								MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
						final int pointerId = ev.getPointerId(pointerIndex);

						if (pointerId == activePointerId) {
							final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
							activePointerId = ev.getPointerId(newPointerIndex);
						}

						break;
					}

					case MotionEvent.ACTION_MOVE: {
						final int pointerIndex = ev.findPointerIndex(activePointerId);
						final float x = ev.getX(pointerIndex);
						final float dx = x - downX;

						if (!shouldMove(dx)) break;

						// current position. moving only over x-axis
						frontViewLastX = frontViewX + dx + (dx > 0 ? -REVEAL_THRESHOLD : REVEAL_THRESHOLD);
						frontView.setX(frontViewLastX);

						if (frontViewLastX > 0) {
							revealRight();
						} else {
							revealLeft();
						}

						break;
					}

					case MotionEvent.ACTION_CANCEL: {
						activePointerId = INVALID_POINTER_ID;
						resolveState();

						break;
					}
				}

				return false;
			}
		});
	}

	private void resolveItem(float x, float y) {
		touchedView = recyclerView.findChildViewUnder(x, y);
		if (touchedView == null) {
			//no child under
			frontView = null;
			return;
		}

		// check if the view is being animated. in that case do not allow to move it
		if (runningAnimationsOn.contains(touchedView)) {
			frontView = null;
			return;
		}

		initViewForItem((ViewHolder) recyclerView.getChildViewHolder(touchedView));
	}

	private void resolveItem(int adapterPosition) {
		initViewForItem((ViewHolder) recyclerView.findViewHolderForAdapterPosition(adapterPosition));
	}

	private void initViewForItem(ViewHolder viewHolder) {
		touchedViewHolder = viewHolder;
		frontView = viewHolder.getFront();
		revealLeftView = viewHolder.getRevealLeft();
		revealRightView = viewHolder.getRevealRight();
		frontViewX = frontView.getX();
		frontViewW = frontView.getWidth();
	}

	private boolean shouldMove(float dx) {
		if (frontView == null) {
			return false;
		}

		if (dx > 0) {
			return revealRightView != null && Math.abs(dx) > REVEAL_THRESHOLD;
		} else {
			return revealLeftView != null && Math.abs(dx) > REVEAL_THRESHOLD;
		}
	}

	private void clear() {
		frontViewX = 0;
		frontViewW = 0;
		frontViewLastX = 0;
		downX = 0;
		downY = 0;
		upX = 0;
		upY = 0;
		downTime = 0;
		upTime = 0;
	}

	private boolean checkQueue() {
		// workaround in case a swipe call while dragging
		Integer next = swipeQueue.poll();
		if (next != null) {
			int pos = Math.abs(next) - 1;
			if (next < 0) {
				swipeLeft(pos);
			} else {
				swipeRight(pos);
			}
			return true;
		} else {
			return false;
		}
	}

	private void hideLeftRevealView() {
		if (revealLeftView != null) {
			revealLeftView.setVisibility(View.GONE);
		}
	}

	private void hideRightRevealView() {
		if (revealRightView != null) {
			revealRightView.setVisibility(View.GONE);
		}
	}

	private void resetPosition() {
		if (frontView == null) {
			return;
		}

		final View animated = touchedView;
		frontView.animate()
				.setDuration(RESET_ANIMATION_DURATION)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.setListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						runningAnimationsOn.add(animated);
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						runningAnimationsOn.remove(animated);
						checkQueue();
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						runningAnimationsOn.remove(animated);
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						runningAnimationsOn.add(animated);
					}
				})
				.x(frontViewX);
	}

	private void resolveState() {
		if (frontView == null) {
			return;
		}

		if (frontViewLastX > frontViewX + frontViewW / SWIPE_THRESHOLD_WIDTH_RATIO) {
			swipeRight();
		} else if (frontViewLastX < frontViewX - frontViewW / SWIPE_THRESHOLD_WIDTH_RATIO) {
			swipeLeft();
		} else {
			float diffX = Math.abs(downX - upX);
			float diffY = Math.abs(downY - upY);

			if (diffX <= 5 && diffY <= 5) {
				int pressTime = (int) (upTime - downTime);
				if (pressTime > LONG_PRESS_TIME) {
					swipeListener.onLongClick(touchedViewHolder.getItemData());
				} else {
					swipeListener.onClick(touchedViewHolder.getItemData());
				}
			}

			resetPosition();
		}

		clear();
	}

	private void swipeRight() {
		if (frontView == null) {
			return;
		}

		final View animated = touchedView;
		frontView.animate()
				.setDuration(SWIPE_ANIMATION_DURATION)
				.setInterpolator(new AccelerateInterpolator())
				.setListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						runningAnimationsOn.add(animated);
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						runningAnimationsOn.remove(animated);
						if (swipeListener.swipeRight(touchedViewHolder.getItemData())) {
							resetPosition();
						} else {
							if (!checkQueue()) {
								hideLeftRevealView();
							}
						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						runningAnimationsOn.remove(animated);
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						runningAnimationsOn.add(animated);
					}
				}).x(frontViewX + frontViewW);
	}

	private void swipeLeft() {
		if (frontView == null) {
			return;
		}

		final View animated = touchedView;
		frontView.animate()
				.setDuration(SWIPE_ANIMATION_DURATION)
				.setInterpolator(new AccelerateInterpolator())
				.setListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						runningAnimationsOn.add(animated);
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						runningAnimationsOn.remove(animated);
						if (swipeListener.swipeLeft(touchedViewHolder.getItemData())) {
							resetPosition();
						} else {
							if (!checkQueue()) {
								hideRightRevealView();
							}
						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						runningAnimationsOn.remove(animated);
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						runningAnimationsOn.add(animated);
					}
				}).x(frontViewX - frontViewW);

	}

	private void revealRight() {
		if (revealLeftView != null) {
			revealLeftView.setVisibility(View.GONE);
		}

		if (revealRightView != null) {
			revealRightView.setVisibility(View.VISIBLE);
		}
	}

	private void revealLeft() {
		if (revealRightView != null) {
			revealRightView.setVisibility(View.GONE);
		}

		if (revealLeftView != null) {
			revealLeftView.setVisibility(View.VISIBLE);
		}
	}


	/**
	 * Exposed methods
	 **/

	public void swipeLeft(int position) {
		// workaround in case a swipe call while dragging
		if (downTime != 0) {
			swipeQueue.add((position + 1) * -1); //use negative to express direction
			return;
		}
		resolveItem(position);
		revealLeft();
		swipeLeft();
	}

	public void swipeRight(int position) {
		// workaround in case a swipe call while dragging
		if (downTime != 0) {
			swipeQueue.add(position + 1);
			return;
		}
		resolveItem(position);
		revealRight();
		swipeRight();
	}


	/**
	 * Public interfaces & classes
	 */

	public interface SwipeListener<T extends Object> {
		boolean swipeLeft(T itemData);

		boolean swipeRight(T itemData);

		void onClick(T itemData);

		void onLongClick(T itemData);
	}

	public interface IViewHolder<T extends Object> {
		View getFront();

		View getRevealLeft();

		View getRevealRight();

		<T extends Object> T getItemData();
	}

	public static abstract class ViewHolder<T extends Object>
			extends RecyclerView.ViewHolder implements IViewHolder {

		public T data;
		public View front;
		public View revealLeft;
		public View revealRight;

		public ViewHolder(View v) {
			super(v);

			ViewGroup vg = (ViewGroup) v;
			front = vg.findViewWithTag("front");
			revealLeft = vg.findViewWithTag("reveal-left");
			revealRight = vg.findViewWithTag("reveal-right");

			int childCount = vg.getChildCount();
			if (front == null) {
				if (childCount < 1) {
					throw new RuntimeException("You must provide a view with tag='front'");
				} else {
					front = vg.getChildAt(childCount - 1);
				}
			}

			if (revealLeft == null || revealRight == null) {
				if (childCount < 2) {
					throw new RuntimeException("You must provide at least one reveal view.");
				} else {
					// set next to last as revealLeft view only if no revealRight was found
					if (revealLeft == null && revealRight == null) {
						revealLeft = vg.getChildAt(childCount - 2);
					}

					// if there are enough children assume the revealRight
					int i = childCount - 3;
					if (revealRight == null && i > -1) {
						revealRight = vg.getChildAt(i);
					}
				}
			}

			if (revealLeft != null) {
				revealLeft.setVisibility(View.GONE);
			}
			if (revealRight != null) {
				revealRight.setVisibility(View.GONE);
			}
		}

		@Override
		public View getFront() {
			return front;
		}

		@Override
		public View getRevealLeft() {
			return revealLeft;
		}

		@Override
		public View getRevealRight() {
			return revealRight;
		}

		@Override
		public T getItemData() {
			return data;
		}
	}

}