package eu.davidea.samples.flexibleadapter.services;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.holders.HeaderHolder;
import eu.davidea.samples.flexibleadapter.holders.ItemHolder;
import eu.davidea.samples.flexibleadapter.items.AbstractItem;
import eu.davidea.samples.flexibleadapter.items.AnimatorExpandableItem;
import eu.davidea.samples.flexibleadapter.items.AnimatorSubItem;
import eu.davidea.samples.flexibleadapter.items.ConfigurationItem;
import eu.davidea.samples.flexibleadapter.items.ExpandableHeaderItem;
import eu.davidea.samples.flexibleadapter.items.ExpandableItem;
import eu.davidea.samples.flexibleadapter.items.ExpandableLevel0Item;
import eu.davidea.samples.flexibleadapter.items.ExpandableLevel1Item;
import eu.davidea.samples.flexibleadapter.items.HeaderItem;
import eu.davidea.samples.flexibleadapter.items.InstagramHeaderItem;
import eu.davidea.samples.flexibleadapter.items.InstagramItem;
import eu.davidea.samples.flexibleadapter.items.OverallItem;
import eu.davidea.samples.flexibleadapter.items.SimpleItem;
import eu.davidea.samples.flexibleadapter.items.StaggeredHeaderItem;
import eu.davidea.samples.flexibleadapter.items.StaggeredItem;
import eu.davidea.samples.flexibleadapter.items.StaggeredItemStatus;
import eu.davidea.samples.flexibleadapter.items.SubItem;
import eu.davidea.samples.flexibleadapter.models.HeaderModel;
import eu.davidea.samples.flexibleadapter.models.ItemModel;

/**
 * Created by Davide Steduto on 23/11/2015.
 * Project FlexibleAdapter.
 */
public class DatabaseService {

    private static final String TAG = DatabaseService.class.getSimpleName();
    private static DatabaseService mInstance;
    private static final int SUB_ITEMS = 4;
    private DatabaseType databaseType = DatabaseType.NONE;

    //Database original items (used as cache)
    private List<AbstractFlexibleItem> mItems = new ArrayList<AbstractFlexibleItem>();
    private Map<StaggeredItemStatus, StaggeredHeaderItem> headers;


