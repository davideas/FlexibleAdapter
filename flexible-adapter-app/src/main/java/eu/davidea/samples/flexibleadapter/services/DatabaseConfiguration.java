package eu.davidea.samples.flexibleadapter.services;

/**
 * @author Davide
 * @since 11/07/2016
 */
public class DatabaseConfiguration {

	//Id
	public static final String
			TITLE = "config_title",
			NUMBER_OF_ITEMS = "number_of_items",
			SEARCH_DELAY = "search_delay",
			ANIMATE_TO_LIMIT = "animate_to_limit",
			NOTIFY_CHANGE = "notify_change",
			NOTIFY_MOVE = "notify_move";

	//Values
	public static int maxSize = 100000;//max number of items
	public static int maxSearchDelay = 1000;//max search delay in ms
	public static int size = 10000;//items
	public static int delay = 200;//ms
	public static int animateToLimit = maxSize;//start with maxSize
	public static boolean notifyChange = true;
	public static boolean notifyMove = false;


	public static void setConfiguration(String id, int value) {
		switch (id) {
			case NUMBER_OF_ITEMS:
				size = value;
				break;
			case SEARCH_DELAY:
				delay = value;
				break;
			case ANIMATE_TO_LIMIT:
				animateToLimit = value;
				break;
			case NOTIFY_CHANGE:
				notifyChange = value == 1;
				break;
			case NOTIFY_MOVE:
				notifyMove = value == 1;
				break;

		}
	}

}