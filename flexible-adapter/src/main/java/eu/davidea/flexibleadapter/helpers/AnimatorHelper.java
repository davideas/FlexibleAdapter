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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * @author Davide Steduto
 * @since 05/09/2016
 */
public class AnimatorHelper {

	protected static final String TAG = AnimatorHelper.class.getSimpleName();

//	private enum AnimatorEnum {
//		ALPHA, SLIDE_IN_LEFT, SLIDE_IN_RIGHT, SLIDE_IN_BOTTOM, SLIDE_IN_TOP, SCALE
//	}

	/*-----------*/
	/* ANIMATORS */
	/*-----------*/

	/**
	 * Item will slide from Left to Right.
	 *
	 * @param animators user defined list
	 * @param view      itemView to animate
	 * @param percent   any % multiplier (between 0 and 1) of the LayoutManager Width
	 * @since 5.0.0-b1
	 */
	public static void slideInFromLeftAnimator(
			@NonNull List<Animator> animators, @NonNull View view,
			RecyclerView recyclerView, @FloatRange(from = 0.0, to= 1.0) float percent) {
		animators.add(ObjectAnimator.ofFloat(view, "translationX", -recyclerView.getLayoutManager().getWidth() * percent, 0));
		if (FlexibleAdapter.DEBUG) Log.v(TAG, "Added LEFT Animator");
	}

	/**
	 * Item will slide from Right to Left.
	 *
	 * @param animators user defined list
	 * @param view      ItemView to animate
	 * @param percent   Any % multiplier (between 0 and 1) of the LayoutManager Width
	 * @since 5.0.0-b1
	 */
	public static void slideInFromRightAnimator(
			@NonNull List<Animator> animators, @NonNull View view,
			RecyclerView recyclerView, @FloatRange(from = 0.0, to = 1.0) float percent) {
		animators.add(ObjectAnimator.ofFloat(view, "translationX", recyclerView.getLayoutManager().getWidth() * percent, 0));
		if (FlexibleAdapter.DEBUG) Log.v(TAG, "Added RIGHT Animator");
	}

	/**
	 * Item will slide from Top of the screen to its natural position.
	 *
	 * @param animators user defined list
	 * @param view      itemView to animate
	 * @since 5.0.0-b7
	 */
	public static void slideInFromTopAnimator(
			@NonNull List<Animator> animators, @NonNull View view,
			RecyclerView recyclerView) {
		animators.add(ObjectAnimator.ofFloat(view, "translationY", -recyclerView.getMeasuredHeight() >> 1, 0));
		if (FlexibleAdapter.DEBUG) Log.v(TAG, "Added TOP Animator");
	}

	/**
	 * Item will slide from Bottom of the screen to its natural position.
	 *
	 * @param animators user defined list
	 * @param view      itemView to animate
	 * @since 5.0.0-b1
	 */
	public static void slideInFromBottomAnimator(
			@NonNull List<Animator> animators, @NonNull View view,
			RecyclerView recyclerView) {
		animators.add(ObjectAnimator.ofFloat(view, "translationY", recyclerView.getMeasuredHeight() >> 1, 0));
		if (FlexibleAdapter.DEBUG) Log.v(TAG, "Added BOTTOM Animator");
	}

	/**
	 * Item will scale to {@code 1.0f}.
	 *
	 * @param animators user defined list
	 * @param view      itemView to animate
	 * @param scaleFrom initial scale value
	 * @since 5.0.0-b1
	 */
	public static void scaleAnimator(
			@NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.0, to = 1.0) float scaleFrom) {
		animators.add(ObjectAnimator.ofFloat(view, "scaleX", scaleFrom, 1f));
		animators.add(ObjectAnimator.ofFloat(view, "scaleY", scaleFrom, 1f));
		if (FlexibleAdapter.DEBUG) Log.v(TAG, "Added SCALE Animator");
	}

}