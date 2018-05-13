/*
 * Copyright (C) 2017-2018 Davidea Solutions Sprl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.samples.flexibleadapter.persistence.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import eu.davidea.samples.flexibleadapter.infrastructure.AppExecutors;


/**
 * A generic class that can provide a resource backed by both the SQLite database and in-memory data.
 *
 * @param <ResultType> The SQLite type entity
 */
public abstract class OfflineBoundItems<ResultType> {

    private final AppExecutors appExecutors;
    private final MediatorLiveData<ResultType> result = new MediatorLiveData<>();

    @MainThread
    public OfflineBoundItems(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        LiveData<ResultType> dbSource = loadFromDb();
        result.addSource(dbSource, data -> {
            result.removeSource(dbSource);
            if (shouldFetch(data)) {
                fetchOfflineItems();
            } else {
                result.addSource(dbSource, result::setValue);
            }
        });
    }

    private void fetchOfflineItems() {
        ResultType response = createOfflineItems();
        appExecutors.diskIO().execute(() -> {
            saveCallResult(response);
            appExecutors.mainThread().execute(() ->
                    // We specially request a new live data, otherwise we will get
                    // immediately last cached value, which may not be updated with
                    // latest results received from network.
                    result.addSource(loadFromDb(), result::setValue)
            );
        });
    }

    public LiveData<ResultType> asLiveData() {
        return result;
    }

    @NonNull
    @MainThread
    protected abstract LiveData<ResultType> loadFromDb();

    @MainThread
    protected abstract boolean shouldFetch(@Nullable ResultType data);

    @NonNull
    @WorkerThread
    protected abstract ResultType createOfflineItems();

    @WorkerThread
    protected abstract void saveCallResult(@NonNull ResultType items);

}