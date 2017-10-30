/*
 * Copyright (C) 2016-2017 Davide Steduto
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
package eu.davidea.viewholders;

import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView;

import eu.davidea.flexibleadapter.common.FlexibleItemAnimator;

/**
 * Interface for {@code itemView} addition/removal animation.
 * <p>Used by {@link FlexibleItemAnimator} when notify events occur. If any of these methods
 * are implemented, the ItemAnimator extending FlexibleItemAnimator is skipped in favour of this
 * ViewHolder implementation.</p>
 *
 * @author Davide Steduto
 * @see FlexibleItemAnimator
 * @since 26/08/2016 Created
 */
public interface AnimatedViewHolder {

    /**
     * Prepares the View for Add Animation. If this method is implemented and returns
     * {@code true}, then this method is performed against
     * {@link FlexibleItemAnimator#preAnimateAddImpl(RecyclerView.ViewHolder)} which will be ignored.
     * <p>Default value is {@code false}.</p>
     *
     * @return {@code true} to confirm the execution of {@link #animateAddImpl(ViewPropertyAnimatorListener, long, int)},
     * of this class, {@code false} to use generic animation for all types of View.
     * @since 5.0.0-b8
     */
    boolean preAnimateAddImpl();

    /**
     * Prepares the View for Remove Animation. If this method is implemented and returns
     * {@code true}, then this method is performed against
     * {@link FlexibleItemAnimator#preAnimateRemoveImpl(RecyclerView.ViewHolder)} which will be ignored.
     * <p>Default value is {@code false}.</p>
     *
     * @return {@code true} to confirm the execution of {@link #animateRemoveImpl(ViewPropertyAnimatorListener, long, int)},
     * of this class, {@code false} to use generic animation for all types of View.
     * @since 5.0.0-b8
     */
    boolean preAnimateRemoveImpl();

    /**
     * Animates this ViewHolder with this specific Add Animation.
     * <p>By returning {@code true} this ViewHolder will perform customized animation, while by
     * returning {@code false} generic animation is applied also for this ViewHolder.</p>
     *
     * @param listener    should assign to {@code ViewCompat.animate().setListener(listener)}
     * @param addDuration duration of add animation
     * @param index       order of execution, starts with 0
     * @return {@code true} to animate with this implementation, {@code false} to use the generic
     * animation.
     * @since 5.0.0-b8
     */
    boolean animateAddImpl(ViewPropertyAnimatorListener listener, long addDuration, int index);

    /**
     * Animates this ViewHolder with this specific Remove Animation.
     * <p>By returning {@code true} this ViewHolder will perform customized animation, while by
     * returning {@code false} generic animation is applied also for this ViewHolder.</p>
     *
     * @param listener       should assign to {@code ViewCompat.animate().setListener(listener)}
     * @param removeDuration duration of remove animation
     * @param index          order of execution, starts with 0  @return {@code true} to animate with this implementation, {@code false} to use the generic
     *                       animation.
     * @since 5.0.0-b8
     */
    boolean animateRemoveImpl(ViewPropertyAnimatorListener listener, long removeDuration, int index);

}