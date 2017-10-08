package eu.davidea.samples.flexibleadapter.holders;

import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.models.HeaderModel;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * The holder item is just a wrapper for the Model item.
 *
 * @author Davide Steduto
 * @since 19/10/2016
 */
public class HeaderHolder extends AbstractHeaderItem<HeaderHolder.HeaderViewHolder>
        implements IFilterable, IHolder<HeaderModel> {

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

    /**
     * Filter is applied to the model fields.
     */
    @Override
    public boolean filter(String constraint) {
        return model.getTitle() != null && model.getTitle().equals(constraint);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_holder_header;
    }

    @Override
    public HeaderViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new HeaderViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
        holder.mTitle.setText(model.getTitle());
        List sectionableList = adapter.getSectionItems(this);
        String subTitle = (sectionableList.isEmpty() ? "Empty section" :
                sectionableList.size() + " section items");
        holder.mSubtitle.setText(subTitle);
    }

    static class HeaderViewHolder extends FlexibleViewHolder {

        @BindView(R.id.title)
        public TextView mTitle;
        @BindView(R.id.subtitle)
        public TextView mSubtitle;

        /**
         * Default constructor.
         */
        HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//true only for header items when will be sticky
            ButterKnife.bind(this, view);
        }
    }

}