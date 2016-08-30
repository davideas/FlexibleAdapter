/*
 * Copyright (C) 2015 Davide Steduto
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

import eu.davidea.flexibleadapter.common.BaseItemAnimator;

public interface AnimatedViewHolder {

	/**
	 * Prepares the View for Add Animation. If this method is implemented and returns {@code true},
	 * then this method is performed against the {@link BaseItemAnimator#preAnimateAddImpl(RecyclerView.ViewHolder)}
	 * which will be ignored.
	 * <p/>
	 * <p>Default value is {@code false}.</p>
	 *
	 * @return {@code true} to confirm the execution of {@link #animateAddImpl(ViewPropertyAnimatorListener, int)},
	 * {@code false} to use generic animation for all types of View.
	 */
	boolean preAnimateAddImpl();

	/**
	 * <p>Default value is {@code false}.</p>
	 *
	 * @return {@code true} to confirm the execution of {@link #animateRemoveImpl(ViewPropertyAnimatorListener, int)},
	 * {@code false} to cancel the animation.
	 */
	boolean preAnimateRemoveImpl();

	/**
	 * By returning {@code true} this ViewHolder will perform customized animation, while by
	 * returning {@code false} generic animation is applied also for this ViewHolder.
	 *
	 * @param listener
	 * @param index    order of execution, starts with 0
	 * @return
	 */
	boolean animateAddImpl(ViewPropertyAnimatorListener listener, int index);

	/**
	 * @param listener
	 * @param index    order of execution, starts with 0
	 * @return
	 */
	boolean animateRemoveImpl(ViewPropertyAnimatorListener listener, int index);

}