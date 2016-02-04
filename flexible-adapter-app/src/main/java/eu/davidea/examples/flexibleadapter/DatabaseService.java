package eu.davidea.examples.flexibleadapter;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.davidea.examples.models.ExpandableItem;
import eu.davidea.examples.models.Item;
import eu.davidea.examples.models.SubItem;
import eu.davidea.flexibleadapter.items.IFlexibleItem;

/**
 * Created by Davide Steduto on 23/11/2015.
 * Project FlexibleAdapter.
 */
public class DatabaseService {

	private static DatabaseService mInstance;
	private static final int ITEMS = 200, SUB_ITEMS = 3, HEADERS = 20;
	public static boolean userLearnedSelection = false;

	private List<IFlexibleItem> mItems = new ArrayList<IFlexibleItem>();

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
			mItems.add(i % 2 == 0 ? newExpandableItem(i) : newExampleItem(i));
		}
	}

	public static Item newExampleItem(int i) {
		Item item = new Item();
		item.setId("I" + (++i));
		item.setTitle("Item " + i);
		item.setSubtitle("Subtitle " + i);
		return item;
	}

	public static ExpandableItem newExpandableItem(int i) {
		//All Items are expandable because they implements IExpandable
		ExpandableItem expandableItem = new ExpandableItem();
		expandableItem.setId("E" + (++i));
		//Let's initially expand the first father with subElements
//		expandableItem.setInitiallyExpanded(i == 2);
		expandableItem.setTitle("Expandable Item " + i);
		//Add subItems every N elements
		//SubItems are not expandable by default, but thy might be if extends/implements IExpandable
		for (int j = 1; j <= SUB_ITEMS; j++) {
			SubItem subItem = new SubItem();
			subItem.setId(expandableItem.getId() + "S" + j);
			subItem.setTitle("Sub Item " + j);
			expandableItem.addSubItem(subItem);
		}
		expandableItem.updateSubTitle();
		return expandableItem;
	}

	public static SparseArray<IFlexibleItem> buildHeaders() {
		SparseArray<IFlexibleItem> headers = new SparseArray<IFlexibleItem>();
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
	public List<IFlexibleItem> getListById(String listId) {
		//listId is not used: we have only 1 type of list in this example
		//Return a copy of the DB: we will perform some tricky code on this list.
		return new ArrayList<IFlexibleItem>(mItems);
	}

	public void swapItem(int fromPosition, int toPosition) {
		Collections.swap(mItems, fromPosition, toPosition);
	}

	public void removeItem(Item item) {
		mItems.remove(item);
	}

	public void removeSubItem(ExpandableItem parent, SubItem child) {
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

	public void addSubItem(int i, ExpandableItem parent, SubItem subItem) {
		parent.addSubItem(i, subItem);
		parent.updateSubTitle();
	}

	public static void onDestroy() {
		mInstance = null;
	}

}