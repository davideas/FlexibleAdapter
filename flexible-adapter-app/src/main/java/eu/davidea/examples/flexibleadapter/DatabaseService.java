package eu.davidea.examples.flexibleadapter;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Davide Steduto on 23/11/2015.
 * Project FlexibleAdapter.
 */
public class DatabaseService {

	private static DatabaseService mInstance;
	private static final int ITEMS = 200, SUB_ITEMS = 3, HEADERS = 20;
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
		item.setId("I"+(++i));
		item.setTitle("Item " + i);

		//All parent items are expandable
		item.setExpandable(true);
		//Let's initially expand the first father with subElements
		//item.setInitiallyExpanded(i == 2);

		//Add subItems every N elements
		//subItems are not expandable by default, but thy might be
		if (i % 1 == 0) {
			item.setTitle("Expandable Item " + i);
			for (int j = 1; j <= SUB_ITEMS; j++) {
				SubItem subItem = new SubItem();
				subItem.setId(item.getId() + "s" + j);
				subItem.setTitle("Sub Item " + j);
				item.addSubItem(subItem);
			}
		}
		item.updateSubTitle();

		return item;
	}

	public static SparseArray<Item> buildHeaders() {
		SparseArray<Item> headers = new SparseArray<Item>();
		for (int i = 0; i < (ITEMS/HEADERS); i++) {
			Item header = new Item();
			header.setId("H" + i);
			header.setTitle("Header " + (i + 1));
			header.setSelectable(false);
			header.setHidden(true);
			headers.put(1 + i * HEADERS, header);
		}
		return headers;
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

	public void swapItem(int fromPosition, int toPosition) {
		Collections.swap(mItems, fromPosition, toPosition);
	}

	public void removeItem(Item item) {
		mItems.remove(item);
	}

	public void removeSubItem(Item parent, SubItem child) {
		if (parent.contains(child)) {
			parent.getSubItems().remove(child);
		}
	}

	public void addItem(int i, Item item) {
		if (i < mItems.size())
			mItems.add(i, item);
		else
			mItems.add(item);
	}

	public void addSubItem(int i, Item parent, SubItem subItem) {
		parent.addSubItem(i, subItem);
		parent.setSubtitle(parent.getSubItemsCount() + " subItems");
	}

	public static void onDestroy() {
		mInstance = null;
	}

}