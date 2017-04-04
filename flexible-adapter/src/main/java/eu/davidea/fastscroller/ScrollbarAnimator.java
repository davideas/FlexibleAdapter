package eu.davidea.fastscroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public class ScrollbarAnimator {
    private static final String PROPERTY_NAME = "translationX";
    protected View bar;
    protected View handle;
    protected AnimatorSet scrollbarAnimatorSet;

    protected long delayInMillis;
    protected long durationInMillis;

    private boolean isAnimating;

    public ScrollbarAnimator(View bar, View handle, long delayInMillis, long durationInMillis) {
        this.bar = bar;
        this.handle = handle;
        this.delayInMillis = delayInMillis;
        this.durationInMillis = durationInMillis;
    }

    public void setDelayInMillis(long delayInMillis) {
        this.delayInMillis = delayInMillis;
    }

    public void showScrollbar() {
        if (bar == null || handle == null) {
            return;
        }

        if (isAnimating) {
            scrollbarAnimatorSet.cancel();
        }

        if (bar.getVisibility() == View.INVISIBLE || handle.getVisibility() == View.INVISIBLE) {
            bar.setVisibility(View.VISIBLE);
            handle.setVisibility(View.VISIBLE);

            scrollbarAnimatorSet = createAnimator(bar, handle, true);
            scrollbarAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    onShowAnimationStop(bar, handle);
                    isAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    onShowAnimationStop(bar, handle);
                    isAnimating = false;
                }
            });
            scrollbarAnimatorSet.start();
            isAnimating = true;
        }
    }

    public void hideScrollbar() {
        if (bar == null || handle == null) {
            return;
        }

        if (isAnimating) {
            scrollbarAnimatorSet.cancel();
        }

        scrollbarAnimatorSet = createAnimator(bar, handle, false);
        scrollbarAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                onHideAnimationStop(bar, handle);
                isAnimating = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                onHideAnimationStop(bar, handle);
                isAnimating = false;
            }
        });
        scrollbarAnimatorSet.start();
        isAnimating = true;
    }

    protected AnimatorSet createAnimator(View bar, View handle, boolean showFlag) {
        ObjectAnimator barAnimator = ObjectAnimator.ofFloat(bar, PROPERTY_NAME, showFlag ? 0 : bar.getWidth());
        ObjectAnimator handleAnimator = ObjectAnimator.ofFloat(handle, PROPERTY_NAME, showFlag ? 0 : handle.getWidth());

        AnimatorSet animator = new AnimatorSet();
        animator.playTogether(barAnimator, handleAnimator);
        animator.setDuration(durationInMillis);
        if (!showFlag) {
            animator.setStartDelay(delayInMillis);
        }

        return animator;
    }

    protected void onShowAnimationStop(View bar, View handle) {
        // do something
    }

    protected void onHideAnimationStop(View bar, View handle) {
        bar.setVisibility(View.INVISIBLE);
        handle.setVisibility(View.INVISIBLE);
        bar.setTranslationX(0);
        handle.setTranslationX(0);
    }
}
