package eu.davidea.flexibleadapter.databinding;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;

public class BindingAdapters {

    @SuppressWarnings("unchecked")
    @BindingAdapter(value = "items")
    public static <T extends IFlexible> void setAdapter(RecyclerView recyclerView, List<T> items) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null && adapter instanceof FlexibleAdapter) {
            ((FlexibleAdapter<T>) adapter).updateDataSet(items);
        } else {
            throw new IllegalStateException("Binding works only with FlexibleAdapter");
        }
    }

}