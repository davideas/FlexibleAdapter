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

import java.util.ArrayList;
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
                items.add(mFactory.create(model));
            }
        }
        return items;
    }

    private boolean isSourceValid(List<?> source) {
        return source != null && !source.isEmpty();
    }

    public interface Factory<Model, Flexible> {
        @NonNull
        Flexible create(Model model);
    }

}