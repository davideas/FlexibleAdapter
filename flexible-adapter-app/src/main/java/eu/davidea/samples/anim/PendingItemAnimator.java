package eu.davidea.samples.anim;

/**
 * This class handles the pending que for you.
 */

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

import java.util.ArrayList;

/**
 * This was pulled from support 21 rc1. This makes sure your options always happen in a certain order
 * Remove -> Move -> Add.
 **/
public abstract class PendingItemAnimator<H extends ViewHolder> extends SimpleItemAnimator {

    private ArrayList<H> mPendingRemovals = new ArrayList<>();
    private ArrayList<H> mPendingAdditions = new ArrayList<>();
    private ArrayList<MoveInfo> mPendingMoves = new ArrayList<>();

    private ArrayList<H> mAdditions = new ArrayList<>();
    private ArrayList<MoveInfo> mMoves = new ArrayList<>();

    private ArrayList<H> mAddAnimations = new ArrayList<>();
    private ArrayList<ViewHolder> mMoveAnimations = new ArrayList<>();
    private ArrayList<H> mRemoveAnimations = new ArrayList<>();

    private static class MoveInfo {
        public ViewHolder holder;
        public int fromX, fromY, toX, toY;

        private MoveInfo(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            this.holder = holder;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    @Override
    public void runPendingAnimations() {
        boolean removalsPending = !mPendingRemovals.isEmpty();
        boolean movesPending = !mPendingMoves.isEmpty();
        boolean additionsPending = !mPendingAdditions.isEmpty();
        if (!removalsPending && !movesPending && !additionsPending) {
            // nothing to animate
            return;
        }
        // First, remove stuff
        runRemoveAnimation();
        // Next, move stuff
        runMoveAnimation(removalsPending, movesPending);
        // Next, add stuff
        runAddAnimation(removalsPending, movesPending, additionsPending);
    }

    protected final void runRemoveAnimation() {
        for (final H holder : mPendingRemovals) {
            animateRemoveImpl(holder).setDuration(getRemoveDuration())
                                     .setListener(new ItemAnimatorListener() {
                                         @Override
                                         public void onAnimationEnd(View view) {
                                             animateRemoveEnded(holder);
                                         }

                                         @Override
                                         public void onAnimationCancel(View view) {
                                             onRemoveCanceled(holder);
                                         }
                                     }).start();
            mRemoveAnimations.add(holder);
        }
        mPendingRemovals.clear();
    }

    protected final void runMoveAnimation(boolean removalsPending, boolean movesPending) {
        if (movesPending) {
            mMoves.addAll(mPendingMoves);
            mPendingMoves.clear();
            Runnable mover = new Runnable() {
                @Override
                public void run() {
                    for (final MoveInfo moveInfo : mMoves) {
                        animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY,
                                moveInfo.toX, moveInfo.toY).setDuration(getMoveDuration())
                                                           .setListener(new ItemAnimatorListener() {
                                                               @Override
                                                               public void onAnimationEnd(View view) {
                                                                   animateMoveEnded(moveInfo.holder);
                                                               }

                                                               @Override
                                                               public void onAnimationCancel(View view) {
                                                                   onMoveCanceled(moveInfo.holder);
                                                               }
                                                           }).start();
                        mMoveAnimations.add(moveInfo.holder);
                    }
                    mMoves.clear();
                }
            };
            if (removalsPending) {
                View view = mMoves.get(0).holder.itemView;
                ViewCompat.postOnAnimationDelayed(view, mover, getRemoveDuration());
            } else {
                mover.run();
            }
        }
    }

    private final void runAddAnimation(boolean removalsPending, boolean movesPending, boolean additionsPending) {
        if (additionsPending) {
            mAdditions.addAll(mPendingAdditions);
            mPendingAdditions.clear();
            Runnable adder = new Runnable() {
                public void run() {
                    for (final H holder : mAdditions) {
                        animateAddImpl(holder).setDuration(getAddDuration())
                                              .setListener(new ItemAnimatorListener() {
                                                  @Override
                                                  public void onAnimationEnd(View view) {
                                                      animateAddEnded(holder);
                                                  }

                                                  @Override
                                                  public void onAnimationCancel(View view) {
                                                      onAddCanceled(holder);
                                                  }
                                              }).start();
                        mAddAnimations.add(holder);
                    }
                    mAdditions.clear();
                }
            };
            if (removalsPending || movesPending) {
                View view = mAdditions.get(0).itemView;
                ViewCompat.postOnAnimationDelayed(view, adder
                        , (removalsPending ? getRemoveDuration() : 0)
                                + (movesPending ? getMoveDuration() : 0));
            } else {
                adder.run();
            }
        }
    }

