/*
 * Copyright (C) 2016-2017 Davide Steduto (Special customization for FlexibleAdapter)
 * Copyright (C) 2015 Wasabeef
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package eu.davidea.flexibleadapter.common;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.davidea.flexibleadapter.utils.Log;
import eu.davidea.viewholders.AnimatedViewHolder;

/**
 * This implementation of {@link RecyclerView.ItemAnimator} provides basic
 * animations on remove, add, and move events that happen to the items in
 * a RecyclerView. RecyclerView uses a DefaultItemAnimator by default.
 *
 * @see RecyclerView#setItemAnimator(RecyclerView.ItemAnimator)
 */
public class FlexibleItemAnimator extends SimpleItemAnimator {

    private ArrayList<ViewHolder> mPendingRemovals = new ArrayList<>();
    private ArrayList<ViewHolder> mPendingAdditions = new ArrayList<>();
    private ArrayList<MoveInfo> mPendingMoves = new ArrayList<>();
    private ArrayList<ChangeInfo> mPendingChanges = new ArrayList<>();

    private ArrayList<ArrayList<ViewHolder>> mAdditionsList = new ArrayList<>();
    private ArrayList<ArrayList<MoveInfo>> mMovesList = new ArrayList<>();
    private ArrayList<ArrayList<ChangeInfo>> mChangesList = new ArrayList<>();

    private ArrayList<ViewHolder> mMoveAnimations = new ArrayList<>();
    private ArrayList<ViewHolder> mChangeAnimations = new ArrayList<>();
    private ArrayList<ViewHolder> mRemoveAnimations = new ArrayList<>();
    private ArrayList<ViewHolder> mAddAnimations = new ArrayList<>();

    protected Interpolator mInterpolator = new LinearInterpolator();
    private TimeInterpolator mDefaultInterpolator;

    private static class MoveInfo {

        public ViewHolder holder;
        int fromX, fromY, toX, toY;

        private MoveInfo(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            this.holder = holder;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    private static class ChangeInfo {

        ViewHolder oldHolder, newHolder;
        int fromX, fromY, toX, toY;

        private ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder) {
            this.oldHolder = oldHolder;
            this.newHolder = newHolder;
        }

        private ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder,
                           int fromX, int fromY, int toX, int toY) {
            this(oldHolder, newHolder);
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        @Override
        public String toString() {
            return "ChangeInfo{" +
                    "oldHolder=" + oldHolder +
                    ", newHolder=" + newHolder +
                    ", fromX=" + fromX +
                    ", fromY=" + fromY +
                    ", toX=" + toX +
                    ", toY=" + toY +
                    '}';
        }
    }

    public FlexibleItemAnimator() {
        super();
        setSupportsChangeAnimations(true);
    }

    public void setInterpolator(Interpolator mInterpolator) {
        this.mInterpolator = mInterpolator;
    }

    @Override
    public final void runPendingAnimations() {
        boolean removalsPending = !mPendingRemovals.isEmpty();
        boolean movesPending = !mPendingMoves.isEmpty();
        boolean changesPending = !mPendingChanges.isEmpty();
        boolean additionsPending = !mPendingAdditions.isEmpty();
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // Nothing to animate
            return;
        }
        // First, remove animations
        runRemoveAnimation();
        // Next, move animations
        runMoveAnimation(removalsPending, movesPending);
        // Next, change animations
        runChangeAnimation(removalsPending, changesPending);
        // Next, add animations
        runAddAnimation(removalsPending, changesPending, movesPending, additionsPending);
    }

    //1st Remove
    private void runRemoveAnimation() {
        // Reverse sorting removal animations
        Collections.sort(mPendingRemovals, new Comparator<ViewHolder>() {
            @Override
            public int compare(ViewHolder vh1, ViewHolder vh2) {
                return (int) (vh2.getItemId() - vh1.getItemId());
            }
        });
        Runnable remover = new Runnable() {
            @Override
            public void run() {
                int index = 0;
                for (ViewHolder holder : mPendingRemovals) {
                    doAnimateRemove(holder, index++);
                }
                mPendingRemovals.clear();
            }
        };
        remover.run();
    }

