package eu.davidea.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

public class HeaderViewHolder extends RecyclerView.ViewHolder {

	public FrameLayout layout;

	public HeaderViewHolder(final Context context) {
		super(new FrameLayout(context));
		this.layout = (FrameLayout) this.itemView;
	}

}