package eu.davidea.examples.flexibleadapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import eu.davidea.examples.models.AbstractExampleItem;
import eu.davidea.examples.models.ExpandableHeaderItem;
import eu.davidea.examples.models.ExpandableItem;
import eu.davidea.examples.models.ExpandableLevel0Item;
import eu.davidea.examples.models.ExpandableLevel1Item;
import eu.davidea.examples.models.HeaderItem;
import eu.davidea.examples.models.SimpleItem;
import eu.davidea.examples.models.SubItem;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
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

	/*---------------------------*/
	/* EXAMPLE DATABASE CREATION */
	/*---------------------------*/

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
			mItems.add(newExpandableSectionItem(i + 1));//With level 0
		}
	}

	public void createExpandableLevelDatabase() {
		HeaderItem header = null;
		mItems.clear();
		for (int i = 0; i < ITEMS; i++) {
			header = i % (ITEMS/HEADERS) == 0 ? newHeader(i) : header;
			mItems.add(newExpandableLevelItem(i + 1));//With level 1
		}
	}

	/*---------------*/
	/* ITEM CREATION */
	/*---------------*/

	/*
	 * Create a Header item for normal items.
	 */
	public static HeaderItem newHeader(int i) {
		i = i * HEADERS/ITEMS + 1;
		HeaderItem header = new HeaderItem("H" + i);
		header.setTitle("Header " + i);
		//header is hidden and un-selectable by default!
		return header;
	}

	/*
	 * Creates a normal item with a Header linked.
	 */
	public static SimpleItem newSimpleItem(int i, HeaderItem header) {
		SimpleItem item;
		header.setSubtitle("Attached to Simple Item " + i);
		item = new SimpleItem("I" + i, header);
		item.setTitle("Simple Item " + i);
		item.setSubtitle("Subtitle " + i);
		return item;
	}

	/*
	 * Creates a normal expandable item with some subItems.
	 * The expandable has a Header linked.
	 */
	public static ExpandableItem newExpandableItem(int i, HeaderItem header) {
		//Items are expandable because they implements IExpandable
		ExpandableItem expandableItem;
		header.setSubtitle("Attached to Expandable Item " + i);
		expandableItem = new ExpandableItem("E" + i, header);
		expandableItem.setTitle("Expandable Item " + i);
		//SubItems are not expandable by default, but they might be if extends/implements IExpandable
		for (int j = 1; j <= SUB_ITEMS; j++) {
			SubItem subItem = new SubItem(expandableItem.getId() + "S" + j);
			subItem.setTitle("Sub Item " + j);
			expandableItem.addSubItem(subItem);
		}
		return expandableItem;
	}

	/*
	 * Creates a special expandable item which is also a Header.
	 * The subItems will have linked its parent as Header!
	 */
	private ExpandableHeaderItem newExpandableSectionItem(int i) {
		ExpandableHeaderItem expandableItem = new ExpandableHeaderItem("E" + i);
		expandableItem.setTitle("Expandable Header " + i);
		for (int j = 1; j <= SUB_ITEMS; j++) {
			SubItem subItem = new SubItem(expandableItem.getId() + "S" + j);
			subItem.setTitle("Sub Item " + j + " in expandable section");
			//In this case the Header is the same parent: ExpandableHeaderItem instance
			subItem.setHeader(expandableItem);
			expandableItem.addSubItem(subItem);
		}
		return expandableItem;
	}

	/*
	 * Creates a special expandable item which has another level of expandable.
	 * <p>IMPORTANT: Give different IDs to each child.</p>
	 */
	private ExpandableLevel0Item newExpandableLevelItem(int i) {
		//ExpandableLevel0Item is an expandable with Level 0
		ExpandableLevel0Item expandableItem = new ExpandableLevel0Item("E" + i);
		expandableItem.setTitle("Expandable Header Two-Levels " + i);
		for (int j = 1; j <= SUB_ITEMS; j++) {
			//ExpandableLevel1Item is an expandable as well with Level 1
			ExpandableLevel1Item expSubItem = new ExpandableLevel1Item(expandableItem.getId() + "SA" + j);
			expSubItem.setTitle("Expandable Sub Item " + j);
			for (int k = 1; k <= 3; k++) {
				SubItem subItem = new SubItem(expandableItem.getId() + expSubItem.getId() + "SB" + k);
				subItem.setTitle("Sub Sub Item " + k);
				expSubItem.addSubItem(subItem);
			}
			expandableItem.addSubItem(expSubItem);
		}
		return expandableItem;
	}

	/*-----------------------*/
	/* MAIN DATABASE METHODS */
	/*-----------------------*/

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

	public void removeSubItem(IExpandable parent, SubItem child) {
		//This split is for my examples
		if (parent instanceof ExpandableItem)
			((ExpandableItem) parent).removeSubItem(child);
		else if (parent instanceof ExpandableHeaderItem)
			((ExpandableHeaderItem) parent).removeSubItem(child);
	}

	public void addItem(int position, AbstractExampleItem item) {
		if (position < mItems.size())
			mItems.add(position, item);
		else
			mItems.add(item);
	}

	public void addSubItem(int position, IExpandable parent, SubItem subItem) {
		//This split is for my examples
		if (parent instanceof ExpandableItem)
			((ExpandableItem) parent).removeSubItem(subItem);
		else if (parent instanceof ExpandableHeaderItem)
			((ExpandableHeaderItem) parent).addSubItem(subItem);
	}

	public static void onDestroy() {
		mInstance = null;
	}

}