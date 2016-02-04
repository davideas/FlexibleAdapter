package eu.davidea.flexibleadapter.items;

import java.util.List;

/**
 * Interface to manage expanding operations on items with
 * {@link eu.davidea.flexibleadapter.FlexibleExpandableAdapter}.
 * <br/>Most of these methods are called in the Adapter.
 * <p>Implements this interface or use {@link AbstractExpandableItem}.</p>
 *
 * @author Davide Steduto
 * @since 17/01/2016 Created
 */
public interface IExpandable<T extends IFlexibleItem> {

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

	boolean isExpanded();

	void setExpanded(boolean expanded);

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

	List<T> getSubItems();

}