    @Override
    /** Override prepHolderForAnimateRemove
     *
     * Called when an item is removed from the RecyclerView. Implementors can choose
     * whether and how to animate that change, but must always call
     * {@link #dispatchRemoveFinished(ViewHolder)} when done, either
     * immediately (if no animation will occur) or after the animation actually finishes.
     * The return value indicates whether an animation has been set up and whether the
     * ItemAnimators {@link #runPendingAnimations()} method should be called at the
     * next opportunity. This mechanism allows ItemAnimator to set up individual animations
     * as separate calls to {@link #animateAdd(H) animateAdd()},
     * {@link #animateMove(ViewHolder, int, int, int, int) animateMove()}, and
     * {@link #animateRemove(ViewHolder) animateRemove()} come in one by one, then
     * start the animations together in the later call to {@link #runPendingAnimations()}.
     *
     * <p>This method may also be called for disappearing items which continue to exist in the
     * RecyclerView, but for which the system does not have enough information to animate
     * them out of view. In that case, the default animation for removing items is run
     * on those items as well.</p>
     *
     * @param holder The item that is being removed.
     * @return true if a later call to {@link #runPendingAnimations()} is requested,
     * false otherwise.
     */
    public boolean animateRemove(final ViewHolder holder) {
        mPendingRemovals.add((H) holder);
        return prepHolderForAnimateRemove((H) holder);
    }

    /**
     * Do whatever you need to do before animation like translating X.
     **/
    protected abstract boolean prepHolderForAnimateRemove(H holder);

    /**
     * Preform your animation. Listener will be overridden.
     **/
    protected abstract ViewPropertyAnimatorCompat animateRemoveImpl(H holder);

    /**
     * This should reset the remove animation.
     *
     * @param holder
     **/
    protected abstract void onRemoveCanceled(H holder);

    protected void animateRemoveEnded(H holder) {
        dispatchRemoveFinished(holder);
        mRemoveAnimations.remove(holder);
        dispatchFinishedWhenDone();
    }

    /**
     * Override prepHolderForAnimateAdd
     *
     * Called when an item is added to the RecyclerView. Implementors can choose
     * whether and how to animate that change, but must always call
     * {@link #dispatchAddFinished(ViewHolder)} when done, either
     * immediately (if no animation will occur) or after the animation actually finishes.
     * The return value indicates whether an animation has been set up and whether the
     * ItemAnimators {@link #runPendingAnimations()} method should be called at the
     * next opportunity. This mechanism allows ItemAnimator to set up individual animations
     * as separate calls to {@link #animateAdd(ViewHolder) animateAdd()},
     * {@link #animateMove(ViewHolder, int, int, int, int) animateMove()}, and
     * {@link #animateRemove(ViewHolder) animateRemove()} come in one by one, then
     * start the animations together in the later call to {@link #runPendingAnimations()}.
     *
     * <p>This method may also be called for appearing items which were already in the
     * RecyclerView, but for which the system does not have enough information to animate
     * them into view. In that case, the default animation for adding items is run
     * on those items as well.</p>
     *
     * @param holder The item that is being added.
     * @return true if a later call to {@link #runPendingAnimations()} is requested,
     * false otherwise.
     */
    @Override
    public boolean animateAdd(final ViewHolder holder) {
        mPendingAdditions.add((H) holder);
        return prepHolderForAnimateAdd((H) holder);
    }

    /**
     * Do whatever you need to do before animation like translating X.
     **/
    protected abstract boolean prepHolderForAnimateAdd(H holder);

    /**
     * Preform your animation. Listeners will be overridden
     **/
    protected abstract ViewPropertyAnimatorCompat animateAddImpl(H holder);

    /**
     * This should reset the add animation
     *
     * @param holder
     **/
    protected abstract void onAddCanceled(H holder);

    protected void animateAddEnded(H holder) {
        dispatchAddFinished(holder);
        mAddAnimations.remove(holder);
        dispatchFinishedWhenDone();
    }

    /**
     * Override prepHolderForAnimateMove
     *
     * Called when an item is moved in the RecyclerView. Implementors can choose
     * whether and how to animate that change, but must always call
     * {@link #dispatchMoveFinished(ViewHolder)} when done, either
     * immediately (if no animation will occur) or after the animation actually finishes.
     * The return value indicates whether an animation has been set up and whether the
     * ItemAnimators {@link #runPendingAnimations()} method should be called at the
     * next opportunity. This mechanism allows ItemAnimator to set up individual animations
     * as separate calls to {@link #animateAdd(ViewHolder) animateAdd()},
     * {@link #animateMove(ViewHolder, int, int, int, int) animateMove()}, and
     * {@link #animateRemove(ViewHolder) animateRemove()} come in one by one, then
     * start the animations together in the later call to {@link #runPendingAnimations()}.
     *
     * @param holder The item that is being moved.
     * @return true if a later call to {@link #runPendingAnimations()} is requested,
     * false otherwise.
     */
    @Override
    public boolean animateMove(final ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        mPendingMoves.add(new MoveInfo((H) holder, fromX, fromY, toX, toY));
        return prepHolderForAnimateMove((H) holder, fromX, fromY, toX, toY);
    }

    @Override
    public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder,
                                 int fromLeft, int fromTop, int toLeft, int toTop) {
        return false;
    }

