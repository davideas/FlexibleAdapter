package eu.davidea.samples.flexibleadapter.services;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.models.AbstractModelItem;
import eu.davidea.samples.flexibleadapter.models.ExpandableHeaderItem;
import eu.davidea.samples.flexibleadapter.models.ExpandableItem;
import eu.davidea.samples.flexibleadapter.models.ExpandableLevel0Item;
import eu.davidea.samples.flexibleadapter.models.ExpandableLevel1Item;
import eu.davidea.samples.flexibleadapter.models.HeaderItem;
import eu.davidea.samples.flexibleadapter.models.OverallItem;
import eu.davidea.samples.flexibleadapter.models.SimpleItem;
import eu.davidea.samples.flexibleadapter.models.SubItem;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;

/**
 * Created by Davide Steduto on 23/11/2015.
 * Project FlexibleAdapter.
 */
public class DatabaseService {

	private static DatabaseService mInstance;
	private static final int ITEMS = 90, SUB_ITEMS = 6, HEADERS = 30;
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

	/*-------------------*/
	/* DATABASE CREATION */
	/*-------------------*/

	/*
	 * List of CardView as entry list, showing the functionality of the library.
	 * It also shows how adapter animation can be configured.
	 */
	public void createOverallDatabase(Resources resources) {
		mItems.clear();
		mItems.add(new OverallItem(R.id.nav_endless_scrolling, resources.getString(R.string.endless_scrolling))
				.withDescription(resources.getString(R.string.endless_scrolling_description))
				.withIcon(resources.getDrawable(R.drawable.ic_playlist_play_grey600_24dp)));

		mItems.add(new OverallItem(R.id.nav_selection_modes, resources.getString(R.string.selection_modes))
				.withDescription(resources.getString(R.string.selection_modes_description))
				.withIcon(resources.getDrawable(R.drawable.ic_select_all_grey600_24dp))
				.withEnabled(false));

		mItems.add(new OverallItem(R.id.nav_expandable, resources.getString(R.string.expandable))
				.withDescription(resources.getString(R.string.expandable_description))
				.withIcon(resources.getDrawable(R.drawable.ic_expandable_grey_600_24dp))
				.withEnabled(false));

		mItems.add(new OverallItem(R.id.nav_multi_level_expandable, resources.getString(R.string.multi_level_expandable))
				.withDescription(resources.getString(R.string.multi_level_expandable_description))
				.withIcon(resources.getDrawable(R.drawable.ic_expandable_grey_600_24dp)));

		mItems.add(new OverallItem(R.id.nav_expandable_sections, resources.getString(R.string.expandable_sections))
				.withDescription(resources.getString(R.string.expandable_sections_description))
				.withIcon(resources.getDrawable(R.drawable.ic_expandable_grey_600_24dp)));

		mItems.add(new OverallItem(R.id.nav_headers_and_sections, resources.getString(R.string.headers_sections))
				.withDescription(resources.getString(R.string.headers_sections_description))
				.withIcon(resources.getDrawable(R.drawable.ic_sections_grey600_24dp)));

		mItems.add(new OverallItem(R.id.nav_instagram_headers, resources.getString(R.string.instagram_headers))
				.withDescription(resources.getString(R.string.instagram_headers_description))
				.withIcon(resources.getDrawable(R.drawable.ic_instagram_grey600_24dp))
				.withEnabled(false));

		mItems.add(new OverallItem(R.id.nav_model_holders, resources.getString(R.string.model_holders))
				.withDescription(resources.getString(R.string.model_holders_description))
				.withIcon(resources.getDrawable(R.drawable.ic_select_inverse_grey600_24dp))
				.withEnabled(false));
	}

	/*
	 * List of simple items TODO: showing different types of animations
	 */
	public void createEndlessDatabase() {
		mItems.clear();
		for (int i = 0; i < ITEMS; i++) {
			mItems.add(newSimpleItem(i + 1, null));
		}
	}

	/*
	 * List of expandable items (headers/sections) with SubItems with Header attached.
	 */
	public void createExpandableSectionsDatabase() {
		mItems.clear();
		for (int i = 0; i < ITEMS; i++) {
			mItems.add(newExpandableSectionItem(i + 1));//With expansion level 0
		}
	}

