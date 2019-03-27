/*
 * Copyright 2016-2018 Davide Steduto
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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import eu.davidea.viewholders.FlexibleViewHolder;

import static eu.davidea.flexibleadapter.utils.LayoutUtils.getClassName;

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
 * <br>30/01/2016 Class now extends {@link SelectableAdapter}
 * <br>13/09/2016 {@link #animateView(RecyclerView.ViewHolder, int)} is now automatically called
 * <br>07/12/2017 Reverse scrolling is independent from Forward scrolling
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class AnimatorAdapter extends SelectableAdapter {

    private static long DEFAULT_DURATION = 300L;

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

    private boolean isReverseEnabled = false, isForwardEnabled = false,
            onlyEntryAnimation = false, animateFromObserver = false;
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
        log.i("Initialized with StableIds=" + stableIds);

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
        log.i("Set animationInitialDelay=%s", initialDelay);
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
        log.i("Set animationDelay=%s", delay);
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
        log.i("Set animationEntryStep=%s", entryStep);
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
        log.i("Set animationDuration=%s", duration);
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
        log.i("Set animationInterpolator=%s", getClassName(interpolator));
        mInterpolator = interpolator;
        return this;
    }

    /**
     * Enables/Disables item animation while forward scrolling and on loading.
     * <br>Enabling forward scrolling will disable {@code onlyEntryAnimation}.
     * <br>Forward scrolling is independent from reverse scrolling.
     * <p>Default value is {@code false}.</p>
     * <b>Note:</b> Loading animation can only be performed if the Adapter is initialized
     * with some items using the constructor.
     *
     * @param enabled true to enable item animation on forward scrolling, false to disable.
     * @return this AnimatorAdapter, so the call can be chained
     * @see #setOnlyEntryAnimation(boolean)
     * @see #setAnimationOnReverseScrolling(boolean)
     * @since 5.0.0-b1
     */
    public AnimatorAdapter setAnimationOnForwardScrolling(boolean enabled) {
        log.i("Set animationOnForwardScrolling=%s", enabled);
        if (enabled) {
            this.onlyEntryAnimation = false;
        }
        isForwardEnabled = enabled;
        return this;
    }

    /**
     * @return true if items are animated when forward scrolling, false only forward
     * @since 5.0.0-b1
     */
    public boolean isAnimationOnForwardScrollingEnabled() {
        return isForwardEnabled;
    }

    /**
     * Enables/Disables reverse scrolling animation.
     * <br>Reverse scrolling is independent from forward scrolling.
     * <p>Default value is {@code false} (only forward).</p>
     *
     * @param enabled true to enable item animation on reverse scrolling, false to disable.
     * @return this AnimatorAdapter, so the call can be chained
     * @see #setOnlyEntryAnimation(boolean)
     * @see #setAnimationOnForwardScrolling(boolean)
     * @since 5.0.0-b1
     */
    public AnimatorAdapter setAnimationOnReverseScrolling(boolean enabled) {
        log.i("Set animationOnReverseScrolling=%s", enabled);
        isReverseEnabled = enabled;
        return this;
    }

    /**
     * @return true if items are animated when reverse scrolling, false only forward
     * @since 5.0.0-b1
     */
    public boolean isAnimationOnReverseScrollingEnabled() {
        return isReverseEnabled;
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
     * Performs only entry animation during the initial loading. Stops the animation after
     * the last visible item in the RecyclerView has been animated.
     * <p><b>Note:</b> Loading animation can only be performed if the Adapter is initialized
     * with some items using the Constructor.</p>
     * Default value is {@code false}.
     *
     * @param enabled true to perform only entry animation, false otherwise
     * @return this AnimatorAdapter, so the call can be chained
     * @see #setAnimationOnForwardScrolling(boolean)
     * @since 5.0.0-b8
     */
    public AnimatorAdapter setOnlyEntryAnimation(boolean enabled) {
        log.i("Set onlyEntryAnimation=%s", enabled);
        if (enabled) {
            this.isForwardEnabled = true;
        }
        this.onlyEntryAnimation = enabled;
        return this;
    }

    /**
     * Cancels any existing animations for given View. Useful when fling.
     */
    private void cancelExistingAnimation(final int hashCode) {
        Animator animator = mAnimators.get(hashCode);
        if (animator != null) {
            animator.end();
        }
    }

    /*--------------*/
    /* MAIN METHODS */
    /*--------------*/

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
    protected final void animateView(final RecyclerView.ViewHolder holder, final int position) {
        if (mRecyclerView == null) {
            return;
        }

        // Use always the max child count reached
        if (mMaxChildViews < mRecyclerView.getChildCount()) {
            mMaxChildViews = mRecyclerView.getChildCount();
        }
        // Animate only during initial loading?
        if (onlyEntryAnimation && mLastAnimatedPosition >= mMaxChildViews) {
            isForwardEnabled = false;
        }
        int lastVisiblePosition = getFlexibleLayoutManager().findLastVisibleItemPosition();
//        log.v("animateView isForwardEnabled=%s isReverseEnabled=%s isFastScroll=%s isNotified=%s mLastAnimatedPosition=%s mMaxChildViews=%s %s",
//                isForwardEnabled, isReverseEnabled, isFastScroll, mAnimatorNotifierObserver.isPositionNotified(), mLastAnimatedPosition,
//                mMaxChildViews, (!isReverseEnabled ? " Pos>LasVisPos=" + (position > lastVisiblePosition) : "")
//        );
        if ((isForwardEnabled || isReverseEnabled)
                && !isFastScroll && holder instanceof FlexibleViewHolder
                && (!mAnimatorNotifierObserver.isPositionNotified() || isScrollableHeaderOrFooter(position))
                && (isScrollableHeaderOrFooter(position)
                || (isForwardEnabled && position > lastVisiblePosition)
                || (isReverseEnabled && position < lastVisiblePosition)
                || (position == 0 && mMaxChildViews == 0))) {

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
            set.setDuration(duration);
            set.addListener(new HelperAnimatorListener(hashCode));
            if (mEntryStep) {
                // Stop stepDelay when screen is filled
                set.setStartDelay(calculateAnimationDelay(holder, position));
            }
            set.start();
            mAnimators.put(hashCode, set);
            //log.v("animateView    Scroll animation on position %s", position);
        }
        mAnimatorNotifierObserver.clearNotified();
        // Update last animated position
        mLastAnimatedPosition = position;
    }

    /**
     * @param position the position just bound
     * @return the delay in milliseconds after which, the animation for next ItemView should start.
     */
    private long calculateAnimationDelay(RecyclerView.ViewHolder holder, int position) {
        long delay;
        int firstVisiblePosition = getFlexibleLayoutManager().findFirstCompletelyVisibleItemPosition();
        int lastVisiblePosition = getFlexibleLayoutManager().findLastCompletelyVisibleItemPosition();

        // Fix for high delay on the first visible item on rotation
        if (firstVisiblePosition < 0 && position >= 0) {
            firstVisiblePosition = position - 1;
        }

        // Last visible position is the last animated when initially loading
        if (position - 1 > lastVisiblePosition) {
            lastVisiblePosition = position - 1;
        }

        int visibleItems = lastVisiblePosition - firstVisiblePosition;
        int numberOfAnimatedItems = position - 1;

        // Normal Forward scrolling after max itemOnScreen is reached
        if (mMaxChildViews == 0 || visibleItems < numberOfAnimatedItems ||
                // Reverse scrolling
                (firstVisiblePosition > 1 && firstVisiblePosition <= mMaxChildViews) ||
                // Reverse scrolling and click on FastScroller
                (position > mMaxChildViews && firstVisiblePosition == -1 && mRecyclerView.getChildCount() == 0)) {

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

        } else { // Forward scrolling before max itemOnScreen is reached
            delay = mInitialDelay + (position * mStepDelay);
        }

//        log.v("animateView %s delay[%s]=%s firstVisible=%s lastVisible=%s lastAnimated=%s visibleItems=%s childCount=%s maxChildCount=%s",
//                getClassName(holder), position, delay, firstVisiblePosition, lastVisiblePosition,
//                numberOfAnimatedItems, visibleItems, mRecyclerView.getChildCount(), mMaxChildViews);

        return delay;
    }

    /*---------------*/
    /* INNER CLASSES */
    /*---------------*/

    /**
     * Observer Class responsible to skip animation when items are notified to avoid
     * double animation with {@link RecyclerView.ItemAnimator}.
     * <p>Also, some items at the edge, are rebound by Android and should not be animated.</p>
     */
    private class AnimatorAdapterDataObserver extends RecyclerView.AdapterDataObserver {
        private boolean notified;
        private Handler mAnimatorHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            public boolean handleMessage(Message message) {
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
            notified = true;
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