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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.viewholders.FlexibleViewHolder;

import static eu.davidea.flexibleadapter.utils.FlexibleUtils.getClassName;

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
 * <br/>13/09/2016 {@link #animateView(RecyclerView.ViewHolder, int)} is now automatically called
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class AnimatorAdapter extends SelectableAdapter {

	private static final String TAG = AnimatorAdapter.class.getSimpleName();

	private Interpolator mInterpolator = new LinearInterpolator();
	private AnimatorAdapterDataObserver mAnimatorNotifierObserver;
	private boolean mEntryStep = true;

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

	private boolean isReverseEnabled = false, shouldAnimate = false,
			onlyEntryAnimation = false, animateFromObserver = false;

	private static long DEFAULT_DURATION = 300L;
	private long mInitialDelay = 0L,
			mStepDelay = 100L,
			mDuration = DEFAULT_DURATION;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	/**
	 * Simple Constructor for Animator Adapter.
	 *
	 * @since 5.0.0-b1
	 */
	AnimatorAdapter(boolean stableIds) {
		super();
		setHasStableIds(stableIds);
		if (DEBUG) Log.i("FlexibleAdapter", "Initialized with StableIds=" + stableIds);

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
	void setScrollAnimate(boolean animate) {
		this.animateFromObserver = animate;
	}

	/**
	 * Sets the initial delay for the first item animation.
	 * <p>Default value is {@code 0ms}.</p>
	 *
	 * @param initialDelay any non negative delay
	 * @return this AnimatorAdapter, so the call can be chained
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationInitialDelay(long initialDelay) {
		if (DEBUG) Log.i(TAG, "Set animationInitialDelay=" + initialDelay);
		mInitialDelay = initialDelay;
		return this;
	}

	/**
	 * Sets the step delay between an animation and the next to be added to the initial delay.
	 * <p>The delay is added on top of the previous delay.</p>
	 * Default value is {@code 100ms}.
	 *
	 * @param delay any positive delay
	 * @return this AnimatorAdapter, so the call can be chained
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationDelay(@IntRange(from = 0) long delay) {
		if (DEBUG) Log.i(TAG, "Set animationDelay=" + delay);
		mStepDelay = delay;
		return this;
	}

	/**
	 * If initial loading animation should use step delay between an item animation and the next.
	 * When false, all items are animated with no delay.
	 * <p>Better to disable when using Grid layouts.</p>
	 * Default value is {@code true}.
	 *
	 * @param entryStep true to enable step delay, false otherwise
	 * @return this AnimatorAdapter, so the call can be chained
	 * since 5.0.0-b8
	 */
	public AnimatorAdapter setAnimationEntryStep(boolean entryStep) {
		if (DEBUG) Log.i(TAG, "Set animationEntryStep=" + entryStep);
		this.mEntryStep = entryStep;
		return this;
	}

	/**
	 * Sets the duration of the animation for ALL items.
	 * <p>Default value is {@code 300ms}.</p>
	 *
	 * @param duration any positive time
	 * @return this AnimatorAdapter, so the call can be chained
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationDuration(@IntRange(from = 1) long duration) {
		if (DEBUG) Log.i(TAG, "Set animationDuration=" + duration);
		mDuration = duration;
		return this;
	}

	/**
	 * Sets a custom interpolator for ALL items.
	 * <p>Default value is {@link LinearInterpolator}.</p>
	 *
	 * @param interpolator any valid non null interpolator
	 * @return this AnimatorAdapter, so the call can be chained
	 */
	public AnimatorAdapter setAnimationInterpolator(@NonNull Interpolator interpolator) {
		if (DEBUG) Log.i(TAG, "Set animationInterpolator=" + getClassName(interpolator));
		mInterpolator = interpolator;
		return this;
	}

	/**
	 * Sets an initial start animation adapter position.
	 * <p>Default value is {@code 0} (1st position).</p>
	 *
	 * @param start non negative minimum position to start animation.
	 * @since 5.0.0-b1
	 * @deprecated Can't be supported anymore due to the new internal condition of non-animations.
	 */
	@Deprecated
	public AnimatorAdapter setAnimationStartPosition(@IntRange(from = 0) int start) {
		if (DEBUG) Log.i(TAG, "Set animationStartPosition=" + start);
		mLastAnimatedPosition = start;
		return this;
	}

	/**
	 * Enables/Disables item animation while scrolling and on loading.
	 * <p>Enabling scrolling will disable onlyEntryAnimation.<br/>
	 * Disabling scrolling will disable also reverse scrolling.</p>
	 * Default value is {@code false}.
	 * <p><b>Note:</b> Loading animation can only be performed if the Adapter is initialized
	 * with some items using the constructor.</p>
	 *
	 * @param enabled true to enable item animation, false to disable them all.
	 * @return this AnimatorAdapter, so the call can be chained
	 * @see #setOnlyEntryAnimation(boolean)
	 * @see #setAnimationOnReverseScrolling(boolean)
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationOnScrolling(boolean enabled) {
		if (DEBUG) Log.i(TAG, "Set animationOnScrolling=" + enabled);
		if (enabled) this.onlyEntryAnimation = false;
		shouldAnimate = enabled;
		return this;
	}

	public boolean isAnimationOnScrollingEnabled() {
		return shouldAnimate;
	}

	/**
	 * Enables reverse scrolling animation if AnimationOnScrolling is also enabled!
	 * <p>Value is ignored if basic animation on scrolling is disabled.</p>
	 * Default value is {@code false} (only forward).
	 *
	 * @param enabled false to animate items only forward, true to also reverse animate
	 * @return this AnimatorAdapter, so the call can be chained
	 * @see #setAnimationOnScrolling(boolean)
	 * @since 5.0.0-b1
	 */
	public AnimatorAdapter setAnimationOnReverseScrolling(boolean enabled) {
		if (DEBUG) Log.i(TAG, "Set animationOnReverseScrolling=" + enabled);
		isReverseEnabled = enabled;
		return this;
	}

	/**
	 * @return true if items are animated also on reverse scrolling, false only forward
	 * @since 5.0.0-b1
	 * @deprecated use {@link #isAnimationOnReverseScrollingEnabled()}
	 */
	@Deprecated
	public boolean isAnimationOnReverseScrolling() {
		return isReverseEnabled;
	}

	/**
	 * @return true if items are animated also on reverse scrolling, false only forward
	 * @since 5.0.0-b1
	 */
	public boolean isAnimationOnReverseScrollingEnabled() {
		return isReverseEnabled;
	}

	/**
	 * Performs only entry animation during the initial loading. Stops the animation after
	 * the last visible item in the RecyclerView has been animated.
	 * <p><b>Note:</b> Loading animation can only be performed if the Adapter is initialized
	 * with some items using the Constructor.</p>
	 * Default value is {@code false}.
	 *
	 * @param enabled true to perform only entry animation, false otherwise
	 * @return this AnimatorAdapter, so the call can be chained
	 * @see #setAnimationOnScrolling(boolean)
	 * @since 5.0.0-b8
	 */
	public AnimatorAdapter setOnlyEntryAnimation(boolean enabled) {
		if (DEBUG) Log.i(TAG, "Set onlyEntryAnimation=" + enabled);
		if (enabled) this.shouldAnimate = true;
		this.onlyEntryAnimation = enabled;
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

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	/**
	 * Build your custom list of {@link Animator} to apply on the ItemView.<br/>
	 * Write the logic based on the position and/or viewType and/or the item selection.
	 * <p><b>Suggestions:</b>
	 * <br/>- You can also use {@link #getItemViewType(int)} to apply different Animation for
	 * each view type.
	 * <br/>- If you want to apply same animation for all items, create new list at class level
	 * and initialize it in the constructor, not inside this method!</p>
	 *
	 * @param itemView  the bounded ItemView
	 * @param position  position can be used to differentiate the list of Animators
	 * @param isForward boolean to be used to differentiate the list of Animators
	 * @return The list of animators to animate all together.
	 * @see #animateView(View, int)
	 * @see #getItemViewType(int)
	 * @since 5.0.0-b1
	 * @deprecated Use {@link AnimatorHelper} from {@link FlexibleViewHolder#scrollAnimators(List, int, boolean)}.
	 */
	@Deprecated
	public List<Animator> getAnimators(View itemView, int position, boolean isForward) {
		return new ArrayList<>();
	}

	/**
	 * Cancels any existing animations for given View. Useful when fling.
	 */
	private void cancelExistingAnimation(final int hashCode) {
		Animator animator = mAnimators.get(hashCode);
		if (animator != null) animator.end();
	}

	/**
	 * Checks if at the provided position, the item is a Header or Footer.
	 *
	 * @param position the position to check
	 * @return true if it's a scrollable item
	 * @since 5.0.0-rc1
	 */
	public abstract boolean isScrollableHeaderOrFooter(int position);

	/**
	 * Performs checks to scroll animate the itemView and in case, it animates the view.
	 * <p><b>Note:</b> If you have to change at runtime the LayoutManager <i>and</i> add
	 * Scrollable Headers too, consider to add them in post, using a {@code delay >= 0},
	 * otherwise scroll animations on all items will not start correctly.</p>
	 *
	 * @param holder   the ViewHolder just bound
	 * @param position the current item position
	 * @since 5.0.0-b1
	 */
	@SuppressWarnings("ConstantConditions")
	protected final void animateView(final RecyclerView.ViewHolder holder, final int position) {
		if (mRecyclerView == null) return;

		// Use always the max child count reached
		if (mMaxChildViews < mRecyclerView.getChildCount()) {
			mMaxChildViews = mRecyclerView.getChildCount();
		}
		// Animate only during initial loading?
		if (onlyEntryAnimation && mLastAnimatedPosition >= mMaxChildViews) {
			shouldAnimate = false;
		}
		int lastVisiblePosition = getFlexibleLayoutManager().findLastVisibleItemPosition();
//		if (DEBUG) {
//			Log.v(TAG, "shouldAnimate=" + shouldAnimate
//					+ " isFastScroll=" + isFastScroll
//					+ " isNotified=" + mAnimatorNotifierObserver.isPositionNotified()
//					+ " isReverseEnabled=" + isReverseEnabled
//					+ " mLastAnimatedPosition=" + mLastAnimatedPosition
//					+ (!isReverseEnabled ? " Pos>LasVisPos=" + (position > lastVisiblePosition) : "")
//					+ " mMaxChildViews=" + mMaxChildViews
//			);
//		}
		if (holder instanceof FlexibleViewHolder && shouldAnimate && !isFastScroll &&
				!mAnimatorNotifierObserver.isPositionNotified() &&
				(position > lastVisiblePosition || isReverseEnabled || isScrollableHeaderOrFooter(position) || (position == 0 && mMaxChildViews == 0))) {

			// Cancel animation is necessary when fling
			int hashCode = holder.itemView.hashCode();
			cancelExistingAnimation(hashCode);

			// User animators
			List<Animator> animators = new ArrayList<>();
			FlexibleViewHolder flexibleViewHolder = (FlexibleViewHolder) holder;
			flexibleViewHolder.scrollAnimators(animators, position, position >= lastVisiblePosition);

			// Execute the animations together
			AnimatorSet set = new AnimatorSet();
			set.playTogether(animators);
			set.setInterpolator(mInterpolator);
			// Single view duration
			long duration = mDuration;
			for (Animator animator : animators) {
				if (animator.getDuration() != DEFAULT_DURATION) {
					duration = animator.getDuration();
				}
			}
			//Log.v(TAG, "duration=" + duration);
			set.setDuration(duration);
			set.addListener(new HelperAnimatorListener(hashCode));
			if (mEntryStep) {
				// Stop stepDelay when screen is filled
				set.setStartDelay(calculateAnimationDelay(position));
			}
			set.start();
			mAnimators.put(hashCode, set);
			if (DEBUG) Log.v(TAG, "animateView    Scroll animation on position " + position);
		}
		mAnimatorNotifierObserver.clearNotified();
		// Update last animated position
		mLastAnimatedPosition = position;
	}

	/**
	 * Animates the view based on the custom animator list built with {@link #getAnimators(View, int, boolean)}.
	 *
	 * @since 5.0.0-b1
	 * @deprecated New system in place. Implement {@link FlexibleViewHolder#scrollAnimators(List, int, boolean)}
	 * and add new animator(s) to the list of {@code animators}.
	 */
	@Deprecated
	public final void animateView(final View itemView, int position) {
//		if (DEBUG)
//			Log.v(TAG, "shouldAnimate=" + shouldAnimate
//					+ " isFastScroll=" + isFastScroll
//					+ " isNotified=" + mAnimatorNotifierObserver.isPositionNotified()
//					+ " isReverseEnabled=" + isReverseEnabled
//					+ " mLastAnimatedPosition=" + mLastAnimatedPosition
//					+ (!isReverseEnabled ? " Pos>AniPos=" + (position > mLastAnimatedPosition) : "")
//			);

		if (shouldAnimate && !isFastScroll && !mAnimatorNotifierObserver.isPositionNotified() &&
				(isReverseEnabled || position > mLastAnimatedPosition || (position == 0 && mRecyclerView.getChildCount() == 0))) {

			//Cancel animation is necessary when fling
			cancelExistingAnimation(itemView.hashCode());

			//Retrieve user animators
			List<Animator> animators = getAnimators(itemView, position, position > mLastAnimatedPosition);

			//Add Alpha animator
			ViewCompat.setAlpha(itemView, 0);
			animators.add(ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f));
			Log.w(TAG, "Started Deprecated Animation on position " + position);

			//Execute the animations
			AnimatorSet set = new AnimatorSet();
			set.playTogether(animators);
			set.setInterpolator(mInterpolator);
			set.setDuration(mDuration);
			set.addListener(new HelperAnimatorListener(itemView.hashCode()));
			if (mEntryStep) {
				set.setStartDelay(calculateAnimationDelay(position));
			}
			set.start();
			mAnimators.put(itemView.hashCode(), set);

			//Animate only during initial loading?
			if (onlyEntryAnimation && mLastAnimatedPosition >= mMaxChildViews) {
				shouldAnimate = false;
			}
		}

		mAnimatorNotifierObserver.clearNotified();
		mLastAnimatedPosition = position;
	}

	/**
	 * @param position the position just bound
	 * @return the delay in milliseconds after which, the animation for next ItemView should start.
	 */
	private long calculateAnimationDelay(int position) {
		long delay;
		int firstVisiblePosition = getFlexibleLayoutManager().findFirstCompletelyVisibleItemPosition();
		int lastVisiblePosition = getFlexibleLayoutManager().findLastCompletelyVisibleItemPosition();

		// Fix for high delay on the first visible item on rotation
		if (firstVisiblePosition < 0 && position >= 0)
			firstVisiblePosition = position - 1;

		// Last visible position is the last animated when initially loading
		if (position - 1 > lastVisiblePosition)
			lastVisiblePosition = position - 1;

		int visibleItems = lastVisiblePosition - firstVisiblePosition;
		int numberOfAnimatedItems = position - 1;

		if (mMaxChildViews == 0 || visibleItems < numberOfAnimatedItems || //Normal Forward scrolling after max itemOnScreen is reached
				(firstVisiblePosition > 1 && firstVisiblePosition <= mMaxChildViews) || //Reverse scrolling
				(position > mMaxChildViews && firstVisiblePosition == -1 && mRecyclerView.getChildCount() == 0)) { //Reverse scrolling and click on FastScroller

			// Base delay is step delay
			delay = mStepDelay;
			if (visibleItems <= 1) {
				// When RecyclerView is initially loading no items are present
				// Use InitialDelay only for the first item
				delay += mInitialDelay;
			} else {
				// Reset InitialDelay only when first item is already animated
				mInitialDelay = 0L;
			}
			int numColumns = getFlexibleLayoutManager().getSpanCount();
			if (numColumns > 1) {
				delay = mInitialDelay + mStepDelay * (position % numColumns);
			}

		} else { //forward scrolling before max itemOnScreen is reached
			delay = mInitialDelay + (position * mStepDelay);
		}

//		if (DEBUG) Log.v(TAG, "Delay[" + position + "]=" + delay +
//				" FirstVisible=" + firstVisiblePosition +
//				" LastVisible=" + lastVisiblePosition +
//				" LastAnimated=" + numberOfAnimatedItems +
//				" VisibleItems=" + visibleItems +
//				" ChildCount=" + mRecyclerView.getChildCount() +
//				" MaxChildCount=" + mMaxChildViews);

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
	 * @deprecated Use {@link AnimatorHelper}.
	 */
	@Deprecated
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
	 * @deprecated Use {@link AnimatorHelper}.
	 */
	@Deprecated
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
	 * @deprecated Use {@link AnimatorHelper}.
	 */
	@Deprecated
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
	 * @deprecated Use {@link AnimatorHelper}.
	 */
	@Deprecated
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
	 * @deprecated Use {@link AnimatorHelper}.
	 */
	@Deprecated
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
	 * @deprecated Use {@link AnimatorHelper}.
	 */
	@Deprecated
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
	 * <p>Also, some items at the edge, are rebound by Android and should not be animated.</p>
	 */
	private class AnimatorAdapterDataObserver extends RecyclerView.AdapterDataObserver {
		private boolean notified;
		private Handler mAnimatorHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
			public boolean handleMessage(Message message) {
				if (DEBUG) Log.v(TAG, "Clear notified for scrolling Animations");
				notified = false;
				return true;
			}
		});

		public boolean isPositionNotified() {
			return notified;
		}

		public void clearNotified() {
			if (notified) {
				mAnimatorHandler.removeCallbacksAndMessages(null);
				mAnimatorHandler.sendMessageDelayed(Message.obtain(mAnimatorHandler), 200L);
			}
		}

		private void markNotified() {
			notified = !animateFromObserver;
//			if (DEBUG)
//				Log.v(TAG, "animateFromObserver=" + animateFromObserver + " notified=" + notified);
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