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
package eu.davidea.flexibleadapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This class is responsible to animate items. Bounded items are animated initially and also
 * when user starts to scroll the list.
 * <p>Animations can be customized for each items applying different logic based on item position
 * and beyond.</p>
 * This class is extended by {@link FlexibleAdapter}.
 *
 * @author Davide Steduto
 * @see FlexibleAdapter
 * @see SelectableAdapter
 * @since 10/01/2016 Created
 * <br/>30/01/2016 Class now extends {@link SelectableAdapter}
 */
public abstract class AnimatorAdapter extends SelectableAdapter {

	protected static final String TAG = AnimatorAdapter.class.getSimpleName();

	private Interpolator mInterpolator = new LinearInterpolator();
	private AnimatorAdapterDataObserver mAnimatorNotifierObserver;

	private enum AnimatorEnum {
		ALPHA, SLIDE_IN_LEFT, SLIDE_IN_RIGHT, SLIDE_IN_BOTTOM, SLIDE_IN_TOP, SCALE
	}

	/**
	 * The active Animators. Keys are hash codes of the Views that are animated.
	 */
	private final SparseArray<Animator> mAnimators = new SparseArray<>();

	/**
	 * The position of the last item that was animated.
	 */
	private int mLastAnimatedPosition = -1;

	/**
	 * Max items RecyclerView displays
	 */
	private int mMaxChildViews = -1;

	/**
	 * Contains type of animators already added
	 */
	private EnumSet<AnimatorEnum> animatorsUsed = EnumSet.noneOf(AnimatorEnum.class);

	private boolean isReverseEnabled = false, shouldAnimate = true,
			onlyEntryAnimation = false, isFastScroll = false, animateFromObserver = false;

	private long mInitialDelay = 0L,
			mStepDelay = 100L,
			mDuration = 300L;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	/**
	 * Simple Constructor for Animator Adapter.
	 *
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter() {
		super();

		//Get notified when an item is changed (should skip animation)
		mAnimatorNotifierObserver = new AnimatorAdapterDataObserver();
		registerAdapterDataObserver(mAnimatorNotifierObserver);
	}

	/*-----------------------*/
	/* CONFIGURATION SETTERS */
	/*-----------------------*/

	/**
	 * @param animate true to notify this Adapter that initialization is started and so
	 *                animate items, false to inform that the operation is complete
	 * @since 5.0.0-b6
	 */
	void setAnimate(boolean animate) {
		this.animateFromObserver = animate;
	}

	/**
	 * Customize the initial delay for the first item animation.
	 * <p>Default value is 0ms.</p>
	 *
	 * @param initialDelay any non negative delay
	 * @return this AnimatorAdapter, so the call can be chained
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationInitialDelay(long initialDelay) {
		mInitialDelay = initialDelay;
		return this;
	}

	/**
	 * Customize the step delay between an animation and the next to be added to the initial delay.
	 * <p>The delay is added on top of the previous delay.</p>
	 * Default value is 100ms.
	 *
	 * @param delay any positive delay
	 * @return this AnimatorAdapter, so the call can be chained
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationDelay(@IntRange(from = 0) long delay) {
		mStepDelay = delay;
		return this;
	}

	/**
	 * Customize the duration of the animation for ALL items.
	 * <p>Default value is 300ms.</p>
	 *
	 * @param duration any positive time
	 * @return this AnimatorAdapter, so the call can be chained
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationDuration(@IntRange(from = 1) long duration) {
		mDuration = duration;
		return this;
	}

	/**
	 * Define a custom interpolator for ALL items.
	 * <p>Default value is {@link LinearInterpolator}.</p>
	 *
	 * @param interpolator any valid non null interpolator
	 * @return this AnimatorAdapter, so the call can be chained
	 */
	public AnimatorAdapter setAnimationInterpolator(@NonNull Interpolator interpolator) {
		mInterpolator = interpolator;
		return this;
	}

