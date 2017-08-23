/*
 * Copyright 2017 Davide Steduto & Arpinca
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
package eu.davidea.fastscroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

public class BubbleAnimator {

    protected ObjectAnimator animator;
    protected View bubble;
    protected long durationInMillis;
    private boolean isAnimating;

    public BubbleAnimator(View bubble, long durationInMillis) {
        this.bubble = bubble;
        this.durationInMillis = durationInMillis;
    }

    public void showBubble() {
        if (bubble == null) {
            return;
        }

        if (isAnimating) {
            animator.cancel();
        }

        if (bubble.getVisibility() != View.VISIBLE) {
            bubble.setVisibility(View.VISIBLE);
            if (isAnimating) {
                animator.cancel();
            }
            animator = createShowAnimator(bubble);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    onShowAnimationStop(bubble);
                    isAnimating = false;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    onShowAnimationStop(bubble);
                    isAnimating = false;
                }
            });

            animator.start();
            isAnimating = true;
        }
    }

    public void hideBubble() {
        if (bubble == null) {
            return;
        }

        if (isAnimating) {
            animator.cancel();
        }

        animator = createHideAnimator(bubble);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                onHideAnimationStop(bubble);
                isAnimating = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                onHideAnimationStop(bubble);
                isAnimating = false;
            }
        });

        animator.start();
        isAnimating = true;
    }

    protected ObjectAnimator createShowAnimator(View bubble) {
        return ObjectAnimator.ofFloat(bubble, "alpha", 0f, 1f).setDuration(durationInMillis);
    }

    protected ObjectAnimator createHideAnimator(View bubble) {
        return ObjectAnimator.ofFloat(bubble, "alpha", 1f, 0f).setDuration(durationInMillis);
    }

    protected void onShowAnimationStop(View bubble) {
        // do nothing
    }

    protected void onHideAnimationStop(View bubble) {
        bubble.setVisibility(View.INVISIBLE);
    }

}