    /**
     * Do whatever you need to do before animation.
     **/
    protected boolean prepHolderForAnimateMove(final H holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder);
            return false;
        }
        if (deltaX != 0) {
            ViewCompat.setTranslationX(view, -deltaX);
        }
        if (deltaY != 0) {
            ViewCompat.setTranslationY(view, -deltaY);
        }
        return true;
    }

    /**
     * Preform your animation. You do not need to override this in most cases cause the default is pretty good.
     * Listeners will be overridden
     **/
    protected ViewPropertyAnimatorCompat animateMoveImpl(final ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        final int deltaX = toX - fromX;
        final int deltaY = toY - fromY;
        ViewCompat.animate(view).cancel();
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0);
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0);
        }
        // TODO: make EndActions end listeners instead, since end actions aren't called when
        // vpas are canceled (and can't end them. why?)
        // need listener functionality in VPACompat for this. Ick.
        return ViewCompat.animate(view).setInterpolator(null).setDuration(getMoveDuration());
    }

    /**
     * This should reset the move animation.
     **/
    protected void onMoveCanceled(ViewHolder holder) {
        ViewCompat.setTranslationX(holder.itemView, 0);
        ViewCompat.setTranslationY(holder.itemView, 0);
    }

    protected void animateMoveEnded(ViewHolder holder) {
        dispatchMoveFinished(holder);
        mMoveAnimations.remove(holder);
        dispatchFinishedWhenDone();
    }

    @Override
    public void endAnimation(ViewHolder item) {
        final View view = item.itemView;
        ViewCompat.animate(view).cancel();
        if (mPendingMoves.contains(item)) {
            ViewCompat.setTranslationY(view, 0);
            ViewCompat.setTranslationX(view, 0);
            dispatchMoveFinished(item);
            mPendingMoves.remove(item);
        }
        if (mPendingRemovals.contains(item)) {
            dispatchRemoveFinished(item);
            mPendingRemovals.remove(item);
        }
        if (mPendingAdditions.contains(item)) {
            dispatchAddFinished(item);
            mPendingAdditions.remove(item);
        }
        if (mMoveAnimations.contains(item)) {
            ViewCompat.setTranslationY(view, 0);
            ViewCompat.setTranslationX(view, 0);
            dispatchMoveFinished(item);
            mMoveAnimations.remove(item);
        }
        if (mRemoveAnimations.contains(item)) {
            dispatchRemoveFinished(item);
            mRemoveAnimations.remove(item);
        }
        if (mAddAnimations.contains(item)) {
            dispatchAddFinished(item);
            mAddAnimations.remove(item);
        }
        dispatchFinishedWhenDone();
    }

    @Override
    public boolean isRunning() {
        return (!mMoveAnimations.isEmpty() ||
                !mRemoveAnimations.isEmpty() ||
                !mAddAnimations.isEmpty() ||
                !mMoves.isEmpty() ||
                !mAdditions.isEmpty());
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
            ViewCompat.animate(view).cancel();
            ViewCompat.setTranslationY(view, 0);
            ViewCompat.setTranslationX(view, 0);
            dispatchMoveFinished(item.holder);
            mPendingMoves.remove(item);
        }
        count = mPendingRemovals.size();
        for (int i = count - 1; i >= 0; i--) {
            H item = mPendingRemovals.get(i);
            dispatchRemoveFinished(item);
            mPendingRemovals.remove(item);
        }
        count = mPendingAdditions.size();
        for (int i = count - 1; i >= 0; i--) {
            H item = mPendingAdditions.get(i);
            dispatchAddFinished(item);
            mPendingAdditions.remove(item);
        }
        if (!isRunning()) {
            return;
        }
        count = mMoveAnimations.size();
        for (int i = count - 1; i >= 0; i--) {
            ViewHolder item = mMoveAnimations.get(i);
            View view = item.itemView;
            ViewCompat.animate(view).cancel();
            ViewCompat.setTranslationY(view, 0);
            ViewCompat.setTranslationX(view, 0);
            dispatchMoveFinished(item);
            mMoveAnimations.remove(item);
        }
        count = mRemoveAnimations.size();
        for (int i = count - 1; i >= 0; i--) {
            H item = mRemoveAnimations.get(i);
            View view = item.itemView;
            ViewCompat.animate(view).cancel();
            dispatchRemoveFinished(item);
            mRemoveAnimations.remove(item);
        }
        count = mAddAnimations.size();
        for (int i = count - 1; i >= 0; i--) {
            H item = mAddAnimations.get(i);
            View view = item.itemView;
            ViewCompat.animate(view).cancel();
            dispatchAddFinished(item);
            mAddAnimations.remove(item);
        }
        mMoves.clear();
        mAdditions.clear();
        dispatchAnimationsFinished();
    }

    /**
     * This class is just convince so that you don't have to override things you don't want to.
     **/
    protected static class ItemAnimatorListener implements ViewPropertyAnimatorListener {

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

    ;
}
