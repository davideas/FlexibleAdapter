package eu.davidea.flexibleadapter.livedata;

import android.support.annotation.NonNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.livedata.items.HeaderHolder;
import eu.davidea.flexibleadapter.livedata.items.ItemHolder;
import eu.davidea.flexibleadapter.livedata.models.HeaderModel;
import eu.davidea.flexibleadapter.livedata.models.ItemModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Davide Steduto
 * @since 07/10/2017
 */
public class FlexibleFactoryTest {

    @Test
    public void createHeaderHolder() {
        HeaderModel header = new HeaderModel("H1");
        HeaderHolder headerHolder = FlexibleFactory.create(HeaderHolder.class, header);
        assertNotNull(headerHolder);
        assertEquals(header, headerHolder.getModel());
    }

    @Test
    public void createItemHolder() {
        HeaderModel header = new HeaderModel("H1");
        HeaderHolder headerHolder = FlexibleFactory.create(HeaderHolder.class, header);

        ItemModel item = new ItemModel("I1", "H2");
        ItemHolder itemHolder = FlexibleFactory.create(ItemHolder.class, item, headerHolder);

        assertNotNull(itemHolder);
        assertEquals(item, itemHolder.getModel());
        assertEquals(headerHolder, itemHolder.getHeader());
        assertEquals(header, itemHolder.getHeader().getModel());
    }

    @Test(expected = RuntimeException.class)
    public void createWithMismatchingParam() {
        FlexibleFactory.create(HeaderHolder.class, new Object());
    }

    @Test
    public void createSectionableItems() throws Exception {
        Map<String, HeaderModel> headers = new HashMap<>(2);
        headers.put("H1", new HeaderModel("H1"));
        headers.put("H2", new HeaderModel("H2"));

        List<ItemModel> items = new ArrayList<>(2);
        ItemModel item1 = new ItemModel("I1", "H1");
        ItemModel item2 = new ItemModel("I2", "H2");
        items.add(item1);
        items.add(item2);

        SectionableFactory sectionableFactory = new SectionableFactory(headers);
        List<AbstractFlexibleItem> adapterItems = FlexibleItemProvider
                .with(sectionableFactory)
                .from(items);

        assertTrue(adapterItems.get(0) instanceof ItemHolder);
        assertEquals(items.size(), adapterItems.size());
        assertEquals(item1, ((ItemHolder) adapterItems.get(0)).getModel());
        assertEquals(item2, ((ItemHolder) adapterItems.get(1)).getModel());
        assertEquals(headers.get("H1"), ((ItemHolder) adapterItems.get(0)).getHeader().getModel());
        assertEquals(headers.get("H2"), ((ItemHolder) adapterItems.get(1)).getHeader().getModel());
    }

    /**
     * Custom Factory, that transform an ItemModel to an IFlexible Item.
     * This Factory produces a Sectionable item and the relative Header.
     */
    private class SectionableFactory implements FlexibleItemProvider.Factory<ItemModel, AbstractFlexibleItem> {

        Map<String, HeaderModel> headers;

        SectionableFactory(Map<String, HeaderModel> headers) {
            this.headers = headers;
        }

        private IHeader getHeader(ItemModel itemModel) {
            return FlexibleFactory.create(HeaderHolder.class, headers.get(itemModel.getType()));
        }

        @NonNull
        @Override
        public AbstractFlexibleItem create(ItemModel itemModel) {
            return FlexibleFactory.create(ItemHolder.class, itemModel, getHeader(itemModel));
        }
    }

}