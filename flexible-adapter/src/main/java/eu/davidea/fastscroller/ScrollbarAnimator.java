package eu.davidea.fastscroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public class ScrollbarAnimator {
    protected View bar;
    protected View handle;
    protected AnimatorSet scrollbarAnimator;

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
            scrollbarAnimator.cancel();
        }

        if (bar.getVisibility() == View.INVISIBLE || handle.getVisibility() == View.INVISIBLE) {
            bar.setVisibility(View.VISIBLE);
            handle.setVisibility(View.VISIBLE);

            scrollbarAnimator = createShowAnimator(bar, handle);
            scrollbarAnimator.addListener(new AnimatorListenerAdapter() {
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
            scrollbarAnimator.start();
            isAnimating = true;
        }
    }

    public void hideScrollbar() {
        if (bar == null || handle == null) {
            return;
        }

        if (scrollbarAnimator != null) {
            scrollbarAnimator.cancel();
        }

       scrollbarAnimator = createHideAnimator(bar, handle);
        scrollbarAnimator.addListener(new AnimatorListenerAdapter() {
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
        scrollbarAnimator.start();
        isAnimating = true;
    }

    protected AnimatorSet createShowAnimator(View bar, View handle) {
        ObjectAnimator barAnimator = ObjectAnimator.ofFloat(bar, "translationX", 0);
        ObjectAnimator handleAnimator = ObjectAnimator.ofFloat(handle, "translationX", 0);
        AnimatorSet scrollbarAnimator = new AnimatorSet();
        scrollbarAnimator.playTogether(barAnimator, handleAnimator);
        scrollbarAnimator.setDuration(durationInMillis);

        return scrollbarAnimator;
    }

    protected AnimatorSet createHideAnimator(View bar, View handle) {
        ObjectAnimator barAnimator = ObjectAnimator.ofFloat(bar, "translationX", bar.getWidth());
        ObjectAnimator handleAnimator = ObjectAnimator.ofFloat(handle, "translationX", handle.getWidth());

        AnimatorSet scrollbarAnimator = new AnimatorSet();
        scrollbarAnimator.playTogether(barAnimator, handleAnimator);
        scrollbarAnimator.setDuration(durationInMillis);
        scrollbarAnimator.setStartDelay(delayInMillis);

        return scrollbarAnimator;
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
