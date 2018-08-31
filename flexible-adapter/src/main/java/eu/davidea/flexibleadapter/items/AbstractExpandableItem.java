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

import java.util.ArrayList;
import java.util.List;

import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Generic implementation of {@link IExpandable} interface with most useful methods to manage
 * expansion and sub items.
 * <p>This abstract class extends {@link AbstractFlexibleItem}.</p>
 *
 * @param <VH> {@link ExpandableViewHolder}
 * @param <S>  The sub item of type {@link IFlexible}
 * @author Davide Steduto
 * @since 17/01/2016 Created
 * <br>18/06/2016 Changed signature with ExpandableViewHolder
 */
public abstract class AbstractExpandableItem<VH extends ExpandableViewHolder, S extends IFlexible>
        extends AbstractFlexibleItem<VH>
        implements IExpandable<VH, S> {

    /* Flags for FlexibleAdapter */
    protected boolean mExpanded = false;

    /* subItems list */
    protected List<S> mSubItems;

	/*--------------------*/
    /* EXPANDABLE METHODS */
	/*--------------------*/

    @Override
    public boolean isExpanded() {
        return mExpanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.mExpanded = expanded;
    }

    @Override
    public int getExpansionLevel() {
        return 0;
    }

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

    @Override
    public final List<S> getSubItems() {
        return mSubItems;
    }

    public final boolean hasSubItems() {
        return mSubItems != null && mSubItems.size() > 0;
    }

    public AbstractExpandableItem setSubItems(List<S> subItems) {
        mSubItems = subItems;
        return this;
    }

    public AbstractExpandableItem addSubItems(int position, List<S> subItems) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.addAll(position, subItems);
        } else {
            if (mSubItems == null)
                mSubItems = new ArrayList<>();
            mSubItems.addAll(subItems);
        }
        return this;
    }

    public final int getSubItemsCount() {
        return mSubItems != null ? mSubItems.size() : 0;
    }

    public S getSubItem(int position) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            return mSubItems.get(position);
        }
        return null;
    }

    public final int getSubItemPosition(S subItem) {
        return mSubItems != null ? mSubItems.indexOf(subItem) : -1;
    }

    public AbstractExpandableItem addSubItem(S subItem) {
        if (mSubItems == null)
            mSubItems = new ArrayList<>();
        mSubItems.add(subItem);
        return this;
    }

    public AbstractExpandableItem addSubItem(int position, S subItem) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.add(position, subItem);
        } else {
            addSubItem(subItem);
        }
        return this;
    }

    public boolean contains(S subItem) {
        return mSubItems != null && mSubItems.contains(subItem);
    }

    public boolean removeSubItem(S item) {
        return item != null && mSubItems != null && mSubItems.remove(item);
    }

    public boolean removeSubItems(List<S> subItems) {
        return subItems != null && mSubItems != null && mSubItems.removeAll(subItems);
    }

    public boolean removeSubItem(int position) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.remove(position);
            return true;
        }
        return false;
    }

}