	/**
	 * Define an initial start animation adapter position.
	 * <p>Default value is 0 (1st position).</p>
	 *
	 * @param start non negative minimum position to start animation.
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationStartPosition(@IntRange(from = 0) int start) {
		mLastAnimatedPosition = start;
		return this;
	}

	/**
	 * Enable/Disable item animation while scrolling and on loading.
	 * <p>Disabling scrolling will disable also reverse scrolling!</p>
	 * Default enabled.
	 *
	 * @param enabled true to enable item animation, false to disable them all.
	 * @return this AnimatorAdapter, so the call can be chained
	 * @see #setAnimationOnReverseScrolling(boolean)
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationOnScrolling(boolean enabled) {
		shouldAnimate = enabled;
		return this;
	}

	public boolean isAnimationOnScrollingEnabled() {
		return shouldAnimate;
	}

	/**
	 * Enable reverse scrolling animation if AnimationOnScrolling is also enabled!<br/>
	 * Default value is disabled (only forward).
	 *
	 * @param enabled false to animate items only forward, true to also reverse animate
	 * @return this AnimatorAdapter, so the call can be chained
	 * @see #setAnimationOnScrolling(boolean)
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationOnReverseScrolling(boolean enabled) {
		isReverseEnabled = enabled;
		return this;
	}

	/**
	 * @return true if items are animated also on reverse scrolling, false only forward
	 * @since 5.0.0-b1
	 */
	public boolean isAnimationOnReverseScrolling() {
		return isReverseEnabled;
	}

	/**
	 * Performs only entry animation during the initial loading. Stops the animation after
	 * the last visible item in the RecyclerView has been animated.
	 * Default value is false (always animate).
	 *
	 * @param onlyEntryAnimation true to perform only entry animation, false otherwise
	 * @return this AnimatorAdapter, so the call can be chained
	 * @since 5.0.0-b8
	 */
	public AnimatorAdapter setOnlyEntryAnimation(boolean onlyEntryAnimation) {
		this.onlyEntryAnimation = onlyEntryAnimation;
		return this;
	}

	/**
	 * @return true if the scrolling animation will occur only at startup until the screen is
	 * filled with the items, false animation will be performed when scrolling too.
	 * @since 5.0.0-b8
	 */
	public boolean isOnlyEntryAnimation() {
		return onlyEntryAnimation;
	}

