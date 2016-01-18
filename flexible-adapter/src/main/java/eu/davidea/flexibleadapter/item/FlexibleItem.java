package eu.davidea.flexibleadapter.item;

import java.util.List;

/**
 * Interface to manage Item operations with {@link eu.davidea.flexibleadapter.FlexibleExpandableAdapter}.
 * <br/>Most of these methods are called in the Adapter.
 * <br/>Implements this interface or use {@link AbstractFlexibleItem}.
 *
 * @author Davide Steduto
 * @since 17/01/2016
 */
public interface FlexibleItem<T> {

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

	boolean isExpandable();

	void setExpandable(boolean expandable);

	boolean isExpanded();

	void setExpanded(boolean expanded);

	/*--------------------*/
	/* SELECTABLE METHODS */
	/*--------------------*/

	boolean isSelectable();

	void setSelectable(boolean selectable);

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

	List<T> getSubItems();

	void setSubItems(List<T> items);

}