    private DatabaseService() {
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

    public void clear() {
        mItems.clear();
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /*
     * List of CardView as entry list, showing the functionality of the library.
     * It also shows how adapter animation can be configured.
     */
    //TODO: Review the description of all examples
    public void createOverallDatabase(Resources resources) {
        databaseType = DatabaseType.OVERALL;
        mItems.clear();

        mItems.add(new OverallItem(R.id.nav_selection_modes, resources.getString(R.string.selection_modes))
                .withDescription(resources.getString(R.string.selection_modes_description))
                .withIcon(resources.getDrawable(R.drawable.ic_select_all_grey600_24dp)));

        mItems.add(new OverallItem(R.id.nav_filter, resources.getString(R.string.filter))
                .withDescription(resources.getString(R.string.filter_description))
                .withIcon(resources.getDrawable(R.drawable.ic_filter_outline_grey600_24dp)));

        mItems.add(new OverallItem(R.id.nav_animator, resources.getString(R.string.animator))
                .withDescription(resources.getString(R.string.animator_description))
                .withIcon(resources.getDrawable(R.drawable.ic_chart_gantt_grey600_24dp)));

        mItems.add(new OverallItem(R.id.nav_headers_and_sections, resources.getString(R.string.headers_sections))
                .withDescription(resources.getString(R.string.headers_sections_description))
                .withIcon(resources.getDrawable(R.drawable.ic_sections_grey600_24dp)));

        mItems.add(new OverallItem(R.id.nav_expandable_sections, resources.getString(R.string.expandable_sections))
                .withDescription(resources.getString(R.string.expandable_sections_description))
                .withIcon(resources.getDrawable(R.drawable.ic_expandable_grey_600_24dp)));

        mItems.add(new OverallItem(R.id.nav_multi_level_expandable, resources.getString(R.string.multi_level_expandable))
                .withDescription(resources.getString(R.string.multi_level_expandable_description))
                .withIcon(resources.getDrawable(R.drawable.ic_expandable_grey_600_24dp)));

        mItems.add(new OverallItem(R.id.nav_endless_scrolling, resources.getString(R.string.endless_scrolling))
                .withDescription(resources.getString(R.string.endless_scrolling_description))
                .withIcon(resources.getDrawable(R.drawable.ic_playlist_play_grey600_24dp)));

        //Special Use Cases
        mItems.add(new OverallItem(R.id.nav_db_headers_and_sections, resources.getString(R.string.databinding))
                .withDescription(resources.getString(R.string.databinding_description))
                .withIcon(resources.getDrawable(R.drawable.ic_link_grey_600_24dp)));

        mItems.add(new OverallItem(R.id.nav_model_holders, resources.getString(R.string.model_holders))
                .withDescription(resources.getString(R.string.model_holders_description))
                .withIcon(resources.getDrawable(R.drawable.ic_select_inverse_grey600_24dp)));

        mItems.add(new OverallItem(R.id.nav_instagram_headers, resources.getString(R.string.instagram_headers))
                .withDescription(resources.getString(R.string.instagram_headers_description))
                .withIcon(resources.getDrawable(R.drawable.ic_instagram_grey600_24dp)));

        mItems.add(new OverallItem(R.id.nav_staggered, resources.getString(R.string.staggered_layout))
                .withDescription(resources.getString(R.string.staggered_description))
                .withIcon(resources.getDrawable(R.drawable.ic_dashboard_grey600_24dp)));

        mItems.add(new OverallItem(R.id.nav_viewpager, resources.getString(R.string.viewpager))
                .withDescription(resources.getString(R.string.viewpager_description))
                .withIcon(resources.getDrawable(R.drawable.ic_view_carousel_grey600_24dp)));
    }

    /*
     * List of Configuration items
     */
    public void createConfigurationDatabase(Resources resources) {
        databaseType = DatabaseType.CONFIGURATION;
        mItems.clear();

        mItems.add(new ConfigurationItem(DatabaseConfiguration.TITLE, ConfigurationItem.NONE)
                .withTitle(resources.getString(R.string.config_title))
                .withDescription(resources.getString(R.string.config_description))
        );
        mItems.add(new ConfigurationItem(DatabaseConfiguration.NUMBER_OF_ITEMS, ConfigurationItem.SEEK_BAR)
                .withTitle(resources.getString(R.string.config_num_of_items))
                .withValue(DatabaseConfiguration.size)//items
                .withMaxValue(DatabaseConfiguration.maxSize)
                .withStepValue(50)
        );
        mItems.add(new ConfigurationItem(DatabaseConfiguration.SEARCH_DELAY, ConfigurationItem.SEEK_BAR)
                .withTitle(resources.getString(R.string.config_delay))
                .withDescription(resources.getString(R.string.config_delay_description))
                .withValue(DatabaseConfiguration.delay)//milliseconds
                .withMaxValue(DatabaseConfiguration.maxSearchDelay)
                .withStepValue(10)
        );
        mItems.add(new ConfigurationItem(DatabaseConfiguration.ANIMATE_TO_LIMIT, ConfigurationItem.SEEK_BAR)
                .withTitle(resources.getString(R.string.config_animate_to_limit))
                .withDescription(resources.getString(R.string.config_animate_to_limit_description))
                .withValue(DatabaseConfiguration.animateToLimit)//limit items
                .withMaxValue(DatabaseConfiguration.maxSize)
                .withStepValue(50)
        );
        mItems.add(new ConfigurationItem(DatabaseConfiguration.NOTIFY_CHANGE, ConfigurationItem.SWITCH)
                .withTitle(resources.getString(R.string.config_notify_change))
                .withDescription(resources.getString(R.string.config_notify_change_description))
                .withValue(DatabaseConfiguration.notifyChange ? 1 : 0)
        );
        mItems.add(new ConfigurationItem(DatabaseConfiguration.NOTIFY_MOVE, ConfigurationItem.SWITCH)
                .withTitle(resources.getString(R.string.config_notify_move))
                .withDescription(resources.getString(R.string.config_notify_move_description))
                .withValue(DatabaseConfiguration.notifyMove ? 1 : 0)
        );
    }

    /*
     * List of Simple items
     */
    public void createEndlessDatabase(int startSize) {
        databaseType = DatabaseType.ENDLESS_SCROLLING;
        mItems.clear();
        for (int i = 0; i < startSize; i++) {
            mItems.add(newSimpleItem(i + 1, null));
        }
    }

    /*
     * List of Animators items (with sections and Animators SubItems).
     */
    public void createAnimatorsDatabase(int size) {
        databaseType = DatabaseType.ANIMATORS;
        mItems.clear();
        for (int i = 0; i < size; i++) {
            mItems.add(newAnimatorItem(i + 1));
        }
    }

    /*
     * List of Simple Items with Header attached. Only Simple Items will be added to the list.
     */
    public void createHeadersSectionsDatabase(int size, int headers) {
        databaseType = DatabaseType.HEADERS_SECTIONS;
        HeaderItem header = null;
        mItems.clear();
        int lastHeaderId = 0;
        for (int i = 0; i < size; i++) {
            header = i % Math.round(size / headers) == 0 ? newHeader(++lastHeaderId) : header;
            mItems.add(newSimpleItem(i + 1, header));
        }
    }

    /*
     * Same as HeadersSection, but with different property type
     */
    public void createDataBindingDatabase(int size, int headers) {
        createHeadersSectionsDatabase(size, headers);
        databaseType = DatabaseType.DATA_BINDING;
    }

    /*
     * List of Holder Items and Header. Only Holder Simple Items will be
     * added to the list. IHolder items hold the model data inside.
     */
    public void createHolderSectionsDatabase(int size, int headers) {
        databaseType = DatabaseType.MODEL_HOLDERS;
        HeaderHolder header = null;
        mItems.clear();
        int lastHeaderId = 0;
        for (int i = 0; i < size; i++) {
            header = i % Math.round(size / headers) == 0 ? newHeaderHolder(++lastHeaderId) : header;
            mItems.add(newItemHolder(i + 1, header));
        }
    }

    /*
     * List of Expandable items (headers/sections) with SubItems with Header attached.
     */
    public void createExpandableSectionsDatabase(int size) {
        databaseType = DatabaseType.EXPANDABLE_SECTIONS;
        mItems.clear();
        for (int i = 0; i < size; i++) {
            mItems.add(newExpandableSectionItem(i + 1));//With expansion level 0
        }
    }

    /*
     * List of Headers (level 0) with Expandable SubItems (level 1) with SubItems.
     */
    public void createExpandableMultiLevelDatabase(int size) {
        databaseType = DatabaseType.EXPANDABLE_MULTI_LEVEL;
        mItems.clear();
        for (int i = 0; i < size; i++) {
            mItems.add(newExpandableLevelItem(i + 1));//With expansion level 1
        }
    }

    /*
     * List of Instagram items
     */
    public void createInstagramHeadersDatabase(int startSize) {
        databaseType = DatabaseType.INSTAGRAM;
        mItems.clear();
        for (int i = 0; i < startSize; i++) {
            mItems.add(newInstagramItem(i + 1));
        }
    }

    /*
     * List of CardView items
     */
    public void createStaggeredDatabase(Context context) {
        databaseType = DatabaseType.LAYOUT_STAGGERED;
        mItems.clear();

        if (headers == null) {
            headers = new HashMap<>();
            headers.put(StaggeredItemStatus.A, new StaggeredHeaderItem(0, context.getString(StaggeredItemStatus.A.getResId())));
            headers.put(StaggeredItemStatus.B, new StaggeredHeaderItem(1, context.getString(StaggeredItemStatus.B.getResId())));
            headers.put(StaggeredItemStatus.C, new StaggeredHeaderItem(2, context.getString(StaggeredItemStatus.C.getResId())));
            headers.put(StaggeredItemStatus.D, new StaggeredHeaderItem(3, context.getString(StaggeredItemStatus.D.getResId())));
            headers.put(StaggeredItemStatus.E, new StaggeredHeaderItem(4, context.getString(StaggeredItemStatus.E.getResId())));
        }

        for (int i = 0; i < 15; i++) {
            mItems.add(newStaggeredItem(i + 1, getHeaderByStatus(StaggeredItemStatus.C)));
        }
        createMergedItems();
    }

    protected void createMergedItems() {
        //Simulating merged items
        if (mItems.size() > 2)
            mergeItem((StaggeredItem) mItems.get(1), (StaggeredItem) mItems.remove(2));
        if (mItems.size() > 5)
            mergeItem((StaggeredItem) mItems.get(4), (StaggeredItem) mItems.remove(5));
        if (mItems.size() > 7)
            mergeItem((StaggeredItem) mItems.get(4), (StaggeredItem) mItems.remove(7));
        if (mItems.size() > 8)
            mergeItem((StaggeredItem) mItems.get(7), (StaggeredItem) mItems.remove(8));
        if (mItems.size() > 8)
            mergeItem((StaggeredItem) mItems.get(7), (StaggeredItem) mItems.remove(8));
        if (mItems.size() > 9)
            mergeItem((StaggeredItem) mItems.get(7), (StaggeredItem) mItems.remove(9));
    }

	/*---------------*/
	/* ITEM CREATION */
	/*---------------*/

    /*
     * Creates a Header item.
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
     * IMPORTANT: Give different IDs to each child and override getExpansionLevel()!
     */
    private ExpandableLevel0Item newExpandableLevelItem(int i) {
        //ExpandableLevel0Item is an expandable with Level=0
        ExpandableLevel0Item expandableItem = new ExpandableLevel0Item("EI" + i);
        expandableItem.setTitle("Expandable Two-Levels " + i);
        for (int j = 1; j <= SUB_ITEMS; j++) {
            //ExpandableLevel1Item is an expandable as well with Level=1
            ExpandableLevel1Item expSubItem = new ExpandableLevel1Item(expandableItem.getId() + "-EII" + j);
            expSubItem.setTitle("Expandable Sub Item " + j);
            for (int k = 1; k <= 3; k++) {
                SubItem subItem = new SubItem(expSubItem.getId() + "-SB" + k);
                subItem.setTitle("Simple Sub Item " + k);
                expSubItem.addSubItem(subItem);
            }
            expandableItem.addSubItem(expSubItem);
        }
        return expandableItem;
    }

    /*
     * Creates a special animator expandable item which is also a Header.
     * The animator subItems will have linked its parent as Header!
     */
    public static AnimatorExpandableItem newAnimatorItem(int i) {
        AnimatorExpandableItem animatorItem = new AnimatorExpandableItem("AH" + i);
        animatorItem.setTitle("Animator Header " + i);
        for (int j = 1; j <= SUB_ITEMS; j++) {
            AnimatorSubItem subItem = new AnimatorSubItem(animatorItem.getId() + "-SB" + j, animatorItem);
            subItem.setTitle("Sub Item " + j);
            animatorItem.addSubItem(subItem);
        }
        return animatorItem;
    }

    /*
      * Creates a similar instagram item with a Header linked.
      */
    public static InstagramItem newInstagramItem(int i) {
        InstagramHeaderItem header = new InstagramHeaderItem("H" + i);
        String place = InstagramRandomData.getRandomPlace();
        return new InstagramItem("I" + i, header)
                .withName(InstagramRandomData.getRandomName())
                .withPlace(place)
                .withImageUrl(InstagramRandomData.getImageUrl(place));
    }

    /*
      * Creates a staggered item with a Header linked.
      */
    public static StaggeredItem newStaggeredItem(int i, StaggeredHeaderItem header) {
        return new StaggeredItem(i, header);
    }

    private HeaderHolder newHeaderHolder(int i) {
        HeaderModel model = new HeaderModel("H" + i);
        model.setTitle("Header " + i);
        return new HeaderHolder(model);
    }

    private ItemHolder newItemHolder(int i, HeaderHolder header) {
        ItemModel model = new ItemModel("I" + i);
        model.setTitle("Holder Item " + i);
        model.setSubtitle("Subtitle " + i);
        return new ItemHolder(model, header);
    }

	/*-----------------------*/
	/* MAIN DATABASE METHODS */
	/*-----------------------*/

    /**
     * @return The original list.
     */
    public List<AbstractFlexibleItem> getDatabaseList() {
        Log.i(TAG, "Database Type: " + databaseType);
        // Until version RC1:
        // Return a copy of the DB: we will perform some tricky code on this list.
        //return new ArrayList<>(mItems);

        // From version RC2:
        // mItems can be returned without making a copy: The copy is now done internally by the Adapter.
        return mItems;
    }

    public boolean isEmpty() {
        return mItems == null || mItems.isEmpty();
    }

    public void moveItem(AbstractFlexibleItem fromItem, AbstractFlexibleItem toItem) {
        int fromPosition = mItems.indexOf(fromItem);
        int toPosition = mItems.indexOf(toItem);
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

    public void removeAll() {
        mItems.clear();
    }

    public void addAll(int position, List<AbstractFlexibleItem> newItems) {
        mItems.addAll(position, newItems);
    }

    public void addAll(List<AbstractFlexibleItem> newItems) {
        mItems.addAll(newItems);
    }

    public void addItem(int position, AbstractItem item) {
        if (position < mItems.size())
            mItems.add(position, item);
        else
            addItem(item);
    }

    public void addItem(AbstractFlexibleItem item, Comparator comparator) {
        addItem(item);
        sort(comparator);
    }

    public void addItem(AbstractFlexibleItem item) {
        mItems.add(item);
    }

    public void addSubItem(int position, IExpandable parent, SubItem subItem) {
        //This split is for my examples
        if (parent instanceof ExpandableItem)
            ((ExpandableItem) parent).addSubItem(subItem);
        else if (parent instanceof ExpandableHeaderItem)
            ((ExpandableHeaderItem) parent).addSubItem(subItem);
    }

    public static void onDestroy() {
        mInstance = null;
    }

	/*---------------------*/
	/* MERGE-SPLIT METHODS */
	/*---------------------*/

    public int getMaxStaggeredId() {
        int count = 1;
        for (AbstractFlexibleItem item : mItems) {
            StaggeredItem staggeredItem = (StaggeredItem) item;
            count += staggeredItem.countMergedItems() + 1;
        }
        return count;
    }

    public StaggeredHeaderItem getHeaderByStatus(StaggeredItemStatus a) {
        return headers.get(a);
    }

    public StaggeredItem getRandomStaggeredItem() {
        return (StaggeredItem) mItems.get(new Random().nextInt(mItems.size() - 1));
    }

    public void resetHeaders() {
        for (StaggeredHeaderItem header : headers.values()) {
            header.setHidden(true);
        }
    }

    public void resetItems() {
        List<StaggeredItem> mergedItems = new ArrayList<>();
        for (AbstractFlexibleItem item : mItems) {
            if (item instanceof StaggeredItem) {
                StaggeredItem staggeredItem = (StaggeredItem) item;
                staggeredItem.setStatus(StaggeredItemStatus.C);
                staggeredItem.setHeader(headers.get(StaggeredItemStatus.C));
                mergedItems.addAll(staggeredItem.splitAllItems());
            }
        }
        for (StaggeredHeaderItem header : headers.values()) {
            header.setHidden(true);
        }
        mItems.addAll(mergedItems);
        sort(new ItemComparatorById());
        createMergedItems();
    }

    public void mergeItem(StaggeredItem mainItem, StaggeredItem itemToMerge) {
        mainItem.mergeItem(itemToMerge);
        itemToMerge.setStatus(mainItem.getStatus());
        //Add more items already merged in itemsToMerge
        if (itemToMerge.getMergedItems() != null) {
            for (StaggeredItem subItem : itemToMerge.getMergedItems()) {
                mainItem.mergeItem(subItem);
                subItem.setStatus(mainItem.getStatus());
                mItems.remove(subItem);
            }
            itemToMerge.setMergedItems(null);
        }
        mItems.remove(itemToMerge);
    }

    public void splitAllItems(StaggeredItem mainItem) {
        splitItem(mainItem, null);
    }

    public void splitItem(StaggeredItem mainItem, StaggeredItem itemToSplit) {
        if (itemToSplit != null) {
            mainItem.splitItem(itemToSplit);
            mItems.add(itemToSplit);
        } else {
            mItems.addAll(mainItem.splitAllItems());
        }
        sort(new ItemComparatorById());
    }

    /**
     * This demonstrates that new content of existing items are really rebound and
     * notified with CHANGE Payload in the Adapter list when refreshed.
     */
    public void updateNewItems() {
        for (IFlexible iFlexible : mItems) {
            if (iFlexible instanceof AbstractItem) {
                AbstractItem item = (AbstractItem) iFlexible;
                item.increaseUpdates();
            }
        }
    }

    public void sort(Comparator<IFlexible> itemComparatorById) {
        Collections.sort(mItems, itemComparatorById);
    }

    /**
     * A simple item comparator by Id.
     */
    public static class ItemComparatorById implements Comparator<IFlexible> {
        @Override
        public int compare(IFlexible lhs, IFlexible rhs) {
            int result = ((StaggeredItem) lhs).getHeader().getOrder() - ((StaggeredItem) rhs).getHeader().getOrder();
            if (result == 0)
                result = ((StaggeredItem) lhs).getId() - ((StaggeredItem) rhs).getId();
            return result;
        }
    }

    /**
     * A complex item comparator able to compare different item types for different view types:
     * Sort by HEADER than by ITEM.
     * <p>In this way items are always displayed in the corresponding section and position.</p>
     */
    public static class ItemComparatorByGroup implements Comparator<IFlexible> {
        @Override
        public int compare(IFlexible lhs, IFlexible rhs) {
            int result = 0;
            if (lhs instanceof StaggeredHeaderItem && rhs instanceof StaggeredHeaderItem) {
                result = ((StaggeredHeaderItem) lhs).getOrder() - ((StaggeredHeaderItem) rhs).getOrder();
            } else if (lhs instanceof StaggeredItem && rhs instanceof StaggeredItem) {
                result = ((StaggeredItem) lhs).getHeader().getOrder() - ((StaggeredItem) rhs).getHeader().getOrder();
                if (result == 0)
                    result = ((StaggeredItem) lhs).getId() - ((StaggeredItem) rhs).getId();
            } else if (lhs instanceof StaggeredItem && rhs instanceof StaggeredHeaderItem) {
                result = ((StaggeredItem) lhs).getHeader().getOrder() - ((StaggeredHeaderItem) rhs).getOrder();
            } else if (lhs instanceof StaggeredHeaderItem && rhs instanceof StaggeredItem) {
                result = ((StaggeredHeaderItem) lhs).getOrder() - ((StaggeredItem) rhs).getHeader().getOrder();
            }
            return result;
        }
    }

}