	/**
	 * Triggered by the FastScroller when handle is touched
	 *
	 * @param scrolling boolean to indicate that the handle is being fast scrolled
	 * @since 5.0.0-b1
	 */
	@Override
	public void onFastScrollerStateChange(boolean scrolling) {
		super.onFastScrollerStateChange(scrolling);
		isFastScroll = scrolling;
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	/**
	 * Build your custom list of {@link Animator} to apply on the ItemView.<br/>
	 * Write the logic based on the position and/or viewType and/or the item selection.
	 * <p><b>Suggestions:</b>
	 * <br/>- A simple boolean for <i>isSelected</i> is preferable instead of {@link #isSelected(int)}
	 * <br/>- You can also use {@link #getItemViewType(int)} to apply different Animation for
	 * each view type.
	 * <br/>- If you want to apply same animation for all items, create new list at class level
	 * and initialize it in the constructor, not inside this method!</p>
	 *
	 * @param itemView   the bounded ItemView
	 * @param position   position can be used to differentiate the list of Animators
	 * @param isSelected boolean to be used to differentiate the list of Animators
	 * @return The list of animators to animate all together.
	 * @see #animateView(View, int, boolean)
	 * @see #getItemViewType(int)
	 * @since 5.0.0-b1
	 */
	public List<Animator> getAnimators(View itemView, int position, boolean isSelected) {
		return new ArrayList<Animator>();
	}

	/**
	 * Cancels any existing animations for given View. Useful when fling.
	 */
	private void cancelExistingAnimation(@NonNull final View itemView) {
		int hashCode = itemView.hashCode();
		Animator animator = mAnimators.get(hashCode);
		if (animator != null) animator.end();
	}

	/**
	 * Animates the view based on the custom animator list built with {@link #getAnimators(View, int, boolean)}.
	 *
	 * @since 5.0.0-b1
	 */
	public final void animateView(final View itemView, int position, boolean isSelected) {
		//FIXME: first completed visible item on rotation gets high delay
		//FIXME: Expanded children: find a way to Not animate items from custom ItemAnimator!!!
		// (ItemAnimators should work in conjunction with AnimatorViewHolder???)
		//TODO: Customize children animations (don't use animateAdd or animateRemove from custom ItemAnimator)

//		if (DEBUG)
//			Log.v(TAG, "shouldAnimate=" + shouldAnimate
//					+ " isFastScroll=" + isFastScroll
//					+ " isNotified=" + mAnimatorNotifierObserver.isPositionNotified()
//					+ " isReverseEnabled=" + isReverseEnabled
//					+ (!isReverseEnabled ? " Pos>AniPos=" + (position > mLastAnimatedPosition) : "")
//			);

		if (shouldAnimate && !isFastScroll && !mAnimatorNotifierObserver.isPositionNotified() &&
				(isReverseEnabled || position > mLastAnimatedPosition || (position == 0 && mRecyclerView.getChildCount() == 0))) {

			//Cancel animation is necessary when fling
			cancelExistingAnimation(itemView);

			//Retrieve user animators
			List<Animator> animators = getAnimators(itemView, position, isSelected);

			//Add Alpha animator if not yet
			ViewCompat.setAlpha(itemView, 0);
			if (!animatorsUsed.contains(AnimatorEnum.ALPHA)) {
				addAlphaAnimator(animators, itemView, 0f);
			}
//			if (DEBUG)
//				Log.v(TAG, "Started Animation on position " + position + " animatorsUsed=" + animatorsUsed);
			//Clear animators since the new item might have different animations
			animatorsUsed.clear();

			//Execute the animations all together
			AnimatorSet set = new AnimatorSet();
			set.playTogether(animators);
			//TODO: Animate with Solution 1 or 2?
			//set.setStartDelay(calculateAnimationDelay1(position));
			set.setStartDelay(calculateAnimationDelay2(position));
			set.setInterpolator(mInterpolator);
			set.setDuration(mDuration);
			set.addListener(new HelperAnimatorListener(itemView.hashCode()));
			set.start();
			mAnimators.put(itemView.hashCode(), set);

			//Animate only during initial loading?
			if (onlyEntryAnimation && mLastAnimatedPosition >= mMaxChildViews) {
				shouldAnimate = false;
			}
		}

		if (mAnimatorNotifierObserver.isPositionNotified())
			mAnimatorNotifierObserver.clearNotified();

		mLastAnimatedPosition = position;
	}

	/**
	 * Solution 1.
	 * Reset stepDelay.
	 */
	private long calculateAnimationDelay1(int position) {
		RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
		int lastVisiblePosition, firstVisiblePosition;
		if (layoutManager instanceof LinearLayoutManager) {
			LinearLayoutManager linearLayout = (LinearLayoutManager) layoutManager;
			lastVisiblePosition = linearLayout.findLastCompletelyVisibleItemPosition();
			firstVisiblePosition = linearLayout.findFirstCompletelyVisibleItemPosition();
		} else {
			StaggeredGridLayoutManager staggeredGridLayout = (StaggeredGridLayoutManager) layoutManager;
			lastVisiblePosition = staggeredGridLayout.findLastCompletelyVisibleItemPositions(null)[0];
			firstVisiblePosition = staggeredGridLayout.findFirstCompletelyVisibleItemPositions(null)[0];
		}

		//Use always the max child count reached
		if (mMaxChildViews < mRecyclerView.getChildCount())
			mMaxChildViews = mRecyclerView.getChildCount();

		if (mLastAnimatedPosition > lastVisiblePosition)
			lastVisiblePosition = mLastAnimatedPosition;

		int visibleItems = lastVisiblePosition - firstVisiblePosition;

//		if (DEBUG) Log.v(TAG, "Position=" + position +
//				" FirstVisible=" + firstVisiblePosition +
//				" LastVisible=" + lastVisiblePosition +
//				" LastAnimated=" + mLastAnimatedPosition +
//				" VisibleItems=" + visibleItems +
//				" ChildCount=" + mRecyclerView.getChildCount());

		//Stop stepDelay when screen is filled
		if (mLastAnimatedPosition > visibleItems || //Normal Forward scrolling
				(firstVisiblePosition > 1 && firstVisiblePosition <= mMaxChildViews)) { //Reverse scrolling
			if (DEBUG) Log.v(TAG, "Reset AnimationDelay on position " + position);
			mInitialDelay = 0L;
		}

		return mInitialDelay += mStepDelay;
	}

	/**
	 * Solution 2.
	 * Returns the delay in milliseconds after which, the animation for next ItemView should start.
	 */
	private long calculateAnimationDelay2(int position) {
		long delay;
		RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
		int lastVisiblePosition, firstVisiblePosition;
		if (layoutManager instanceof LinearLayoutManager) {
			LinearLayoutManager linearLayout = (LinearLayoutManager) layoutManager;
			lastVisiblePosition = linearLayout.findLastCompletelyVisibleItemPosition();
			firstVisiblePosition = linearLayout.findFirstCompletelyVisibleItemPosition();
		} else {
			StaggeredGridLayoutManager staggeredGridLayout = (StaggeredGridLayoutManager) layoutManager;
			lastVisiblePosition = staggeredGridLayout.findLastCompletelyVisibleItemPositions(null)[0];
			firstVisiblePosition = staggeredGridLayout.findFirstCompletelyVisibleItemPositions(null)[0];
		}

		if (mLastAnimatedPosition > lastVisiblePosition)
			lastVisiblePosition = mLastAnimatedPosition;

		int numberOfItemsOnScreen = lastVisiblePosition - firstVisiblePosition;
		int numberOfAnimatedItems = position - 1;

		//Save max child count reached
		if (mMaxChildViews < mRecyclerView.getChildCount())
			mMaxChildViews = mRecyclerView.getChildCount();

		if (numberOfItemsOnScreen == 0 || numberOfItemsOnScreen < numberOfAnimatedItems || //Normal Forward scrolling after max itemOnScreen is reached
				(firstVisiblePosition > 1 && firstVisiblePosition <= mMaxChildViews) || //Reverse scrolling
				(position > mMaxChildViews && firstVisiblePosition == -1 && mRecyclerView.getChildCount() == 0)) { //Reverse scrolling and click on FastScroller

			//Base delay is step delay
			delay = mStepDelay;
			if (numberOfItemsOnScreen <= 1) {
				//When RecyclerView is initially loading no items are present
				//Use InitialDelay only for the first item
				delay += mInitialDelay;
			} else {
				//Reset InitialDelay only when first item is already animated
				mInitialDelay = 0L;
			}
			if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
				int numColumns = ((GridLayoutManager) mRecyclerView.getLayoutManager()).getSpanCount();
				delay = mInitialDelay + mStepDelay * (position % numColumns);
			}

		} else {//forward scrolling before max itemOnScreen is reached
			delay = mInitialDelay + (position * mStepDelay);
		}

//		if (DEBUG) Log.v(TAG, "Delay[" + position + "]=" + delay +
//				" FirstVisible=" + firstVisiblePosition +
//				" LastVisible=" + lastVisiblePosition +
//				" LastAnimated=" + mLastAnimatedPosition +
//				" VisibleItems=" + numberOfItemsOnScreen +
//				" ChildCount=" + mRecyclerView.getChildCount());

		return delay;
	}