    //2nd Move
    private void runMoveAnimation(boolean removalsPending, boolean movesPending) {
        if (movesPending) {
            final ArrayList<MoveInfo> moves = new ArrayList<>();
            moves.addAll(mPendingMoves);
            mMovesList.add(moves);
            mPendingMoves.clear();
            Runnable mover = new Runnable() {
                @Override
                public void run() {
                    for (MoveInfo moveInfo : moves) {
                        animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY,
                                moveInfo.toX, moveInfo.toY);
                    }
                    moves.clear();
                    mMovesList.remove(moves);
                }
            };
            if (removalsPending) {
                View view = moves.get(0).holder.itemView;
                ViewCompat.postOnAnimationDelayed(view, mover, getRemoveDuration());
            } else {
                mover.run();
            }
        }
    }

    //3rd Change
    private void runChangeAnimation(boolean removalsPending, boolean changesPending) {
        // Change animation to run in parallel with move animations
        if (changesPending) {
            final ArrayList<ChangeInfo> changes = new ArrayList<>();
            changes.addAll(mPendingChanges);
            mChangesList.add(changes);
            mPendingChanges.clear();
            Runnable changer = new Runnable() {
                @Override
                public void run() {
                    for (ChangeInfo change : changes) {
                        animateChangeImpl(change);
                    }
                    changes.clear();
                    mChangesList.remove(changes);
                }
            };
            if (removalsPending) {
                ViewHolder holder = changes.get(0).oldHolder;
                ViewCompat.postOnAnimationDelayed(holder.itemView, changer, getRemoveDuration());
            } else {
                changer.run();
            }
        }
    }

    //4th Add
    private void runAddAnimation(boolean removalsPending, boolean changesPending,
                                 boolean movesPending, boolean additionsPending) {
        if (additionsPending) {
            final ArrayList<ViewHolder> additions = new ArrayList<>();
            // Sorting addition animations based on it's original layout position
            Collections.sort(mPendingAdditions, new Comparator<ViewHolder>() {
                @Override
                public int compare(ViewHolder vh1, ViewHolder vh2) {
                    return vh1.getLayoutPosition() - vh2.getLayoutPosition();
                }
            });
            additions.addAll(mPendingAdditions);
            mAdditionsList.add(additions);
            mPendingAdditions.clear();
            Runnable adder = new Runnable() {
                public void run() {
                    int index = 0;
                    for (ViewHolder holder : additions) {
                        doAnimateAdd(holder, index++);
                    }
                    additions.clear();
                    mAdditionsList.remove(additions);
                }
            };
            if (removalsPending || movesPending || changesPending) {
                long removeDuration = removalsPending ? getRemoveDuration() : 0;
                long moveDuration = movesPending ? getMoveDuration() : 0;
                long changeDuration = changesPending ? getChangeDuration() : 0;
                long totalDelay = removeDuration + Math.max(moveDuration, changeDuration);
                View view = additions.get(0).itemView;
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay);
            } else {
                adder.run();
            }
        }
    }

	/* ====== */
    /* REMOVE */
    /* ====== */

    /**
     * Prepares the View for Remove Animation.
     * <p>- If {@link AnimatedViewHolder#preAnimateRemoveImpl()} is implemented and returns
     * {@code true}, then ViewHolder has precedence and the implementation of this method is ignored;
     * <br>- If <u>not</u>, the implementation of this method is therefore performed.</p>
     * Default value is {@code true}.
     *
     * @param holder the ViewHolder
     * @return {@code true} if a later call to {@link #runPendingAnimations()} is requested,
     * false otherwise.
     */
    @SuppressWarnings("UnusedParameters")
    protected boolean preAnimateRemoveImpl(final ViewHolder holder) {
        return true;
    }

    /**
     * Performs the Remove Animation of this ViewHolder.
     * <p>- If {@link AnimatedViewHolder#animateRemoveImpl(ViewPropertyAnimatorListener, long, int)} is
     * implemented and returns true, then ViewHolder has precedence and the implementation of this
     * method is ignored;
     * <br>- If <u>not</u>, the implementation of this method is therefore performed.</p>
     *
     * @param holder the ViewHolder
     * @param index  the progressive order of execution
     */
    protected void animateRemoveImpl(final ViewHolder holder, int index) {
        //Free to implement
    }

    private boolean preAnimateRemove(final ViewHolder holder) {
        clear(holder.itemView);
        boolean consumed = false;
        if (holder instanceof AnimatedViewHolder) {
            consumed = ((AnimatedViewHolder) holder).preAnimateRemoveImpl();
        }
        return consumed || preAnimateRemoveImpl(holder);
    }

    private void doAnimateRemove(final ViewHolder holder, final int index) {
        Log.v("AnimateRemove on itemId %s", holder.getItemId());
        boolean consumed = false;
        if (holder instanceof AnimatedViewHolder) {
            consumed = ((AnimatedViewHolder) holder).animateRemoveImpl(new DefaultRemoveVpaListener(holder), getRemoveDuration(), index);
        }
        if (!consumed) {
            animateRemoveImpl(holder, index);
        }
        mRemoveAnimations.add(holder);
    }

    @Override
    public final boolean animateRemove(final ViewHolder holder) {
        endAnimation(holder);
        return preAnimateRemove(holder) && mPendingRemovals.add(holder);
    }

	/* === */
    /* ADD */
    /* === */

    /**
     * Prepares the View for Add Animation.
     * <p>- If {@link AnimatedViewHolder#preAnimateAddImpl()} is implemented and returns
     * {@code true}, then ViewHolder has precedence and the implementation of this method is ignored;
     * <br>- If <u>not</u>, the implementation of this method is therefore performed.</p>
     * Default value is {@code true}.
     *
     * @param holder the ViewHolder
     * @return {@code true} if a later call to {@link #runPendingAnimations()} is requested,
     * false otherwise.
     */
    protected boolean preAnimateAddImpl(final ViewHolder holder) {
        return true;
    }

    /**
     * Performs the Add Animation of this ViewHolder.
     * <p>- If {@link AnimatedViewHolder#animateAddImpl(ViewPropertyAnimatorListener, long, int)} is
     * implemented and returns {@code true}, then ViewHolder has precedence and the implementation
     * of this method is ignored;
     * <br>- If <u>not</u>, the implementation of this method is therefore performed.</p>
     *
     * @param holder the ViewHolder
     * @param index  the progressive order of execution
     */
    protected void animateAddImpl(final ViewHolder holder, int index) {
        //Free to implement
    }

    private boolean preAnimateAdd(final ViewHolder holder) {
        clear(holder.itemView);
        boolean consumed = false;
        if (holder instanceof AnimatedViewHolder) {
            consumed = ((AnimatedViewHolder) holder).preAnimateAddImpl();
        }
        return consumed || preAnimateAddImpl(holder);
    }

    private void doAnimateAdd(final ViewHolder holder, final int index) {
        Log.v("AnimateAdd on itemId=%s position=%s", holder.getItemId(), holder.getLayoutPosition());
        boolean consumed = false;
        if (holder instanceof AnimatedViewHolder) {
            consumed = ((AnimatedViewHolder) holder).animateAddImpl(new DefaultAddVpaListener(holder), getAddDuration(), index);
        }
        if (!consumed) {
            animateAddImpl(holder, index);
        }
        mAddAnimations.add(holder);
    }

    @Override
    public final boolean animateAdd(final ViewHolder holder) {
        endAnimation(holder);
        return preAnimateAdd(holder) && mPendingAdditions.add(holder);
    }

	/* ==== */
    /* MOVE */
    /* ==== */

    @Override
    public final boolean animateMove(final ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        fromX += holder.itemView.getTranslationX();
        fromY += holder.itemView.getTranslationY();
        resetAnimation(holder);
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder);
            return false;
        }
        if (deltaX != 0) {
            view.setTranslationX(-deltaX);
        }
        if (deltaY != 0) {
            view.setTranslationY(-deltaY);
        }
        mPendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY));
        return true;
    }

    private void animateMoveImpl(final ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        final int deltaX = toX - fromX;
        final int deltaY = toY - fromY;
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0);
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0);
        }
        // TODO: make EndActions end listeners instead, since end actions aren't called when
        // VPAs are canceled (and can't end them. why?)
        // Need listener functionality in VPACompat for this. Ick.
        mMoveAnimations.add(holder);
        final ViewPropertyAnimatorCompat animation = ViewCompat.animate(view);
        animation.setDuration(getMoveDuration()).setListener(new VpaListenerAdapter() {
            @Override
            public void onAnimationStart(View view) {
                dispatchMoveStarting(holder);
            }

            @Override
            public void onAnimationCancel(View view) {
                if (deltaX != 0) {
                    view.setTranslationX(0);
                }
                if (deltaY != 0) {
                    view.setTranslationY(0);
                }
            }

            @Override
            public void onAnimationEnd(View view) {
                animation.setListener(null);
                dispatchMoveFinished(holder);
                mMoveAnimations.remove(holder);
                dispatchFinishedWhenDone();
            }
        }).start();
    }

	/* ====== */
    /* CHANGE */
	/* ====== */

    @Override
    public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder,
                                 int fromX, int fromY, int toX, int toY) {
        if (oldHolder == newHolder) {
            // Don't know how to run change animations when the same view holder is re-used.
            // run a move animation to handle position changes.
            return animateMove(oldHolder, fromX, fromY, toX, toY);
        }
        final float prevTranslationX = oldHolder.itemView.getTranslationX();
        final float prevTranslationY = oldHolder.itemView.getTranslationY();
        final float prevAlpha = oldHolder.itemView.getAlpha();
        resetAnimation(oldHolder);
        int deltaX = (int) (toX - fromX - prevTranslationX);
        int deltaY = (int) (toY - fromY - prevTranslationY);
        // Recover prev translation state after ending animation
        oldHolder.itemView.setTranslationX(prevTranslationX);
        oldHolder.itemView.setTranslationY(prevTranslationY);
        oldHolder.itemView.setAlpha(prevAlpha);
        if (newHolder != null) {
            // Carry over translation values
            resetAnimation(newHolder);
            newHolder.itemView.setTranslationX(-deltaX);
            newHolder.itemView.setTranslationY(-deltaY);
            newHolder.itemView.setAlpha(0);
        }
        mPendingChanges.add(new ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY));
        return true;
    }

    private void animateChangeImpl(final ChangeInfo changeInfo) {
        final ViewHolder holder = changeInfo.oldHolder;
        final View view = holder == null ? null : holder.itemView;
        final ViewHolder newHolder = changeInfo.newHolder;
        final View newView = newHolder != null ? newHolder.itemView : null;
        if (view != null) {
            final ViewPropertyAnimatorCompat oldViewAnim = ViewCompat.animate(view).setDuration(
                    getChangeDuration());
            mChangeAnimations.add(changeInfo.oldHolder);
            oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX);
            oldViewAnim.translationY(changeInfo.toY - changeInfo.fromY);
            oldViewAnim.alpha(0).setListener(new VpaListenerAdapter() {
                @Override
                public void onAnimationStart(View view) {
                    dispatchChangeStarting(changeInfo.oldHolder, true);
                }

                @Override
                public void onAnimationEnd(View view) {
                    oldViewAnim.setListener(null);
                    view.setAlpha(1);
                    view.setTranslationX(0);
                    view.setTranslationY(0);
                    dispatchChangeFinished(changeInfo.oldHolder, true);
                    mChangeAnimations.remove(changeInfo.oldHolder);
                    dispatchFinishedWhenDone();
                }
            }).start();
        }
        if (newView != null) {
            final ViewPropertyAnimatorCompat newViewAnimation = ViewCompat.animate(newView);
            mChangeAnimations.add(changeInfo.newHolder);
            newViewAnimation.translationX(0).translationY(0).setDuration(getChangeDuration()).
                    alpha(1).setListener(new VpaListenerAdapter() {
                @Override
                public void onAnimationStart(View view) {
                    dispatchChangeStarting(changeInfo.newHolder, false);
                }

                @Override
                public void onAnimationEnd(View view) {
                    newViewAnimation.setListener(null);
                    newView.setAlpha(1);
                    newView.setTranslationX(0);
                    newView.setTranslationY(0);
                    dispatchChangeFinished(changeInfo.newHolder, false);
                    mChangeAnimations.remove(changeInfo.newHolder);
                    dispatchFinishedWhenDone();
                }
            }).start();
        }
    }

    private void endChangeAnimation(List<ChangeInfo> infoList, ViewHolder item) {
        for (int i = infoList.size() - 1; i >= 0; i--) {
            ChangeInfo changeInfo = infoList.get(i);
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo);
                }
            }
        }
    }

    private void endChangeAnimationIfNecessary(ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
        }
    }

    private boolean endChangeAnimationIfNecessary(ChangeInfo changeInfo, ViewHolder item) {
        boolean oldItem = false;
        if (changeInfo.newHolder == item) {
            changeInfo.newHolder = null;
        } else if (changeInfo.oldHolder == item) {
            changeInfo.oldHolder = null;
            oldItem = true;
        } else {
            return false;
        }
        item.itemView.setAlpha(1);
        item.itemView.setTranslationX(0);
        item.itemView.setTranslationY(0);
        dispatchChangeFinished(item, oldItem);
        return true;
    }

    @Override
    public void endAnimation(ViewHolder item) {
        final View view = item.itemView;
        // This will trigger end callback which should set properties to their target values.
        ViewCompat.animate(view).cancel();
        // TODO: if some other animations are chained to end, how do we cancel them as well?
        for (int i = mPendingMoves.size() - 1; i >= 0; i--) {
            MoveInfo moveInfo = mPendingMoves.get(i);
            if (moveInfo.holder == item) {
                view.setTranslationY(0);
                view.setTranslationX(0);
                dispatchMoveFinished(item);
                mPendingMoves.remove(i);
            }
        }
        endChangeAnimation(mPendingChanges, item);
        if (mPendingRemovals.remove(item)) {
            clear(item.itemView);
            dispatchRemoveFinished(item);
        }
        if (mPendingAdditions.remove(item)) {
            clear(item.itemView);
            dispatchAddFinished(item);
        }

        for (int i = mChangesList.size() - 1; i >= 0; i--) {
            ArrayList<ChangeInfo> changes = mChangesList.get(i);
            endChangeAnimation(changes, item);
            if (changes.isEmpty()) {
                mChangesList.remove(i);
            }
        }
        for (int i = mMovesList.size() - 1; i >= 0; i--) {
            ArrayList<MoveInfo> moves = mMovesList.get(i);
            for (int j = moves.size() - 1; j >= 0; j--) {
                MoveInfo moveInfo = moves.get(j);
                if (moveInfo.holder == item) {
                    view.setTranslationY(0);
                    view.setTranslationX(0);
                    dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        mMovesList.remove(i);
                    }
                    break;
                }
            }
        }
        for (int i = mAdditionsList.size() - 1; i >= 0; i--) {
            ArrayList<ViewHolder> additions = mAdditionsList.get(i);
            if (additions.remove(item)) {
                clear(item.itemView);
                dispatchAddFinished(item);
                if (additions.isEmpty()) {
                    mAdditionsList.remove(i);
                }
            }
        }
        // Animations should be ended by the cancel above.
        // Used during DEBUGGING; Commented in final version.
