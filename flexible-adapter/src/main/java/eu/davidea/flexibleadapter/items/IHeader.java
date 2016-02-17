package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

/**
 * @author Davide Steduto
 * @since 15/02/2016
 */
public interface IHeader<VH extends RecyclerView.ViewHolder> extends IFlexible<VH> {

	/**
	 * If the header should be sticky on the top until next header comes and takes its place.
	 */
	boolean isSticky();

	/**
	 * Sets if this header should be sticky on the top on scroll.
	 *
	 * @param sticky true to make this header sticky, false otherwise.
	 */
	IFlexible setSticky(boolean sticky);

}