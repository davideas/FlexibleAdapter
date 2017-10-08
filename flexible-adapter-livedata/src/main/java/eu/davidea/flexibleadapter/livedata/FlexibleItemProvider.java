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

import eu.davidea.flexibleadapter.items.IFlexible;

/**
 * Generic item providers for any {@code IFlexible} items.
 *
 * @param <Model>    the type the original item
 * @param <Flexible> the IFlexible item for the list to provide to the adapter
 * @author Davide Steduto
 * @since 05/10/2017
 */
public class FlexibleItemProvider<Model, Flexible> {

    private final Factory<Model, Flexible> mFactory;

    /**
     * @param factory the IFlexible item creator
     */
    private FlexibleItemProvider(Factory<Model, Flexible> factory) {
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
     * Creates a new list of {@link IFlexible} items.
     *
     * @param source the list with original items
     * @return A List of {@code IFlexible} items.
     */
    @NonNull
    @MainThread
    public List<Flexible> from(List<Model> source) {
        List<Flexible> items = new ArrayList<>();
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