	/*-----------*/
	/* ANIMATORS */
	/*-----------*/

	/**
	 * This is the default animator.<br/>
	 * Alpha animator will be always automatically added.
	 * <p><b>Note:</b> Only 1 animator of the same compatible type can be added.<br/>
	 * Incompatible with ALPHA animator.</p>
	 *
	 * @param animators user defined list
	 * @param view      itemView to animate
	 * @param alphaFrom starting alpha value
	 * @since 5.0.0-b1
	 */
	private void addAlphaAnimator(
			@NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.0, to = 1.0) float alphaFrom) {
		if (animatorsUsed.contains(AnimatorEnum.ALPHA)) return;
		animators.add(ObjectAnimator.ofFloat(view, "alpha", alphaFrom, 1f));
		animatorsUsed.add(AnimatorEnum.ALPHA);
	}

	/**
	 * Item will slide from Left to Right.<br/>
	 * Ignored if LEFT, RIGHT, TOP or BOTTOM animators were already added.
	 * <p><b>Note:</b> Only 1 animator of the same compatible type can be added per time.<br/>
	 * Incompatible with LEFT, TOP, BOTTOM animators.<br/>
	 *
	 * @param animators user defined list
	 * @param view      itemView to animate
	 * @param percent   any % multiplier (between 0 and 1) of the LayoutManager Width
	 * @since 5.0.0-b1
	 */
	public void addSlideInFromLeftAnimator(
			@NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.0, to = 1.0) float percent) {
		if (animatorsUsed.contains(AnimatorEnum.SLIDE_IN_LEFT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_RIGHT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_TOP) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_BOTTOM)) return;
		animators.add(ObjectAnimator.ofFloat(view, "translationX", -mRecyclerView.getLayoutManager().getWidth() * percent, 0));
		animatorsUsed.add(AnimatorEnum.SLIDE_IN_LEFT);
	}

	/**
	 * Item will slide from Right to Left.<br/>
	 * Ignored if LEFT, RIGHT, TOP or BOTTOM animators were already added.
	 * <p><b>Note:</b> Only 1 animator of the same compatible type can be added per time.<br/>
	 * Incompatible with RIGHT, TOP, BOTTOM animators.<br/>
	 *
	 * @param animators user defined list
	 * @param view      ItemView to animate
	 * @param percent   Any % multiplier (between 0 and 1) of the LayoutManager Width
	 * @since 5.0.0-b1
	 */
	public void addSlideInFromRightAnimator(
			@NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.0, to = 1.0) float percent) {
		if (animatorsUsed.contains(AnimatorEnum.SLIDE_IN_LEFT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_RIGHT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_TOP) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_BOTTOM)) return;
		animators.add(ObjectAnimator.ofFloat(view, "translationX", mRecyclerView.getLayoutManager().getWidth() * percent, 0));
		animatorsUsed.add(AnimatorEnum.SLIDE_IN_RIGHT);
	}

	/**
	 * Item will slide from Top of the screen to its natural position.<br/>
	 * Ignored if LEFT, RIGHT, TOP or BOTTOM animators were already added.
	 * <p><b>Note:</b> Only 1 animator of the same compatible type can be added per time.<br/>
	 * Incompatible with LEFT, RIGHT, TOP, BOTTOM animators.</p>
	 *
	 * @param animators user defined list
	 * @param view      itemView to animate
	 * @since 5.0.0-b7
	 */
	public void addSlideInFromTopAnimator(
			@NonNull List<Animator> animators, @NonNull View view) {
		if (animatorsUsed.contains(AnimatorEnum.SLIDE_IN_LEFT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_RIGHT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_TOP) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_BOTTOM)) return;
		animators.add(ObjectAnimator.ofFloat(view, "translationY", -mRecyclerView.getMeasuredHeight() >> 1, 0));
		animatorsUsed.add(AnimatorEnum.SLIDE_IN_TOP);
	}

	/**
	 * Item will slide from Bottom of the screen to its natural position.<br/>
	 * Ignored if LEFT, RIGHT, TOP or BOTTOM animators were already added.
	 * <p><b>Note:</b> Only 1 animator of the same compatible type can be added per time.<br/>
	 * Incompatible with LEFT, RIGHT, TOP, BOTTOM animators.</p>
	 *
	 * @param animators user defined list
	 * @param view      itemView to animate
	 * @since 5.0.0-b1
	 */
	public void addSlideInFromBottomAnimator(
			@NonNull List<Animator> animators, @NonNull View view) {
		if (animatorsUsed.contains(AnimatorEnum.SLIDE_IN_LEFT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_RIGHT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_TOP) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_BOTTOM)) return;
		animators.add(ObjectAnimator.ofFloat(view, "translationY", mRecyclerView.getMeasuredHeight() >> 1, 0));
		animatorsUsed.add(AnimatorEnum.SLIDE_IN_BOTTOM);
	}

	/**
	 * Item will scale.<br/>
	 * Ignored if SCALE animator was already added.
	 * <p><b>Note:</b> Only 1 animator of the same compatible type can be added per time.<br/>
	 * Incompatible with LEFT, RIGHT, BOTTOM animators.<br/>
	 *
	 * @param animators user defined list
	 * @param view      itemView to animate
	 * @param scaleFrom initial scale value
	 * @since 5.0.0-b1
	 */
	public void addScaleInAnimator(
			@NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.0, to = 1.0) float scaleFrom) {
		if (animatorsUsed.contains(AnimatorEnum.SCALE)) return;
		animators.add(ObjectAnimator.ofFloat(view, "scaleX", scaleFrom, 1f));
		animators.add(ObjectAnimator.ofFloat(view, "scaleY", scaleFrom, 1f));
		animatorsUsed.add(AnimatorEnum.SCALE);
	}

	/*---------------*/
	/* INNER CLASSES */
	/*---------------*/

	/**
	 * Observer Class responsible to skip animation when items are notified to avoid
	 * double animation with {@link android.support.v7.widget.RecyclerView.ItemAnimator}.
	 * <p>Also, some items at the edge, are rebounded by Android and should not be animated.</p>
	 */
	private class AnimatorAdapterDataObserver extends RecyclerView.AdapterDataObserver {
		private boolean notified;
		private Handler mAnimatorHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
			public boolean handleMessage(Message message) {
				if (DEBUG) Log.v(TAG, "Clear notified for binding Animations");
				notified = false;
				return true;
			}
		});

		public boolean isPositionNotified() {
			return notified;
		}

		public void clearNotified() {
			mAnimatorHandler.removeCallbacksAndMessages(null);
			mAnimatorHandler.sendMessageDelayed(Message.obtain(mAnimatorHandler), 200L);
		}

		private void markNotified() {
			notified = !animateFromObserver;
		}

		@Override
		public void onChanged() {
			markNotified();
		}

		@Override
		public void onItemRangeChanged(int positionStart, int itemCount) {
			markNotified();
		}

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			markNotified();
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			markNotified();
		}

		@Override
		public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
			markNotified();
		}
	}

	/**
	 * Helper Class to clear Animators List used to avoid multiple Item animation on same
	 * position when fling.
	 */
	private class HelperAnimatorListener implements Animator.AnimatorListener {
		int key;

		HelperAnimatorListener(int key) {
			this.key = key;
		}

		@Override
		public void onAnimationStart(Animator animation) {

		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mAnimators.remove(key);
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	}

}