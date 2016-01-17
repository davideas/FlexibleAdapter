package eu.davidea.flexibleadapter.item;

import java.util.List;

/**
 * @author Davide Steduto
 * @since 17/01/2016
 */
public interface FlexibleItem<T> {

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

	boolean isExpandable();

	void setExpandable(boolean expandable);

	T withExpandable(boolean expandable);

	boolean isExpanded();

	void setExpanded(boolean expanded);

	void setInitiallyExpanded(boolean expanded);

	T withInitiallyExpanded(boolean expanded);

	/*--------------------*/
	/* SELECTABLE METHODS */
	/*--------------------*/

	boolean isSelectable();

	void setSelectable(boolean selectable);

	T withSelectable(boolean selectable);

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

	List<T> getSubItems();

	void setSubItems(List<T> items);

	T withSubItems(List<T> items);

}