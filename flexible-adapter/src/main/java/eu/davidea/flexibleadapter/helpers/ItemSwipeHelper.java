/*
 * Copyright (C) 2016 Davide Steduto
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import eu.davidea.flexibleadapter.FlexibleAdapter;

public class ItemSwipeHelper {

	private static final int SWIPE_ANIMATION_DURATION = 300;
	private static final int RESET_ANIMATION_DURATION = 500;
	private static final int REVEAL_THRESHOLD = 50;
	private static final int SWIPE_THRESHOLD_WIDTH_RATIO = 5;

	private static final int INVALID_POINTER_ID = -1;
	private int activePointerId = INVALID_POINTER_ID;

	private RecyclerView recyclerView;
	private FlexibleAdapter.OnItemSwipeListener swipeListener;
	private View touchedView;
	private ItemTouchHelperCallback.ViewHolderCallback touchedViewHolder;
	private View frontView;
	private View rearLeftView;
	private View rearRightView;

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


	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	public ItemSwipeHelper(RecyclerView recyclerView, FlexibleAdapter.OnItemSwipeListener swipeListener) {
		this.recyclerView = recyclerView;
		this.swipeListener = swipeListener;
		init();
	}

	/*-----------------*/
	/* PRIVATE METHODS */
	/*-----------------*/

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

		initViewForItem(recyclerView.getChildViewHolder(touchedView));
	}

	private void resolveItem(int adapterPosition) {
		initViewForItem(recyclerView.findViewHolderForAdapterPosition(adapterPosition));
	}

	private void initViewForItem(RecyclerView.ViewHolder viewHolder) {
		if (viewHolder instanceof ItemTouchHelperCallback.ViewHolderCallback) {
			ItemTouchHelperCallback.ViewHolderCallback viewHolderCallback = (ItemTouchHelperCallback.ViewHolderCallback) viewHolder;
			touchedViewHolder = viewHolderCallback;
			frontView = viewHolderCallback.getFrontView();
			rearLeftView = viewHolderCallback.getRearLeftView();
			rearRightView = viewHolderCallback.getRearRightView();
			frontViewX = frontView.getX();
			frontViewW = frontView.getWidth();
		}
	}

	private boolean shouldMove(float dx) {
		if (frontView == null) {
			return false;
		}

		if (dx > 0) {
			return rearRightView != null && Math.abs(dx) > REVEAL_THRESHOLD;
		} else {
			return rearLeftView != null && Math.abs(dx) > REVEAL_THRESHOLD;
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
		if (rearLeftView != null) {
			rearLeftView.setVisibility(View.GONE);
		}
	}

	private void hideRightRevealView() {
		if (rearRightView != null) {
			rearRightView.setVisibility(View.GONE);
		}
	}

	private void resetPosition() {
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
			resetPosition();
		}

		clear();
	}

	private void swipeRight() {
		if (frontView == null) {
			return;
		}
		frontView.animate()
				.setDuration(SWIPE_ANIMATION_DURATION)
				.setInterpolator(new AccelerateInterpolator())
				.setListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						runningAnimationsOn.add(touchedView);
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						int position = recyclerView.getChildAdapterPosition(touchedView);
						runningAnimationsOn.remove(touchedView);
//						if (swipeListener.onItemSwipe(position, ItemTouchHelper.RIGHT)) {
//							resetPosition();
//						} else {
//							if (!checkQueue()) {
//								hideLeftRevealView();
//							}
//						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						runningAnimationsOn.remove(touchedView);
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						runningAnimationsOn.add(touchedView);
					}
				}).x(frontViewX + frontViewW);
	}

	private void swipeLeft() {
		if (frontView == null) {
			return;
		}

		frontView.animate()
				.setDuration(SWIPE_ANIMATION_DURATION)
				.setInterpolator(new AccelerateInterpolator())
				.setListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						runningAnimationsOn.add(touchedView);
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						int position = recyclerView.getChildAdapterPosition(touchedView);
						runningAnimationsOn.remove(touchedView);
//						if (swipeListener.onItemSwipe(position, ItemTouchHelper.LEFT)) {
//							resetPosition();
//						} else {
//							if (!checkQueue()) {
//								hideRightRevealView();
//							}
//						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						runningAnimationsOn.remove(touchedView);
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						runningAnimationsOn.add(touchedView);
					}
				}).x(frontViewX - frontViewW);
	}

	private void revealRight() {
		if (rearLeftView != null) {
			rearLeftView.setVisibility(View.GONE);
		}
		if (rearRightView != null) {
			rearRightView.setVisibility(View.VISIBLE);
		}
	}

	private void revealLeft() {
		if (rearRightView != null) {
			rearRightView.setVisibility(View.GONE);
		}
		if (rearLeftView != null) {
			rearLeftView.setVisibility(View.VISIBLE);
		}
	}

	/*-----------------*/
	/* EXPOSED METHODS */
	/*-----------------*/

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

}