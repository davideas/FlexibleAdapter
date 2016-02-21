package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

/**
 * Wrapper empty interface to identify if the current item is a header.
 *
 * @author Davide Steduto
 * @since 15/02/2016
 */
public interface IHeader<VH extends RecyclerView.ViewHolder> extends IFlexible<VH> {

}