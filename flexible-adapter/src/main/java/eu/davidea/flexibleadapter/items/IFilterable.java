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

import java.io.Serializable;

/**
 * When user wants to search through the list, an item, in order to be to collected, must implement
 * this interface. The filter object can be of any type and must implement Serializable type
 * to support configuration changes, for example: String.
 *
 * @author Davide Steduto
 * @see IFlexible
 * @see IExpandable
 * @see IHeader
 * @see IHolder
 * @see ISectionable
 * @since 18/02/2016 Created
 */
public interface IFilterable<F extends Serializable> {

    /**
     * Checks and performs the filter on this item. Filter object can be of any type: you can
     * apply any filter your use case foresees.
     * <p><b>Note:</b> Filter method makes use of {@code HashSet}, in case you implemented
     * {@link Object#equals(Object)} you should implement {@link Object#hashCode()} too!</p>
     *
     * @param constraint the filter applied by the user. In case of String it's always lowercase.
     * @return true if this item should be collected by the Adapter for the filtered list, false otherwise
     */
    boolean filter(F constraint);

}