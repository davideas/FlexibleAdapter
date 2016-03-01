/*
 * Copyright 2015-2016 Davide Steduto
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
package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

/**
 * Abstract class for items that holds a header item.
 *
 * @param <VH>
 * @param <T>
 */
public abstract class AbstractSectionableItem<VH extends RecyclerView.ViewHolder, T extends IHeader>
		extends AbstractFlexibleItem<VH>
		implements ISectionable<VH, T> {

	/**
	 * The header of this item
	 */
	protected T header;

	@Override
	public T getHeader() {
		return header;
	}

	@Override
	public IFlexible setHeader(T header) {
		this.header = header;
		return this;
	}

}