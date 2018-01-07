/*
 * Copyright 2017 Davide Steduto
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
package eu.davidea.flexibleadapter.livedata;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Generic items providers for any {@code Adapter}.
 * <p>This extension is a complement to the new
 * <a href="https://developer.android.com/topic/libraries/architecture/index.html">Android Architecture Components</a>
 * able to simplify and customize the implementation of the ViewModel responsible for
 * preparing data (items) for <i>any</i> Adapter.</p>
 *
 * @param <Model>       the type of the model item coming from the repository
 * @param <AdapterItem> the AdapterItem item for the list to provide to the Adapter
 * @author Davide Steduto
 * @since 05/10/2017
 */
public class FlexibleItemProvider<Model, AdapterItem> {

    private final Factory<Model, AdapterItem> mFactory;

    /**
     * @param factory the AdapterItem item creator
     */
    private FlexibleItemProvider(Factory<Model, AdapterItem> factory) {
        mFactory = factory;
    }

    /**
     * Creates a {@link FlexibleItemProvider}.
     * <p>It uses the given {@link FlexibleItemProvider.Factory} to instantiate new IFlexible
     * items.</p>
     *
     * @param factory a {@code Factory} to instantiate new IFlexible items
     * @return a FlexibleItemProvider instance
     */
    @MainThread
    public static <Model, Flexible> FlexibleItemProvider<Model, Flexible> with
    (@NonNull FlexibleItemProvider.Factory<Model, Flexible> factory) {
        return new FlexibleItemProvider<>(factory);
    }

    /**
     * Creates a new list of {@code IFlexible} items.
     *
     * @param source the list with original items
     * @return A List of {@code IFlexible} items.
     */
    @NonNull
    @MainThread
    public List<AdapterItem> from(List<Model> source) {
        List<AdapterItem> items = new ArrayList<>();
        if (isSourceValid(source)) {
            for (Model model : source) {
                AdapterItem item = mFactory.create(model);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * Creates a new list of sorted {@code IFlexible} items.
     *
     * @param source     the list with original items
     * @param comparator comparator to sort the items
     * @return A List of {@code IFlexible} items.
     */
    @NonNull
    @MainThread
    public List<AdapterItem> from(List<Model> source, Comparator<AdapterItem> comparator) {
        List<AdapterItem> items = from(source);
        if (comparator != null) Collections.sort(items, comparator);
        return items;
    }

    private boolean isSourceValid(List<?> source) {
        return source != null && !source.isEmpty();
    }

    public interface Factory<Model, Flexible> {
        /**
         * Creates an Adapter item starting from the model object.
         * <p>For more complex list, such Header, Sectionable, Expandable with sub-items, this method must
         * return the main items.<br>For instance if we have <i>always</i> Sectionable objects,
         * we <b>WILL</b> call {@code mAdapter.setDisplayHeadersAtStartUp(true)}:
         * <pre>
         * if (model.isSectionable()) {
         *     Header header = mHeadersMap.get(model.getHeaderId());
         *     return new Sectionable(model, header);
         * }
         * return null;
         * </pre>
         * If instead, your use case <i>may</i> have empty sections: Header instance must be forced <u>not hidden</u>
         * and returned, we <b>WILL</b> call {@code mAdapter.setHeadersShown(true)} and <b>WON'T</b> call
         * {@code mAdapter.setDisplayHeadersAtStartUp(true)}:
         * <pre>
         * if (model.isHeader()) {
         *     Header header = mHeadersMap.get(model.getId());
         *     header.setHidden(false); // or in its constructor
         *     return header;
         * } else { //is a sectionable
         *     Header header = mHeadersMap.get(model.getHeaderId());
         *     return new Sectionable(model, header);
         * }
         * </pre>
         * <b>Note:</b> {@code mHeadersMap} is a HashMap field in this Factory instance previously built only
         * with headers and is <b>optional</b>: depends if you have correctly implemented the {@code equals()}
         * and {@code hashCode()} methods, but is preferable to have a unique instance of each header!
         *
         * @param model the original model object from Repository
         * @return The Flexible item, or null to skip it, if the item will be added automatically by the Adapter
         */
        @Nullable
        Flexible create(Model model);
    }

}