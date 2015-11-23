package eu.davidea.examples.flexibleadapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Davide Steduto on 23/11/2015.
 * Project FlexibleAdapter.
 */
public class DatabaseService {

	private static DatabaseService mInstance;
	private static final int ITEMS = 1000;

	private List<Item> mItems = new ArrayList<Item>();

	public static DatabaseService getInstance() {
		if (mInstance == null) {
			mInstance = new DatabaseService();
		}
		return mInstance;
	}

	DatabaseService() {
		init();
	}

	private void init() {
		for (int i = 1; i <= ITEMS; i++) {
			mItems.add(getNewExampleItem(i));
		}
	}

	public static Item getNewExampleItem(int i) {
		Item item = new Item();
		item.setId(i);
		item.setTitle("Item "+i);
		item.setSubtitle("Subtitle " + i);
		return item;
	}

	public List<Item> getListById(String listId, String searchText) {
		//listId is not used, we have only 1 type of list in this example

		List<Item> filteredItems = new ArrayList<Item>();
		if (searchText != null && searchText.length() > 0) {
			for (Item item : mItems) {
				if (filterItem(item, searchText))
					filteredItems.add(item);
			}
			return filteredItems;
		}
		return mItems;
	}

	/**
	 * Custom filter.
	 *
	 * @param item The item to filter
	 * @param constraint the current searchText
	 *
	 * @return true if a match exists in the title or in the subtitle, false if no match found.
	 */
	private static boolean filterItem(Item item, String constraint) {
		return item.getTitle().contains(constraint) || item.getSubtitle().contains(constraint);
	}

	public void removeItem(Item item) {
		mItems.remove(item);
	}

}