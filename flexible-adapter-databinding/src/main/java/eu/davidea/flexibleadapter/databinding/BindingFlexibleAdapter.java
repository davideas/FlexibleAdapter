package eu.davidea.flexibleadapter.databinding;

import android.databinding.ObservableList;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;

public class BindingFlexibleAdapter<T extends IFlexible> extends FlexibleAdapter<T> {

    private final WeakReferenceOnListChangedCallback<T> callback = new WeakReferenceOnListChangedCallback<>(this);

    public BindingFlexibleAdapter() {
        this(null);
    }

    public BindingFlexibleAdapter(@Nullable Object listeners) {
        this(listeners, false);
    }

    public BindingFlexibleAdapter(@Nullable Object listeners, boolean stableIds) {
        super(null, listeners, stableIds);
    }

    @Override
    public void updateDataSet(List<T> items, boolean animate) {
        if (items instanceof ObservableList) {
            ((ObservableList<T>) items).addOnListChangedCallback(callback);
        }
        super.updateDataSet(items, animate);
    }

    private static class WeakReferenceOnListChangedCallback<T extends IFlexible> extends ObservableList.OnListChangedCallback<ObservableList<T>> {
        final WeakReference<BindingFlexibleAdapter<T>> adapterRef;

        WeakReferenceOnListChangedCallback(BindingFlexibleAdapter<T> adapter) {
            this.adapterRef = new WeakReference<>(adapter);
        }

        @Override
        public void onChanged(ObservableList sender) {
            BindingFlexibleAdapter<T> adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            ensureChangeOnMainThread();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(ObservableList sender, final int positionStart, final int itemCount) {
            BindingFlexibleAdapter<T> adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            ensureChangeOnMainThread();
            adapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(ObservableList sender, final int positionStart, final int itemCount) {
            BindingFlexibleAdapter<T> adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            ensureChangeOnMainThread();
            adapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(ObservableList sender, final int fromPosition, final int toPosition, final int itemCount) {
            BindingFlexibleAdapter<T> adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            ensureChangeOnMainThread();
            for (int i = 0; i < itemCount; i++) {
                adapter.notifyItemMoved(fromPosition + i, toPosition + i);
            }
        }

        @Override
        public void onItemRangeRemoved(ObservableList sender, final int positionStart, final int itemCount) {
            BindingFlexibleAdapter<T> adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            ensureChangeOnMainThread();
            adapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        private static void ensureChangeOnMainThread() {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                throw new IllegalStateException("You can only modify the ObservableList on the main thread.");
            }
        }
    }

}