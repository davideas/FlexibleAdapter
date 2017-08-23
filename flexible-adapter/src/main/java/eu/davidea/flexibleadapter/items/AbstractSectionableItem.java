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
 * Generic implementation of {@link ISectionable} interface for items that hold a header item.
 * <p>This abstract class extends {@link AbstractFlexibleItem}.</p>
 *
 * @param <VH> {@link android.support.v7.widget.RecyclerView.ViewHolder}
 * @param <H>  The header item of type {@link IHeader}
 * @author Davide Steduto
 * @since 20/01/2016 Created
 */
public abstract class AbstractSectionableItem<VH extends RecyclerView.ViewHolder, H extends IHeader>
        extends AbstractFlexibleItem<VH>
        implements ISectionable<VH, H> {

    /**
     * The header of this item
     */
    protected H header;

    public AbstractSectionableItem(H header) {
        this.header = header;
    }

    @Override
    public H getHeader() {
        return header;
    }

    @Override
    public void setHeader(H header) {
        this.header = header;
    }

}