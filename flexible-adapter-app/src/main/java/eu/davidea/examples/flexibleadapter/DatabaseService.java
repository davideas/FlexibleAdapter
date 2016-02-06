package eu.davidea.examples.flexibleadapter;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.davidea.examples.models.AbstractExampleItem;
import eu.davidea.examples.models.ExpandableItem;
import eu.davidea.examples.models.HeaderItem;
import eu.davidea.examples.models.SimpleItem;
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

	private List<AbstractExampleItem> mItems = new ArrayList<AbstractExampleItem>();

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
			mItems.add(i % 2 == 0 ? newExpandableItem(i) : newSimpleItem(i));
		}
	}

	public static SimpleItem newSimpleItem(int i) {
		SimpleItem item = new SimpleItem("I" + (++i));
		item.setTitle("Simple Item " + i);
		item.setSubtitle("Subtitle " + i);
		return item;
	}

	public static ExpandableItem newExpandableItem(int i) {
		//All Items are expandable because they implements IExpandable
		ExpandableItem expandableItem = new ExpandableItem("E" + (++i));
		//Let's initially expand the first father with subElements
//		expandableItem.setInitiallyExpanded(i == 2);
		expandableItem.setTitle("Expandable Item " + i);
		//Add subItems every N elements
		//SubItems are not expandable by default, but thy might be if extends/implements IExpandable
		for (int j = 1; j <= SUB_ITEMS; j++) {
			SubItem subItem = new SubItem(expandableItem.getId() + "S" + j);
			subItem.setTitle("Sub Item " + j);
			expandableItem.addSubItem(subItem);
		}
		return expandableItem;
	}

	public static SparseArray<AbstractExampleItem> buildHeaders() {
		SparseArray<AbstractExampleItem> headers = new SparseArray<AbstractExampleItem>();
		for (int i = 0; i < (ITEMS/HEADERS); i++) {
			HeaderItem header = new HeaderItem("H" + i);
			header.setTitle("Header " + (i + 1));
			header.setSelectable(false);
			header.setHidden(true);
			headers.put(1 + i * HEADERS, header);
		}
		return headers;
	}

	/**
	 * @return Always a copy of the original list.
	 */
	public List<AbstractExampleItem> getListById() {
		//listId is not used: we have only 1 type of list in this example
		//Return a copy of the DB: we will perform some tricky code on this list.
		return new ArrayList<AbstractExampleItem>(mItems);
	}

	public void swapItem(int fromPosition, int toPosition) {
		Collections.swap(mItems, fromPosition, toPosition);
	}

	public void removeItem(IFlexibleItem item) {
		mItems.remove(item);
	}

	public void removeSubItem(ExpandableItem parent, SubItem child) {
		parent.removeSubItem(child);
	}

	public void addItem(int position, AbstractExampleItem item) {
		if (position < mItems.size())
			mItems.add(position, item);
		else
			mItems.add(item);
	}

	public void addSubItem(int position, ExpandableItem parent, SubItem subItem) {
		parent.addSubItem(position, subItem);
	}

	public static void onDestroy() {
		mInstance = null;
	}

}