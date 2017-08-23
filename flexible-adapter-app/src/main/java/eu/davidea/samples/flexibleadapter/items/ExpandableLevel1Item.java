package eu.davidea.samples.flexibleadapter.items;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.samples.flexibleadapter.R;

/**
 * This is an experiment to evaluate how a Section with header can also be expanded/collapsed.
 * <p>Here, it still benefits of the common fields declared in AbstractItem.</p>
 * It's important to note that, the ViewHolder must be specified in all &lt;diamond&gt; signature.
 */
public class ExpandableLevel1Item
        extends AbstractItem<ExpandableItem.ParentViewHolder>
        implements IExpandable<ExpandableItem.ParentViewHolder, SubItem>, IFilterable {

    /* Flags for FlexibleAdapter */
    private boolean mExpanded = false;

    /* subItems list */
    private List<SubItem> mSubItems;


    public ExpandableLevel1Item(String id) {
        super(id);
        setDraggable(true);
        setSwipeable(true);
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
        return 1;
    }//This allows +1 level of expansion

    @Override
    public boolean filter(String constraint) {
        return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint) ||
                getSubtitle() != null && getSubtitle().toLowerCase().trim().contains(constraint);
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
    public int getLayoutRes() {
        return R.layout.recycler_expandable_item;
    }

    @Override
    public ExpandableItem.ParentViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ExpandableItem.ParentViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, ExpandableItem.ParentViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();

        setSubtitle(adapter.getCurrentChildren(this).size() + " subItems");
        holder.mSubtitle.setText(getSubtitle());

        // Background, when bound the first time
        if (payloads.size() == 0) {
            holder.mTitle.setText(getTitle());

            Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
                    Color.WHITE, Color.parseColor("#dddddd"), // Same color of divider
                    DrawableUtils.getColorControlHighlight(context));
            DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
            DrawableUtils.setBackgroundCompat(holder.frontView, drawable);
        } else {
            Log.d(this.getClass().getSimpleName(), "ExpandableHeaderItem Payload " + payloads);
        }

        // ANIMATION EXAMPLE!! ImageView - Handle Flip Animation on Select ALL and Deselect ALL
        if (adapter.isSelectAll() || adapter.isLastItemInActionMode()) {
            // Consume the Animation
            holder.mFlipView.flip(adapter.isSelected(position), 200L);
        } else {
            // Display the current flip status
            holder.mFlipView.flipSilently(adapter.isSelected(position));
        }
    }

    @Override
    public String toString() {
        return "ExpandableLevel-1[" + super.toString() + "//SubItems" + mSubItems + "]";
    }

}