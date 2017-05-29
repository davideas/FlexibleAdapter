package eu.davidea.flexibleadapter;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
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
	List<AbstractFlexibleItem> mItems;

	@Before
	public void setUp() throws Exception {
		DatabaseService.getInstance().createHeadersSectionsDatabase(30, 5);
		mItems = DatabaseService.getInstance().getDatabaseList();
	}

	@Test
	public void testUpdateDataSet_WithAnimation() throws Exception {
		mAdapter = new FlexibleAdapter<>(mItems);
		mAdapter.showAllHeaders();

		List<AbstractFlexibleItem> initialItems = mAdapter.getCurrentItems();
		mAdapter.updateDataSet(DatabaseService.getInstance().getDatabaseList(), true);
		List<AbstractFlexibleItem> updatedItems = mAdapter.getCurrentItems();

		assertEquals(initialItems.size(), mAdapter.getItemCount());
		assertThat(initialItems, Matchers.contains(updatedItems.toArray()));
	}

	@Test
	public void testUpdateDataSet_WithNotifyDataSetChanged() throws Exception {
		mAdapter = new FlexibleAdapter<>(mItems);
		mAdapter.showAllHeaders();

		List<AbstractFlexibleItem> initialItems = mAdapter.getCurrentItems();
		mAdapter.updateDataSet(DatabaseService.getInstance().getDatabaseList());
		List<AbstractFlexibleItem> updatedItems = mAdapter.getCurrentItems();

		assertEquals(initialItems.size(), mAdapter.getItemCount());
		assertThat(initialItems, Matchers.contains(updatedItems.toArray()));
	}

}