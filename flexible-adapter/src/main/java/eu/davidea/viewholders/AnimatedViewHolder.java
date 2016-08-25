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

public interface AnimatedViewHolder {

	/**
	 * <p>Default value is {@code true}.</p>
	 *
	 * @return {@code true} to confirm the execution of {@link #animateAddImpl(ViewPropertyAnimatorListener, int)},
	 * {@code false} to cancel the animation.
	 */
	boolean preAnimateAddImpl();

	/**
	 * <p>Default value is {@code true}.</p>
	 *
	 * @return {@code true} to confirm the execution of {@link #animateAddImpl(ViewPropertyAnimatorListener, int)},
	 * {@code false} to cancel the animation.
	 */
	boolean preAnimateRemoveImpl();

	/**
	 * @param listener
	 * @param index    order of execution, starts with 0
	 */
	void animateAddImpl(ViewPropertyAnimatorListener listener, int index);

	/**
	 * @param listener
	 * @param index    order of execution, starts with 0
	 */
	void animateRemoveImpl(ViewPropertyAnimatorListener listener, int index);

}