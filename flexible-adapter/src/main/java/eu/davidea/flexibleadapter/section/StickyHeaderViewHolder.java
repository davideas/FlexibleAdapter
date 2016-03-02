package eu.davidea.flexibleadapter.section;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

public class StickyHeaderViewHolder extends HeaderViewHolder {
    public RecyclerView.ViewHolder realItemHolder;

    public StickyHeaderViewHolder(final Context context, RecyclerView.ViewHolder itemHolder) {
        super(context);
        if (itemHolder != null) {
            this.layout.setClipChildren(false);
            this.realItemHolder = itemHolder;
            this.layout.addView(this.realItemHolder.itemView);
        }
    }
}
