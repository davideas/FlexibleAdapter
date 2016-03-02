package eu.davidea.flexibleadapter.section;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

public class HeaderViewHolder extends RecyclerView.ViewHolder {
    public RecyclerView.ViewHolder realItemHolder;
    public FrameLayout layout;

    public HeaderViewHolder(final Context context) {
        super(new FrameLayout(context));
        this.layout = (FrameLayout) this.itemView;
    }
}
