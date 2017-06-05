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
 * Wrapper empty interface to identify if the current item is a header.
 *
 * @author Davide Steduto
 * @see IFlexible
 * @see IExpandable
 * @see IFilterable
 * @see IHolder
 * @see ISectionable
 * @since 15/02/2016 Created
 * <br>18/06/2016 Changed signature with FlexibleViewHolder
 */
public interface IHeader<VH extends FlexibleViewHolder> extends IFlexible<VH> {

}