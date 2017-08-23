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

/**
 * When user wants to search through the list, in order to be to collected, an item must implement
 * this interface.
 *
 * @author Davide Steduto
 * @see IFlexible
 * @see IExpandable
 * @see IHeader
 * @see IHolder
 * @see ISectionable
 * @since 18/02/2016 Created
 */
public interface IFilterable {

    /**
     * Checks and performs the filter on this item, you can apply the logic and the filter on
     * every fields your use case foresees.
     * <p><b>Note:</b> Filter method makes use of {@code HashSet}, in case you implemented
     * {@link Object#equals(Object)} you should implement {@link Object#hashCode()} too!</p>
     *
     * @param constraint the search text typed by the user always provided in lowercase
     * @return true if this item should be collected by the Adapter for the filtered list, false otherwise
     */
    boolean filter(String constraint);

}