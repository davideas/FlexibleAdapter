package eu.davidea.flexibleadapter.item;

import java.util.List;

/**
 * Interface to manage Item operations with {@link eu.davidea.flexibleadapter.FlexibleExpandableAdapter}.
 * <br/>Most of these methods are called in the Adapter.
 * <br/>Implements this interface or use {@link AbstractExpandableItem}.
 *
 * @author Davide Steduto
 * @since 17/01/2016
 */
public interface IExpandableItem<T> extends IFlexibleItem<T> {

	/*--------------------*/
	/* EXPANDABLE METHODS */
	/*--------------------*/

	boolean isExpandable();

	void setExpandable(boolean expandable);

	boolean isExpanded();

	void setExpanded(boolean expanded);

	/*-------------------*/
	/* SUB ITEMS METHODS */
	/*-------------------*/

	T getParent();

	void setParent(T item);

	List<T> getSubItems();

	void setSubItems(List<T> items);

	int getSubItemsCount();

	T getSubItem(int position);

	boolean contains(T item);

	void addSubItem(T item);

	void addSubItem(int position, T item);

	boolean removeSubItem(T item);

	boolean removeSubItemAt(int position);

	void restoreDeletedSubItems();

}