package eu.davidea.flexibleadapter.item;

/**
 * Basic Interface to manage Item operations like selection
 *
 * @author Davide Steduto
 * @since 19/01/2016
 */
public interface IFlexibleItem<T> {

	/*--------------------*/
	/* SELECTABLE METHODS */
	/*--------------------*/

	boolean isSelectable();

	void setSelectable(boolean selectable);

}