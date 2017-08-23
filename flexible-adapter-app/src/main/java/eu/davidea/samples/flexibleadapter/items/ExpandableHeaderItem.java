package eu.davidea.samples.flexibleadapter.items;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.Payload;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.items.ExpandableHeaderItem.ExpandableHeaderViewHolder;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * This is an experiment to evaluate how a Section with header can also be expanded/collapsed.
 * <p>Here, it still benefits of the common fields declared in AbstractItem.</p>
 * It's important to note that, the ViewHolder must be specified in all &lt;diamond&gt; signature.
 */
public class ExpandableHeaderItem
        extends AbstractItem<ExpandableHeaderViewHolder>
        implements IExpandable<ExpandableHeaderViewHolder, SubItem>, IHeader<ExpandableHeaderViewHolder> {

    /* Flags for FlexibleAdapter */
    private boolean mExpanded = false;

    /* subItems list */
    private List<SubItem> mSubItems;


    public ExpandableHeaderItem(String id) {
        super(id);
        setDraggable(true);
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
    public List<SubItem> getSubItems() {
        return mSubItems;
    }

    public final boolean hasSubItems() {
        return mSubItems != null && mSubItems.size() > 0;
    }

    public boolean removeSubItem(SubItem item) {
        return item != null && mSubItems.remove(item);
    }

    public boolean removeSubItem(int position) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.remove(position);
            return true;
        }
        return false;
    }

    public void addSubItem(SubItem subItem) {
        if (mSubItems == null)
            mSubItems = new ArrayList<>();
        mSubItems.add(subItem);
    }

    public void addSubItem(int position, SubItem subItem) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.add(position, subItem);
        } else
            addSubItem(subItem);
    }

    @Override
    public int getSpanSize(int spanCount, int position) {
        return spanCount;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_expandable_header_item;
    }

    @Override
    public ExpandableHeaderViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ExpandableHeaderViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ExpandableHeaderViewHolder holder, int position, List payloads) {
        if (payloads.size() > 0) {
            Log.d(this.getClass().getSimpleName(), "ExpandableHeaderItem Payload " + payloads + " - " + getTitle());
        } else {
            Log.d(this.getClass().getSimpleName(), "ExpandableHeaderItem NoPayload - " + getTitle());
            holder.mTitle.setText(getTitle());
        }
        setSubtitle(String.valueOf(adapter.getCurrentChildren(this).size()) +
                " subItems (" + (isExpanded() ? "expanded" : "collapsed") + ")");
        holder.mSubtitle.setText(getSubtitle());
    }

    /**
     * Provide a reference to the views for each data item.
     * Complex data labels may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder.
     */
    static class ExpandableHeaderViewHolder extends ExpandableViewHolder {

        TextView mTitle;
        TextView mSubtitle;
        ImageView mHandleView;

        ExpandableHeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//True for sticky
            mTitle = view.findViewById(R.id.title);
            mSubtitle = view.findViewById(R.id.subtitle);
            this.mHandleView = view.findViewById(R.id.row_handle);
            if (adapter.isHandleDragEnabled()) {
                this.mHandleView.setVisibility(View.VISIBLE);
                setDragHandleView(mHandleView);
            } else {
                this.mHandleView.setVisibility(View.GONE);
            }

            // Support for StaggeredGridLayoutManager
            setFullSpan(true);
        }

        /**
         * Allows to expand or collapse child views of this itemView when {@link View.OnClickListener}
         * event occurs on the entire view.
         * <p>This method returns always true; Extend with "return false" to Not expand or collapse
         * this ItemView onClick events.</p>
         *
         * @return always true, if not overridden
         * @since 5.0.0-b1
         */
        @Override
        protected boolean isViewExpandableOnClick() {
            return true;//default=true
        }

        /**
         * Allows to collapse child views of this ItemView when {@link View.OnLongClickListener}
         * event occurs on the entire view.
         * <p>This method returns always true; Extend with "return false" to Not collapse this
         * ItemView onLongClick events.</p>
         *
         * @return always true, if not overridden
         * @since 5.0.0-b1
         */
        protected boolean isViewCollapsibleOnLongClick() {
            return true;//default=true
        }

        /**
         * Allows to notify change and rebound this itemView on expanding and collapsing events,
         * in order to update the content (so, user can decide to display the current expanding status).
         * <p>This method returns always false; Override with {@code "return true"} to trigger the
         * notification.</p>
         *
         * @return true to rebound the content of this itemView on expanding and collapsing events,
         * false to ignore the events
         * @see #expandView(int)
         * @see #collapseView(int)
         * @since 5.0.0-rc1
         */
        @Override
        protected boolean shouldNotifyParentOnClick() {
            return true;//default=false
        }

        /**
         * Expands or Collapses based on the current state.
         *
         * @see #shouldNotifyParentOnClick()
         * @see #expandView(int)
         * @see #collapseView(int)
         * @since 5.0.0-b1
         */
        @Override
        protected void toggleExpansion() {
            super.toggleExpansion(); //If overridden, you must call the super method
        }

        /**
         * Triggers expansion of this itemView.
         * <p>If {@link #shouldNotifyParentOnClick()} returns {@code true}, this view is rebound
         * with payload {@link Payload#EXPANDED}.</p>
         *
         * @see #shouldNotifyParentOnClick()
         * @since 5.0.0-b1
         */
        @Override
        protected void expandView(int position) {
            super.expandView(position); //If overridden, you must call the super method
            // Let's notify the item has been expanded. Note: from 5.0.0-rc1 the next line becomes
            // obsolete, override the new method shouldNotifyParentOnClick() as showcased here
            //if (mAdapter.isExpanded(position)) mAdapter.notifyItemChanged(position, true);
        }

        /**
         * Triggers collapse of this itemView.
         * <p>If {@link #shouldNotifyParentOnClick()} returns {@code true}, this view is rebound
         * with payload {@link Payload#COLLAPSED}.</p>
         *
         * @see #shouldNotifyParentOnClick()
         * @since 5.0.0-b1
         */
        @Override
        protected void collapseView(int position) {
            super.collapseView(position); //If overridden, you must call the super method
            // Let's notify the item has been collapsed. Note: from 5.0.0-rc1 the next line becomes
            // obsolete, override the new method shouldNotifyParentOnClick() as showcased here
            //if (!mAdapter.isExpanded(position)) mAdapter.notifyItemChanged(position, true);
        }

    }

    @Override
    public String toString() {
        return "ExpandableHeaderItem[" + super.toString() + "//SubItems" + mSubItems + "]";
    }

}