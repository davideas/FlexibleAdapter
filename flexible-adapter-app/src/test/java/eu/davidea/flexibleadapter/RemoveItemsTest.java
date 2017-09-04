package eu.davidea.flexibleadapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

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

    FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    List<AbstractFlexibleItem> mItems;

    @Before
    public void setUp() throws Exception {
        DatabaseService.getInstance().createEndlessDatabase(30);
        mItems = DatabaseService.getInstance().getDatabaseList();
        mAdapter = new FlexibleAdapter<>(mItems);
    }

    @SuppressWarnings("Range")
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

    @SuppressWarnings("Range")
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

}