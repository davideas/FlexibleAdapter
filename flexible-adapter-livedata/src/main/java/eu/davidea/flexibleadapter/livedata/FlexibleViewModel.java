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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic ViewModel for any Adapter which monitor the Source of type {@code I} in input and
 * generates a list of items of type {@code O} as output.
 *
 * @param <I> the input Source
 * @param <O> the model type for the list of items.
 * @author Davide Steduto
 * @since 05/10/2017
 */
public abstract class FlexibleViewModel<I, O> extends ViewModel {

    private MediatorLiveData<List<O>> items;

    public FlexibleViewModel() {
        items = new MediatorLiveData<>();
        items.addSource(getSource(), new Observer<I>() {
            @Override
            public void onChanged(@Nullable I source) {
                List<O> adapterList = new ArrayList<>();
                mapList(source, adapterList);
                items.setValue(adapterList);
            }
        });
    }

    /**
     * @return the LiveData to observe with the list of items for the any Adapter.
     */
    @NonNull
    public MediatorLiveData<List<O>> getItems() {
        return items;
    }

    /**
     * Retrieves the LiveData coming from <i>Local</i> or <i>Remote</i> repository.
     *
     * @return the LiveData in input
     */
    @NonNull
    protected abstract LiveData<I> getSource();

    @MainThread
    protected abstract void mapList(I source, @NonNull List<O> adapterList);

    protected boolean isSourceValid(I source) {
        return true;
    }

    protected boolean isSourceListValid(List<?> source) {
        return source != null && !source.isEmpty();
    }

}