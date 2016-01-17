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
	public static boolean userLearnedSelection = false;

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
		for (int i = 0; i < ITEMS; i++) {
			mItems.add(newExampleItem(i));
		}
	}

	public static Item newExampleItem(int i) {
		Item item = new Item();
		item.setId(++i);
		item.setTitle("Item " + i);
		item.setSubtitle("Subtitle " + i);
		item.withExpandable(true);
		if (i % 5 == 0) {
			item.setTitle("Expandable Item " + i);
			item.setInitiallyExpanded(i == 5);
			for (int j = 1; j <= 5; j++) {
				Item subItem = new Item();
				subItem.setId(++i * j *(-1));
				subItem.setTitle("Sub Item " + j);
				item.addSubItem(subItem);
			}
		}
		return item;
	}

	/**
	 * @param listId The type of the list
	 * @return Always a copy of the original list.
	 */
	public List<Item> getListById(String listId) {
		//listId is not used: we have only 1 type of list in this example
		//Return a copy of the DB: we will perform some tricky code on this list.
		return new ArrayList<Item>(mItems);
	}

	public void removeItem(Item item) {
		mItems.remove(item);
	}

	public void addItem(int i, Item item) {
		if (i < mItems.size())
			mItems.add(i, item);
		else
			mItems.add(item);
	}

	public static void onDestroy() {
		mInstance = null;
	}

}