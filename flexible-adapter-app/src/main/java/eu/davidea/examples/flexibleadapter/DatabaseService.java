package eu.davidea.examples.flexibleadapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import eu.davidea.examples.models.AbstractExampleItem;
import eu.davidea.examples.models.ExpandableHeaderItem;
import eu.davidea.examples.models.ExpandableItem;
import eu.davidea.examples.models.HeaderItem;
import eu.davidea.examples.models.SimpleItem;
import eu.davidea.examples.models.SubItem;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;

/**
 * Created by Davide Steduto on 23/11/2015.
 * Project FlexibleAdapter.
 */
public class DatabaseService {

	private static DatabaseService mInstance;
	private static final int ITEMS = 90, SUB_ITEMS = 3, HEADERS = 30;
	private static AtomicInteger atomicInteger = new AtomicInteger(0);

	//TODO FOR YOU: Use userLearnedSelection from settings
	public static boolean userLearnedSelection = false;

	//Database original items
	private List<AbstractFlexibleItem> mItems = new ArrayList<AbstractFlexibleItem>();

	DatabaseService() {
	}

	public static DatabaseService getInstance() {
		if (mInstance == null) {
			mInstance = new DatabaseService();
		}
		return mInstance;
	}

	public void createOverallDatabase() {
		HeaderItem header = null;
		mItems.clear();
		for (int i = 0; i < ITEMS; i++) {
			header = i % (ITEMS/HEADERS) == 0 ? newHeader(i) : header;
			mItems.add(i % 3 == 0 ?
					newExpandableItem(i + 1, header) :
					newSimpleItem(i + 1, header));
		}
	}

	public void createExpandableSectionsDatabase() {
		HeaderItem header = null;
		mItems.clear();
		for (int i = 0; i < ITEMS; i++) {
			header = i % (ITEMS/HEADERS) == 0 ? newHeader(i) : header;
			mItems.add(newExpandableSectionItem(i + 1));
		}
	}

	public static HeaderItem newHeader(int i) {
		i = i * HEADERS/ITEMS + 1;
		HeaderItem header = new HeaderItem("H" + i);
		header.setTitle("Header " + i);
		//header is hidden and un-selectable by default!
		return header;
	}

	public static SimpleItem newSimpleItem(int i, HeaderItem header) {
		SimpleItem item;
		if (header != null) {
			header.setSubtitle("Attached to Simple Item " + i);
			item = new SimpleItem("I" + i, header);
		} else {
			item = new SimpleItem("I" + i);
		}
		item.setTitle("Simple Item " + i);
		item.setSubtitle("Subtitle " + i);
		return item;
	}

	public static ExpandableItem newExpandableItem(int i, HeaderItem header) {
		//Items are expandable because they implements IExpandable
		ExpandableItem expandableItem;
		if (header != null) {
			header.setSubtitle("Attached to Expandable Item " + i);
			expandableItem = new ExpandableItem("E" + i, header);
		} else {
			expandableItem = new ExpandableItem("E" + i);
		}
		//Experimenting NEW features
		//Let's initially expand the first parent item with subElements
//		expandableItem.setExpanded(i == 3);
//		expandableItem.setSelectable(false);
		expandableItem.setTitle("Expandable Item " + i);
		//SubItems are not expandable by default, but thy might be if extends/implements IExpandable
		for (int j = 1; j <= SUB_ITEMS; j++) {
			SubItem subItem = new SubItem(expandableItem.getId() + "S" + j);
			subItem.setTitle("Sub Item " + j);
			expandableItem.addSubItem(subItem);
		}
		return expandableItem;
	}

	private AbstractFlexibleItem newExpandableSectionItem(int i) {
		ExpandableHeaderItem expandableItem = new ExpandableHeaderItem("E" + i);
		expandableItem.setTitle("Expandable Header Item " + i);
		for (int j = 1; j <= SUB_ITEMS; j++) {
			SubItem subItem = new SubItem(expandableItem.getId() + "S" + j);
			subItem.setTitle("Sub Item " + j + " in expandable section header");
			//In this case the header is the same parent: ExpandableHeaderItem
			subItem.setHeader(expandableItem);
			expandableItem.addSubItem(subItem);
		}
		return expandableItem;
	}

	/**
	 * @return Always a copy of the original list.
	 */
	public List<AbstractFlexibleItem> getListById() {
		//Return a copy of the DB: we will perform some tricky code on this list.
		return new ArrayList<AbstractFlexibleItem>(mItems);
	}

	public void swapItem(int fromPosition, int toPosition) {
		Collections.swap(mItems, fromPosition, toPosition);
	}

	public void removeItem(IFlexible item) {
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