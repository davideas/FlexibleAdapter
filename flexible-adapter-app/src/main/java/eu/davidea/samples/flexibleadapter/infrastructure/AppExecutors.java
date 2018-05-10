/*
 * Copyright (C) 2017 The Android Open Source Project
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
package eu.davidea.samples.flexibleadapter.infrastructure;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Global executor pools for the whole application.
 * <p>Grouping tasks like this avoids the effects of task starvation
 * (e.g. disk reads don't wait behind webservice requests).</p>
 */
@Singleton
public class AppExecutors {

    private final Executor diskIO;

    private final Executor networkIO;

    private final Executor mainThread;

    @Inject
    public AppExecutors() {
        this(new MainThreadExecutor(), new DiskIOThreadExecutor(), Executors.newFixedThreadPool(3));
    }

    @VisibleForTesting
    private AppExecutors(Executor mainThread, Executor diskIO, Executor networkIO) {
        this.mainThread = mainThread;
        this.diskIO = diskIO;
        this.networkIO = networkIO;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    /**
     * Executor that runs a task on main thread.
     */
    private static class MainThreadExecutor implements Executor {

        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }

    /**
     * Executor that runs a task on a new background thread.
     */
    private static class DiskIOThreadExecutor implements Executor {

        private final Executor mDiskIO;

        DiskIOThreadExecutor() {
            mDiskIO = Executors.newSingleThreadExecutor();
        }

        @Override
        public void execute(@NonNull Runnable command) {
            mDiskIO.execute(command);
        }
    }

}