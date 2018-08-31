package eu.davidea.flexibleadapter.livedata.items;

import android.view.View;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.livedata.models.ItemModel;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * A classic Flexible item.
 *
 * @author Davide Steduto
 * @since 07/10/2017
 */
public class FlexibleItem extends AbstractFlexibleItem<FlexibleItem.ItemViewHolder> {

    private String id;

    /**
     * The header item must in its bounds, it must implement IHeader, therefore: HeaderHolder!
     */
    public FlexibleItem(ItemModel model) {
        this.id = model.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FlexibleItem) {
            FlexibleItem inItem = (FlexibleItem) o;
            return id.equals(inItem.id);
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
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