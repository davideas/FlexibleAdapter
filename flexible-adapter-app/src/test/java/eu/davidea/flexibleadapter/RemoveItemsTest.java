package eu.davidea.flexibleadapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

import static org.junit.Assert.assertEquals;

/**
 * @author Davide
 * @since 29/08/2017
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class RemoveItemsTest {

    private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    private List<AbstractFlexibleItem> mItems;

    @Before
    public void setUp() throws Exception {
        DatabaseService.getInstance().createEndlessDatabase(30);
        mItems = DatabaseService.getInstance().getDatabaseList();
        mAdapter = new FlexibleAdapter<>(mItems);
    }

    @SuppressWarnings("unchecked")
    private void createSignalAdapter(final CountDownLatch signal) {
        mAdapter = new FlexibleAdapter(mItems) {
            @Override
            protected void onPostFilter() {
                super.onPostFilter();
                signal.countDown();
            }
        };
    }

    @Test
    public void testRemoveItems() {
        // Items must be deleted from bottom in a cycle for
        // Remember: Adapter is backed by a List object.
        for (int i = mAdapter.getItemCount(); i >= 7; i--) {
            AbstractFlexibleItem item = mAdapter.getItem(i);
            // No necessary. but using getGlobalPositionOf() asynchronously,
            // it ensures the position of that item
            mAdapter.removeItem(mAdapter.getGlobalPositionOf(item));
        }
        assertEquals(7, mAdapter.getItemCount());
    }

    @Test
    public void testRemoveSelectedItems() {
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            if (i >= 7) mAdapter.addSelection(i);
        }
        assertEquals(23, mAdapter.getSelectedItemCount());
        mAdapter.removeAllSelectedItems();
        assertEquals(0, mAdapter.getSelectedItemCount());
        assertEquals(7, mAdapter.getItemCount());
    }

    @Test
    public void testRemoveItemsWithFilter() throws InterruptedException {
        CountDownLatch signal = new CountDownLatch(2);
        createSignalAdapter(signal);

        final int INITIAL_COUNT = mItems.size();
        final int REMOVE_COUNT = 5;
        mAdapter.setFilter("1"); // No delay
        mAdapter.filterItems();

        signal.await(100L, TimeUnit.MILLISECONDS);
        mAdapter.removeRange(0, REMOVE_COUNT);
        mAdapter.setFilter(""); // No delay
        mAdapter.filterItems();

        signal.await(100L, TimeUnit.MILLISECONDS);
        assertEquals(INITIAL_COUNT - REMOVE_COUNT, mAdapter.getItemCount());
    }

}