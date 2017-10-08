package eu.davidea.flexibleadapter.livedata;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;

/**
 * @author Davide
 * @since 08/10/2017
 */
public class FlexibleLiveAdapter<T extends IFlexible> extends FlexibleAdapter<T> {

    private LifecycleOwner mOwner;

    public FlexibleLiveAdapter(@Nullable List<T> items, LifecycleOwner owner) {
        this(items, null, false, owner);
    }

    public FlexibleLiveAdapter(@Nullable List<T> items, @Nullable Object listeners, LifecycleOwner owner) {
        this(items, listeners, false, owner);
    }

    public FlexibleLiveAdapter(@Nullable List<T> items, @Nullable Object listeners, boolean stableIds, LifecycleOwner owner) {
        super(items, listeners, stableIds);
        this.mOwner = owner;
    }

    public void observeUpdate(@NonNull LiveData<List<T>> items) {
        items.observe(mOwner, new Observer<List<T>>() {
            @Override
            public void onChanged(@Nullable List<T> list) {
                updateDataSet(list);
            }
        });
    }

}