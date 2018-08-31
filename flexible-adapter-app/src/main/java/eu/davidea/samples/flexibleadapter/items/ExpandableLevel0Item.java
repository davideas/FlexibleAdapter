package eu.davidea.samples.flexibleadapter.items;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.items.ExpandableLevel0Item.L0ViewHolder;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;
import eu.davidea.samples.flexibleadapter.services.DatabaseType;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * This is an experiment to evaluate how a Section with header can also be expanded/collapsed.
 * <p>Here, it still benefits of the common fields declared in AbstractItem.</p>
 * It's important to note that, the ViewHolder must be specified in all &lt;diamond&gt; signature.
 */
public class ExpandableLevel0Item
        extends AbstractItem<L0ViewHolder>
        implements IExpandable<L0ViewHolder, ExpandableLevel1Item>, IHeader<L0ViewHolder> {

    /* Flags for FlexibleAdapter */
    private boolean mExpanded = false;

    /* subItems list */
    private List<ExpandableLevel1Item> mSubItems;


    public ExpandableLevel0Item(String id) {
        super(id);
        //We start with header shown and expanded
        setHidden(false);
        setExpanded(true);
        //NOT selectable (otherwise ActionMode will be activated on long click)!
        setSelectable(false);
    }

    @Override
    public boolean isExpanded() {
        return mExpanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
    }

    @Override
    public int getExpansionLevel() {
        return 0;
    }

    @Override
    public List<ExpandableLevel1Item> getSubItems() {
        return mSubItems;
    }

    public final boolean hasSubItems() {
        return mSubItems != null && mSubItems.size() > 0;
    }

    public boolean removeSubItem(ExpandableLevel1Item item) {
        return item != null && mSubItems.remove(item);
    }

    public boolean removeSubItem(int position) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.remove(position);
            return true;
        }
        return false;
    }

    public void addSubItem(ExpandableLevel1Item subItem) {
        if (mSubItems == null)
            mSubItems = new ArrayList<ExpandableLevel1Item>();
        mSubItems.add(subItem);
    }

    public void addSubItem(int position, ExpandableLevel1Item subItem) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.add(position, subItem);
        } else
            addSubItem(subItem);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_expandable_header_item;
    }

    @Override
    public L0ViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new L0ViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, L0ViewHolder holder, int position, List payloads) {
        if (payloads.size() > 0) {
            Log.d(this.getClass().getSimpleName(), "ExpandableHeaderItem Payload " + payloads);
        } else {
            holder.mTitle.setText(getTitle());
        }
        setSubtitle(adapter.getCurrentChildren(this).size() + " subItems");
        holder.mSubtitle.setText(getSubtitle());
    }

    /**
     * Provide a reference to the views for each data item.
     * Complex data labels may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder.
     */
    static class L0ViewHolder extends ExpandableViewHolder {

        public TextView mTitle;
        public TextView mSubtitle;
        public ImageView mHandleView;

        public L0ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//True for sticky
            mTitle = view.findViewById(R.id.title);
            mSubtitle = view.findViewById(R.id.subtitle);
            this.mHandleView = view.findViewById(R.id.row_handle);
            if (adapter.isHandleDragEnabled() &&
                    DatabaseService.getInstance().getDatabaseType() == DatabaseType.EXPANDABLE_SECTIONS) {
                this.mHandleView.setVisibility(View.VISIBLE);
                setDragHandleView(mHandleView);
            } else {
                this.mHandleView.setVisibility(View.GONE);
            }

            // Support for StaggeredGridLayoutManager
            setFullSpan(true);
        }

        @Override
        protected boolean isViewExpandableOnClick() {
            return true; //default=true
        }
    }

    @Override
    public String toString() {
        return "ExpandableLevel-0[" + super.toString() + "//SubItems" + mSubItems + "]";
    }

}