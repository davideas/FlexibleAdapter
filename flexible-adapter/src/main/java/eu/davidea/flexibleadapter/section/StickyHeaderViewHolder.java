package eu.davidea.flexibleadapter.section;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

class StickyHeaderViewHolder extends RecyclerView.ViewHolder {

	public RecyclerView.ViewHolder realItemHolder;
	public FrameLayout layout;

	public StickyHeaderViewHolder(final Context context, RecyclerView.ViewHolder itemHolder) {
		super(new FrameLayout(context));
		this.layout = (FrameLayout) this.itemView;
		if (itemHolder != null) {
			this.realItemHolder = itemHolder;
			this.layout.setClipChildren(false);
			this.layout.addView(this.realItemHolder.itemView);
		}
	}

}