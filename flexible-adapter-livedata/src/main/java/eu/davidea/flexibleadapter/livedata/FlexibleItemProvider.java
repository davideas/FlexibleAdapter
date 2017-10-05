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

import java.util.List;

import eu.davidea.flexibleadapter.items.IFlexible;

/**
 * Generic item Providers for a {@code IFlexible} items.
 *
 * @author Davide Steduto
 * @since 05/10/2017
 */
public class FlexibleItemProvider {

    private final Factory mFactory;

    FlexibleItemProvider(Factory factory) {
        mFactory = factory;
    }

    /**
     * Creates a new {@link IFlexible} item.
     *
     * @param itemClass The class of the ViewModel to create an instance of it if.
     * @param <T>       The type parameter for the {@code IFlexible} item.
     * @return A {@code IFlexible} item that is an instance of the given type {@code T}.
     */
    @NonNull
    @MainThread
    public <T extends IFlexible, M> T map(@NonNull M model, @NonNull Class<T> itemClass) {
        return mFactory.create(itemClass, model);
    }

    private boolean isSourceListValid(List<?> source) {
        return source != null && !source.isEmpty();
    }

    /**
     * Implementations of {@code Factory} interface are responsible to instantiate IFlexible items.
     */
    public interface Factory {
        /**
         * Creates a new instance of the given {@code Class}.
         *
         * @param itemClass a {@code Class} whose instance is requested
         * @param <T>       the type parameter for the IFlexible item.
         * @return a newly created IFlexible item
         */
        <T extends IFlexible, M> T create(Class<T> itemClass, M model);
    }

}