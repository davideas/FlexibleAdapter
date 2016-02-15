package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

/**
 * @author Davide Steduto
 * @since 07/02/2016
 */
public interface ISectionable<VH extends RecyclerView.ViewHolder, T extends IHeader>
		extends IFlexibleItem<VH> {

	T getHeader();

	IFlexibleItem setHeader(T header);

}