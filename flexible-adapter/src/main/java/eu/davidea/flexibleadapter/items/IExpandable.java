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

import java.util.List;

import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Interface to manage expanding operations on items with
 * {@link eu.davidea.flexibleadapter.FlexibleAdapter}.
 * <p>Implements this interface or use {@link AbstractExpandableItem}.</p>
 *
 * @author Davide Steduto
 * @see IFlexible
 * @see IFilterable
 * @see IHeader
 * @see IHolder
 * @see ISectionable
 * @since 17/01/2016 Created
 * <br>18/06/2016 Changed signature with ExpandableViewHolder
 */
public interface IExpandable<VH extends ExpandableViewHolder, S extends IFlexible>
        extends IFlexible<VH> {

	/*--------------------*/
    /* EXPANDABLE METHODS */
	/*--------------------*/

    boolean isExpanded();

    void setExpanded(boolean expanded);

    /**
     * Establish the level of the expansion of this type of item in case of multi level expansion.
     * <p>Default value of first level should return 0.</p>
     * Sub expandable items should return a level +1 for each sub level.
     *
     * @return the level of the expansion of this type of item
     */
    int getExpansionLevel();

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

    List<S> getSubItems();

}