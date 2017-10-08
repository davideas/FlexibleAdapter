package eu.davidea.flexibleadapter.livedata.items;

import android.view.View;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.flexibleadapter.livedata.models.HeaderModel;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * The holder item is just a wrapper for the Model item.
 *
 * @author Davide Steduto
 * @since 19/10/2016
 */
public class HeaderHolder extends AbstractHeaderItem<HeaderHolder.HeaderViewHolder>
        implements IHolder<HeaderModel> {

    private HeaderModel model;

    public HeaderHolder(HeaderModel model) {
        this.model = model;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HeaderHolder) {
            HeaderHolder inItem = (HeaderHolder) o;
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
    public HeaderModel getModel() {
        return model;
    }

    @Override
    public int getLayoutRes() {
        return 0;
    }

    @Override
    public HeaderViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new HeaderViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
        // Not implemented for the scope of the test
    }

    static class HeaderViewHolder extends FlexibleViewHolder {
        /**
         * Default constructor.
         */
        HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true); //true only for header items when will be sticky
        }
    }

}