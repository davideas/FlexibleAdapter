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
package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

/**
 * Generic implementation of {@link IExpandable} interface combined with {@link IHeader} interface
 * with most useful methods to manage expandable sections with sticky headers and sub items of
 * type {@link ISectionable}.<br/>
 * This abstract class extends also {@link AbstractExpandableItem}.
 * <p>Call {@code super()} in the constructor to auto-configure the section status as shown,
 * expanded, not selectable.</p>
 *
 * @author Davide Steduto
 * @since 01/04/2016 Created
 */
public abstract class AbstractExpandableHeaderItem<VH extends RecyclerView.ViewHolder, S extends ISectionable>
		extends AbstractExpandableItem<VH, S>
		implements IHeader<VH> {

	/**
	 * By default, expandable header is shown, expanded and not selectable.
	 */
	public AbstractExpandableHeaderItem() {
		setHidden(false);
		setExpanded(true);
		setSelectable(false);
	}

}