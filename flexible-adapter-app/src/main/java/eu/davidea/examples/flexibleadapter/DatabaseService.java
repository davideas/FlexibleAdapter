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
		item.setId(""+(++i));
		item.setTitle("Item " + i);

		//All parent items are expandable
		item.setExpandable(true);
		//Let's initially expand the first father with subElements
		item.setInitiallyExpanded(i == 5);

		//Add subItems every 5 elements
		//subItems are not expandable by default
		if (i % 5 == 0) {
			item.setTitle("Expandable Item " + i);
			for (int j = 1; j <= 5; j++) {
				Item subItem = new Item();
				subItem.setId(i+"s"+j);
				subItem.setTitle("Sub Item " + j);
				subItem.setParent(item);
				item.addSubItem(subItem);
			}
		}
		item.updateSubTitle();

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
		//Is a Parent?
		if (item.isExpandable()) {
			mItems.remove(item);
		} else {
			Item parent = item.getParent();
			if (parent.contains(item)) {
				parent.removeSubItem(item);
				parent.updateSubTitle();
			}
		}
	}

	public void addItem(int i, Item item) {
		if (i < mItems.size())
			mItems.add(i, item);
		else
			mItems.add(item);
	}

	public void addSubItem(int i, Item parent, Item subItem) {
		parent.addSubItem(i, subItem);
		parent.setSubtitle(parent.getSubItemsCount() + " subItems");
	}

	public static void onDestroy() {
		mInstance = null;
	}

}