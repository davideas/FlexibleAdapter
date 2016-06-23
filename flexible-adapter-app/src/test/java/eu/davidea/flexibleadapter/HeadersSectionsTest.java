package eu.davidea.flexibleadapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.samples.flexibleadapter.services.DatabaseService;

import static org.junit.Assert.assertTrue;

/**
 * @author Davide Steduto
 * @since 23/06/2016
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class HeadersSectionsTest {

	FlexibleAdapter<AbstractFlexibleItem> mAdapter;
	List<AbstractFlexibleItem> mItems;

	@Before
	public void setUp() throws Exception {
		DatabaseService.getInstance().createHeadersSectionsDatabase();
		mItems = DatabaseService.getInstance().getDatabaseList();
		mAdapter = new FlexibleAdapter<>(mItems);
	}

	@Test
	public void testItemCount() throws Exception {
		assertTrue(mAdapter.getItemCount() == mItems.size());
	}

	@Test
	public void testSetDisplayHeadersAtStartUp() throws Exception {
		assertTrue(mAdapter.getHeaderItems().size() == 0);
		mAdapter.setDisplayHeadersAtStartUp(true);
		System.out.println("Headers = " + mAdapter.getHeaderItems().size());
		mAdapter.setDisplayHeadersAtStartUp(true);
		System.out.println("Headers = " + mAdapter.getHeaderItems().size());
		assertTrue(mAdapter.getHeaderItems().size() > 0);
	}

	@Test
	public void testIsHeader() throws Exception {
	}

	@Test
	public void testHasHeader() throws Exception {

	}

	@Test
	public void testHasSameHeader() throws Exception {

	}

	@Test
	public void testGetHeaderOf() throws Exception {

	}

	@Test
	public void testShowAllHeaders() throws Exception {

	}

	@Test
	public void testHideAllHeaders() throws Exception {

	}

}