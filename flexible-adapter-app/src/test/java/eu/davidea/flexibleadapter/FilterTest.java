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
 * @author Davide Steduto
 * @since 18/10/2016
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class FilterTest {

    FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    List<AbstractFlexibleItem> mItems;

    @Before
    public void setUp() throws Exception {
        DatabaseService.getInstance().createHeadersSectionsDatabase(30, 5);
        mItems = DatabaseService.getInstance().getDatabaseList();
    }

    @Test
    public void testNoDelayFilter() throws Exception {
        mAdapter = new FlexibleAdapter<>(mItems);
        mAdapter.showAllHeaders();
        mAdapter.setSearchText("1");
        mAdapter.filterItems(DatabaseService.getInstance().getDatabaseList());
        assertEquals(21, mAdapter.getItemCount());
    }

}