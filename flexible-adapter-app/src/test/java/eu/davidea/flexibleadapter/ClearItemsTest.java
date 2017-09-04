package eu.davidea.flexibleadapter;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.samples.flexibleadapter.items.HeaderItem;
import eu.davidea.samples.flexibleadapter.items.ScrollableFooterItem;
import eu.davidea.samples.flexibleadapter.items.ScrollableLayoutItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Davide
 * @since 24/05/2017
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class ClearItemsTest {

    FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    List<AbstractFlexibleItem> mItems;
    AbstractFlexibleItem scrollableHeader;
    AbstractFlexibleItem scrollableFooter;

    @Before
    public void setUp() throws Exception {
        DatabaseService.getInstance().createHeadersSectionsDatabase(30, 5);
        mItems = DatabaseService.getInstance().getDatabaseList();
        mAdapter = new FlexibleAdapter<>(mItems);
        mAdapter.setDisplayHeadersAtStartUp(true);
        scrollableHeader = new ScrollableLayoutItem("SLI");
        scrollableFooter = new ScrollableFooterItem("SFI");
    }

    @Test
    public void testClearAllBut_Empty() {
        mAdapter.clearAllBut();
        assertEquals(0, mAdapter.getItemCount());
    }

    @Test
    public void testClear_EmptyAdapter() {
        FlexibleAdapter<AbstractFlexibleItem> adapter = new FlexibleAdapter<AbstractFlexibleItem>(null) {
            @Override
            public IHeader getHeaderOf(@NonNull AbstractFlexibleItem item) {
                assertNotNull(item);
                return super.getHeaderOf(item);
            }
        };
        adapter.updateDataSet(new ArrayList<AbstractFlexibleItem>(), false);
        adapter.clear();
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testClearAllBut_OnlyScrollableItems() {
        // Add scrollable items
        mAdapter.addScrollableHeader(scrollableHeader);
        mAdapter.addScrollableFooter(scrollableFooter);
        assertEquals(37, mAdapter.getItemCount());
        assertEquals(35, mAdapter.getMainItemCount());

        // Clear all, retains only scrollable items
        mAdapter.clearAllBut();
        assertEquals(2, mAdapter.getItemCount());
        assertEquals(0, mAdapter.getMainItemCount());
        assertEquals(scrollableHeader, mAdapter.getItem(0));
        assertEquals(scrollableFooter, mAdapter.getItem(1));
    }

    @Test
    public void testClearAllBut_WithoutScrollableItems() {
        // Grab the LayoutRes to retain
        AbstractFlexibleItem headerItem = mAdapter.getItem(0);
        assertEquals(35, mAdapter.getItemCount());
        assertEquals(35, mAdapter.getMainItemCount());

        // Clear all simple items, retains header items
        mAdapter.clearAllBut(headerItem.getItemViewType());
        assertEquals(5, mAdapter.getItemCount());
        assertEquals(5, mAdapter.getMainItemCount());
        assertTrue(mAdapter.getItem(0) instanceof HeaderItem);
        assertTrue(mAdapter.getItem(mAdapter.getItemCount() - 1) instanceof HeaderItem);
    }

    @Test
    public void testClearAllBut_WithScrollableItems() {
        // Grab the LayoutRes to retain
        AbstractFlexibleItem headerItem = mAdapter.getItem(0);

        // Add scrollable items
        mAdapter.addScrollableHeader(scrollableHeader);
        mAdapter.addScrollableFooter(scrollableFooter);
        assertEquals(37, mAdapter.getItemCount());
        assertEquals(35, mAdapter.getMainItemCount());

        // Clear all simple items, retains header items (...and scrollable items)
        mAdapter.clearAllBut(headerItem.getItemViewType());
        assertEquals(7, mAdapter.getItemCount());
        assertEquals(5, mAdapter.getMainItemCount());
        assertEquals(scrollableHeader, mAdapter.getItem(0));
        assertEquals(scrollableFooter, mAdapter.getItem(mAdapter.getItemCount() - 1));
    }

    @Test
    public void testRemoveItemsOfType() {
        // Grab the LayoutRes to delete
        AbstractFlexibleItem simpleItem = mAdapter.getItem(1);
        assertEquals(35, mAdapter.getItemCount());

        // Delete all items of type simple
        mAdapter.removeItemsOfType(simpleItem.getItemViewType());
        assertEquals(5, mAdapter.getItemCount());
    }

}