	/*
	 * List of headers (level 0) with expandable SubItems (level 1) with SubItems.
	 */
	public void createExpandableMultiLevelDatabase() {
		mItems.clear();
		for (int i = 0; i < ITEMS; i++) {
			mItems.add(newExpandableLevelItem(i + 1));//With expansion level 1
		}
	}

	/*
	 * List of Simple Items with Header attached. Only Simple Items will be added to the list.
	 */
	public void createHeadersSectionsDatabase() {
		HeaderItem header = null;
		mItems.clear();
		for (int i = 0; i < ITEMS; i++) {
			header = i % (ITEMS/HEADERS) == 0 ? newHeader(i * HEADERS/ITEMS + 1) : header;
			mItems.add(newSimpleItem(i + 1, header));
		}
	}

	/*---------------*/
	/* ITEM CREATION */
	/*---------------*/

	/*
	 * Create a Header item for normal items.
	 */
	public static HeaderItem newHeader(int i) {
		HeaderItem header = new HeaderItem("H" + i);
		header.setTitle("Header " + i);
		//header is hidden and un-selectable by default!
		return header;
	}

	/*
	 * Creates a normal item with a Header linked.
	 */
	public static SimpleItem newSimpleItem(int i, IHeader header) {
		SimpleItem item = new SimpleItem("I" + i, (HeaderItem) header);
		item.setTitle("Simple Item " + i);
		return item;
	}

	/*
	 * Creates a normal expandable item with some subItems.
	 * The expandable has a Header linked.
	 */
	public static ExpandableItem newExpandableItem(int i, IHeader header) {
		//Items are expandable because they implements IExpandable
		ExpandableItem expandableItem = new ExpandableItem("E" + i, (HeaderItem) header);
		expandableItem.setTitle("Expandable Item " + i);
		//SubItems are not expandable by default, but they might be if extends/implements IExpandable
		for (int j = 1; j <= SUB_ITEMS; j++) {
			SubItem subItem = new SubItem(expandableItem.getId() + "-SB" + j);
			subItem.setTitle("Sub Item " + j);
			expandableItem.addSubItem(subItem);
		}
		return expandableItem;
	}

	/*
	 * Creates a special expandable item which is also a Header.
	 * The subItems will have linked its parent as Header!
	 */
	public static ExpandableHeaderItem newExpandableSectionItem(int i) {
		ExpandableHeaderItem expandableItem = new ExpandableHeaderItem("EH" + i);
		expandableItem.setTitle("Expandable Header " + i);
		for (int j = 1; j <= SUB_ITEMS; j++) {
			SubItem subItem = new SubItem(expandableItem.getId() + "-SB" + j);
			subItem.setTitle("Sub Item " + j);
			//In this case the Header is the same parent: ExpandableHeaderItem instance
			subItem.setHeader(expandableItem);
			expandableItem.addSubItem(subItem);
		}
		return expandableItem;
	}

	/*
	 * Creates a special expandable item which has another level of expandable.
	 * <p>IMPORTANT: Give different IDs to each child and override getExpansionLevel().</p>
	 */
	private ExpandableLevel0Item newExpandableLevelItem(int i) {
		//ExpandableLevel0Item is an expandable with Level=0
		ExpandableLevel0Item expandableItem = new ExpandableLevel0Item("EI" + i);
		expandableItem.setTitle("Expandable Two-Levels " + i);
		for (int j = 1; j <= SUB_ITEMS; j++) {
			//ExpandableLevel1Item is an expandable as well with Level=1
			ExpandableLevel1Item expSubItem = new ExpandableLevel1Item(expandableItem.getId() + "-EL" + j);
			expSubItem.setTitle("Expandable Sub Item " + j);
			for (int k = 1; k <= 3; k++) {
				SubItem subItem = new SubItem(expandableItem.getId() + expSubItem.getId() + "-SB" + k);
				subItem.setTitle("Simple Sub Item " + k);
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
	public List<AbstractFlexibleItem> getDatabaseList() {
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

	public void addItem(int position, AbstractModelItem item) {
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