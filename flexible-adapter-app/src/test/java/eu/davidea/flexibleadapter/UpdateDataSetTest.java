package eu.davidea.flexibleadapter;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.utils.Log;
import eu.davidea.samples.flexibleadapter.ui.items.SimpleItem;
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

    private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    private List<AbstractFlexibleItem> mInitialItems;

    @Before
    public void setUp() throws Exception {
        DatabaseService.getInstance().createHeadersSectionsDatabase(30, 5);
        mInitialItems = DatabaseService.getInstance().getDatabaseList();
        FlexibleAdapter.enableLogs(Log.Level.VERBOSE);
    }

    @SuppressWarnings("unchecked")
    private void createSignalAdapter(final CountDownLatch signal) {
        mAdapter = new FlexibleAdapter(mInitialItems) {
            @Override
            protected void onPostUpdate() {
                super.onPostUpdate();
                signal.countDown();
            }
        };
        mAdapter.showAllHeaders();
    }

    @Test
    public void testUpdateDataSet_WithNotifyDataSetChanged() throws Exception {
        CountDownLatch signal = new CountDownLatch(1);
        createSignalAdapter(signal);

        List<AbstractFlexibleItem> initialItems = mAdapter.getCurrentItems();
        mAdapter.updateDataSet(DatabaseService.getInstance().getDatabaseList());
        List<AbstractFlexibleItem> updatedItems = mAdapter.getCurrentItems();

        signal.await(300L, TimeUnit.MILLISECONDS);
        assertEquals(initialItems.size(), mAdapter.getItemCount());
        assertThat(initialItems, Matchers.contains(updatedItems.toArray()));
    }

    @Test
    public void testUpdateDataSet_WithFineGrainedNotifications() throws Exception {
        CountDownLatch signal = new CountDownLatch(1);
        createSignalAdapter(signal);

        List<AbstractFlexibleItem> initialItems = mAdapter.getCurrentItems();
        mAdapter.updateDataSet(DatabaseService.getInstance().getDatabaseList(), true);
        List<AbstractFlexibleItem> updatedItems = mAdapter.getCurrentItems();

        signal.await(300L, TimeUnit.MILLISECONDS);
        assertEquals(initialItems.size(), mAdapter.getItemCount());
        assertThat(initialItems, Matchers.contains(updatedItems.toArray()));
    }

    @Test
    public void testUpdateDataSet_WithWithoutNotifyChange() throws Exception {
        CountDownLatch signal = new CountDownLatch(1);
        createSignalAdapter(signal);

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
        signal.await(300L, TimeUnit.MILLISECONDS);
        assertEquals(updatedItems_withNotifyChange.size(), updatedItems_withoutNotifyChange.size());
        assertThat(updatedItems_withNotifyChange, Matchers.contains(updatedItems_withoutNotifyChange.toArray()));
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            IFlexible iFlexible = updatedItems_withNotifyChange.get(i);
            if (iFlexible instanceof SimpleItem) {
                SimpleItem item1 = (SimpleItem) updatedItems_withNotifyChange.get(i);
                SimpleItem item2 = (SimpleItem) updatedItems_withoutNotifyChange.get(i);
                //assertThat(item1, Matchers.samePropertyValuesAs(item2)); // Problem with Matchers
                assertEquals(item1.getId(), item2.getId());
                assertEquals(item1.getTitle(), item2.getTitle());
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