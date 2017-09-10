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
package eu.davidea.flexibleadapter.helpers;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import eu.davidea.flexibleadapter.utils.Log;

/**
 * @author Davide Steduto
 * @since 05/09/2016
 */
public class AnimatorHelper {

	/*-----------*/
    /* ANIMATORS */
	/*-----------*/

    /**
     * This is the default animator.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     * @param alphaFrom starting alpha value
     * @since 5.0.0-b1
     */
    public static void alphaAnimator(
            @NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.0, to = 1.0) float alphaFrom) {
        view.setAlpha(0);
        animators.add(ObjectAnimator.ofFloat(view, "alpha", alphaFrom, 1f));
    }

    /**
     * Item will slide from Left to Right.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     * @param percent   any % multiplier (between 0 and 1) of the LayoutManager Width
     * @since 5.0.0-b1
     */
    public static void slideInFromLeftAnimator(
            @NonNull List<Animator> animators, @NonNull View view,
            RecyclerView recyclerView, @FloatRange(from = 0.0, to = 1.0) float percent) {
        alphaAnimator(animators, view, 0f);
        animators.add(ObjectAnimator.ofFloat(view, "translationX", -recyclerView.getLayoutManager().getWidth() * percent, 0));
        Log.v("Added LEFT Animator");
    }

    /**
     * Item will slide from Right to Left.
     *
     * @param animators user defined list of animators
     * @param view      ItemView to animate
     * @param percent   Any % multiplier (between 0 and 1) of the LayoutManager Width
     * @since 5.0.0-b1
     */
    public static void slideInFromRightAnimator(
            @NonNull List<Animator> animators, @NonNull View view,
            RecyclerView recyclerView, @FloatRange(from = 0.0, to = 1.0) float percent) {
        alphaAnimator(animators, view, 0f);
        animators.add(ObjectAnimator.ofFloat(view, "translationX", recyclerView.getLayoutManager().getWidth() * percent, 0));
        Log.v("Added RIGHT Animator");
    }

    /**
     * Item will slide from Top of the screen to its natural position.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     * @since 5.0.0-b7
     */
    public static void slideInFromTopAnimator(
            @NonNull List<Animator> animators, @NonNull View view,
            RecyclerView recyclerView) {
        alphaAnimator(animators, view, 0f);
        animators.add(ObjectAnimator.ofFloat(view, "translationY", -recyclerView.getMeasuredHeight() >> 1, 0));
        Log.v("Added TOP Animator");
    }

    /**
     * Item will slide from Bottom of the screen to its natural position.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     * @since 5.0.0-b1
     */
    public static void slideInFromBottomAnimator(
            @NonNull List<Animator> animators, @NonNull View view,
            RecyclerView recyclerView) {
        alphaAnimator(animators, view, 0f);
        animators.add(ObjectAnimator.ofFloat(view, "translationY", recyclerView.getMeasuredHeight() >> 1, 0));
        Log.v("Added BOTTOM Animator");
    }

    /**
     * Item will scale to {@code 1.0f}.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     * @param scaleFrom initial scale value
     * @since 5.0.0-b1
     */
    public static void scaleAnimator(
            @NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.0, to = 1.0) float scaleFrom) {
        alphaAnimator(animators, view, 0f);
        animators.add(ObjectAnimator.ofFloat(view, "scaleX", scaleFrom, 1f));
        animators.add(ObjectAnimator.ofFloat(view, "scaleY", scaleFrom, 1f));
        Log.v("Added SCALE Animator");
    }

    /**
     * Item will flip from {@code 0.0f} to {@code 1.0f}.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     * @since 5.0.0-rc1
     */
    public static void flipAnimator(@NonNull List<Animator> animators, @NonNull View view) {
        alphaAnimator(animators, view, 0f);
        animators.add(ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f));
        Log.v("Added FLIP Animator");
    }

    /**
     * Adds a custom duration to the current view.
     *
     * @param animators user defined list of animators
     * @param duration  duration in milliseconds
     */
    public static void setDuration(@NonNull List<Animator> animators, @IntRange(from = 0) long duration) {
        if (animators.size() > 0) {
            Animator animator = animators.get(animators.size() - 1);
            animator.setDuration(duration);
        }
    }

}