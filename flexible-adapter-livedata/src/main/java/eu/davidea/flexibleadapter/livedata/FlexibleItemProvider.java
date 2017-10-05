package eu.davidea.flexibleadapter.livedata;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.items.IFlexible;

/**
 * Custom Transformations for a {@link LiveData} class.
 *
 * @author steduda
 * @since 05/10/2017
 */
public class FlexibleItemProvider {

    private final Factory mFactory;

    private FlexibleItemProvider(Factory factory) {
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

    /**
     * Maps the original list on the main thread into LiveData of IFlexible items.
     *
     * @param liveData  a {@code LiveData} to listen to
     * @param itemClass a function to apply
     * @param <I>       the input type of model object
     * @param <O>       the output type of IFlexible item
     * @return a LiveData which emits resulting values
     */
    @MainThread
    public <I, O extends IFlexible> LiveData<List<O>> map(final LiveData<List<I>> liveData,
                                                          final @NonNull Class<O> itemClass) {
        final MutableLiveData<List<O>> result = new MutableLiveData<>();
        List<O> list = new ArrayList<>();
        List<I> source = liveData.getValue();
        if (isSourceListValid(source)) {
            for (I item : source) {
                list.add(map(item, itemClass));
            }
        }
        result.setValue(list);
        return result;
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