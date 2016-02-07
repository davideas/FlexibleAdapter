package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Interface to manage expanding operations on items with
 * {@link eu.davidea.flexibleadapter.FlexibleAdapter}.
  * <p>Implements this interface or use {@link AbstractExpandableItem}.</p>
 *
 * @author Davide Steduto
 * @since 17/01/2016 Created
 */
public interface IExpandable<VH extends RecyclerView.ViewHolder, S extends IFlexibleItem>
		extends IFlexibleItem<VH> {

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

	boolean isExpanded();

	void setExpanded(boolean expanded);

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

	List<S> getSubItems();

}