package eu.davidea.samples.flexibleadapter.items;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;

public class StaggeredItem extends AbstractSectionableItem<StaggeredItem.ViewHolder, StaggeredHeaderItem> {

    public static final String HASH = "#";
    public static final String SPACE = " ";
    public static final String EMPTY = "";

    private int id;
    private StaggeredItemStatus status = StaggeredItemStatus.C;
    private List<StaggeredItem> mergedItems;

    public StaggeredItem(int id, StaggeredHeaderItem header) {
        super(header);
        this.id = id;
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof StaggeredItem) {
            StaggeredItem inItem = (StaggeredItem) inObject;
            return this.id == inItem.id;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StaggeredItemStatus getStatus() {
        return status;
    }

    public void setStatus(StaggeredItemStatus status) {
        this.status = status;
    }

    public boolean hasMergedItems() {
        return mergedItems != null;
    }

    public String getMergedItemsAsText() {
        if (mergedItems == null) return EMPTY;
        StringBuilder mergedText = new StringBuilder();
        for (StaggeredItem mergedItem : mergedItems) {
            mergedText.append(SPACE).append(HASH).append(mergedItem.getId());
        }
        return mergedText.toString();
    }

    public int countMergedItems() {
        return hasMergedItems() ? mergedItems.size() : 0;
    }

    public List<StaggeredItem> getMergedItems() {
        return mergedItems;
    }

    public void setMergedItems(List<StaggeredItem> mergedItems) {
        this.mergedItems = mergedItems;
    }

    public void mergeItem(StaggeredItem staggeredItem) {
        if (mergedItems == null) {
            mergedItems = new ArrayList<>(1);
        }
        mergedItems.add(staggeredItem);
    }

    public void splitItem(StaggeredItem staggeredItem) {
        if (mergedItems != null) {
            mergedItems.remove(staggeredItem);
            if (mergedItems.isEmpty()) mergedItems = null;
        }
    }

    public List<StaggeredItem> splitAllItems() {
        List<StaggeredItem> newItems = new ArrayList<>();
        if (mergedItems != null) {
            newItems = new ArrayList<>(mergedItems);
            mergedItems = null;
        }
        return newItems;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_staggered_item;
    }

    @Override
    public ViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, final ViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();

        //Item Id
        holder.itemTextView.setText(toString());

        //Item Status
        holder.statusTextView.setText(status.getResId());
        DrawableUtils.setBackgroundCompat(holder.itemView, DrawableUtils.getSelectableBackgroundCompat(
                status.getColor(), Utils.getColorAccent(context), Color.WHITE));

        //Blink after moving the item
        for (Object payload : payloads) {
            if (payload.equals("blink")) {
                holder.itemView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        holder.itemView.setPressed(true);
                    }
                }, 100L);
                holder.itemView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        holder.itemView.setPressed(false);
                    }
                }, 800L);
            }
        }

        //Merge info
        if (mergedItems != null) {
            float extraHeight = Math.min(mergedItems.size(), 3) *
                    context.getResources().getDimension(R.dimen.card_extra_height);
            holder.cardView.getLayoutParams().height = Utils.dpToPx(holder.itemView.getContext(),
                    context.getResources().getDimension(R.dimen.card_height) + extraHeight);
            holder.mergedTextView.setText(
                    context.getResources().getString(R.string.merged_with, getMergedItemsAsText()));
            holder.mergedTextView.setVisibility(View.VISIBLE);
        } else {
            //if (FlexibleUtils.hasKitkat()) TransitionManager.beginDelayedTransition(holder.cardView);
            holder.cardView.getLayoutParams().height = Utils.dpToPx(holder.itemView.getContext(),
                    context.getResources().getDimension(R.dimen.card_height));
            holder.mergedTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public String toString() {
        return HASH + id;
    }

    static class ViewHolder extends FlexibleViewHolder {

        @BindView(R.id.card_view)
        CardView cardView;
        @BindView(R.id.item_id)
        TextView itemTextView;
        @BindView(R.id.text_merged)
        TextView mergedTextView;
        @BindView(R.id.text_status)
        TextView statusTextView;

        /**
         * Default constructor.
         *
         * @param view    The {@link View} being hosted in this ViewHolder
         * @param adapter Adapter instance of type {@link FlexibleAdapter}
         */
        public ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            ButterKnife.bind(this, view);
        }

    }

}