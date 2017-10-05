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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import eu.davidea.flexibleadapter.items.IFlexible;

@Singleton
public class IFlexibleFactory implements FlexibleItemProvider.Factory {

    private final Map<Class<? extends IFlexible>, Provider<IFlexible>> creators;

    @Inject
    public IFlexibleFactory(Map<Class<? extends IFlexible>, Provider<IFlexible>> creators) {
        this.creators = creators;
    }

    /**
     * Creates a new instance of the given {@code Class}.
     *
     * @param itemClass a {@code Class} whose instance is requested
     * @param model
     * @return a newly created IFlexible item
     */
    @Override
    public <T extends IFlexible, M> T create(Class<T> itemClass, M model) {
        Provider<? extends IFlexible> creator = creators.get(itemClass);
        if (creator == null) {
            for (Map.Entry<Class<? extends IFlexible>, Provider<IFlexible>> entry : creators.entrySet()) {
                if (itemClass.isAssignableFrom(entry.getKey())) {
                    creator = entry.getValue();
                    break;
                }
            }
        }
        if (creator == null) {
            throw new IllegalArgumentException("Unknown model class " + itemClass);
        }
        try {
            return (T) creator.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}