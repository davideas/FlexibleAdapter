package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class SubItem extends AbstractItem<SubItem.ChildViewHolder>
        implements ISectionable<SubItem.ChildViewHolder, IHeader>, IFilterable {

    /**
     * The header of this item
     */
    IHeader header;

    public SubItem(String id) {
        super(id);
        setDraggable(true);
    }

    @Override
    public IHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(IHeader header) {
        this.header = header;
    }

    /**
     * Called by the FlexibleAdapter when it wants to check if this item should be bound
     * again with new content.
     * <p>
     * You should return {@code true} whether you want this item will be updated because
     * its visual representations will change.
     * <p>
     * This method is called only if {@link FlexibleAdapter#setNotifyChangeOfUnfilteredItems(boolean)}
     * is enabled.
     * <p>Default value is {@code true}.</p>
     *
     * @param newItem The new item object with the new content
     * @return True will trigger a new binding to display new content, false if the content shown
     * is already the latest data.
     */
    @Override
    public boolean shouldNotifyChange(IFlexible newItem) {
        SubItem subItem = (SubItem) newItem;
        return !title.equals(subItem.getTitle()); // Should be bound again if title is different
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_sub_item;
    }

    @Override
    public ChildViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ChildViewHolder(view, adapter);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ChildViewHolder holder, int position, List payloads) {
        //In case of searchText matches with Title or with an SimpleItem's field
        // this will be highlighted
        if (adapter.hasSearchText()) {
            Context context = holder.itemView.getContext();
            FlexibleUtils.highlightText(holder.mTitle, getTitle(), adapter.getSearchText(),
                    context.getResources().getColor(R.color.colorAccent_light));
        } else {
            holder.mTitle.setText(getTitle());
        }

        if (getHeader() != null) {
            setSubtitle("Header " + getHeader().toString());
        }
    }

    @Override
    public boolean filter(String constraint) {
        return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint);
    }

    /**
     * Provide a reference to the views for each data item.
     * Complex data labels may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder.
     */
    static final class ChildViewHolder extends FlexibleViewHolder {

        public ImageView mHandleView;
        public TextView mTitle;

        public ChildViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mTitle = view.findViewById(R.id.title);
            this.mHandleView = view.findViewById(R.id.row_handle);
            if (adapter.isHandleDragEnabled()) {
                this.mHandleView.setVisibility(View.VISIBLE);
                setDragHandleView(mHandleView);
            } else {
                this.mHandleView.setVisibility(View.GONE);
            }
        }

        @Override
        public float getActivationElevation() {
            return eu.davidea.utils.Utils.dpToPx(itemView.getContext(), 4f);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.scaleAnimator(animators, itemView, 0f);
        }
    }

    @Override
    public String toString() {
        return "SubItem[" + super.toString() + "]";
    }

}