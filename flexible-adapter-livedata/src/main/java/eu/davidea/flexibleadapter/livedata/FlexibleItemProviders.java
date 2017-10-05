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

/**
 * Generic provider for a specific factory of {@code IFlexible} items.
 *
 * @author Davide Steduto
 * @since 05/10/2017
 */
public class FlexibleItemProviders {

    private FlexibleItemProviders() {
    }

    /**
     * Creates a {@link FlexibleItemProvider}.
     * <p>
     * It uses the given {@link FlexibleItemProvider.Factory} to instantiate new IFlexible items.
     *
     * @param factory a {@code Factory} to instantiate new IFlexible items
     * @return a FlexibleItemProvider instance
     */
    @MainThread
    public static FlexibleItemProvider of(@NonNull FlexibleItemProvider.Factory factory) {
        return new FlexibleItemProvider(factory);
    }

}