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
import eu.davidea.samples.flexibleadapter.ui.items.SimpleItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

import static org.junit.Assert.assertEquals;

/**
 * @author Davide Steduto
 * @since 18/10/2016
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class FilterTest {

    private FlexibleAdapter<AbstractFlexibleItem> mAdapter;
    private List<AbstractFlexibleItem> mItems;

    @Before
    public void setUp() throws Exception {
        DatabaseService.getInstance().createHeadersSectionsDatabase(30, 5);
        mItems = DatabaseService.getInstance().getDatabaseList();
    }

    @SuppressWarnings("unchecked")
    private void createSignalAdapter(final CountDownLatch signal) {
        mAdapter = new FlexibleAdapter(mItems) {
            @Override
            protected void onPostFilter() {
                super.onPostFilter();
                signal.countDown();
                verifyResult();
            }
        };
        mAdapter.showAllHeaders();
    }

    @Test
    public void testFilter() throws Throwable {
        CountDownLatch signal = new CountDownLatch(1);
        createSignalAdapter(signal);

        mAdapter.setFilter("1"); // No delay
        mAdapter.filterItems(DatabaseService.getInstance().getDatabaseList());

        signal.await(100L, TimeUnit.MILLISECONDS);
    }

    private void verifyResult() {
        int count = 0;
        for (AbstractFlexibleItem dbItem : mItems) {
            SimpleItem simpleItem = (SimpleItem) dbItem;
            if (simpleItem.filter("1")) {
                count++;
            }
        }
        assertEquals(count, mAdapter.getItemCount());
    }

}