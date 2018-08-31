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

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Generic ViewModel for any Adapter which loads the source of type {@code Source} and
 * generates the relative List of items of type {@code AdapterItem} as output in LiveData
 * observable object.
 * <p>Source loading is triggered by providing a LiveData "identifier" which this ViewModel reacts
 * when value changes.</p>
 * The identifier holds the keys to identify the source of data desired and normally implements
 * {@code equals()} and {@code hashCode()} properly.
 *
 * @param <Source>      the type of <b>input</b>, for instance {@code List<Model>} or {@code Resource<List<Model>}.
 * @param <AdapterItem> the type of <b>output</b> for the Adapter list served in LiveData observable object.
 * @param <Identifier>  the type of <b>identifier</b> as trigger for loading the {@code Source}.
 * @author Davide Steduto
 * @since 05/10/2017
 */
public abstract class FlexibleViewModel<Source, AdapterItem, Identifier> extends ViewModel {

    protected LiveData<List<AdapterItem>> liveItems;
    protected MutableLiveData<Identifier> identifier;

    public FlexibleViewModel() {
        identifier = new MutableLiveData<>();
        liveItems = Transformations.switchMap(identifier, new Function<Identifier, LiveData<List<AdapterItem>>>() {
            @Override
            public LiveData<List<AdapterItem>> apply(Identifier input) {
                return Transformations.map(getSource(input), new Function<Source, List<AdapterItem>>() {
                    @Override
                    public List<AdapterItem> apply(Source source) {
                        if (isSourceValid(source)) {
                            return map(source);
                        } else {
                            return liveItems.getValue();
                        }
                    }
                });
            }
        });
    }

    /**
     * Provides "live" items to observe as input for the Adapter.
     *
     * @return the LiveData to observe with the list of items for the any Adapter.
     */
    @NonNull
    public LiveData<List<AdapterItem>> getLiveItems() {
        return liveItems;
    }

    /**
     * Triggers the loading of the {@code Source} only if the value is <u>not</u> equals to the
     * previous value.
     *
     * @param identifier the {@code Source} identifier to provide to the repository
     */
    public void loadSource(@NonNull Identifier identifier) {
        if (!identifier.equals(this.identifier.getValue())) {
            this.identifier.setValue(identifier);
        }
    }

    /**
     * Retrieves the LiveData coming from <i>Local</i> or <i>Remote</i> repository.
     *
     * @param identifier the {@code Source} identifier to provide to the repository
     * @return the LiveData, input for the mapping
     */
    @NonNull
    protected abstract LiveData<Source> getSource(@NonNull Identifier identifier);

    /**
     * Checks if resource is valid before mapping the items.
     * <p>Should be implemented by checking, at least, if the {@code Source} is <u>not</u>
     * {@code null} and if the original list is <u>not</u> empty.</p>
     *
     * @param source the type of input {@code Source} LiveData containing the original list
     * @return {@code true} if source is valid, {@code false} otherwise
     */
    protected abstract boolean isSourceValid(@Nullable Source source);

    /**
     * Maps the {@code Source} containing the original list to a list suitable for the Adapter.
     * <p><b>Tip:</b> User can use a custom implementation of
     * {@link eu.davidea.flexibleadapter.livedata.FlexibleItemProvider.Factory} that together with
     * {@link FlexibleItemProvider} will help to map the model to an Adapter item.</p>
     *
     * @param source the type of input {@code Source} LiveData containing the original list
     * @return the mapped list suitable for the Adapter.
     */
    @MainThread
    protected abstract List<AdapterItem> map(@NonNull Source source);

}