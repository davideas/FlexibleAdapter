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
package eu.davidea.flexibleadapter;

/**
 * Payload occurs only for {@code notifyItemChanged()}.
 * <p>The value of this enumeration will be provided to the bind method to optimize the view
 * binding in order to update only the inner views interested for the change.</p>
 * You can still pass your own <i>Object</i> instead of one of these values.
 *
 * @author Davide Steduto
 * @since 5.0.0-b8 16/07/2016
 */
public enum Payload {
    /** for a general update of the content item */
    CHANGE,
    /** when no more load is triggered */
    NO_MORE_LOAD,
    /** when the filter has changed and item is still visible */
    FILTER,
    /** when header or parent receive back its child after the restoration */
    UNDO,
    /** when a subItem is added to the siblings below a parent */
    ADD_SUB_ITEM,
    /** when a subItem is removed from the siblings below a parent */
    REM_SUB_ITEM,
    /** when item has been moved, the original header/parent receives this payload */
    MOVE,
    /** when linking a header from a sectionable item */
    LINK,
    /** when un-linking a header from a sectionable item */
    UNLINK,
    /** when items are notified due to Selecting All items / Clearing Selection) */
    SELECTION,
    /** when items are notified after a merge */
    MERGE,
    /** when items are notified after a split */
    SPLIT,
    /** when an item is expanded by the user or system */
    EXPANDED,
    /** when an item is collapsed by the user or system */
    COLLAPSED
}