package eu.davidea.flexibleadapter.livedata.items;

import android.view.View;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.flexibleadapter.livedata.models.ItemModel;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * The holder item is just a wrapper for the Model item. It is most used to hold the
 * original item coming from <u>local</u> or <u>remote</u> repository.
 *
 * <p>Holder item can be used to display the same modelData in multiple RecyclerViews managed by
 * different Adapters, you can implement a derived IFlexible item to HOLD your data model object!</p>
 *
 * In this way you can separate the memory zones of the flags (enabled, expanded, hidden,
 * selectable) used by a specific Adapter, to be independent by another Adapter. For instance,
 * an item can be Shown and Expanded in a RV, while in the other RV can be Hidden or Not Expanded!
 *
 * @author Davide Steduto
 * @since 07/10/2017
 */
public class ItemHolder extends AbstractSectionableItem<ItemHolder.ItemViewHolder, HeaderHolder>
        implements IHolder<ItemModel> {

    private ItemModel model;

    /**
     * The header item must in its bounds, it must implement IHeader, therefore: HeaderHolder!
     */
    public ItemHolder(ItemModel model, HeaderHolder header) {
        super(header);
        this.model = model;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ItemHolder) {
            ItemHolder inItem = (ItemHolder) o;
            return model.equals(inItem.getModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }

    /**
     * @return the model object
     */
    @Override
    public ItemModel getModel() {
        return model;
    }

    @Override
    public int getLayoutRes() {
        //noinspection ResourceType
        return 1;
    }

    @Override
    public ItemViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ItemViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, ItemViewHolder holder, int position, List payloads) {
        // Not implemented for the scope of the test
    }

    static class ItemViewHolder extends FlexibleViewHolder {
        /**
         * Default constructor.
         */
        ItemViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
        }
    }

}