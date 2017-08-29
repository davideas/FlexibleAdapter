package eu.davidea.flexibleadapter;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.utils.Log;
import eu.davidea.samples.flexibleadapter.items.SimpleItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Davide Steduto
 * @since 18/10/2016
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class UpdateDataSetTest {

    FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    List<AbstractFlexibleItem> mInitialItems;

    @Before
    public void setUp() throws Exception {
        DatabaseService.getInstance().createHeadersSectionsDatabase(30, 5);
        mInitialItems = DatabaseService.getInstance().getDatabaseList();
        FlexibleAdapter.enableLogs(Log.Level.VERBOSE);
    }

    @Test
    public void testUpdateDataSet_WithNotifyDataSetChanged() throws Exception {
        mAdapter = new FlexibleAdapter<>(mInitialItems);
        mAdapter.showAllHeaders();

        List<AbstractFlexibleItem> initialItems = mAdapter.getCurrentItems();
        mAdapter.updateDataSet(DatabaseService.getInstance().getDatabaseList());
        List<AbstractFlexibleItem> updatedItems = mAdapter.getCurrentItems();

        assertEquals(initialItems.size(), mAdapter.getItemCount());
        assertThat(initialItems, Matchers.contains(updatedItems.toArray()));
    }

    @Test
    public void testUpdateDataSet_WithFineGrainedNotifications() throws Exception {
        mAdapter = new FlexibleAdapter<>(mInitialItems);
        mAdapter.showAllHeaders();

        List<AbstractFlexibleItem> initialItems = mAdapter.getCurrentItems();
        mAdapter.updateDataSet(DatabaseService.getInstance().getDatabaseList(), true);
        List<AbstractFlexibleItem> updatedItems = mAdapter.getCurrentItems();

        assertEquals(initialItems.size(), mAdapter.getItemCount());
        assertThat(initialItems, Matchers.contains(updatedItems.toArray()));
    }

    @Test
    public void testUpdateDataSet_WithWithoutNotifyChange() throws Exception {
        mAdapter = new FlexibleAdapter<>(mInitialItems);
        mAdapter.showAllHeaders();

        // Let's change the DB
        changeDatabaseContent();
        List<AbstractFlexibleItem> dbItems = DatabaseService.getInstance().getDatabaseList();

        // updateDataSet with Notify
        mAdapter.setNotifyChangeOfUnfilteredItems(true);
        mAdapter.updateDataSet(dbItems, true);
        List<AbstractFlexibleItem> updatedItems_withNotifyChange = mAdapter.getCurrentItems();

        // Restart
        setUp();
        mAdapter = new FlexibleAdapter<>(mInitialItems);
        mAdapter.showAllHeaders();
        changeDatabaseContent();

        // The content of the 2 DBs must coincide
        assertThat(dbItems, Matchers.contains(DatabaseService.getInstance().getDatabaseList().toArray()));

        // Change behavior and updateDataSet
        mAdapter.setNotifyChangeOfUnfilteredItems(false);
        mAdapter.updateDataSet(DatabaseService.getInstance().getDatabaseList(), true);
        List<AbstractFlexibleItem> updatedItems_withoutNotifyChange = mAdapter.getCurrentItems();

        // The content of the 2 lists "with Notify" and "without Notify" must coincide
        assertEquals(updatedItems_withNotifyChange.size(), updatedItems_withoutNotifyChange.size());
        assertThat(updatedItems_withNotifyChange, Matchers.contains(updatedItems_withoutNotifyChange.toArray()));
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            IFlexible iFlexible = updatedItems_withNotifyChange.get(i);
            if (iFlexible instanceof SimpleItem) {
                SimpleItem item1 = (SimpleItem) updatedItems_withNotifyChange.get(i);
                SimpleItem item2 = (SimpleItem) updatedItems_withoutNotifyChange.get(i);
                assertThat(item1, Matchers.samePropertyValuesAs(item2));
            }
        }
    }

    private void changeDatabaseContent() {
        // Remove item pos=2
        AbstractFlexibleItem itemToDelete = mAdapter.getItem(2);
        DatabaseService.getInstance().removeItem(itemToDelete);
        // Modify items content
        DatabaseService.getInstance().updateNewItems();
        // Add item pos=last
        IHeader header = mAdapter.getSectionHeader(30);
        AbstractFlexibleItem itemToAdd = DatabaseService.newSimpleItem(31, header);
        DatabaseService.getInstance().addItem(itemToAdd);
    }
}