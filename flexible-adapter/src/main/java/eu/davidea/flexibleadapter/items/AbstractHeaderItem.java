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

import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Generic implementation of {@link IHeader} interface. By default this item is hidden and not
 * selectable.
 * <p>This abstract class extends {@link AbstractFlexibleItem}.</p>
 * The ViewHolder must be of type {@link FlexibleViewHolder} to assure correct StickyHeader
 * behaviours.
 *
 * @param <VH> {@link FlexibleViewHolder}
 * @author Davide Steduto
 * @since 17/01/2016 Created
 * <br>18/06/2016 Changed signature with FlexibleViewHolder
 */
public abstract class AbstractHeaderItem<VH extends FlexibleViewHolder>
        extends AbstractFlexibleItem<VH>
        implements IHeader<VH> {

    /**
     * By default, header is hidden and not selectable
     */
    public AbstractHeaderItem() {
        setHidden(true);
        setSelectable(false);
    }

}