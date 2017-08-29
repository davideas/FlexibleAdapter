package eu.davidea.flexibleadapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Davide Steduto
 * @since 23/06/2016
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class HeadersSectionsTest {

    FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    List<AbstractFlexibleItem> mItems;

    @Before
    public void setUp() throws Exception {
        DatabaseService.getInstance().createHeadersSectionsDatabase(30, 5);
        mItems = DatabaseService.getInstance().getDatabaseList();
    }

    @Test
    public void testItemCount() throws Exception {
        mAdapter = new FlexibleAdapter<>(mItems);
        assertEquals(mItems.size(), mAdapter.getItemCount());
    }

    @Test
    public void testSetDisplayHeadersAtStartUp() throws Exception {
        mAdapter = new FlexibleAdapter<>(mItems);
        assertEquals(0, mAdapter.getHeaderItems().size());

        //1st call to display headers
        mAdapter.setDisplayHeadersAtStartUp(true);
        System.out.println("1st call Headers = " + mAdapter.getHeaderItems().size());
        assertEquals(5, mAdapter.getHeaderItems().size());

        //2nd call to display headers
        mAdapter.setDisplayHeadersAtStartUp(true);
        System.out.println("2nd call Headers = " + mAdapter.getHeaderItems().size());
        assertEquals(5, mAdapter.getHeaderItems().size());

        //3rd call to display headers
        mAdapter.showAllHeaders();
        System.out.println("3rd call Headers = " + mAdapter.getHeaderItems().size());
        assertEquals(5, mAdapter.getHeaderItems().size());
    }

    @Test
    public void testEmptyAdapterAddItemsWithHeader() throws Exception {
        mAdapter = new FlexibleAdapter<>(null);

        mAdapter.addItems(0, mItems);
        assertEquals(mItems.size(), mAdapter.getItemCount());

        mAdapter.setDisplayHeadersAtStartUp(true);
        assertEquals(5, mAdapter.getHeaderItems().size());
    }

    @Test
    public void testIsHeader() throws Exception {
        mAdapter = new FlexibleAdapter<>(mItems);
        mAdapter.setDisplayHeadersAtStartUp(true);
        AbstractFlexibleItem item = mAdapter.getItem(0);
        assertNotNull(item);
        assertTrue(mAdapter.isHeader(item));

    }

    @Test
    public void testHasHeader() throws Exception {
        mAdapter = new FlexibleAdapter<>(mItems);
        mAdapter.setDisplayHeadersAtStartUp(true);
        AbstractFlexibleItem item = mAdapter.getItem(1);
        assertNotNull(item);
        assertTrue(mAdapter.hasHeader(item));
    }

    @Test
    public void testHasSameHeader() throws Exception {
        mAdapter = new FlexibleAdapter<>(mItems);
        mAdapter.setDisplayHeadersAtStartUp(true);
        AbstractFlexibleItem item1 = mAdapter.getItem(1);
        IHeader header = mAdapter.getHeaderOf(item1);
        assertNotNull(header);
        AbstractFlexibleItem item2 = mAdapter.getItem(3);
        assertNotNull(item2);
        assertTrue(mAdapter.hasSameHeader(item2, header));
    }

    @Test
    public void testGetHeaderOf() throws Exception {
        mAdapter = new FlexibleAdapter<>(mItems);
        mAdapter.setDisplayHeadersAtStartUp(true);
        IHeader header = mAdapter.getHeaderOf(mItems.get(1));
        assertNotNull(header);
    }

    @Test
    public void testShowAndHideAllHeaders() throws Exception {
        mAdapter = new FlexibleAdapter<>(mItems);
        mAdapter.showAllHeaders();
        assertEquals(5, mAdapter.getHeaderItems().size());
        mAdapter.hideAllHeaders();
        assertEquals(0, mAdapter.getHeaderItems().size());
    }

    @Test
    public void testGetSectionItemPositions() throws Exception {
        mAdapter = new FlexibleAdapter<>(mItems);
        mAdapter.setDisplayHeadersAtStartUp(true);
        IHeader header = mAdapter.getHeaderOf(mItems.get(1));
        List<Integer> positions = mAdapter.getSectionItemPositions(header);
        assertNotNull(positions);
        assertTrue(positions.size() > 0);
        Integer count = 1;
        for (Integer position : positions) {
            assertEquals(count++, position);
        }
    }

}