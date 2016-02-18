package eu.davidea.flexibleadapter.items;

/**
 * When user wants to search through the list, in order to be to collected, an item must implement
 * this interface.
 *
 * @author Davide Steduto
 * @since 18/02/2016 Created
 */
public interface IFilterable {

	/**
	 * Checks and performs the filter on this item, you can apply the logic and the filter on
	 * every fields your use case foreseen.
	 *
	 * @param constraint the search text typed by the user
	 * @return true if this item should be collected by the Adapter for the filtered list, false otherwise
	 */
	boolean filter(String constraint);

}