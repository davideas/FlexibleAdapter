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
public interface IExpandableItem<S extends IExpandableItem> extends IFlexibleItem {

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

	//T getParent();

	//void setParent(T item);

	List<S> getSubItems();

	//void setSubItems(List<T> items);

	int getSubItemsCount();

	//T getSubItem(int position);

	//int getSubItemPosition(T item);

	//boolean contains(T item);

	//void addSubItem(T item);

	//void addSubItem(int position, T item);

	//boolean removeSubItem(T item);

	//boolean removeSubItem(int position);

}