//		if (mRemoveAnimations.remove(item)) {
//			throw new IllegalStateException(
//					"After animation is cancelled, item should not be in mRemoveAnimations list");
//		}
//		if (mAddAnimations.remove(item)) {
//			throw new IllegalStateException(
//					"After animation is cancelled, item should not be in mAddAnimations list");
//		}
//		if (mChangeAnimations.remove(item)) {
//			throw new IllegalStateException(
//					"After animation is cancelled, item should not be in mChangeAnimations list");
//		}
//		if (mMoveAnimations.remove(item)) {
//			throw new IllegalStateException(
//					"After animation is cancelled, item should not be in mMoveAnimations list");
//		}
        dispatchFinishedWhenDone();
    }

    private void resetAnimation(ViewHolder holder) {
        if (mDefaultInterpolator == null) {
            mDefaultInterpolator = new ValueAnimator().getInterpolator();
        }
        // Clear Interpolator
        holder.itemView.animate().setInterpolator(mDefaultInterpolator);
        endAnimation(holder);
    }

    @Override
    public boolean isRunning() {
        return (!mPendingAdditions.isEmpty() ||
                !mPendingChanges.isEmpty() ||
                !mPendingMoves.isEmpty() ||
                !mPendingRemovals.isEmpty() ||
                !mMoveAnimations.isEmpty() ||
                !mRemoveAnimations.isEmpty() ||
                !mAddAnimations.isEmpty() ||
                !mChangeAnimations.isEmpty() ||
                !mMovesList.isEmpty() ||
                !mAdditionsList.isEmpty() ||
                !mChangesList.isEmpty());
    }

    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call {@link #dispatchAnimationsFinished()} to notify any
     * listeners.
     */
    private void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }

    @Override
    public void endAnimations() {
        int count = mPendingMoves.size();
        for (int i = count - 1; i >= 0; i--) {
            MoveInfo item = mPendingMoves.get(i);
            View view = item.holder.itemView;
            view.setTranslationY(0);
            view.setTranslationX(0);
            dispatchMoveFinished(item.holder);
            mPendingMoves.remove(i);
        }
        count = mPendingRemovals.size();
        for (int i = count - 1; i >= 0; i--) {
            ViewHolder item = mPendingRemovals.get(i);
            dispatchRemoveFinished(item);
            mPendingRemovals.remove(i);
        }
        count = mPendingAdditions.size();
        for (int i = count - 1; i >= 0; i--) {
            ViewHolder item = mPendingAdditions.get(i);
            clear(item.itemView);
            dispatchAddFinished(item);
            mPendingAdditions.remove(i);
        }
        count = mPendingChanges.size();
        for (int i = count - 1; i >= 0; i--) {
            endChangeAnimationIfNecessary(mPendingChanges.get(i));
        }
        mPendingChanges.clear();
        if (!isRunning()) {
            return;
        }

        int listCount = mMovesList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<MoveInfo> moves = mMovesList.get(i);
            count = moves.size();
            for (int j = count - 1; j >= 0; j--) {
                MoveInfo moveInfo = moves.get(j);
                ViewHolder item = moveInfo.holder;
                View view = item.itemView;
                view.setTranslationY(0);
                view.setTranslationX(0);
                dispatchMoveFinished(moveInfo.holder);
                moves.remove(j);
                if (moves.isEmpty()) {
                    mMovesList.remove(moves);
                }
            }
        }
        listCount = mAdditionsList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<ViewHolder> additions = mAdditionsList.get(i);
            count = additions.size();
            for (int j = count - 1; j >= 0; j--) {
                ViewHolder item = additions.get(j);
                View view = item.itemView;
                view.setAlpha(1);
                dispatchAddFinished(item);
                // Prevent exception when removal already occurred during finishing animation
                if (j < additions.size()) {
                    additions.remove(j);
                }
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions);
                }
            }
        }
        listCount = mChangesList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<ChangeInfo> changes = mChangesList.get(i);
            count = changes.size();
            for (int j = count - 1; j >= 0; j--) {
                endChangeAnimationIfNecessary(changes.get(j));
                if (changes.isEmpty()) {
                    mChangesList.remove(changes);
                }
            }
        }

        cancelAll(mRemoveAnimations);
        cancelAll(mMoveAnimations);
        cancelAll(mAddAnimations);
        cancelAll(mChangeAnimations);

        dispatchAnimationsFinished();
    }

    private void cancelAll(List<ViewHolder> viewHolders) {
        for (int i = viewHolders.size() - 1; i >= 0; i--) {
            ViewCompat.animate(viewHolders.get(i).itemView).cancel();
        }
    }

    private static void clear(View v) {
        v.setAlpha(1);
        v.setScaleY(1);
        v.setScaleX(1);
        v.setTranslationY(0);
        v.setTranslationX(0);
        v.setRotation(0);
        v.setRotationY(0);
        v.setRotationX(0);
        v.setPivotY(v.getMeasuredHeight() / 2);
        v.setPivotX(v.getMeasuredWidth() / 2);
        v.animate().setInterpolator(null).setStartDelay(0);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If the payload list is not empty, DefaultItemAnimator returns <code>true</code>.
     * When this is the case:
     * <ul>
     * <li>If you override {@code animateChange()}, both ViewHolder arguments will be the same
     * instance.</li>
     * <li>If you are not overriding {@code animateChange()}, then DefaultItemAnimator will call
     * {@code animateMove()} and run a move animation instead.</li>
     * </ul>
     */
    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder,
                                             @NonNull List<Object> payloads) {
        return !payloads.isEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads);
    }

    private static class VpaListenerAdapter implements ViewPropertyAnimatorListener {

        @Override
        public void onAnimationStart(View view) {
        }

        @Override
        public void onAnimationEnd(View view) {
        }

        @Override
        public void onAnimationCancel(View view) {
        }
    }

    protected class DefaultAddVpaListener extends VpaListenerAdapter {

        ViewHolder mViewHolder;

        public DefaultAddVpaListener(final ViewHolder holder) {
            mViewHolder = holder;
        }

        @Override
        public void onAnimationStart(View view) {
            dispatchAddStarting(mViewHolder);
        }

        @Override
        public void onAnimationCancel(View view) {
            clear(view);
        }

        @Override
        public void onAnimationEnd(View view) {
            clear(view);
            dispatchAddFinished(mViewHolder);
            mAddAnimations.remove(mViewHolder);
            dispatchFinishedWhenDone();
        }
    }

    protected class DefaultRemoveVpaListener extends VpaListenerAdapter {

        ViewHolder mViewHolder;

        public DefaultRemoveVpaListener(final ViewHolder holder) {
            mViewHolder = holder;
        }

        @Override
        public void onAnimationStart(View view) {
            dispatchRemoveStarting(mViewHolder);
        }

        @Override
        public void onAnimationCancel(View view) {
            clear(view);
        }

        @Override
        public void onAnimationEnd(View view) {
            clear(view);
            dispatchRemoveFinished(mViewHolder);
            mRemoveAnimations.remove(mViewHolder);
            dispatchFinishedWhenDone();
        }
    }

}